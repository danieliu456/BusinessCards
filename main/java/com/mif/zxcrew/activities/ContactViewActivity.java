package com.mif.zxcrew.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.mif.zxcrew.helpers.TessHelper;
import com.mif.zxcrew.ocrcards.CustomAdapter;
import com.mif.zxcrew.ocrcards.Database;
import com.mif.zxcrew.ocrcards.R;
import com.mif.zxcrew.txtclassifier.Card;

/**
 * ContactViewActivity.java
 *
 * Purpose: Creates Contact List, responsible for list_view_activity.xml functionality
 *
 * onCreate() adds contacts to the list, on item click and on item long click features, creates search list, sets up Bottom Navigation menu;
 *
 * showPopup() creates Dialog for quick menu features, sets up call, message and edit buttons;
 *
 * isNumeric() checks if given String is only from numbers;
 *
 * onRequestPermissionsResult() called when user submits permissions;
 *
 * onActivityResult() called when returning from ContactEditActivity.java;
 *
 * @author Aivaras Ivoskus
 * @author Ugnius Versekenas
 */

public class ContactViewActivity extends AppCompatActivity {

    static final int MY_PERMISSIONS = 5;

    ListView list;
    CustomAdapter customAdapter;
    SearchView searchView;
    Dialog contactDialog;
    Card card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Load Database
        Database.loadContacts(this);

        //Create layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view_activity);


/*
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(this);

    builder.setTitle("Delete entry")
    .setMessage("Are you sure you want to delete this entry?")
    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            // continue with delete
        }
     })
    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            // do nothing
        }
     })
    .setIcon(android.R.drawable.ic_dialog_alert)
    .show();

*/
        contactDialog = new Dialog(this);


        list = (ListView)findViewById(R.id.contact_listView);
        customAdapter = new CustomAdapter(getApplicationContext(), Database.getLoadedContacts());
        list.setAdapter(customAdapter);
        list.setOnItemClickListener(new ItemList());
        list.setOnItemLongClickListener(new ItemListLong());

        ActivityCompat.requestPermissions(ContactViewActivity.this,
                new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                MY_PERMISSIONS);

        //search list
        searchView=(SearchView) findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                customAdapter.getFilter().filter(newText);
                return false;
            }
        });


        //Sets up Bottom menu_navigation menu
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigationView);

        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId())
                {
                    case R.id.navigation_contacts:
                        return true;

                    case R.id.navigation_capture:
                        Intent intentMain = new Intent(ContactViewActivity.this, CameraActivity.class);
                        startActivity(intentMain);
                        return true;

                    case R.id.navigation_settings:
                        Intent intentSettings = new Intent(ContactViewActivity.this, SettingsActivity.class);
                        startActivity(intentSettings);
                        return true;
                }
                return false;
            }
        });
    }

    public void ShowPopup(View v, final int position) {

        contactDialog.setContentView(R.layout.custompopup);
        //contactDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        card = Database.getContactByIndex(position).getCard(0);
        //Set text
        TextView nameLName = (TextView) contactDialog.findViewById(R.id.nameLname);
        TextView companyPhone = (TextView) contactDialog.findViewById(R.id.companyPhone);

        if(card.getName()!=null) nameLName.setText(card.getName()+ " ");
        if(card.getLname()!=null) nameLName.append(card.getLname());
        if(card.getCompany()!=null) companyPhone.setText(card.getCompany()+" ");
        if(card.getTelNo()!=null && !card.getTelNo().equals("")) companyPhone.append(card.getTelNo());
        else if (card.getMobNo()!=null) companyPhone.append(card.getMobNo());

        LinearLayout layoutCall = (LinearLayout) contactDialog.findViewById(R.id.layoutCall);
        LinearLayout layoutMessage = (LinearLayout) contactDialog.findViewById(R.id.layoutMessage);
        LinearLayout layoutEdit = (LinearLayout) contactDialog.findViewById(R.id.layoutEdit);

        //call button
        layoutCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number;
                final Intent callIntent = new Intent(Intent.ACTION_DIAL);
                if(card.getTelNo()!=null && !card.getTelNo().equals("")) {
                    number = card.getTelNo();
                }
                else if(card.getMobNo()!=null && !card.getMobNo().equals("")) {
                    number = card.getMobNo();
                }
                else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(ContactViewActivity.this, R.style.AppCompatAlertDialogStyle);
                    alert.setTitle("Information");
                    alert.setMessage("No Work or Home Tel.");
                    alert.setPositiveButton("OK", null);
                    alert.show();
                    return;
                }
                final String phone=number;

                if(number.startsWith("+")) {
                    number = number.replace("+", "");
                }
                if(!isNumeric(number)){
                    AlertDialog.Builder alert = new AlertDialog.Builder(ContactViewActivity.this, R.style.AppCompatAlertDialogStyle);
                    alert.setTitle("Information");
                    alert.setMessage("Work or Home Tel. is invalid");
                    alert.setPositiveButton("OK", null);
                    alert.show();
                    return;
                }

                callIntent.setData(Uri.parse("tel:"+phone));
                //dialog are you sure
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                Toast.makeText(ContactViewActivity.this, "Make call for "+phone, Toast.LENGTH_SHORT).show();
                                startActivity(callIntent);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ContactViewActivity.this, R.style.AppCompatAlertDialogStyle);
                builder.setMessage("Are you sure you want to call: "+phone+" ?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });

        //message button
        layoutMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number;
                final Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                if(card.getTelNo()!=null && !card.getTelNo().equals("")) {
                    number = card.getTelNo();
                }
                else if(card.getMobNo()!=null && !card.getMobNo().equals("")) {
                    number = card.getMobNo();
                }
                else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(ContactViewActivity.this, R.style.AppCompatAlertDialogStyle);
                    alert.setTitle("Information");
                    alert.setMessage("No Work or Home Tel.");
                    alert.setPositiveButton("OK", null);
                    alert.show();
                    return;
                }
                final String phone=number;

                if(number.startsWith("+")) {
                    number = number.replace("+", "");
                }
                if(!isNumeric(number)){
                    AlertDialog.Builder alert = new AlertDialog.Builder(ContactViewActivity.this, R.style.AppCompatAlertDialogStyle);
                    alert.setTitle("Information");
                    alert.setMessage("Work or Home Tel. is invalid");
                    alert.setPositiveButton("OK", null);
                    alert.show();
                    return;
                }

                sendIntent.setData(Uri.parse("sms:"+phone));
                //dialog are you sure
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                Toast.makeText(ContactViewActivity.this, "Message "+phone, Toast.LENGTH_SHORT).show();
                                startActivity(sendIntent);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ContactViewActivity.this, R.style.AppCompatAlertDialogStyle);
                builder.setMessage("Are you sure you want to message: "+phone+" ?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });

        //edit Button
        layoutEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //dialog are you sure
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                Intent intent = new Intent(ContactViewActivity.this, ContactEditActivity.class);
                                intent.putExtra(ContactEditActivity.KEY_CONTACT_INDEX, position);
                                intent.putExtra(ContactEditActivity.KEY_CARD_INDEX, 0);
                                startActivityForResult(intent, 0);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ContactViewActivity.this, R.style.AppCompatAlertDialogStyle);
                builder.setMessage("Are you sure you want to edit this card ?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();



            }
        });

        contactDialog.show();
    }

    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Success, permissions received
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        System.out.println("ACTIVITY RESULT: " + requestCode);
        if(resultCode == 10){
            customAdapter.refreshLists();
            customAdapter.notifyDataSetChanged();
        }
    }



    class ItemList implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){

            Intent intent = new Intent(ContactViewActivity.this, ContactProfileScrollActivity.class);
            intent.putExtra(ContactEditActivity.KEY_CONTACT_INDEX, position);

            startActivityForResult(intent, 0);
        }
    }

    class ItemListLong implements AdapterView.OnItemLongClickListener{
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            ShowPopup(view, position);
            return false;
        }


    }
}