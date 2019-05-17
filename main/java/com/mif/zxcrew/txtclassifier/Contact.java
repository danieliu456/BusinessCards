package com.mif.zxcrew.txtclassifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Contact.java
 *
 * Purpose: class for store cards which depends for one person.
 *
 * @author - Aurimas Garnevicius
 * @author - Aivaras Ivoskus
 */

public class Contact {

    private String name;
    private String lastname;
    private ArrayList<Card> cardCards = new ArrayList<>();

    // Unique ID given to Contact when creating
    private int xmlIdentifier;

    // Used when building
    public Contact(int xmlIdentifier){
        this.xmlIdentifier = xmlIdentifier;
    }

    public Contact(){
        // -1 means it doesn't have any identifications, and will have one made on File output process
        xmlIdentifier = -1;
    }

    public void addCard(Card cc)
    {
        cardCards.add(cc);
    }

    public int getCardLength()
    {
        return cardCards.size();
    }

    public Card getCard(int i){
        return cardCards.get(i);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public int getXmlIdentifier() {
        return xmlIdentifier;
    }

    public void setXmlIdentifier(int xmlIdentifier) {
        this.xmlIdentifier = xmlIdentifier;
    }

    public List<Card> getCards() { return cardCards; }

}
