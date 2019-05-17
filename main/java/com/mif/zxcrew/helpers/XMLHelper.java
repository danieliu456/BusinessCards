package com.mif.zxcrew.helpers;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import com.mif.zxcrew.txtclassifier.Card;
import com.mif.zxcrew.txtclassifier.Contact;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
/**
* XMLHelper.java
*
* Purpose: Creates or reads XML tree;
*
* readContact() reads xml file;
*
* saveContact() saves contact to file;
*
* createContactStructure() creates root elements;
*
* appendCardElement() creates node elements;
*
* @author Aivaras Ivoskus
* @author Daniel Spakovskij
 */
public class XMLHelper {

    public static final String TAG = "XMLHELPER";
    public static final String CONTACTS_DIR = "contacts";

    // Reads file from given path and returns Contact
    public static Contact readContact(String pathToFile) throws ParserConfigurationException, SAXException, IOException {

        //Get Document Builder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        //Build Document
        Document document =  builder.parse(new File(pathToFile));
        Element rootElement = document.getDocumentElement();

        //Normalize the XML Structure;
        rootElement.normalize();

        // Retrieve identifier
        int identifier = Integer.parseInt(rootElement.getAttribute("identifier"));
        Contact contact = new Contact(identifier);

        // Assign names
        contact.setName(rootElement.getAttribute("firstname"));
        contact.setLastname(rootElement.getAttribute("lastname"));

        // Retrieves all card nodes and populates found details
        NodeList nList = document.getElementsByTagName("card");



        for(int j = 0; j < nList.getLength(); j++){

            Node nNode = nList.item(j);

            if(nNode.getNodeType() == Node.ELEMENT_NODE){
                Element eElement = (Element) nNode;
                Card cc = new Card(contact);

                if(eElement.getElementsByTagName("firstname").item(0) != null)
                    cc.setName(eElement.getElementsByTagName("firstname").item(0).getTextContent());
                if(eElement.getElementsByTagName("lastname").item(0) != null)
                    cc.setLname(eElement.getElementsByTagName("lastname").item(0).getTextContent());
                if(eElement.getElementsByTagName("company").item(0) != null)
                    cc.setCompany(eElement.getElementsByTagName("company").item(0).getTextContent());
                if(eElement.getElementsByTagName("position").item(0) != null)
                    cc.setPosition(eElement.getElementsByTagName("position").item(0).getTextContent());
                if(eElement.getElementsByTagName("telephonenumber").item(0) != null)
                    cc.setTelNo(eElement.getElementsByTagName("telephonenumber").item(0).getTextContent());
                if(eElement.getElementsByTagName("mobilenumber").item(0) != null)
                    cc.setMobNo(eElement.getElementsByTagName("mobilenumber").item(0).getTextContent());
                if(eElement.getElementsByTagName("mail").item(0) != null)
                    cc.setEmail(eElement.getElementsByTagName("mail").item(0).getTextContent());
                if(eElement.getElementsByTagName("date").item(0) != null)
                    cc.setTakenOn(eElement.getElementsByTagName("date").item(0).getTextContent());
                if(eElement.getElementsByTagName("comment").item(0) != null)
                    cc.setComment(eElement.getElementsByTagName("comment").item(0).getTextContent());

                Log.i(TAG, "Found card name: " + cc.getLname());
                contact.addCard(cc);
            }

        }

        return contact;
    }

    // Saves contact as formed in Contact class, together with cards inside of it
    public static void saveContact(ContextWrapper wrapper, Contact contact) throws IOException, SAXException, ParserConfigurationException {

        try
        {
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8" );

            String savePath = "";

            // If XML Identifier is not set, and we must set it
            if(contact.getXmlIdentifier() == -1) {
                String checkPath = "";
                int i = 0;
                // We will look for fitting XML identifier
                do {
                    checkPath = wrapper.getDir(CONTACTS_DIR, Context.MODE_PRIVATE).getAbsolutePath() + "/" + contact.getName() + contact.getLastname() + i + ".xml";
                    File file = new File(checkPath);

                    // If such file doesn't exist, we'll assign i as XMLIdentifier and save file in such location
                    if (!file.exists()) {
                        contact.setXmlIdentifier(i);
                        savePath = checkPath;
                    }

                    i++;


                } while (contact.getXmlIdentifier() == -1);
            } else {
                savePath = getPathForContact(wrapper, contact);
            }

            // We must create document AFTER we know the XML identifier
            Document document = createContactStructure(contact);

            // Pass the path to FileOutputStream
            Log.i(TAG, "Saving XML to: " + savePath);
            Log.i(TAG, contact.getName()+contact.getLastname());
            tr.transform(new DOMSource(document), new StreamResult(new FileOutputStream(savePath)));

        }catch(TransformerException te) {
            System.out.println(te.getMessage());
        }catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    // Creates Contact Structure using Contact class and the contacts inside of it
    private static Document createContactStructure(Contact contact)
    {
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            //use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            //create instance of DOM
            doc = db.newDocument();
            //create root element
            Element rootElement = doc.createElement("contact");
            rootElement.setAttribute("identifier", String.valueOf(contact.getXmlIdentifier()));

            rootElement.setAttribute("firstname", contact.getName());
            rootElement.setAttribute("lastname", contact.getLastname());

            for(int i = 0; i < contact.getCardLength(); i++){
                appendCardElement(contact.getCard(i), doc, rootElement);
            }

            doc.appendChild(rootElement);

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }

        return doc;
    }

    // Appends the Card Element to the Contact element
    private static void appendCardElement(Card card, Document document, Element rootElement){

        Element element = null;

        // Create new card element and give it the date
        Element cardElement = document.createElement("card");

        // Append it to the root which must be the contact
        rootElement.appendChild(cardElement);

        if(card.getPosition() != null) {
            element = document.createElement("position");
            element.appendChild(document.createTextNode(card.getPosition()));
            cardElement.appendChild(element);
        }

        if(card.getCompany() != null) {
            element = document.createElement("company");
            element.appendChild(document.createTextNode(card.getCompany()));
            cardElement.appendChild(element);
        }

        if(card.getName() != null) {
            element = document.createElement("firstname");
            element.appendChild(document.createTextNode(card.getName()));
            cardElement.appendChild(element);
        }

        if(card.getLname() != null) {
            element = document.createElement("lastname");
            element.appendChild(document.createTextNode(card.getLname()));
            cardElement.appendChild(element);
        }

        if(card.getComment() != null) {
            element = document.createElement("comment");
            element.appendChild(document.createTextNode(card.getComment()));
            cardElement.appendChild(element);
        }

        if(card.getMobNo() != null) {
            element = document.createElement("mobilenumber");
            element.appendChild(document.createTextNode(card.getMobNo()));
            cardElement.appendChild(element);
        }

        if(card.getEmail() != null) {
            element = document.createElement("mail");
            element.appendChild(document.createTextNode(card.getEmail()));
            cardElement.appendChild(element);
        }

        if(card.getTelNo() != null) {
            element = document.createElement("telephonenumber");
            element.appendChild(document.createTextNode(card.getTelNo()));
            cardElement.appendChild(element);
        }

        if(card.getTakenOnFormatted() != null) {
            element = document.createElement("date");
            element.appendChild(document.createTextNode(card.getTakenOnFormatted()));
            cardElement.appendChild(element);
        }

    }

    public static void deleteContact(ContextWrapper wrapper, Contact contact) {
        File file = new File(getPathForContact(wrapper, contact));
        if(file.exists())
            file.delete();
        else
            Log.i(TAG, "Couldn't delete the contact");
    }

    public static String getPathForContact(ContextWrapper wrapper, Contact contact){
        return wrapper.getDir(CONTACTS_DIR, Context.MODE_PRIVATE).getAbsolutePath() + "/" + contact.getName() + contact.getLastname() + contact.getXmlIdentifier() + ".xml";
    }
}
