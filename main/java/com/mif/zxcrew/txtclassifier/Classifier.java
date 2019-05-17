package com.mif.zxcrew.txtclassifier;

import com.mif.zxcrew.helpers.TessHelper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * Classifier.java
 *
 * Purpose: classifies returned text from Teseerect. Recognize name, surname, job position, company
 * name, telephone number, mobile number and email.
 *
 * constructor - prepares dictionaries for further manipulation and clears recognized text array
 * from whitespaces.
 *
 * setData() - sets class Card entities.
 *
 * deleteNumbers() - sometimes Tesseract changes letter to number. When program search for name,
 * surname, company name and job position, there is a one of conditions to exist only letters. So
 * this class changes numbers to most similar letter.
 *
 * hardLetterDeletion() - happens that Tesseract put some symbol on some letter, for example 'š'
 * and program doesn't recognize it as letter. So this method replace letters like this to letters
 * that are in english alphabet.
 *
 * nameInsertion() - method which creates name and surname entities depending on words number in line.
 *
 * @author - Aurimas Garnevicius
 * @author - Daniel Spakovskij
 */

public class Classifier {

    static final String LTU = "lit";
    static final String ENG = "eng";

    private LinkedList<String> data;
    private String[] recognized; //array for collecting not recognized words
    private Dictionary dict; //dictionary of words for checking
    private Dictionary dictName;
    private Dictionary dictPosition;
    Card card = new Card();

    public Card getCard(){
        return card;
    }

    public Classifier(String recognizedText, String lang){
        //Set up dictionary for checking
        dict = new Dictionary();
        dict.build(TessHelper.GetTessDataDir().getAbsolutePath() + "/" + lang + "_DIC.dic");
        dictName = new Dictionary();
        dictName.build(TessHelper.GetTessDataDir().getAbsolutePath() + "/" + lang + "_NAME.dic");
        dictPosition = new Dictionary();
        try {
            dictPosition.build(TessHelper.GetTessDataDir().getAbsolutePath() + "/" + lang + "_POSITION.dic");
        }catch (Exception e){
            dictPosition.build(TessHelper.GetTessDataDir().getAbsolutePath() + "/eng_POSITION.dic");
        }
        String[] split = recognizedText.split("\n");

       //Prepare recognized text. Clean from whitespaces
       data = new LinkedList<String>(Arrays.asList(split));
       data.removeAll(Arrays.asList(null, ""));


       setData(lang);

        recognized = new String[data.size()];
        for (int i = 0; i < data.size(); i++){
            recognized[i] = data.get(i);
        }

       card.setTypingSuggestions(recognized);
    }

    public void setData(String lang) {
        Pattern p = Pattern.compile("^[ A-Za-z-.,]+$");//check if only letters in string

        for(String temporary: data) {

            if(card.getName() == null && card.getCompany() == null && card.getPosition() == null
                    && lang.equals(ENG))
                temporary = hardLettersDeletion(temporary);

            //Search name
            if(p.matcher(deleteNumbers(temporary)).matches() && card.getName() == null) {

                String[] tmp = temporary.split(" ");
                if (tmp.length > 1) {
                    //spin through line strings
                    for (int j = 0; j < tmp.length; j++) {
                        if (dictName.contains(tmp[j].toLowerCase()) || dictPosition.contains(tmp[j].toLowerCase())){
                            if(dictPosition.contains(tmp[0].replace(".", "").toLowerCase())){
                                nameInsertion(temporary, 0);
                            }
                            else if (dictPosition.contains(tmp[tmp.length - 1].replace(".", "").toLowerCase())){
                                nameInsertion(temporary, 1);
                            }
                        }
                    }

                    if(card.getName() == null){
                        int n = 0;//for tracking how much words don't contain in dictionary
                        for(int j = 0; j < tmp.length; j++){
                            if(!dict.contains(tmp[j].toLowerCase()))
                                n++;
                        }
                        if(n >= 1)
                            nameInsertion(temporary, 2);
                    }
                }
            }

            //Search position
            else if(p.matcher(deleteNumbers(temporary)).matches() && card.getPosition() == null){
                int n = 0;//for tracking how much words don't contain in dictionary
                String[] tmp = temporary.split(" ");

                //spin through line strings
                for (int j = 0; j < tmp.length; j++) {
                    System.out.println(dict.contains(tmp[j].toLowerCase())+tmp[j]);
                    //count how many words is not found in dictionary
                    if (!dict.contains(tmp[j].toLowerCase()))
                        n++;
                }
                if(n == 0) {
                    card.setPosition(temporary);
                }
            }

            //search company name
            else if(p.matcher(deleteNumbers(temporary)).matches() && card.getCompany()== null && card.getName() != null ){

                String[] tmp = temporary.split(" ");
                int n = 0; //for tracking how much words don't contain in dictionary
                for (int j = 0; j < tmp.length; j++){
                    if(!dict.contains(tmp[j].toLowerCase()))
                        n++;
                }
                //company name contains one word that doesn't contain dictionary or zero
                if(n <= 1 && !temporary.equals(card.getPosition())) {
                    card.setCompany(temporary);
                }


            }else if ((temporary.contains("@") || temporary.contains("©") )&& card.getEmail() == null || temporary.toLowerCase().contains("email") ||
                    temporary.toLowerCase().contains("e-mail") || temporary.toLowerCase().contains("mail")) {

                temporary = temporary.toLowerCase().replaceAll("email", "");
                if(temporary.length() > 8)
                    card.setEmail(temporary);
            }
            else if (temporary.contains("+") && card.getTelNo() == null || temporary.toLowerCase().contains("tel")) {
                temporary = temporary.replaceAll("[^0-9-+-]", "");
                card.setTelNo(temporary);
            }
            else if (temporary.contains("+") && card.getMobNo() == null ||temporary.toLowerCase().contains("mob")) {
                temporary = temporary.replaceAll("[^0-9-+-]", "");
                if(temporary.length() > 8)
                    card.setMobNo(temporary);
            }
        }
    }

    private String deleteNumbers (String tmp) {


        String[] letters = tmp.split("(?!^)");
        String result = "";
        for (int i = 0; i < letters.length; i++) {
            if (letters[i].equals(" ")){
                result = result + letters[i];
                continue;
            }
            try {
                int a = Integer.valueOf(letters[i]);
                switch (a) {
                    case 0:
                        letters[i] = "O";
                        break;
                    case 1:
                        letters[i] = "i";
                        break;
                    case 2:
                        letters[i] = "Z";
                        break;
                    case 3:
                        letters[i] = "E";
                        break;
                    case 4:
                        letters[i] = "A";
                        break;
                    case 5:
                        letters[i] = "S";
                        break;
                    case 6:
                        letters[i] = "b";
                        break;
                    case 7:
                        letters[i] = "T";
                        break;
                    case 8:
                        letters[i] = "B";
                        break;
                    case 9:
                        letters[i] = "g";
                        break;

                }
                result = result + letters[i];
            }catch (Exception e) {
                result = result + letters[i];
            }

        }
        return result;
    }

    private String hardLettersDeletion(String tmp){
        String[] letters = tmp.split("(?!^)");
        String result = "";


        for(int q = 0; q < letters.length; q++){
            if(letters[q].toLowerCase().matches("[äåæāăąàááã]")){
                if (Character.isUpperCase(letters[q].charAt(0))){
                    result = result + "A";
                }else{
                    result = result + "a";
                }
            }else if(letters[q].toLowerCase().matches("[šß§śş]")){
                if (Character.isUpperCase(letters[q].charAt(0))){
                    result = result + "S";
                }else{
                    result = result + "s";
                }
            }else if(letters[q].toLowerCase().matches("[ďđ]")){
                if (Character.isUpperCase(letters[q].charAt(0))){
                    result = result + "D";
                }else{
                    result = result + "d";
                }
            }else if(letters[q].toLowerCase().matches("[ģğ]")){
                if (Character.isUpperCase(letters[q].charAt(0))){
                    result = result + "G";
                }else{
                    result = result + "g";
                }
            }else if(letters[q].toLowerCase().matches("ķ")){
                if (Character.isUpperCase(letters[q].charAt(0))){
                    result = result + "K";
                }else{
                    result = result + "k";
                }
            }else if(letters[q].toLowerCase().matches("[ĺļľł]")){
                if (Character.isUpperCase(letters[q].charAt(0))){
                    result = result + "L";
                }else{
                    result = result + "l";
                }
            }else if(letters[q].toLowerCase().matches("[ëēěĕəęėèéê]")){
                if (Character.isUpperCase(letters[q].charAt(0))){
                    result = result + "E";
                }else{
                    result = result + "e";
                }
            }else if(letters[q].toLowerCase().matches("[þťțţ]")){
                if (Character.isUpperCase(letters[q].charAt(0))){
                    result = result + "T";
                }else{
                    result = result + "t";
                }
            }else if(letters[q].toLowerCase().matches("[űůüûúùūų]")){
                if (Character.isUpperCase(letters[q].charAt(0))){
                    result = result + "U";
                }else{
                    result = result + "u";
                }
            }else if(letters[q].toLowerCase().matches("[ıīïîíìį]")){
                if (Character.isUpperCase(letters[q].charAt(0))){
                    result = result + "I";
                }else{
                    result = result + "i";
                }
            }else if(letters[q].toLowerCase().matches("[œőøöõôóò]")){
                if (Character.isUpperCase(letters[q].charAt(0))){
                    result = result + "O";
                }else{
                    result = result + "o";
                }
            }else if(letters[q].toLowerCase().matches("[žźż]")){
                if (Character.isUpperCase(letters[q].charAt(0))){
                    result = result + "Z";
                }else{
                    result = result + "z";
                }
            }else if(letters[q].toLowerCase().matches("[čçć]")){
                if (Character.isUpperCase(letters[q].charAt(0))){
                    result = result + "C";
                }else{
                    result = result + "c";
                }
            }else if(letters[q].toLowerCase().matches("[ñńņň]")){
                if (Character.isUpperCase(letters[q].charAt(0))){
                    result = result + "N";
                }else{
                    result = result + "n";
                }
            }
            else
                result = result + letters[q];
        }
        if(tmp.contains(","))
            return result.substring(0, result.indexOf(","));
        else
            return result;
    }

    private void nameInsertion(String name, int code){
        String[] tmp = name.split(" ");

        if(tmp.length == 3 && code == 0){
            card.setName(tmp[1]);
            card.setLname(tmp[2]);
        }
        else if(tmp.length == 4 && code == 0){
            card.setName(tmp[1] + tmp[2]);
            card.setName(tmp[3]);
        }
        else if (tmp.length == 3 && code == 1) {
            card.setName(tmp[0]);
            card.setLname(tmp[1]);

        } else if (tmp.length == 4 && code == 1) {
            card.setName(tmp[0] + " " + tmp[1]);
            card.setLname(tmp[2]);

        }else if (tmp.length == 2 && code == 2){
            card.setName(tmp[0]);
            card.setLname(tmp[1]);
        }else if (tmp.length == 3 && code == 2){
            card.setName(tmp[0] + " " + tmp[1]);
            card.setLname(tmp[2]);
        }

    }
}
