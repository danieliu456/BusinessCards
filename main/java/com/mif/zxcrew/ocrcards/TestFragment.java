package com.mif.zxcrew.ocrcards;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mif.zxcrew.activities.ContactEditActivity;
import com.mif.zxcrew.txtclassifier.Card;
import com.mif.zxcrew.txtclassifier.Contact;

import java.util.ArrayList;

/**
 * TestFragment.java
 *
 * Purpose: Responsible for setting up ViewPager page;
 *
 * newInstance() receives KEY_CONTACT_INDEX and KEY_CARD_INDEX from previous activity;
 *
 * onCreate() creates fragment;
 *
 * onCreateView() sets up layout, textfields, image and buttons;
 *
 * isNumeric() checks if given String is only from numbers;
 *
 * addTextField() adds TextField with given text;
 *
 * @author Ugnius Versekenas
 */

public final class TestFragment extends Fragment {

    public static final String KEY_CONTACT_INDEX = "KEY_CONTACT_INDEX";
    public static final String KEY_CARD_INDEX = "KEY_CARD_INDEX";
    private LinearLayout cardLayout;
    Bundle bundle;

    Contact contact;
    Card card;
    //other data layout
    LinearLayout scrollLayout;
    //L.Name and F.Name layout
    LinearLayout layout;
    ArrayList<TextFieldProfile> textFields = new ArrayList<>();

    public static TestFragment newInstance(String content, Context c, int contactKey, int cardKey) {

        //receive keys of contact index and card index
        TestFragment fragment = new TestFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_CONTACT_INDEX,contactKey);
        bundle.putInt(KEY_CARD_INDEX, cardKey);
        fragment.setArguments(bundle);

        fragment.mContent = content;
        return fragment;
    }

    private String mContent = "???";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup Rootview = (ViewGroup) inflater.inflate(R.layout.fragment_contact_profile,
                container, false);

        cardLayout = (LinearLayout) Rootview.findViewById(R.id.card);
        cardLayout.setBackgroundColor(Color.LTGRAY);
        // Initialize required components
        scrollLayout = Rootview.findViewById(R.id.contactProfileScrollContainer);

        //card = Database.getContactByIndex(0);
        bundle = this.getArguments();
        if (bundle != null) {
            contact = Database.getContactByIndex(bundle.getInt(KEY_CONTACT_INDEX, -1));
            card = contact.getCard(bundle.getInt(KEY_CARD_INDEX));
        }

        //add name and lname to existing TextView
        TextView textNameLname = (TextView)Rootview.findViewById(R.id.textNameLname);
        textNameLname.setText(card.getName()+" "+ card.getLname());

        //add TextFields with other information
        addTextField("Company","Company").setText(card.getCompany());
        addTextField("Position", "Position").setText(card.getPosition());
        addTextField("Work. Tel.", "Work. Tel.").setText(card.getTelNo())
                .getTextViewInput().setInputType(InputType.TYPE_CLASS_PHONE);
        addTextField("Home. Tel.", "Home. Tel.").setText(card.getMobNo())
                .getTextViewInput().setInputType(InputType.TYPE_CLASS_PHONE);
        addTextField("Email", "Email").setText(card.getEmail())
                .getTextViewInput().setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        //add ImageView
        ImageView image = Rootview.findViewById(R.id.contactEditImagePreviewProfile);
        image.setImageBitmap(card.getBitmap());

        LinearLayout layoutCall = (LinearLayout) Rootview.findViewById(R.id.layoutCall);
        LinearLayout layoutMessage = (LinearLayout) Rootview.findViewById(R.id.layoutMessage);
        LinearLayout layoutEdit = (LinearLayout) Rootview.findViewById(R.id.layoutEdit);

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
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
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
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
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
                                Toast.makeText(getActivity(), "Make call for "+phone, Toast.LENGTH_SHORT).show();
                                startActivity(callIntent);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
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
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
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
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
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
                                Toast.makeText(getActivity(), "Message "+phone, Toast.LENGTH_SHORT).show();
                                startActivity(sendIntent);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
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
                                Intent intent = new Intent(getActivity(), ContactEditActivity.class);
                                intent.putExtra(ContactEditActivity.KEY_CONTACT_INDEX, bundle.getInt(KEY_CONTACT_INDEX, -1));
                                intent.putExtra(ContactEditActivity.KEY_CARD_INDEX, bundle.getInt(KEY_CARD_INDEX, -1));
                                startActivityForResult(intent, 0);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
                builder.setMessage("Are you sure you want to edit this card ?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });
        return Rootview;
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

    TextFieldProfile addTextField(String name, String hint){

        View view;

        // Inflate the text field layout
        view = getLayoutInflater().inflate(R.layout.text_field_profile, scrollLayout, false);
        scrollLayout.addView(view);

        TextView textViewInput = (TextView) view.findViewById(R.id.textFieldInputProfile);
        TextView textViewName = (TextView)view.findViewById(R.id.textFieldNameProfile);

        TextFieldProfile textField = new TextFieldProfile(textViewName, textViewInput, name,hint);
        textFields.add(textField);

        return textField;
    }

    // OTHER CLASSES

    class TextFieldProfile {

        TextView textViewName;
        TextView textViewInput;

        public TextFieldProfile(TextView textViewName, TextView textViewInput, String name, String hint){
            this.textViewName = textViewName;
            this.textViewInput = textViewInput;

            if(name.equals("L.Name") || name.equals("F.Name")){
                //do not set up information for Name and LName
                textViewName.setText("");
            }
            else {
                textViewName.setText(name);
            }
            textViewInput.setText(hint);

        }

        public String getText() { return textViewInput.getText().toString(); }
        public TextFieldProfile setText(String text) { textViewInput.setText(text); return this; }


        public TextView getTextViewInput() {
            return textViewInput;
        }

    }
}
