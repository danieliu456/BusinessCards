package com.mif.zxcrew.helpers;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

import com.mif.zxcrew.txtclassifier.Card;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
/**
* VCardHelper.java
*
* Purpose: class helps to manipulate VCard
*
* contactTovCard() converts words to VCard string;
*
* addToContactBook() add contact to phone contact list;
*
* VCardToContainer() read VCard file and converts to container
*
* @author Daniel Spakovskij;
 */
public class VCardHelper {

    public final static String TAG = "VCARDHELPER";

    public static String contactTovCard(String name, String lname, String company_name, String job_title, String worktel, String hometel, String mail) {



            String str = "BEGIN:VCARD\n" +
                    "VERSION:4.0\n" +
                    "N:" + lname + ";" + name + ";;;\n" +
                    "FN:" + name + " " + lname + "\n" +
                    "ORG:" + company_name + "\n" +
                    "TITLE:" + job_title + "\n" +
                    "TEL;TYPE=work,voice;VALUE=uri:tel:" + worktel + "\n" + // darbo telefonas
                    "TEL;TYPE=home,voice;VALUE=uri:tel:" + hometel + "\n" + // namu telefonas
                    "EMAIL:" + mail + "\n" +
                    "REV:20080424T195243Z\n" +
                    "END:VCARD";



            return str;
    }

    public static void addToContactBook(ContentResolver resolver, String name, String lname, String company_name, String job_title, String worktel, String hometel, String mail) throws IOException {
        String fullname = name + " " + lname;
        ArrayList<ContentProviderOperation> contact = new ArrayList<ContentProviderOperation>();

        contact.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI).
                withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null).
                withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());

        if (fullname != null) {
            contact.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).
                    withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).
                    withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE).
                    withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, fullname).build());
        }
        if (worktel != null) {
            contact.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).
                    withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).
                    withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE).
                    withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, worktel).
                    withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());
        }
        if (hometel != null) {
            contact.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).
                    withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).
                    withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE).
                    withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, hometel).
                    withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK).build());
        }
        if (mail != null) {
            contact.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).
                    withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).
                    withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE).
                    withValue(ContactsContract.CommonDataKinds.Email.DATA, mail).
                    withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK).build());
        }
        if (!company_name.equals("") && !job_title.equals("")) {
            contact.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).
                    withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).
                    withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE).
                    withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, company_name).
                    withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK).
                    withValue(ContactsContract.CommonDataKinds.Organization.TITLE, job_title).
                    withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK).build());
        }

        try {

            resolver.applyBatch(ContactsContract.AUTHORITY,contact);
        } catch (RemoteException e){ e.printStackTrace();}
        catch (OperationApplicationException e){ e.printStackTrace();}


    }

    public static Card vCardToContainer(String s) throws IOException
    {
        String name, lname, company_name,job_title,worktel,hometel,mail,temporary;
        Card cc = new Card();

        // VDataBuilder builder = new VdataBuilder();
        // VCardParser parder = new VCardParser();
        BufferedReader br = null;
        String sCurrentLine;

        InputStream is = new ByteArrayInputStream(s.getBytes());
        br = new BufferedReader(new InputStreamReader(is)); //new FileReader(fileName));
        while ((sCurrentLine = br.readLine()) != null)
        {

            try {
                if (sCurrentLine.contains("FN:") == true) {
                    temporary = sCurrentLine.substring((sCurrentLine.indexOf(":") + 1), sCurrentLine.length());
                    String mas[] = temporary.split(" ");
                    name = mas[0];
                    lname = mas[1];
                    cc.setName(name);
                    cc.setLname(lname);
                    // System.out.println(name);

                } else if (sCurrentLine.contains("ORG") == true) {
                    company_name = sCurrentLine.substring((sCurrentLine.indexOf(":") + 1), sCurrentLine.length());
                    cc.setCompany(company_name);
                    // System.out.println(company_name);

                } else if (sCurrentLine.contains("TITLE") == true) {
                    job_title = sCurrentLine.substring((sCurrentLine.indexOf(":") + 1), sCurrentLine.length());
                    cc.setPosition(job_title);
                    //System.out.println(job_title);

            }
            else if(sCurrentLine.contains("TYPE=work")==true)
            {
                worktel=sCurrentLine.substring((sCurrentLine.indexOf("tel:")+4),sCurrentLine.length());
                cc.setMobNo(worktel);
                //System.out.println(worktel);

            }
            else if(sCurrentLine.contains("TYPE=home")==true)
            {
                hometel=sCurrentLine.substring((sCurrentLine.indexOf("tel:")+4),sCurrentLine.length());
                cc.setTelNo(hometel);
                //System.out.println(hometel);

                } else if (sCurrentLine.contains("EMAIL") == true) {
                    mail = sCurrentLine.substring((sCurrentLine.indexOf(":") + 1), sCurrentLine.length());
                    cc.setEmail(mail);
                    // System.out.println(mail);

                }
            } catch (Exception e){
                Log.e(TAG, "Failed at scanning a part of vcard " + e.getMessage());
            }
        }

        return cc;
    }


}
