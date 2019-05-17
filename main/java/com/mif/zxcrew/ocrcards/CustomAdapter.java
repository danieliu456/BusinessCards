package com.mif.zxcrew.ocrcards;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.mif.zxcrew.helpers.XMLHelper;
import com.mif.zxcrew.txtclassifier.Card;
import com.mif.zxcrew.txtclassifier.Contact;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * CustomAdapter.java
 *
 * Purpose: holds all the contact information and displays it visually;
 *
 * getView() sets visual view of contact;
 *
 * @author Aivaras Ivoskus
 * @author Ugnius Versekenas
 */
public class CustomAdapter extends ArrayAdapter<Contact> implements Filterable{

    LayoutInflater inflater;
    CustomFilter filter;
    List<Contact> filteredList = new LinkedList<>();

    public CustomAdapter(Context applicationContext, List<Contact> arrayList) {
        super(applicationContext, R.layout.activity_list_item, arrayList);
        inflater = (LayoutInflater.from(applicationContext));
        this.filteredList.addAll(Database.getLoadedContacts());
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        view = inflater.inflate(R.layout.activity_list_item, null);
        TextView item = (TextView) view.findViewById(R.id.item);
        TextView subItem = (TextView) view.findViewById(R.id.subitem);
        TextView subItem2 = (TextView) view.findViewById(R.id.subitem2);
        ImageView image = (ImageView) view.findViewById(R.id.image);
        item.setText(getItem(i).getName()+"  "+getItem(i).getLastname());
        subItem.setText(getItem(i).getCard(0).getCompany());
        subItem2.setText(getItem(i).getCard(0).getTelNo());
        image.setImageBitmap(getItem(i).getCard(0).getBitmap());
        return view;
    }

    // Clear after deleting something
    public void refreshLists(){
        filteredList.clear();
        filteredList.addAll(Database.getLoadedContacts());
    }

    @NonNull
    @Override
    public Filter getFilter() {
        if(filter == null){
            filter=new CustomFilter();
        }
        return filter;
    }

    class CustomFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if(constraint != null && constraint.length()>0){
                constraint=constraint.toString().toLowerCase();
                ArrayList<Contact> filters = new ArrayList<>();
                for(int i = 0; i< filteredList.size(); i++){
                    if(filteredList.get(i).getName().toLowerCase().contains(constraint) ||
                            filteredList.get(i).getCard(0).getLname().toLowerCase().contains(constraint) ||
                            filteredList.get(i).getCard(0).getCompany().toLowerCase().contains(constraint) ||
                            filteredList.get(i).getCard(0).getPosition().toLowerCase().contains(constraint) ||
                            filteredList.get(i).getCard(0).getMobNo().toLowerCase().contains(constraint) ||
                            filteredList.get(i).getCard(0).getTelNo().toLowerCase().contains(constraint) ||
                            filteredList.get(i).getCard(0).getEmail().toLowerCase().contains(constraint)){
                        filters.add(filteredList.get(i));
                    }
                }
                results.count= filters.size();
                results.values = filters;
            } else {
                results.count= filteredList.size();
                results.values= filteredList;
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            clear();
            for(Contact container : (List<Contact>) results.values)
                add(container);

            notifyDataSetChanged();
        }
    }

}