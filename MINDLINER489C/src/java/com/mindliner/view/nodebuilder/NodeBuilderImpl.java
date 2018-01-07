package com.mindliner.view.nodebuilder;

import com.mindliner.clientobjects.mlcObject;
import com.mindliner.entities.Colorizer;
import com.mindliner.gui.color.BaseColorizer;
import com.mindliner.gui.color.ColorManager;
import com.mindliner.gui.color.FixedKeyColorizer;
import com.mindliner.view.background.BackgroundPainter;
import com.mindliner.view.connectors.ConnectorFactory;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import com.mindliner.clientobjects.MlMapNode;
import com.mindliner.view.connectors.NodeConnection.ConnectorType;
import com.mindliner.view.MindmapNodeImpl;
import javax.swing.JComponent;

/**
 * This class builds stand-alone nodes from the list of words specified.
 *
 * @author Marius Messerli
 */
public class NodeBuilderImpl implements NodeBuilder {

    private Font font = new JLabel().getFont();
    // the maximum number of characters used of the headline
    private int maxCharacterCount = 64;
    private BackgroundPainter backgroundPainter;
    private boolean showAttributes = true;
    private boolean showImages = true;
    private boolean showDescription = true;
    private ConnectorType connectorType = ConnectorType.CurvedTextClear;

    public NodeBuilderImpl(BackgroundPainter backgroundPainter) {
        this.backgroundPainter = backgroundPainter;
    }

    @Override
    public List<MlMapNode> buildNodes(List<mlcObject> objects) {
        List<MlMapNode> nodes = new ArrayList<>();
        for (mlcObject o : objects) {
            MlMapNode node = wrapObject(o);
            node.setExpanded(true);
            nodes.add(node);
        }
        return nodes;
    }

    @Override
    public MlMapNode wrapObject(mlcObject object) {
        BaseColorizer fkc = ColorManager.getColorizerForType(Colorizer.ColorDriverAttribute.FixedKey);

        assert (object != null);
        MindmapNodeImpl node = new MindmapNodeImpl(object);
        node.setBackgroundPainter(backgroundPainter);
        node.setBackgroundColor(fkc.getColorForObject(FixedKeyColorizer.FixedKeys.MAP_NODE_BACKGROUND));
        node.setConnector(ConnectorFactory.getConnector(connectorType));
        node.setMaxHeadlineCharacter(maxCharacterCount);
        node.setShowAttributes(showAttributes);
        node.setShowDescription(showDescription);
        node.setShowImage(showImages);
        node.setFont(font);
        return node;
    }

    @Override
    public void setFont(Font font) {
        this.font = font;
    }

    @Override
    public void setMaxCharacterCount(int maxCharacters) {
        maxCharacterCount = maxCharacters;
    }

    @Override
    public void setConnectorType(ConnectorType connectorType) {
        this.connectorType = connectorType;
    }

    @Override
    public void setBackgroundPainter(BackgroundPainter backgroundPainter) {
        this.backgroundPainter = backgroundPainter;
    }

    public int getMaxCharacterCount() {
        return maxCharacterCount;
    }

    @Override
    public void setShowAttributes(boolean status) {
        showAttributes = status;
    }

    @Override
    public void setShowDescription(boolean status) {
        showDescription = status;
    }

    @Override
    public void setShowImages(boolean status) {
        showImages = status;
    }
    
    

}
