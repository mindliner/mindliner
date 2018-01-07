/*
 * This class imports http://freemind.sourceforge.net/wiki/index.php Maps into Mindliner.
 * (C) Marius Messerli, January 2012
 */
package com.mindliner.importer.maps;

import com.mindliner.cache.CacheEngineStatic;
import com.mindliner.cache.DefaultObjectAttributes;
import com.mindliner.clientobjects.mlcObject;
import com.mindliner.exceptions.ImportException;
import com.mindliner.managers.ImportManagerRemote;
import com.mindliner.objects.transfer.MltObject;
import com.mindliner.objects.transfer.mltKnowlet;
import com.mindliner.objects.transfer.mltObjectCollection;
import com.mindliner.serveraccess.RemoteLookupAgent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Marius Messerli
 */
public class MlFreeMindImporter implements MlMapImporter {

    private Document freemindDocument = null;
    private final List<String> importedFingerprints;

    public MlFreeMindImporter() {
        this.importedFingerprints = new ArrayList<>();
    }

    private void parseFile(File file) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        freemindDocument = dBuilder.parse(file);
        freemindDocument.getDocumentElement().normalize();
    }

    /**
     * Imports the map into a set of new objects. I use mlsObjects (instead of
     * their mlc versions) because these objects need to be transmitted to and
     * de-serlialized on the server at once.
     *
     * @param file
     * @return
     * @throws com.mindliner.exceptions.ImportException
     */
    @Override
    public mlcObject importMap(File file) throws ImportException {
        int confidentialityId = DefaultObjectAttributes.getConfidentialityForDefaultClient().getId();
        int priority = DefaultObjectAttributes.getPriority().getId();
        try {
            parseFile(file);
            MltObject rootObject = new mltObjectCollection();
            rootObject.setHeadline(file.getName());
            rootObject.setDescription("Imported Freemind Map, http://freemind.sourceforge.net/wiki/index.php");
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootObject);

            // in Freemind maps all nodes are children of the very first node (map center)
            // therefore we only import the first node
            NodeList nodeList = freemindDocument.getElementsByTagName("node");
            if (nodeList.getLength() > 0) {
                importChildren(rootNode, nodeList.item(0));
            }

            ImportManagerRemote importManager = (ImportManagerRemote) RemoteLookupAgent.getManagerForClass(ImportManagerRemote.class);
            // rootId is mapped with key -1. See importManager interface for further information
            Map<Integer, Integer> result = importManager.importObjectTree(rootNode, confidentialityId, priority);
            int rootId = result.get(-1);
            mlcObject newRoot = CacheEngineStatic.getObject(rootId);
            if (newRoot != null) {
                return newRoot;
            } else {
                throw new ImportException("Encountered a problem with the creation of the new objects. Please verify status of objects imported.");
            }
        } catch (NamingException ex) {
            throw new ImportException("Could not reach the server, please try later.");
        } catch (ParserConfigurationException ex) {
            throw new ImportException("There was a problem with the parser configuration. Please report this issue.");
        } catch (SAXException ex) {
            throw new ImportException("There was a problem with the structure of the file. Please report this issue.");
        } catch (IOException ex) {
            throw new ImportException(ex.getMessage());
        }
    }

    private void importChildren(DefaultMutableTreeNode mindlinerParent, Node docNode) {
        if (isElementNode(docNode)) {
            mltKnowlet newChild = new mltKnowlet();
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(newChild);
            mindlinerParent.add(childNode);
            setMindlinerObjectAttributes(newChild, docNode);
            Element e = (Element) docNode;
            importedFingerprints.add(createFingerPrint(e));
            NodeList children = docNode.getChildNodes();
            int childNodeCount = children.getLength();
            for (int i = 0; i < childNodeCount; i++) {
                Node docChild = children.item(i);
                importChildren(childNode, docChild);
            }
        }
    }

    private boolean isElementNode(Node n) {
        if (n.getNodeType() != Node.ELEMENT_NODE) {
            return false;
        }
        Element e = (Element) n;
        if (e.getTagName().equals("node")) {
            return true;
        }
        return false;
    }

    /**
     * Freemind nodes are either plain text nodes which have the headline as
     * TEXT element of the node element or a HTML node which have the headline
     * as the first richcontent element of the node.
     *
     * @param e The node element
     * @return The headline
     */
    private String retrieveHeadline(Element e) {
        String plainTextHeadline = e.getAttribute("TEXT");

        if (!plainTextHeadline.isEmpty()) {
            return plainTextHeadline;
        }
        NodeList richContentNodeList = e.getElementsByTagName("richcontent");
        if (richContentNodeList.getLength() > 0) {
            Element firstRichContentElement = (Element) richContentNodeList.item(0);
            NodeList htmlNodeParagraphs = firstRichContentElement.getElementsByTagName("p");
            if (htmlNodeParagraphs.getLength() > 0) {
                String head = htmlNodeParagraphs.item(0).getTextContent();
                return head.trim();
            }
        }
        return "";
    }

    private String retrieveDescription(Element e) {
        StringBuilder description = new StringBuilder();
        String newline = System.getProperty("line.separator");

        String link = e.getAttribute("LINK");
        if (!link.isEmpty()) {
            description.append(link).append("\n");
        }
        NodeList richContentNodeList = e.getElementsByTagName("richcontent");
        if (richContentNodeList.getLength() > 0) {
            Element descriptionElement = null;
            // in case of a HTML node a single richcontent element is the headline, in case of a plain text node it is the description
            String plainTextHeadline = e.getAttribute("TEXT");
            if (!plainTextHeadline.isEmpty()) {
                descriptionElement = (Element) richContentNodeList.item(0);
            } else {
                if (richContentNodeList.getLength() > 1) {
                    descriptionElement = (Element) richContentNodeList.item(1);
                }
            }
            if (descriptionElement != null) {
                NodeList descriptionParagraphs = descriptionElement.getElementsByTagName("p");
                for (int i = 0; i < descriptionParagraphs.getLength(); i++) {
                    Element p = (Element) descriptionParagraphs.item(i);
                    String plainDescription = p.getTextContent().trim();
                    if (!plainDescription.isEmpty()) {
                        description.append(plainDescription).append(newline);
                    }
                }
            }
        }
        return description.toString();
    }

    private void setMindlinerObjectAttributes(MltObject o, Node documentNode) {
        Element e = (Element) documentNode;
        Calendar cal = Calendar.getInstance();
        o.setHeadline(retrieveHeadline(e));
        o.setDescription(retrieveDescription(e));

        try {
            String modified = e.getAttribute("MODIFIED");
            if (!modified.isEmpty()) {
                Long millis = Long.parseLong(modified);
                cal.setTimeInMillis(millis);
                o.setModificationDate(cal.getTime());
            }
            String created = e.getAttribute("CREATED");
            if (!created.isEmpty()) {
                Long millis = Long.parseLong(created);
                cal.setTimeInMillis(millis);
                o.setCreationDate(cal.getTime());
            }
        } catch (NumberFormatException ex) {
            System.err.println("Modification or creation date format exception for node "
                    + o.getHeadline() + ". " + ex.getMessage());
        }
    }

    private String createFingerPrint(Element e) {
        String id = e.getAttribute("ID");
        if (!id.isEmpty()) {
            return id;
        } else {
            return e.getAttribute("MODIFIED") + e.getAttribute("CREATED") + e.getAttribute("TEXT");
        }
    }
}
