package com.example.m.recipebook;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
        private List<String> ParentItem;
        private HashMap<String, List<String>> ChildItem;

        public ExpandableListAdapter(Context context, List<String> parentItem, HashMap<String, List<String>> childItem) {
            this.context = context;
            this.ParentItem = parentItem;
            this.ChildItem = childItem;
        }


        @Override
        public int getGroupCount() {
            return this.ParentItem.size();
        }

        @Override
        public int getChildrenCount(int listPosition) {
            return this.ChildItem.get(this.ParentItem.get(listPosition)).size();
        }


        @Override
        public Object getGroup(int listPosition) {
            return this.ParentItem.get(listPosition);
        }

        @Override
        public Object getChild(int listPosition, int expandedListPosition) {
            return this.ChildItem.get(this.ParentItem.get(listPosition)).get(expandedListPosition);
        }

        @Override
        public long getGroupId(int listPosition) {
            return listPosition;
        }

        @Override
        public long getChildId(int listPosition, int expandedListPosition) {
            return expandedListPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int listPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            String listTitle = (String) getGroup(listPosition);
            if (convertView == null){
                LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.expand_list_group, null);
            }
            TextView listTitleTV = (TextView) convertView.findViewById(R.id.TV_ExpandableListTitle);
            listTitleTV.setTypeface(null, Typeface.BOLD);
            listTitleTV.setText(listTitle);
            return convertView;
        }

        @Override
        public View getChildView(int listPosition, int expandedListPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            final String expandedListText = (String) getChild(listPosition,expandedListPosition);
            if (convertView == null){
                LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.expand_list_item, null);
            }
            TextView text = (TextView) convertView.findViewById(R.id.expandableListItem);
            text.setText(""+ expandedListText);
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int listPosition, int expandedListPosition) {
            return true;
        }

        public static LinkedHashMap<String, List<String>> getData() {
            LinkedHashMap<String, List<String>> ParentItem = new LinkedHashMap<String, List<String>>();

            List<String> Starters = new ArrayList<String>();
            Starters.add("Meats");
            Starters.add("Breads");
            Starters.add("Cheeses");
            Starters.add("Tapas");
            Starters.add("Vegetables");
            Starters.add("Antipasto");
            Starters.add("Other Starters");


            List<String> MainCourse = new ArrayList<String>();
            MainCourse.add("Meat");
            MainCourse.add("Beef");
            MainCourse.add("Pork");
            MainCourse.add("Pasta");
            MainCourse.add("Roast");
            MainCourse.add("Rice");
            MainCourse.add("Potatoes");
            MainCourse.add("Seafood");
            MainCourse.add("Sandwiches");
            MainCourse.add("Chicken");
            MainCourse.add("Burgers");
            MainCourse.add("Pies");
            MainCourse.add("Other Main Courses");

            List<String> World = new ArrayList<>();
            World.add("African");
            World.add("Asian");
            World.add("European");
            World.add("Middle Eastern");
            World.add("North American");
            World.add("Oceanic");
            World.add("South American");
            World.add("Mediterranean");
            World.add("Caribbean");
            World.add("Other Regions");

            List<String> Drinks = new ArrayList<>();
            Drinks.add("Tea");
            Drinks.add("Coffee");
            Drinks.add("Soda");
            Drinks.add("Juices");
            Drinks.add("Mocktails");
            Drinks.add("Milkshakes");
            Drinks.add("Smoothies");
            Drinks.add("Chocolate");
            Drinks.add("Hot Drinks");
            Drinks.add("Natural");
            Drinks.add("Other Drinks");

            List<String> DrinksAlcoholic = new ArrayList<>();
            DrinksAlcoholic.add("Cocktails");
            DrinksAlcoholic.add("Wine");
            DrinksAlcoholic.add("Beer");
            DrinksAlcoholic.add("Cider");
            DrinksAlcoholic.add("Occasions");
            DrinksAlcoholic.add("Other Alcohols");

            List<String> Desserts = new ArrayList<>();
            Desserts.add("Cakes");
            Desserts.add("Tarts");
            Desserts.add("Ice cream");
            Desserts.add("Puddings");
            Desserts.add("Chocolates");
            Desserts.add("Cookies");
            Desserts.add("Candies");
            Desserts.add("Pastries");
            Desserts.add("Other Desserts");

            List<String> Soups = new ArrayList<>();
            Soups.add("Consomme");
            Soups.add("Broth");
            Soups.add("Puree");
            Soups.add("Cold Soups");
            Soups.add("Meat Soups");
            Soups.add("Fish Soups");
            Soups.add("Vegetable Soups");
            Soups.add("Other Soups");

            List<String> Salads = new ArrayList<>();
            Salads.add("Rice Salads");
            Salads.add("Fruit Salads");
            Salads.add("Green Salads");
            Salads.add("Chicken Salads");
            Salads.add("Rice Salads");
            Salads.add("Meat Salads");
            Salads.add("Seafood Salads");
            Salads.add("Other Salads");

            List<String> Breads = new ArrayList<>();
            Breads.add("White Bread");
            Breads.add("Brown Bread");
            Breads.add("Rye Bread");
            Breads.add("Sourdough");
            Breads.add("Rolls");
            Breads.add("Bagels");
            Breads.add("Brioche");
            Breads.add("Other Breads");

            List<String> Others = new ArrayList<>();
            Others.add("Other Recipes");


            ParentItem.put("Starters", Starters);
            ParentItem.put("Main Course", MainCourse);
            ParentItem.put("World", World);
            ParentItem.put("Drinks", Drinks);
            ParentItem.put("Drinks Alcoholic", DrinksAlcoholic);
            ParentItem.put("Desserts", Desserts);
            ParentItem.put("Soups", Soups);
            ParentItem.put("Salads", Salads);
            ParentItem.put("Breads", Breads);
            ParentItem.put("Others", Others);


            return ParentItem;

        }
}
