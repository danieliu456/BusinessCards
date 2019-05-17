package com.mif.zxcrew.txtclassifier;

import android.graphics.Bitmap;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Card.java
 *
 * Purpose: store information from recognized card.
 *
 * @author - Aurimas Garnevicius
 */

public class Card {

    private Contact belongsTo;

    private Bitmap bitmap;
    private String name;
    private String lname;
    private String company;
    private String position;
    private String telNo;
    private String mobNo;
    private String email;
    private String comment;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");

    private Date takenOn;

    // suggestions
    private String[] typingSuggestions;

    public Card(){
        takenOn = new Date();
    }

    public Card(Contact belongsTo){
        this.belongsTo = belongsTo;
        takenOn = new Date();
    }

    public void setTypingSuggestions(String[] typingSuggestions) {
        this.typingSuggestions = typingSuggestions;
    }

    public String[] getTypingSuggestions() {
        return typingSuggestions;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getTelNo() {
        return telNo;
    }

    public void setTelNo(String telNo) {
        this.telNo = telNo;
    }

    public String getMobNo() {
        return mobNo;
    }

    public void setMobNo(String mobNo) {
        this.mobNo = mobNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getTakenOn() {
        return takenOn;
    }

    public String getTakenOnFormatted() {
        return sdf.format(takenOn);
    }

    public void setTakenOn(Date takenOn) {
        this.takenOn = takenOn;
    }

    // Parser
    public void setTakenOn(String takenOn) {
        try {
            this.takenOn = sdf.parse(takenOn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Contact getBelongsTo() {
        return belongsTo;
    }

    public void setBelongsTo(Contact belongsTo) {
        this.belongsTo = belongsTo;
    }
}

