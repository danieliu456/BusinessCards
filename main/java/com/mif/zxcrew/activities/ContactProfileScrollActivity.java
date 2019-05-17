package com.mif.zxcrew.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mif.zxcrew.ocrcards.Database;
import com.mif.zxcrew.ocrcards.KKViewPager;
import com.mif.zxcrew.ocrcards.R;
import com.mif.zxcrew.ocrcards.TestFragmentAdapter;
import com.mif.zxcrew.txtclassifier.Card;
import com.mif.zxcrew.txtclassifier.Contact;

/**
 * ContactProfileScroll.java
 *
 * Purpose: Responsible for activity_contact_profile_scroll.xml functionality
 *
 * onCreate() receives KEY_CONTACT_INDEX and sets up Contact Profile Scroll Activity layout;
 *
 * onCreateOptionsMenu() sets up toolbar buttons and adds listeners;
 *
 * initKKViewPager() sets up View Pager, makes scrollable layout if more than two cards belongs to
 * the same contact;
 *
 * @author Aivaras Ivoskus
 * @author Ugnius Versekenas
 */

public class ContactProfileScrollActivity extends AppCompatActivity {

    public static final String KEY_CONTACT_INDEX = "KEY_CONTACT_INDEX";

    private KKViewPager mPager;
    public static String[] countOfCards;

    Contact contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_profile_scroll);

        setSupportActionBar((Toolbar)findViewById(R.id.toolbarContactProfileScroll));

        // Receive card info and then make text fields
        if(getIntent().hasExtra(KEY_CONTACT_INDEX)){
            final int key = getIntent().getIntExtra(KEY_CONTACT_INDEX, -1);
            contact = Database.getContactByIndex(key);
        }

        countOfCards = new String[contact.getCards().size()];
        for(int i = 0; i < countOfCards.length; i++) {
            //if exists add to CountCards
            countOfCards[i] = contact.getName();
        }
        //set cardKey
        for(int i = 0; i< countOfCards.length; i++) {
            initKKViewPager(i);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.

        ImageButton imageCopy = findViewById(R.id.toolbar_copy);
        imageCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Retrieve current contact needed
                Card card = contact.getCard(mPager.getCurrentItem());

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                String text;
                text = card.getName()+" "+ card.getLname()+" "+ card.getCompany()+" "+ card.getPosition()
                        +" "+ card.getMobNo()+" "+ card.getTelNo()+" "+ card.getEmail();
                ClipData clip = ClipData.newPlainText("Card", text);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(ContactProfileScrollActivity.this, "Card information saved to clip board", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageButton imageCheck = findViewById(R.id.toolbar_check_profile);
        imageCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //saveAndExit();
                Intent intent = new Intent(ContactProfileScrollActivity.this, ContactViewActivity.class);
                startActivity(intent);
            }
        });
        return true;
    }

    private void initKKViewPager(int cardKey) {

        mPager = (KKViewPager) findViewById(R.id.kk_pager);
        mPager.setAdapter(new TestFragmentAdapter(getSupportFragmentManager(),
                ContactProfileScrollActivity.this, countOfCards, getIntent().getIntExtra(KEY_CONTACT_INDEX, -1), cardKey));

        mPager.setAnimationEnabled(true);
        mPager.setFadeEnabled(true);
        mPager.setFadeFactor(0.6f);

    }

}
