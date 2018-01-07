/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.cache;

import com.mindliner.image.LazyImage;
import com.mindliner.serveraccess.OnlineManager;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Caches the images of MlcImageURL objects. it keeps images up to a certain
 * number in the main memory, then the least recently used images are removed
 * from memory and are written to disk (to not overflow memory), see LruCache.
 * <p>
 * Requested images that are not present in the cache (i.e. not in memory and
 * not on disk) are downloaded in a background thread, see ImageDownloader
 *
 * @author Dominic Plangger
 */
public class ImageCache {

    private static final String IMAGE_CACHE_DIR = "images";
    private static final String IMAGE_CACHE_NAME_EXTENSION = "image";
    private static final String IMAGE_FILE_PREFIX = "image";
    // java image objects are not associated with a type, therefore the type of the image file written to disk is unimportant.
    // we just need a format that supports all image attributes like transparency, etc.
    private static final String IMAGE_FILE_FORMAT = "png";
    private static final int MAX_IMAGES = 150; // number of images to keep in main memory
    private static final int INTERVALL = 200; // ms
    private Map<Integer, String> urlMap;
    private final Map<Integer, LazyImage> lazyImageMap;
    private final Map<Integer, Image> memImages;
    private Map<Integer, File> diskImages;
    private final ImageDownloader downloader;
    private final Timer timer;

    public ImageCache() {
        downloader = new ImageDownloader();
        timer = new Timer();
        lazyImageMap = new HashMap<>();
        urlMap = Collections.synchronizedMap(new HashMap<Integer, String>());
        diskImages = Collections.synchronizedMap(new HashMap<Integer, File>());
        memImages = Collections.synchronizedMap(new LruCache());
    }

    public LazyImage getImageAsync(int id, String url) {
        // getImage is called very often (at every repaint), therefore we cache
        // the lazyImage objects here. this handling simplifies the invalidation of images
        // (i.e. after an update event we can simply remove the lazy image from the lazyImageMap)
        LazyImage lazyImg = lazyImageMap.get(id);
        if (lazyImg != null) {
            return lazyImg;
        }

        lazyImg = new LazyImage();
        lazyImageMap.put(id, lazyImg);

        if (url != null && !url.isEmpty()) {
            if (!urlMap.containsKey(id) && memImages.containsKey(id)) {
                // case: first a server-side stored image, then changed to URL image
                memImages.remove(id);
            }
            // check if the url of the cached image is the same as the new one
            // if not, clear cache entries for this id
            checkCurrentness(id, url);
            urlMap.put(id, url);
        }

        if (memImages.containsKey(id)) {
            Image img = memImages.get(id);
            lazyImg.setImage(img);
            return lazyImg;
        }

        // download image in background thread. Also, read image from disk in the background thread
        // as it may take a while for bigger images
        ImageTask task = new ImageTask(id, url, lazyImg);
        downloader.addTask(task);

        return lazyImg;
    }

    public Image getImageSync(int id) {
        if (memImages.containsKey(id)) {
            return memImages.get(id);
        }

        if (diskImages.containsKey(id)) {
            File imgFile = diskImages.get(id);
            try {
                BufferedImage image = ImageIO.read(imgFile);
                memImages.put(id, image);
                return image;
            } catch (IOException ex) {
                Logger.getLogger(ImageCache.class.getName()).log(Level.WARNING, "Failed to read image from disk", ex);
                diskImages.remove(id);
            }
        }

        return null;

    }

    public void putImage(int id, Image image) {
        LazyImage lazyImg = new LazyImage();
        lazyImg.setImage(image);
        lazyImageMap.put(id, lazyImg);
        memImages.put(id, image);
        if (diskImages.containsKey(id)) {
            File f = diskImages.get(id);
            if (f.exists()) {
                f.delete();
            }
            diskImages.remove(id);
        }
        urlMap.remove(id);
    }

    public void invalidateImage(int id) {
        lazyImageMap.remove(id);
    }

    public void initialize() {
        timer.schedule(downloader, 0, INTERVALL);

        FileInputStream fis = null;
        try {
            File f = new File(CacheEngineStatic.getDataCacheFilePath(IMAGE_CACHE_NAME_EXTENSION));
            fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            urlMap = (Map<Integer, String>) ois.readObject();
            diskImages = (Map<Integer, File>) ois.readObject();
        } catch (IOException ex) {
            Logger.getLogger(ImageCache.class.getName()).log(Level.INFO, "Image does not exist. [{0}]", ex.getMessage());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ImageCache.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ImageCache.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void storeCache() {
        // write images that are in memory but not persistent to disk.
        for (Map.Entry<Integer, Image> entry : memImages.entrySet()) {
            int id = entry.getKey();
            if (!diskImages.containsKey(id)) {
                Image img = entry.getValue();
                if (img != null) {
                    File imgFile = imageToDisk(id, img);
                    diskImages.put(id, imgFile);
                }
            }
        }

        // store maps
        FileOutputStream fos = null;
        try {
            File f = new File(CacheEngineStatic.getDataCacheFilePath(IMAGE_CACHE_NAME_EXTENSION));
            fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(urlMap);
            oos.writeObject(diskImages);
            fos.close();
        } catch (IOException ex) {
            Logger.getLogger(ImageCache.class.getName()).log(Level.WARNING, "Could not write cache to disk", ex);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(ImageCache.class.getName()).log(Level.WARNING, "Could not close output stream", ex);
            }
        }
    }

    /**
     * checks whether the url changed. if yes, delete all entries of the image
     * in the cache (we need to reload it)
     */
    private void checkCurrentness(int id, String url) {
        if (urlMap.containsKey(id)) {
            String cachedUrl = urlMap.get(id);
            if (!url.equals(cachedUrl)) {
                urlMap.remove(id);
                memImages.remove(id);
                if (diskImages.containsKey(id)) {
                    File oldFile = diskImages.get(id);
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                    diskImages.remove(id);
                }
            }
        }
    }

    private class ImageDownloader extends TimerTask {

        private final ConcurrentLinkedQueue<ImageTask> queue = new ConcurrentLinkedQueue<ImageTask>();

        public void addTask(ImageTask task) {
            queue.add(task);
        }

        @Override
        public void run() {
            while (!queue.isEmpty()) {
                ImageTask task = queue.poll();
                int id = task.getId();
                LazyImage lazyImage = task.getLazyImage();

                // is image present in memory?
                if (memImages.containsKey(id)) {
                    lazyImage.setImage(memImages.get(id));
                    continue;
                }

                // is image present on disk?
                if (diskImages.containsKey(id)) {
                    File imgFile = diskImages.get(id);
                    try {
                        BufferedImage image = ImageIO.read(imgFile);
                        lazyImage.setImage(image);
                        memImages.put(id, image);
                        continue;
                    } catch (IOException ex) {
                        Logger.getLogger(ImageCache.class.getName()).log(Level.WARNING, "Failed to read image from disk", ex);
                        diskImages.remove(id);
                    }
                }

                // download image
                if (OnlineManager.isOnline() && task.getUrl() != null && !task.getUrl().isEmpty()) {
                    try {
                        URL url = new URL(task.getUrl());
                        BufferedImage image = ImageIO.read(url);
                        lazyImage.setImage(image);
                        memImages.put(id, image);
                        continue;
                    } catch (Exception ex) {
                        Logger.getLogger(ImageCache.class.getName()).log(Level.WARNING, "Failed to download image from URL: " + task.getUrl(), ex);
                        lazyImage.setHasError(true);
                    }
                }
            }
        }
    }

    private class LruCache extends LinkedHashMap<Integer, Image> {

        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, Image> eldest) {
            if (size() > MAX_IMAGES) {
                // remove eldest image from memory and write it to disk
                Image img = eldest.getValue();
                int id = eldest.getKey();
                imageToDisk(id, img);
                lazyImageMap.remove(id);
                return true;
            }
            return false;
        }
    }

    private class ImageTask {

        private int id;
        private String url;
        private LazyImage lazyImage;

        public ImageTask(int id, String url, LazyImage lazyImage) {
            this.id = id;
            this.url = url;
            this.lazyImage = lazyImage;
        }

        public int getId() {
            return id;
        }

        public String getUrl() {
            return url;
        }

        public LazyImage getLazyImage() {
            return lazyImage;
        }
    }

    private String getImageCacheLocation() {
        String cacheLoc = CacheEngineStatic.getCacheDirectoryForCurrentUser();
        String fileSeparator = System.getProperty("file.separator");
        return cacheLoc + fileSeparator + IMAGE_CACHE_DIR;
    }

    private void ensureDirExists(String loc) {
        File dir = new File(loc);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    private File createImageFile(String imgCacheDir, int id) {
        String fileSeparator = System.getProperty("file.separator");
        String sb = String.valueOf(imgCacheDir + fileSeparator + IMAGE_FILE_PREFIX) + Integer.toString(id) + "." + IMAGE_FILE_FORMAT;
        File f = new File(sb);
        return f;
    }

    private File imageToDisk(int id, Image img) {
        String loc = getImageCacheLocation();
        ensureDirExists(loc);
        File imgFile = createImageFile(loc, id);
        BufferedImage bImg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bImg.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        try {
            ImageIO.write(bImg, IMAGE_FILE_FORMAT, imgFile);
            diskImages.put(id, imgFile);
        } catch (IOException ex) {
            Logger.getLogger(ImageCache.class.getName()).log(Level.SEVERE, "Could not write image to disk", ex);
        }
        return imgFile;
    }

}
