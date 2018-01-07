/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mindliner.connector;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlcImage;
import com.mindliner.clientobjects.mlcKnowlet;
import com.mindliner.clientobjects.mlcObject;
import static com.mindliner.clipboard.ClipboardParser.MAX_HEADLINE_LENGTH;
import com.mindliner.commands.CommandRecorder;
import com.mindliner.commands.ImageUpdateCommand;
import com.mindliner.commands.ObjectCreationCommand;
import com.mindliner.entities.MlsImage;
import com.mindliner.entities.SoftwareFeature;
import com.mindliner.events.ObjectChangeManager;
import com.mindliner.serveraccess.OnlineManager;
import com.mindliner.thread.FileIndexWorker;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.util.PDFTextStripperByArea;

/**
 * Uploader that creates knowlets for each file after upload. PDF files
 * are also scanned for any highlighted text or annotations which will be added
 * as child notes. Also for image files an mlcImage will be created. 
 *
 * @author Dominic Plangger
 */
public class FileUploadThread extends AbstractUploadThread {

    protected static final String FILE_PATH_PREFIX = "Path: ";
    protected static final String PDF_MIME_TYPE = "application/pdf";

    protected final List<File> droppedFiles;
    protected final mlcObject linkTarget;

    public FileUploadThread(List<File> files, mlcObject linkTarget, CloudConnector connector) throws FileNotFoundException {
        super(connector);
        this.droppedFiles = files;
        this.linkTarget = linkTarget;
        List<UploadSource> s = new ArrayList<>();
        for (File f : files) {
            s.add(new UploadSource(f));
        }
        setSource(s);
    }

    @Override
    protected void afterUpload(List<UploadResult> result) {
        if (result == null) {
            result = new ArrayList<>();
            for (File f : droppedFiles) {
                UploadResult ur = new UploadResult();
                ur.setOpenUrl(FILE_PATH_PREFIX + f.toURI());
                result.add(ur);
            }
        }
        // create a knowlet for each file
        createMindlinerObjects(result);
    }

    protected void createMindlinerObjects(List<UploadResult> result) {
        CommandRecorder cr = CommandRecorder.getInstance();
        for (int i = 0; i < droppedFiles.size(); i++) {
            File f = droppedFiles.get(i);
            UploadResult ur = result.get(i);
            
            StringBuilder builder = new StringBuilder();
            boolean isImage = false;
            String url = null;
            
            if (ur.getDownloadUrl() != null) {
                builder.append(ur.getDownloadUrl()).append("\n");
                // Check if the file is a supported image. If yes, create an image node instead of a knowlet
                url = ur.getDownloadUrl().replace(CloudConnector.DOWNLOAD_LINK, "");
                isImage = checkForImage(url);
            }
            if (ur.getOpenUrl() != null) {
                builder.append(ur.getOpenUrl());
            }
            
            ObjectCreationCommand cmd = new ObjectCreationCommand(linkTarget, isImage ? MlcImage.class : mlcKnowlet.class, f.getName(), builder.toString());
            if (CacheEngineStatic.getCurrentUser().isAuthorizedForFeature(SoftwareFeature.CurrentFeatures.FILE_INDEXING)) {
                cmd.addProgressListener(new FileIndexWorker(f));
            }
            cr.scheduleCommand(cmd);
            if (!OnlineManager.waitForServerMessages()) {
                ObjectChangeManager.objectCreated(cmd.getObject());
            }
            if (isImage) {
                cr.scheduleCommand(new ImageUpdateCommand(cmd.getObject(), null, cmd.getObject().getHeadline(), MlsImage.ImageType.URL, url));
            }
            // Check if file is a pdf file. If yes, create relatives for all annotations in the pdf file
            checkForPdf(f, cmd.getObject());
        }
    }

    private boolean checkForImage(String part) {
        try {
            URL url = new URL(part);
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();
            ImageInputStream iis = ImageIO.createImageInputStream(is);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            return readers.hasNext();
        } catch (IOException ex) {
            return false;
        }
    }

    private void checkForPdf(File f, mlcObject object) {
        String mime;
        try {
            mime = Files.probeContentType(f.toPath());
        } catch (IOException ex) {
            Logger.getLogger(FileUploadThread.class.getName()).log(Level.WARNING, "Failed probe mime type of file [" + f.getAbsolutePath() + "]", ex);
            return;
        }
        // If it is a pdf file analyze it and extract all highlighted sentences and annotated notes
        // and create knowlets for each of them
        if (PDF_MIME_TYPE.equals(mime) || f.getAbsolutePath().endsWith(".pdf")) {
            try {
                PDDocument document = PDDocument.load(f);
                List<PDPage> pages = (List<PDPage>) document.getDocumentCatalog().getAllPages();
                List<String[]> highlights = new ArrayList<>();
                List<String> notes = new ArrayList<>();
                for (PDPage p : pages) {
                    for (PDAnnotation a : p.getAnnotations()) {
                        if (a instanceof PDAnnotationTextMarkup) {
                            // Each highlighted text per line is defined by a rectangle (4 points/quad points).
                            // Therefore we loop through the rectangles and extract the underlying text.
                            float pageHeight = pages.get(0).getMediaBox().getUpperRightY();
                            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                            PDAnnotationTextMarkup ma = (PDAnnotationTextMarkup) a;
                            float[] points = ma.getQuadPoints();
                            StringBuilder str = new StringBuilder();
                            for (int i = 0; i < points.length; i += 8) {
                                // First upper left, then upper right, then lower left, then lower right point
                                Rectangle2D.Double rect = new Rectangle2D.Double();
                                rect.height = points[i + 1] - points[i + 5];
                                rect.width = points[i + 2] - points[i];
                                rect.x = points[i] - 1;
                                rect.y = pageHeight - points[i + 1];

                                stripper.addRegion("class" + i, rect);
                                stripper.extractRegions(p);
                                String line = stripper.getTextForRegion("class" + i);
                                str.append(line);
                            }
                            // Highlighted text may contain a text note
                            String[] content = new String[2];
                            content[0] = a.getContents();
                            content[1] = str.toString();
                            highlights.add(content);
                        }
                        if (a instanceof PDAnnotationText) {
                            notes.add(a.getContents());
                        }
                    }
                }
                createAnnotationRelatives(highlights, notes, object);
            } catch (IOException ex) {
                Logger.getLogger(FileUploadThread.class.getName()).log(Level.SEVERE, "Failed to analyze pdf file [" + f.getAbsolutePath() + "]", ex);
            }
        }

    }

    private void createAnnotationRelatives(List<String[]> highlights, List<String> notes, mlcObject object) {
        CommandRecorder cr = CommandRecorder.getInstance();
        for (String[] content : highlights) {
            // prefer annotated text as headline and highlighted text as description.
            // if annotated text is too long, append it to description
            if (content[0] == null || content[0].isEmpty()) {
                if (content[1] == null || content[1].isEmpty()) {
                    Logger.getLogger(FileUploadThread.class.getName()).log(Level.WARNING, "PDF analysis resulted in an empty markup annotation");
                    continue;
                }
                content[0] = content[1].length() > MAX_HEADLINE_LENGTH ? content[1].substring(0, MAX_HEADLINE_LENGTH) : content[1];
            } else if (content[0].length() > MAX_HEADLINE_LENGTH) {
                content[1] = content[1] + "\n\n Note: \n" + content[0];
                content[0] = content[0].substring(0, MAX_HEADLINE_LENGTH);
            }
            ObjectCreationCommand cmd = new ObjectCreationCommand(object, mlcKnowlet.class, content[0], content[1]);
            cr.scheduleCommand(cmd);
            if (!OnlineManager.waitForServerMessages()) {
                ObjectChangeManager.objectCreated(cmd.getObject());
            }
        }

        for (String content : notes) {
            if (content == null || content.isEmpty()) {
                Logger.getLogger(FileUploadThread.class.getName()).log(Level.WARNING, "PDF analysis resulted in an empty text annotation");
                continue;
            }
            // Use annotated text as headline if it is not too long.
            // Otherwise put it in the description
            String headline = content;
            String descr = "";
            if (content.length() > MAX_HEADLINE_LENGTH) {
                descr = headline;
                headline = headline.substring(0, MAX_HEADLINE_LENGTH);
            }
            ObjectCreationCommand cmd = new ObjectCreationCommand(object, mlcKnowlet.class, headline, descr);
            cr.scheduleCommand(cmd);
            if (!OnlineManager.waitForServerMessages()) {
                ObjectChangeManager.objectCreated(cmd.getObject());
            }
        }
    }
}
