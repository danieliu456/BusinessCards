package com.mif.zxcrew.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mif.zxcrew.helpers.VCardHelper;
import com.mif.zxcrew.ocrcards.Database;
import com.mif.zxcrew.ocrcards.R;
import com.mif.zxcrew.txtclassifier.Card;
import com.mif.zxcrew.txtclassifier.Contact;

import java.util.ArrayList;

/**
 * ContactEditActivity.java
 *
 * Purpose: Responsible for contact_layout.xml functionality
 *
 * onCreate() sets up Contact Edit Activity layout;
 *
 * addTextField() adds TextField with given text;
 *
 * onCreateOptionsMenu() sets up toolbar buttons and adds listeners;
 *
 * isIfValid() returns true;
 *
 * saveAndExit() saves changed information, writes to XML and exits;
 *
 * deleteAndExit() removes Contact card with information and exits;
 *
 * setSelectedTextFlied() Checkbox functionality, changes information between TextFields when two
 * checkboxes selected;
 *
 * @author Aurimas Garnevicius
 * @author Aivaras Ivoskus
 * @author Ugnius Versekenas
 */

public class ContactEditActivity extends AppCompatActivity {

    public static final String KEY_CONTACT_INDEX = "KEY_CONTACT_INDEX";
    public static final String KEY_CARD_INDEX = "KEY_CARD_INDEX";

    Card card;
    LinearLayout scrollLayout;
    ArrayList<TextField> textFields = new ArrayList<>();
    TextField selectedTextField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_layout);

        setSupportActionBar((Toolbar)findViewById(R.id.toolbarContactEdit));

        //Initialize required components
        scrollLayout = findViewById(R.id.contactEditScrollContainer);

        //Receive card info and then make text fields
        if(getIntent().hasExtra(KEY_CONTACT_INDEX)){
            int i = getIntent().getIntExtra(KEY_CARD_INDEX, -1);
            int j = getIntent().getIntExtra(KEY_CONTACT_INDEX, -1);
            card = Database.getLoadedContacts().get(j).getCard(i);
        }

        //add TextFields and ImageView
        addTextField("F.Name","First Name").setText(card.getName());
        addTextField("L.Name","Last Name").setText(card.getLname());
        addTextField("Company","Company").setText(card.getCompany());
        addTextField("Position", "Position").setText(card.getPosition());
        addTextField("Work. Tel.", "Work. Tel.").setText(card.getTelNo())
                .getEditText().setInputType(InputType.TYPE_CLASS_PHONE);
        addTextField("Home. Tel.", "Home. Tel.").setText(card.getMobNo())
                .getEditText().setInputType(InputType.TYPE_CLASS_PHONE);
        addTextField("Email", "Email").setText(card.getEmail())
                .getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        ImageView image = findViewById(R.id.contactEditImagePreview);
        image.setImageBitmap(card.getBitmap());
    }

    private TextField addTextField(String name, String hint){

        //Inflate the text field layout
        View view = getLayoutInflater().inflate(R.layout.text_field, scrollLayout, false);
        scrollLayout.addView(view);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        AutoCompleteTextView editText = (AutoCompleteTextView) view.findViewById(R.id.textFieldInput);
        if(card.getTypingSuggestions()!= null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, card.getTypingSuggestions());
            editText.setAdapter(adapter);
            editText.setDropDownAnchor(R.id.contactEditImagePreview);
        }

        TextView textView = (TextView)view.findViewById(R.id.textFieldName);
        CheckBox checkBox = (CheckBox)view.findViewById(R.id.textFieldCheck);

        TextField textField = new TextField(textView,editText,checkBox,name,hint);
        textFields.add(textField);
        return textField;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.

        ImageButton imageCheck = findViewById(R.id.toolbar_check);
        imageCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndExit();
                Intent intent = new Intent(ContactEditActivity.this, ContactProfileScrollActivity.class);
                intent.putExtra(ContactEditActivity.KEY_CONTACT_INDEX, getIntent().getIntExtra(KEY_CONTACT_INDEX, -1));
                intent.putExtra(ContactEditActivity.KEY_CARD_INDEX, getIntent().getIntExtra(KEY_CARD_INDEX, -1));
                startActivity(intent);
            }
        });

        ImageButton imageDelete = findViewById(R.id.toolbar_delete);
        imageDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ContactEditActivity.this, R.style.AppCompatAlertDialogStyle);
                builder.setMessage("Are you sure you want to delete card entry?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteAndExit();
                                Intent intent = new Intent(ContactEditActivity.this, ContactViewActivity.class);
                                startActivityForResult(intent, 0);
                            }

                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing;
                            }
                });

                builder.create().show();
            }
        });
        return true;
    }

    boolean isIfValid(){
        return true;
    }

    void saveAndExit(){

        if(!isIfValid())
            return;

        card.setName(textFields.get(0).getText());
        card.setLname(textFields.get(1).getText());
        card.setCompany(textFields.get(2).getText());
        card.setPosition(textFields.get(3).getText());
        card.setTelNo(textFields.get(4).getText());
        card.setMobNo(textFields.get(5).getText());
        card.setEmail(textFields.get(6).getText());

        try {
            VCardHelper.addToContactBook(this.getContentResolver(), card.getName(), card.getLname(), card.getCompany(),
                    card.getPosition(), card.getMobNo(), card.getTelNo(), card.getEmail());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Database.updateContact(this, card.getBelongsTo());
        setResult(10);
        finish();

    }

    void deleteAndExit(){
        int i = getIntent().getIntExtra(KEY_CARD_INDEX, -1);
        int j = getIntent().getIntExtra(KEY_CONTACT_INDEX, -1);
        Contact contact = Database.getContactByIndex(j);
        contact.getCards().remove(i);

        if(contact.getCardLength() == 0)
            Database.deleteContact(this, contact);
        else
            Database.updateContact(this, contact);

        setResult(10);
        finish();
    }

    void setSelectedTextField(TextField textField){
        if(selectedTextField == null){
            selectedTextField = textField;
        } else {

            // Memorize and replace
            String str = selectedTextField.getEditText().getText().toString();
            selectedTextField.getEditText().setText(textField.getEditText().getText().toString());
            textField.getEditText().setText(str);

            // Reset checkboxes
            selectedTextField.getCheckBox().setChecked(false);
            textField.getCheckBox().setChecked(false);

            // Set to null again
            selectedTextField = null;
        }
    }

    // OTHER CLASSES
    class TextField {

        TextView textView;
        AutoCompleteTextView editText;
        CheckBox checkBox;

        public TextField(TextView textView, AutoCompleteTextView editText, CheckBox checkBox, String name, String hint){
            this.textView = textView;
            this.editText = editText;
            this.checkBox = checkBox;

            textView.setText(name);
            editText.setHint(hint);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setSelectedTextField(TextField.this);
                }
            });
        }

        public String getText() { return editText.getText().toString(); }
        public TextField setText(String text) { editText.setText(text); return this; }

        public AutoCompleteTextView getEditText() {
            return editText;
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }
    }
}
