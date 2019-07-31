package com.example.m.recipebook.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class SqliteDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Recipes.db";
    private static final String TABLE_NAME = "recipes_table";
    private static final String KEY_ID = "ID";
    private static final String COL2 = "TITLE";
    private static final String COL3 = "DESCRIPTION";

    private static final String TABLE_NAME_INGRED = "ingredients_table";
    private static final String COL_ING1 = "ING1";
    private static final String COL_ING2 = "ING2";
    private static final String COL_ING3 = "ING3";
    private static final String COL_ING4 = "ING4";
    private static final String COL_ING5 = "ING5";
    private static final String COL_ING6 = "ING6";
    private static final String COL_ING7 = "ING7";
    private static final String COL_ING8 = "ING8";
    private static final String COL_ING9 = "ING9";
    private static final String COL_ING10 = "ING10";
    private static final String COL_ING11 = "ING11";
    private static final String COL_ING12 = "ING12";
    private static final String COL_ING13 = "ING13";
    private static final String COL_ING14 = "ING14";
    private static final String COL_ING15 = "ING15";

    private static final String TABLE_NAME_EQUIPMENT = "equipment_table";
    private static final String COL_EQUIP1 = "EQUIP1";
    private static final String COL_EQUIP2 = "EQUIP2";
    private static final String COL_EQUIP3 = "EQUIP3";
    private static final String COL_EQUIP4 = "EQUIP4";
    private static final String COL_EQUIP5 = "EQUIP5";
    private static final String COL_EQUIP6 = "EQUIP6";
    private static final String COL_EQUIP7 = "EQUIP7";
    private static final String COL_EQUIP8 = "EQUIP8";
    private static final String COL_EQUIP9 = "EQUIP9";
    private static final String COL_EQUIP10 = "EQUIP10";
    private static final String COL_EQUIP11 = "EQUIP11";
    private static final String COL_EQUIP12 = "EQUIP12";
    private static final String COL_EQUIP13 = "EQUIP13";
    private static final String COL_EQUIP14 = "EQUIP14";
    private static final String COL_EQUIP15 = "EQUIP15";

    private static final String TABLE_NAME_METHOD = "method_table";
    private static final String COL_MET1 = "MET1";
    private static final String COL_MET2 = "MET2";
    private static final String COL_MET3 = "MET3";
    private static final String COL_MET4 = "MET4";
    private static final String COL_MET5 = "MET5";
    private static final String COL_MET6 = "MET6";
    private static final String COL_MET7 = "MET7";
    private static final String COL_MET8 = "MET8";
    private static final String COL_MET9 = "MET9";
    private static final String COL_MET10 = "MET10";
    private static final String COL_MET11 = "MET11";
    private static final String COL_MET12 = "MET12";
    private static final String COL_MET13 = "MET13";
    private static final String COL_MET14 = "MET14";
    private static final String COL_MET15 = "MET15";



    public SqliteDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE " + TABLE_NAME + "("+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + " TITLE TEXT, DESCRIPTION TEXT )";
        String createIngredTable = "CREATE TABLE " + TABLE_NAME_INGRED + "(" + KEY_ID + " INTEGER, " + "ING1 TEXT, ING2 TEXT, ING3 TEXT, ING4 TEXT, ING5 TEXT, ING6 TEXT,ING7 TEXT, ING8 TEXT, ING9 TEXT, ING10 TEXT, ING11 TEXT, ING12 TEXT, ING13 TEXT, ING14 TEXT, ING15 TEXT)";
        String createEquipTable = "CREATE TABLE " + TABLE_NAME_EQUIPMENT + "(" + KEY_ID + " INTEGER, " + "EQUIP1 TEXT, EQUIP2 TEXT, EQUIP3 TEXT, EQUIP4 TEXT, EQUIP5 TEXT, EQUIP6 TEXT,EQUIP7 TEXT, EQUIP8 TEXT, EQUIP9 TEXT, EQUIP10 TEXT, EQUIP11 TEXT, EQUIP12 TEXT, EQUIP13 TEXT, EQUIP14 TEXT, EQUIP15 TEXT)";
        String createMethodTable = "CREATE TABLE " + TABLE_NAME_METHOD + "(" + KEY_ID + " INTEGER, " + "MET1 TEXT, MET2 TEXT, MET3 TEXT, MET4 TEXT, MET5 TEXT, MET6 TEXT,MET7 TEXT, MET8 TEXT, MET9 TEXT, MET10 TEXT, MET11 TEXT, MET12 TEXT, MET13 TEXT, MET14 TEXT, MET15 TEXT)";
        sqLiteDatabase.execSQL(createTable);
        sqLiteDatabase.execSQL(createIngredTable);
        sqLiteDatabase.execSQL(createEquipTable);
        sqLiteDatabase.execSQL(createMethodTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS '" + TABLE_NAME + "'");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS '" + TABLE_NAME_INGRED + "'");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS '" + TABLE_NAME_EQUIPMENT + "'");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS '" + TABLE_NAME_METHOD + "'");
        onCreate(sqLiteDatabase);
    }




    public boolean addData(String title, String description, List ingredients, List equipment, List method){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, title);
        contentValues.put(COL3, description);
        long id = db.insertWithOnConflict(TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        Log.d(TAG, "addData: " + id);

        ContentValues ingredientsContentValues = new ContentValues();
        ingredientsContentValues.put(KEY_ID, id);
        try {
            if (ingredients != null) {
                int ingredientsSize = ingredients.size();
                if (ingredientsSize < 15) {
                    ingredientsSize++;
                    for (int size = ingredientsSize; size < 16; size++) {
                        ingredients.add(" ");
                    }
                }
                ingredientsContentValues.put(COL_ING1, String.valueOf(ingredients.get(0)));
                ingredientsContentValues.put(COL_ING2, String.valueOf(ingredients.get(1)));
                ingredientsContentValues.put(COL_ING3, String.valueOf(ingredients.get(2)));
                ingredientsContentValues.put(COL_ING4, String.valueOf(ingredients.get(3)));
                ingredientsContentValues.put(COL_ING5, String.valueOf(ingredients.get(4)));
                ingredientsContentValues.put(COL_ING6, String.valueOf(ingredients.get(5)));
                ingredientsContentValues.put(COL_ING7, String.valueOf(ingredients.get(6)));
                ingredientsContentValues.put(COL_ING8, String.valueOf(ingredients.get(7)));
                ingredientsContentValues.put(COL_ING9, String.valueOf(ingredients.get(8)));
                ingredientsContentValues.put(COL_ING10, String.valueOf(ingredients.get(9)));
                ingredientsContentValues.put(COL_ING11, String.valueOf(ingredients.get(10)));
                ingredientsContentValues.put(COL_ING12, String.valueOf(ingredients.get(11)));
                ingredientsContentValues.put(COL_ING13, String.valueOf(ingredients.get(12)));
                ingredientsContentValues.put(COL_ING14, String.valueOf(ingredients.get(13)));
                ingredientsContentValues.put(COL_ING15, String.valueOf(ingredients.get(14)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        long result1 = db.insert(TABLE_NAME_INGRED, null, ingredientsContentValues);

        ContentValues equipmentContentValues = new ContentValues();
        equipmentContentValues.put(KEY_ID, id);
        if (equipment != null) {
            int equipmentSize = equipment.size();
            if (equipmentSize < 15) {
                equipmentSize++;
                for (int size = equipmentSize; size < 16; size++) {
                    equipment.add(" ");
                }
            }
            equipmentContentValues.put(COL_EQUIP1, String.valueOf(equipment.get(0)));
            equipmentContentValues.put(COL_EQUIP2, String.valueOf(equipment.get(1)));
            equipmentContentValues.put(COL_EQUIP3, String.valueOf(equipment.get(2)));
            equipmentContentValues.put(COL_EQUIP4, String.valueOf(equipment.get(3)));
            equipmentContentValues.put(COL_EQUIP5, String.valueOf(equipment.get(4)));
            equipmentContentValues.put(COL_EQUIP6, String.valueOf(equipment.get(5)));
            equipmentContentValues.put(COL_EQUIP7, String.valueOf(equipment.get(6)));
            equipmentContentValues.put(COL_EQUIP8, String.valueOf(equipment.get(7)));
            equipmentContentValues.put(COL_EQUIP9, String.valueOf(equipment.get(8)));
            equipmentContentValues.put(COL_EQUIP10, String.valueOf(equipment.get(9)));
            equipmentContentValues.put(COL_EQUIP11, String.valueOf(equipment.get(10)));
            equipmentContentValues.put(COL_EQUIP12, String.valueOf(equipment.get(11)));
            equipmentContentValues.put(COL_EQUIP13, String.valueOf(equipment.get(12)));
            equipmentContentValues.put(COL_EQUIP14, String.valueOf(equipment.get(13)));
            equipmentContentValues.put(COL_EQUIP15, String.valueOf(equipment.get(14)));
        }
        long result2 = db.insert(TABLE_NAME_EQUIPMENT, null, equipmentContentValues);

        ContentValues methodContentValues = new ContentValues();
        methodContentValues.put(KEY_ID, id);
        if (method != null) {
            int methodSize = method.size();
            if (methodSize < 15) {
                methodSize++;
                for (int size = methodSize; size < 16; size++) {
                    method.add(" ");
                }
            }
            methodContentValues.put(COL_MET1, String.valueOf(method.get(0)));
            methodContentValues.put(COL_MET2, String.valueOf(method.get(1)));
            methodContentValues.put(COL_MET3, String.valueOf(method.get(2)));
            methodContentValues.put(COL_MET4, String.valueOf(method.get(3)));
            methodContentValues.put(COL_MET5, String.valueOf(method.get(4)));
            methodContentValues.put(COL_MET6, String.valueOf(method.get(5)));
            methodContentValues.put(COL_MET7, String.valueOf(method.get(6)));
            methodContentValues.put(COL_MET8, String.valueOf(method.get(7)));
            methodContentValues.put(COL_MET9, String.valueOf(method.get(8)));
            methodContentValues.put(COL_MET10, String.valueOf(method.get(9)));
            methodContentValues.put(COL_MET11, String.valueOf(method.get(10)));
            methodContentValues.put(COL_MET12, String.valueOf(method.get(11)));
            methodContentValues.put(COL_MET13, String.valueOf(method.get(12)));
            methodContentValues.put(COL_MET14, String.valueOf(method.get(13)));
            methodContentValues.put(COL_MET15, String.valueOf(method.get(14)));
        }
        long result3 = db.insert(TABLE_NAME_METHOD, null, methodContentValues);

        if (result1 == -1){
            return false;
        }
        else {
            return true;
        }
    }

    public Cursor showData(int type){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor data = null;
        if (type == 0) {
            data = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        }
        else if (type == 1) {
            data = db.rawQuery("SELECT * FROM " + TABLE_NAME_INGRED, null);
        }
        else if (type == 2) {
            data = db.rawQuery("SELECT * FROM " + TABLE_NAME_EQUIPMENT, null);
        }
        else if (type == 3) {
            data = db.rawQuery("SELECT * FROM " + TABLE_NAME_METHOD, null);
        }

        return data;
    }

    public boolean updateData(String id, String title, String description, List ingredients, List equipment, List method){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //contentValues.put(COL1, id);
        contentValues.put(COL2, title);
        contentValues.put(COL3, description);
        db.update(TABLE_NAME, contentValues, KEY_ID + " = ?", new String[]{String.valueOf(id)});

        ContentValues ingredientsContentValues = new ContentValues();

        //ingredientsContentValues.put(COL1, id);
        if (ingredients != null) {
            int ingredientsSize = ingredients.size();
            if (ingredientsSize < 15) {
                ingredientsSize++;
                for (int size = ingredientsSize; size < 16; size++) {
                    ingredients.add(" ");
                }
            }
            ingredientsContentValues.put(COL_ING1, String.valueOf(ingredients.get(0)));
            ingredientsContentValues.put(COL_ING2, String.valueOf(ingredients.get(1)));
            ingredientsContentValues.put(COL_ING3, String.valueOf(ingredients.get(2)));
            ingredientsContentValues.put(COL_ING4, String.valueOf(ingredients.get(3)));
            ingredientsContentValues.put(COL_ING5, String.valueOf(ingredients.get(4)));
            ingredientsContentValues.put(COL_ING6, String.valueOf(ingredients.get(5)));
            ingredientsContentValues.put(COL_ING7, String.valueOf(ingredients.get(6)));
            ingredientsContentValues.put(COL_ING8, String.valueOf(ingredients.get(7)));
            ingredientsContentValues.put(COL_ING9, String.valueOf(ingredients.get(8)));
            ingredientsContentValues.put(COL_ING10, String.valueOf(ingredients.get(9)));
            ingredientsContentValues.put(COL_ING11, String.valueOf(ingredients.get(10)));
            ingredientsContentValues.put(COL_ING12, String.valueOf(ingredients.get(11)));
            ingredientsContentValues.put(COL_ING13, String.valueOf(ingredients.get(12)));
            ingredientsContentValues.put(COL_ING14, String.valueOf(ingredients.get(13)));
            ingredientsContentValues.put(COL_ING15, String.valueOf(ingredients.get(14)));
        }
        db.update(TABLE_NAME_INGRED, ingredientsContentValues, KEY_ID + " = ?", new String[]{String.valueOf(id)});

        ContentValues equipmentContentValues = new ContentValues();

        // equipmentContentValues.put(COL1, id);
        if (equipment != null) {
            int equipmentSize = equipment.size();
            if (equipmentSize < 15) {
                equipmentSize++;
                for (int size = equipmentSize; size < 16; size++) {
                    equipment.add(" ");
                }
            }
            equipmentContentValues.put(COL_EQUIP1, String.valueOf(equipment.get(0)));
            equipmentContentValues.put(COL_EQUIP2, String.valueOf(equipment.get(1)));
            equipmentContentValues.put(COL_EQUIP3, String.valueOf(equipment.get(2)));
            equipmentContentValues.put(COL_EQUIP4, String.valueOf(equipment.get(3)));
            equipmentContentValues.put(COL_EQUIP5, String.valueOf(equipment.get(4)));
            equipmentContentValues.put(COL_EQUIP6, String.valueOf(equipment.get(5)));
            equipmentContentValues.put(COL_EQUIP7, String.valueOf(equipment.get(6)));
            equipmentContentValues.put(COL_EQUIP8, String.valueOf(equipment.get(7)));
            equipmentContentValues.put(COL_EQUIP9, String.valueOf(equipment.get(8)));
            equipmentContentValues.put(COL_EQUIP10, String.valueOf(equipment.get(9)));
            equipmentContentValues.put(COL_EQUIP11, String.valueOf(equipment.get(10)));
            equipmentContentValues.put(COL_EQUIP12, String.valueOf(equipment.get(11)));
            equipmentContentValues.put(COL_EQUIP13, String.valueOf(equipment.get(12)));
            equipmentContentValues.put(COL_EQUIP14, String.valueOf(equipment.get(13)));
            equipmentContentValues.put(COL_EQUIP15, String.valueOf(equipment.get(14)));
        }
        db.update(TABLE_NAME_EQUIPMENT, equipmentContentValues, KEY_ID + " = ?", new String[]{String.valueOf(id)});

        ContentValues methodContentValues = new ContentValues();

        //methodContentValues.put(COL1, id);
        if (method != null) {
            int methodSize = method.size();
            if (methodSize < 15) {
                methodSize++;
                for (int size = methodSize; size < 16; size++) {
                    method.add(" ");
                }
            }
            methodContentValues.put(COL_MET1, String.valueOf(method.get(0)));
            methodContentValues.put(COL_MET2, String.valueOf(method.get(1)));
            methodContentValues.put(COL_MET3, String.valueOf(method.get(2)));
            methodContentValues.put(COL_MET4, String.valueOf(method.get(3)));
            methodContentValues.put(COL_MET5, String.valueOf(method.get(4)));
            methodContentValues.put(COL_MET6, String.valueOf(method.get(5)));
            methodContentValues.put(COL_MET7, String.valueOf(method.get(6)));
            methodContentValues.put(COL_MET8, String.valueOf(method.get(7)));
            methodContentValues.put(COL_MET9, String.valueOf(method.get(8)));
            methodContentValues.put(COL_MET10, String.valueOf(method.get(9)));
            methodContentValues.put(COL_MET11, String.valueOf(method.get(10)));
            methodContentValues.put(COL_MET12, String.valueOf(method.get(11)));
            methodContentValues.put(COL_MET13, String.valueOf(method.get(12)));
            methodContentValues.put(COL_MET14, String.valueOf(method.get(13)));
            methodContentValues.put(COL_MET15, String.valueOf(method.get(14)));
        }
        db.update(TABLE_NAME_METHOD, methodContentValues, KEY_ID + " = ?", new String[]{String.valueOf(id)});

        return true;
    }

    public Integer deleteRecipe(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        db.delete(TABLE_NAME_INGRED, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        db.delete(TABLE_NAME_EQUIPMENT, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        return db.delete(TABLE_NAME_METHOD, KEY_ID + " = ?", new String[]{String.valueOf(id)});
    }
}
