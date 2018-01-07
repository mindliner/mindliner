package com.mindliner.view;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.clientobjects.MlClientClassHandler;
import com.mindliner.clientobjects.MlcImage;
import com.mindliner.clientobjects.mlcContact;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.clientobjects.mlcTask;
import com.mindliner.entities.Colorizer;
import com.mindliner.entities.ObjectAttributes;
import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.FixedKeyColorizer;
import com.mindliner.image.LazyImage;
import com.mindliner.img.icons.MlIconLoader;
import com.mindliner.img.icons.MlIconManager;
import com.mindliner.styles.MlTriangle;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class draws the mindmap nodes.
 *
 * @author Marius Messerli
 */
public class MindmapNodeImpl extends MlDefaultMapNode {

    private final int DEFAULT_WIDTH = 100;

    // spacing constants
    private final int NODE_FRAME_SIZE = 10;
    private final int VERTICAL_SPACE = 5;
    private final int HORIZONTAL_SPACE = 5;
    private final int CHILD_INDICATOR_SIZE = 12;

    // image constants
    private static final int IMAGE_SIZE_X = 256;
    private static final int IMAGE_SIZE_Y = 150;

    // text decorator constants
    private static final int MIN_DESCRIPTION_WIDTH = 300;
    private final int MAX_LINE_COUNT = 6;

    // icon constants
    private final int iconWidth = 32;
    private final int iconHeight = 32;

    private boolean showAttributes = true;
    private String attributeString = null;
    private boolean showDescription = true;
    private boolean showImage = true;

    private Font attributesFont;
    private Font headlineFont;
    private Font descriptionFont;

    private int maxHeadlineCharacter = 64;

    private double currentYPosition;
    private double currentXPosition;

    private double averageDescriptionCharacterWidth = 0;

    private FontRenderContext frc = null;
    private Graphics2D g2 = null;

    public MindmapNodeImpl(mlcObject o) {
        super(o);
        width = 100;
    }

    @Override
    public void setFont(Font f) {
        super.setFont(f);
        attributesFont = new Font(f.getFontName(), Font.PLAIN, (int) (f.getSize() * 0.8));
        headlineFont = new Font(f.getFontName(), getObject().isArchived() ? Font.ITALIC : Font.BOLD, f.getSize());
        descriptionFont = new Font(f.getFontName(), Font.PLAIN, (int) (f.getSize() * 0.9));
    }

    @Override
    public Font getFont() {
        return super.getFont();
    }

    private String getAttributeString() {
        if (attributeString == null) {
            List<ObjectAttributes> attrs = new ArrayList<>();
            attrs.add(ObjectAttributes.Owner);
            attrs.add(ObjectAttributes.ModificationDate);
            attrs.add(ObjectAttributes.DataPool);
            attrs.add(ObjectAttributes.WorkMinutes);
            attrs.add(ObjectAttributes.DueDate);
            attributeString = AttributeStringBuilder.getAttributeDescription(attrs, this);
        }
        return attributeString;
    }

    private int getTextHeight(Font f, String text) {
        Rectangle2D bounds = f.getStringBounds(text, frc);
        return (int) bounds.getHeight();
    }

    private int getTextWidth(Font f, String text) {
        if (frc == null) {
            return DEFAULT_WIDTH;
        }
        Rectangle2D bounds = f.getStringBounds(text, frc);
        return (int) bounds.getWidth();
    }

    private Dimension drawIcon(Image img, double x, double y, boolean draw) {
        if (g2 == null) {
            return null;
        }
        FontRenderContext frc = g2.getFontRenderContext();
        double w = img.getWidth(null);
        double h = img.getHeight(null);
        double zoomScale = frc.getTransform().getScaleX();
        if (w > h) {
            double zoomedTargetWidth = iconWidth * zoomScale;
            h = zoomedTargetWidth / w * h;
            w = zoomedTargetWidth;
        } else {
            double zoomedHeight = iconHeight * zoomScale;
            w = zoomedHeight / h * w;
            h = zoomedHeight;
        }
        if (draw) {
            g2.drawImage(img, (int) x, (int) y, (int) w, (int) h, null);
        }
        return new Dimension((int) w, (int) h);

    }

    private Dimension drawOptionalIcons(double x, double y, boolean draw) {

        List<Image> icons = getIcons();
        if (icons.isEmpty()) {
            return new Dimension(0, 0);
        }
        for (int i = 0; i < icons.size(); i++) {
            Image img = icons.get(i);
            drawIcon(img, x + i * (iconWidth + HORIZONTAL_SPACE), y, draw);
        }
        return new Dimension(icons.size() * (iconWidth + HORIZONTAL_SPACE), iconHeight);
    }

    private Image getImage() {
        if (getObject() instanceof mlcContact) {
            mlcContact c = (mlcContact) getObject();
            if (c.getProfilePicture() != null) {
                LazyImage image = CacheEngineStatic.getImageAsync(c.getProfilePicture());
                return image.getImage();
            }
        } else if (getObject() instanceof MlcImage) {
            MlcImage i = (MlcImage) getObject();
            LazyImage image = CacheEngineStatic.getImageAsync(i);
            return image.getImage();
        }
        return null;
    }

    // determins the required scale factor to make the image fit the target size
    private double getScaleForFit(Image img) {
        if (img == null || (img.getWidth(null) < IMAGE_SIZE_X && img.getHeight(null) < IMAGE_SIZE_Y)) {
            return 1.0;
        }
        double overWidth = (double) img.getWidth(null) / IMAGE_SIZE_X;
        double overHeight = (double) img.getHeight(null) / IMAGE_SIZE_Y;
        return 1 / Math.max(overWidth, overHeight);
    }

    private Dimension drawImage(double x, double y, boolean draw) {
        if (getObject() instanceof MlcImage || getObject() instanceof mlcContact) {
            Image img = getImage();
            if (img != null) {
                double scale = getScaleForFit(img);
                int imageXSize = (int) (scale * img.getWidth(null));
                int imageYSize = (int) (scale * img.getHeight(null));
                if (draw) {
                    g2.drawImage(img, (int) x, (int) y, imageXSize, imageYSize, null);
                }
                return new Dimension(imageXSize, imageYSize);
            }
        }
        return new Dimension(0, 0);
    }

    private Dimension drawHeadline(double x, double y, boolean draw) {
        if (g2 == null) {
            return null;
        }
        int endIndex = Math.min(getObject().getHeadline().length(), maxHeadlineCharacter);
        String headline = getObject().getHeadline().substring(0, endIndex);

        if (draw) {
            g2.setFont(headlineFont);
            g2.setColor(getNodeColorToSelection());
            g2.drawString(headline, (int) x, (int) y);
        }
        return new Dimension(getTextWidth(headlineFont, headline), getTextHeight(headlineFont, headline));
    }

    private double getCharactersPerLine(FontRenderContext frc) {
        int requiredWidth = getTextWidth(descriptionFont, getObject().getDescription());
        averageDescriptionCharacterWidth = requiredWidth / getObject().getDescription().length();
        return Math.floor(Math.max(MIN_DESCRIPTION_WIDTH, getSize().width) - 2 * NODE_FRAME_SIZE) / averageDescriptionCharacterWidth;
    }

    private void drawHorizontalLine(Graphics2D g, double x, double y) {
        BaseColorizer fkc = ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);
        Color previousColor = g.getColor();
        g.setColor(fkc.getColorForObject(FixedKeyColorizer.FixedKeys.MAP_DESCRIPTION_SEPARATOR));
        Stroke previousStroke = g.getStroke();
        g.setStroke(new BasicStroke(2));
        g.drawLine((int) x, (int) y, (int) x + Math.max(MIN_DESCRIPTION_WIDTH, (int) getSize().width), (int) y);
        g.setColor(previousColor);
        g.setStroke(previousStroke);
    }

    private Dimension drawDescription(double x, double y, boolean draw) {
        Dimension returnDimension = new Dimension(0, 0);
        if (getObject().getDescription().trim().isEmpty()) {
            return returnDimension;
        }
        int lineHeight = getTextHeight(descriptionFont, getObject().getDescription());

        if (draw) {
            g2.setFont(descriptionFont);
            g2.setColor(getNodeColorToSelection());
            drawHorizontalLine(g2, x, y);
        }
        y += VERTICAL_SPACE + lineHeight;

        StringTokenizer st = new StringTokenizer(getObject().getDescription(), System.getProperty("line.separator"), true);
        int lineCount = 0;
        float currentY = (float) y;
        double maxCharsPerLine = getCharactersPerLine(frc);
        StringBuilder sb = new StringBuilder();
        while (lineCount < MAX_LINE_COUNT && st.hasMoreTokens()) {
            String currentToken = st.nextToken();
            if (!currentToken.equals(System.getProperty("line.separator"))) {

                if (sb.toString().length() < maxCharsPerLine) {
                    if (sb.toString().length() + currentToken.length() < maxCharsPerLine) {
                        sb.append(currentToken);
                        if (st.hasMoreTokens()) {
                            sb.append(" ");
                        }
                    } else {
                        sb.append(currentToken.substring(0, (int) maxCharsPerLine - sb.toString().length() - 1));
                    }
                }
            } else if (!sb.toString().isEmpty()) {
                if (draw) {
                    g2.drawString(sb.toString(), (float) x, currentY);
                }
                lineCount++;
                currentY += lineHeight;
                sb = new StringBuilder();
            }
        }
        // we may need the draw the last line
        if (!sb.toString().isEmpty()) {
            if (draw) {
                g2.drawString(sb.toString(), (float) x, currentY);
            }
            lineCount++;
        }
        returnDimension = new Dimension(Math.max(MIN_DESCRIPTION_WIDTH, getSize().width), 2 * VERTICAL_SPACE + lineCount * lineHeight);
        return returnDimension;

    }

    /**
     * This method layouts all components and computes the total size of the
     * node.
     *
     * @param draw If true the node is actually drawn, if false only its size is
     * computed and stored in nodeWidth and nodeHeight
     */
    private void layoutAndDraw(boolean draw) {
        double currentLineWidth = 0;

        int nodeHeight = 0;
        int nodeWidth = 0;

        double lineStartXPosition;

        lineStartXPosition = currentXPosition = getPosition().getX() + NODE_FRAME_SIZE;
        currentYPosition = getPosition().getY() + NODE_FRAME_SIZE;

        // make space for the text fully inside the node
        currentYPosition += getTextHeight(attributesFont, "Sample Text");

        // DRAW ATTRIBUTES
        if (showAttributes) {
            Dimension attributeDimension = drawAttributes(draw);
            nodeHeight += attributeDimension.height;
            nodeWidth += attributeDimension.width;
            currentYPosition += attributeDimension.height + VERTICAL_SPACE;
        }

        // DRAW Type Icon
        Dimension iconDimensions; // required to center the headline on the icons along y
        iconDimensions = drawIcon(getTypeIcon(g2), currentXPosition, currentYPosition, draw);
        nodeWidth = Math.max(nodeWidth, iconDimensions.width) + HORIZONTAL_SPACE;
        nodeHeight += iconDimensions.height + VERTICAL_SPACE;
        currentLineWidth = iconDimensions.width + HORIZONTAL_SPACE;

        // DRAW OPTIONAL ICONS
        currentXPosition += iconDimensions.width + HORIZONTAL_SPACE;
        Dimension optionalIconWidth = drawOptionalIcons(currentXPosition, currentYPosition, draw);
        currentLineWidth += optionalIconWidth.width;
        currentXPosition += optionalIconWidth.width;
        currentYPosition += iconDimensions.height;

        // DRAW HEADLINE
        int centeringOffset = iconDimensions.height / 3;
        Dimension headlineDimensions = drawHeadline(currentXPosition, currentYPosition - centeringOffset, draw);
        currentLineWidth += headlineDimensions.width;

        nodeWidth = (int) Math.max(nodeWidth, currentLineWidth);

        currentYPosition += headlineDimensions.height;

        // DRAW IMAGE
        Dimension imageDimension;
        if (showImage) {
            currentXPosition = lineStartXPosition;
            imageDimension = drawImage(currentXPosition, currentYPosition, draw);
            nodeWidth = Math.max(nodeWidth, imageDimension.width);
            nodeHeight += imageDimension.height + VERTICAL_SPACE;
            currentYPosition += imageDimension.height + VERTICAL_SPACE;
        }

        // drawDescription needs to know the available space, hence we set it here already  
        setSize(nodeWidth, nodeHeight);

        // DRAW DESCRIPTION
        if (showDescription) {
            currentXPosition = lineStartXPosition;
            Dimension descriptionDimension = drawDescription(currentXPosition, currentYPosition, draw);
            nodeHeight += descriptionDimension.height + VERTICAL_SPACE;
            nodeWidth = Math.max(nodeWidth, descriptionDimension.width);
        }
        nodeHeight += 2 * getTextHeight(attributesFont, "Sample Text");
        nodeHeight += NODE_FRAME_SIZE;
        nodeWidth += 3 * NODE_FRAME_SIZE;
        nodeWidth += CHILD_INDICATOR_SIZE;
        setSize(nodeWidth, nodeHeight);
    }

    @Override
    public void draw(Graphics g, boolean layoutOnly) {
        g2 = (Graphics2D) g;
        frc = g2.getFontRenderContext();

        // first run a silent "draw" to compute sizes
        layoutAndDraw(false);

        if (layoutOnly) {
            return;
        }

        // draw frame first (bottom most)
        drawBox(getPosition().getX(), getPosition().getY(), getSize().width, getSize().height, getNodeFillColor(), true, true);

        // and now actually draw node
        layoutAndDraw(true);
        if (isExpanded()) {
            drawChildConnections(g2);
        }
    }

    private Color getNodeFillColor() {
        if (MapNodeStatusManager.isCandidateTargetNode(this)) {
            return getBackgroundColor();
        } else if (MapNodeStatusManager.isSelected(this)) {
            return getColor();
        } else if (MapNodeStatusManager.isSelectionClone(this)) {
            return Color.LIGHT_GRAY;
        } else if (isJustModified()) {
            BaseColorizer fkc = ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);
            return fkc.getColorForObject(FixedKeyColorizer.FixedKeys.MAP_NODE_JUST_MODIFIED);
        } else {
            return getBackgroundColor();
        }
    }

    private Dimension drawBox(double x, double y, double w, double h, Color c, boolean fill, boolean draw) {
        RoundRectangle2D r = new RoundRectangle2D.Double(x, y, w, h, 4, 4);
        Color transparentColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (255 * 0.8));
        if (draw) {
            g2.setColor(transparentColor);
            g2.fill(r);
            drawChildIndicator(g2, transparentColor.darker(), true, r, CHILD_INDICATOR_SIZE);
            g2.setColor(getNodeFrameColor());
            g2.draw(r);
        }
        return new Dimension((int) r.getWidth(), (int) r.getHeight());
    }

    // draws a triangle if the child nodes are sorted automatically or two rectangles if their order is set manually
    private void drawChildIndicator(Graphics g, Color c, boolean fill, RoundRectangle2D r, int size) {
        if (getObject().getRelativeCount() >= (getParentNode() == null ? 1 : 2)) {
            g.setColor(c);
            if (!getObject().isRelativesOrdered()) {
                MlTriangle triangle = new MlTriangle(
                        MlTriangle.Direction.East, size,
                        (int) (r.getX() + r.getWidth() - size - HORIZONTAL_SPACE),
                        (int) (r.getY() + r.getHeight() / 2 - CHILD_INDICATOR_SIZE / 2 /*- size - VERTICAL_SPACE*/),
                        fill);
                triangle.paint(g);
            } else {
                int symbolHeight = CHILD_INDICATOR_SIZE/3;
                int symbolOffset = CHILD_INDICATOR_SIZE / 2;
                g.fillRect(
                        (int) (r.getX() + r.getWidth() - size - HORIZONTAL_SPACE),
                        (int) (r.getY() + r.getHeight() / 2 - symbolOffset /*- size - VERTICAL_SPACE*/),
                        CHILD_INDICATOR_SIZE, symbolHeight);
                g.fillRect(
                        (int) (r.getX() + r.getWidth() - size - HORIZONTAL_SPACE),
                        (int) (r.getY() + r.getHeight() / 2 + symbolOffset - symbolHeight /*- size - VERTICAL_SPACE*/),
                        CHILD_INDICATOR_SIZE, symbolHeight);
            }
        }
    }

    private Color getNodeFrameColor() {
        if (MapNodeStatusManager.isCandidateTargetNode(this)) {
            return Color.red;
        } else if (MapNodeStatusManager.isSelected(this)) {
            return getColor().darker();
        } else if (MapNodeStatusManager.isSelectionClone(this)) {
            return new Color(180, 180, 180);
        } else {
            return new Color(180, 180, 180);
        }
    }

    private Dimension drawAttributes(boolean draw) {
        if (g2 == null) {
            return null;
        }
        // DRAW ATTRIBUTES
        String text = getAttributeString();
        if (draw) {
            g2.setFont(attributesFont);
            g2.setColor(getNodeColorToSelection());
            g2.drawString(text, (float) currentXPosition, (float) currentYPosition);
        }
        return new Dimension(getTextWidth(attributesFont, text), getTextHeight(attributesFont, text));
    }

    @Override
    public boolean isWithin(Point2D p, Graphics g) {
        Rectangle2D r = new Rectangle2D.Double(
                getPosition().getX(),
                getPosition().getY(),
                getSize().width,
                getSize().height);
        return r.contains(p);
    }

    private Color getNodeColorToSelection() {
        Color result;
        if (MapNodeStatusManager.isSelected(this)) {
            result = ColorManager.getForegroundColor(getColor());
        } else if (MapNodeStatusManager.isSelectionClone(this)) {
            result = Color.BLACK;
        } else {
            result = getColor();
        }
        return result;
    }

    public Image getTypeIcon(Graphics2D g2) {
        MlIconManager.IconSize iconSize = determineResolutionForZoom();

        if (getObject() instanceof mlcTask) {
            mlcTask t = (mlcTask) getObject();
            if (t.isCompleted()) {
                return MlIconLoader.getImage(iconSize, "checkbox.png");
            } else {
                return MlIconLoader.getImage(iconSize, "checkbox_unchecked.png");
            }
        }
        return MlIconManager.getImageForType(MlClientClassHandler.getTypeByClass(getObject().getClass()), iconSize);
    }

    private MlIconManager.IconSize determineResolutionForZoom() {
        if (g2 == null) {
            return MlIconManager.IconSize.sixteen;
        }
        double scaleX = frc.getTransform().getScaleX();
        MlIconManager.IconSize iconSize;
        if (scaleX < 2) {
            iconSize = MlIconManager.IconSize.sixtyfour;
        } else {
            iconSize = MlIconManager.IconSize.onetwentyeight;
        }
        return iconSize;
    }

    private List<Image> getIcons() {
        List<Image> icons = new ArrayList<>();
        icons.clear();
        if (getObject().isPrivateAccess()) {
            Image i = MlIconLoader.getImage(MlIconManager.IconSize.twentyFour, "lock.png");
            if (i != null) {
                icons.add(i);
            }
        }
        if (getObject().isArchived()) {
            Image i = MlIconLoader.getImage(MlIconManager.IconSize.twentyFour, "jar.png");
            if (i != null) {
                icons.add(i);
            }
        }
        if (getObject() instanceof mlcTask && !CacheEngineStatic.getCurrentWorkers((mlcTask) getObject()).isEmpty()) {
            Image i = MlIconLoader.getImage(MlIconManager.IconSize.twentyFour, "gear_run.png");
            if (i != null) {
                icons.add(i);
            }
        }
        if (getObject().getIcons() != null) {
            getObject().getIcons().forEach((MlcImage i) -> {
                icons.add(i.getIcon().getImage());
            });
        }
        return icons;
    }

    public int getMaxHeadlineCharacter() {
        return maxHeadlineCharacter;
    }

    public void setMaxHeadlineCharacter(int maxHeadlineCharacter) {
        this.maxHeadlineCharacter = maxHeadlineCharacter;
    }

    public void setShowAttributes(boolean showAttributes) {
        this.showAttributes = showAttributes;
    }

    public void setShowDescription(boolean showDescription) {
        this.showDescription = showDescription;
    }

    public void setShowImage(boolean showImage) {
        this.showImage = showImage;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public int getLevel() {
        return level;
    }

}
