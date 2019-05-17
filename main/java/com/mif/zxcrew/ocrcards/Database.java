package com.mif.zxcrew.ocrcards;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.mif.zxcrew.helpers.VCardHelper;
import com.mif.zxcrew.helpers.XMLHelper;
import com.mif.zxcrew.txtclassifier.Card;
import com.mif.zxcrew.txtclassifier.Contact;

import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Wrapper;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Database.java
 * Purpose: Holds information about contacts staticly, so all activities could reach
 * it without having to send it to each other. Also responsible for I/O operations.
 * Data lives until all of the activities or processes are closed.
 *
 * loadedContacts holds all contacts;
 *
 * insertsAndSaveContact() adds  new contact to ArrayList;
 *
 * getSimilarContacts() finds same contact;
 *
 * deleteContact() deletes contact
 *
 * updateContact() calls for saveContact function, and creates new InsertResult object;
 *
 * saveContact() saves contact picture and calls for XMLHelper to save contact;
 *
 * loadContacts() loads existing contacts;
 *
 * getContacts() reads VCard file.
 *
 * @author Aivaras Ivoskus
 * @author Daniel Spakovskij
 * @version 1.0
 */
public class Database {


    public static final String TAG = "DATABASE";

    private static boolean isLoaded = false;
    private static ArrayList<Contact> loadedContacts = new ArrayList<>();
    public static ArrayList<Contact> getLoadedContacts() {
        return loadedContacts;
    }

    public static Contact getContactByIndex(int index) {
        return loadedContacts.get(index);
    }

    public static InsertResult insertAndSaveContact(ContextWrapper wrapper, Contact contact, Card card){

        contact.addCard(card);
        loadedContacts.add(contact);
        saveContact(wrapper, contact);

        return new InsertResult(contact.getCardLength() - 1,loadedContacts.size() - 1); // Returns the index of Card
    }

    // Returns similar Contacts
    // Used to check if there are any similar contacts and to prompt user to
    public static Contact[] getSimilarContacts(Contact contact){

        ArrayList<Contact> tempContacts = new ArrayList<>();

        for (int i = 0; i < loadedContacts.size(); i++){
            if(loadedContacts.get(i).getName().equals(contact.getName()) && loadedContacts.get(i).getLastname().equals(contact.getLastname())){
                tempContacts.add(loadedContacts.get(i));
            }
        }

        // Convert from ArrayList to simple array.
        Contact[] similarContacts = new Contact[tempContacts.size()];
        similarContacts = tempContacts.toArray(similarContacts);
        return similarContacts;
    }

    public static void deleteContact(ContextWrapper wrapper, Contact contact){
        loadedContacts.remove(contact);
        XMLHelper.deleteContact(wrapper, contact);

        if(!loadedContacts.remove(contact))
            Log.e(TAG, "Failed to remove loaded card from the array");


        File file = new File(wrapper.getFilesDir().getAbsolutePath() + "/"+ contact.getName() + ".vcf");
        if(file != null)
            file.delete();
    }


    public static InsertResult updateContact(ContextWrapper wrapper, Contact contact, Card card){
        contact.addCard(card);

        saveContact(wrapper, contact);

        return new InsertResult(contact.getCardLength() - 1,loadedContacts.indexOf(contact)); // Returns the index of Card
    }

    public static InsertResult updateContact(ContextWrapper wrapper, Contact contact){
        saveContact(wrapper, contact);

        return new InsertResult(contact.getCardLength() - 1,loadedContacts.indexOf(contact)); // Returns the index of Card
    }

    private static void saveContact(ContextWrapper wrapper, Contact contact){

        try {
            XMLHelper.saveContact(wrapper, contact);

            // Save bitmaps
            for(Card card : contact.getCards()){

                String path = wrapper.getDir("jpg", 0).getAbsolutePath() + "/"+card.getTakenOnFormatted()+".jpg";
                File file = new File(path);
                Log.i(TAG, "Saving bitmap to: " + path);

                // If file exists, we don't save this picture again
                if(file.exists())
                    continue;

                // Get byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                card.getBitmap().compress(Bitmap.CompressFormat.JPEG, 70, stream);
                byte[] bitmapArray = stream.toByteArray();

                FileOutputStream outputStream = new FileOutputStream (file);
                outputStream.write(bitmapArray);
                outputStream.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadContacts(ContextWrapper wrapper)
    {
        if(isLoaded){
            // Is loaded and will not repeat process
            Log.i(TAG, "Tried to load Contacts when already loaded.");
            return;
        }

        File[] files = wrapper.getDir(XMLHelper.CONTACTS_DIR, 0).listFiles();

        Log.i(TAG, "Found files: " + files.length);
        for(int i = 0; i < files.length; i++){

            // Check if skip
            if(files[i].isDirectory())
                continue;
            else if(files[i].length() == 0)
                continue;
            else if (!fileExtension(files[i].getAbsolutePath()).equals("xml")) {
                Log.e(TAG, "Skipped:" + files[i].getName());
                continue;
            }

            String filePath = files[i].getPath();

            try {

                Contact contact = XMLHelper.readContact(filePath);

                // Assign bitmap to each card
                for(Card card : contact.getCards()){
                    String path = wrapper.getDir("jpg",0).getAbsolutePath().toString()+"/"+ card.getTakenOnFormatted() +".jpg";
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    card.setBitmap(bitmap);
                }

                Log.e(TAG, "Found a following Contact: " + contact.getName() + " with first card: " + contact.getCard(0).getLname());

                loadedContacts.add(contact);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        isLoaded = true;
    }

    @Deprecated
    public static Card[] getContacts(ContextWrapper wrapper)
    {
        File[] files = wrapper.getFilesDir().listFiles();
        List<Card> containerList = new ArrayList<>();

        Log.e(TAG, "Found files: " + files.length);
        for(int i = 0; i < files.length; i++){

            if(files[i].isDirectory())
                continue;
            else if(files[i].length() == 0)
                continue;
            else if (!files[i].getAbsolutePath().contains("@")) {
                Log.i(TAG, "Skipped:" + files[i].getName());
                continue;
            }

            String filePath = files[i].getPath();
            String vCard;

            try {

                FileInputStream fis = new FileInputStream(filePath);
                BufferedReader br = new BufferedReader( new InputStreamReader(fis));
                StringBuilder sb = new StringBuilder();
                String line;
                while(( line = br.readLine()) != null ) {
                    sb.append( line );
                    sb.append( '\n' );
                }

                vCard = sb.toString();
                Card cc = VCardHelper.vCardToContainer(vCard);

                cc.setBitmap(BitmapFactory.decodeFile(
                        wrapper.getDir("jpg",0).getAbsolutePath().toString()+"/"+cc.getEmail() +".jpg"));

                Log.e(TAG, "Found a following Card: " + cc.getName());
                containerList.add(cc);
            } catch (Exception e) {
                Log.e(TAG, "Error on: " + e.toString());
                e.printStackTrace();
            }
        }

        Card[] containers = new Card[containerList.size()];
        containers = containerList.toArray(containers);

        return containers;
    }

    // Gets file extension
    private static String fileExtension(String path){
        int i = path.lastIndexOf('.');
        return path.substring(i+1);
    }

    public static class InsertResult{

        InsertResult(int cardIndex, int contactIndex){
            this.cardIndex = cardIndex;
            this.contactIndex = contactIndex;
        }

        private int cardIndex;
        private int contactIndex;

        public int getCardIndex() {
            return cardIndex;
        }

        public int getContactIndex() {
            return contactIndex;
        }

    }
}

