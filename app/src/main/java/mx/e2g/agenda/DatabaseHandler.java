package mx.e2g.agenda;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tec on 3/04/15.
 */
public class DatabaseHandler extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "db_contactsManager",
        TABLE_CONTACTS = "contacts",
        KEY_ID = "id",
        KEY_NAME = "name",
        KEY_PHONE = "phone",
        KEY_EMAIL = "email",
        KEY_ADDRESS = "address",
        KEY_IMAGEURI = "imageUri";
    private static final String TAG = "DatabaseHandler";

    public DatabaseHandler(Context context){
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE " + TABLE_CONTACTS + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_NAME + " VARCHAR(100)," +
                KEY_PHONE + " VARCHAR(100)," +
                KEY_EMAIL + " VARCHAR(100)," +
                KEY_ADDRESS + " VARCHAR(100)," +
                KEY_IMAGEURI + " VARCHAR(100))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXIST " + TABLE_CONTACTS );

        onCreate(db);
    }

    public void createContact(Contact contact){
        SQLiteDatabase db = getWritableDatabase();
        Log.d(TAG," Nombre: " + contact.getName());
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getName().trim());
        values.put(KEY_PHONE, contact.getPhone().trim());
        values.put(KEY_EMAIL, contact.getEmail().trim());
        values.put(KEY_ADDRESS, contact.getAddress().trim());
        values.put(KEY_IMAGEURI, contact.getImgUri().toString()); // Es necesario obtener el .toString()

        db.insert(TABLE_CONTACTS, null, values);
        db.close();
    }

    public Contact getContactById(int id){
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID,KEY_NAME,KEY_PHONE,KEY_EMAIL,KEY_ADDRESS,KEY_IMAGEURI}, KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null );

        if (cursor != null)
            cursor.moveToFirst();

        Contact contact = new Contact(Integer.parseInt(cursor.getString(0)),cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), Uri.parse(cursor.getString(5)) );

        return contact;
    }

    public boolean existContactByName(Contact contact){
        SQLiteDatabase db = getReadableDatabase();
        Log.d(TAG," existContactByName Nombre: " + contact.getName());
        Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID,KEY_NAME,KEY_PHONE,KEY_EMAIL,KEY_ADDRESS,KEY_IMAGEURI}, KEY_NAME + "=?", new String[] { contact.getName().toString().trim() }, null, null, null, null );
        //Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CONTACTS + " WHERE " + KEY_NAME + "=?" , new String[] { contact.getName().toString().trim().toLowerCase() });
        int count = cursor.getCount();
        Log.d(TAG, " existContactByName count: " + count);
        if (cursor.moveToFirst()){
            Log.d(TAG, " existContactByName cursor: " + cursor.getString(1));
            return true;
        } else {
            return false;
        }
    }

    public void deleteContact(Contact contact){
        SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_CONTACTS, KEY_ID + "=?", new String[] { String.valueOf(contact.getId())});
        Log.d(TAG, "Borrando al contacto : " + contact.getName());
        db.close();
    }

    public int getContactsCount(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CONTACTS, null);
        int count = cursor.getCount();
        db.close();
        cursor.close();

        return count;
    }

    public int updateContact(Contact contact){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getName());
        values.put(KEY_PHONE, contact.getPhone());
        values.put(KEY_EMAIL, contact.getEmail());
        values.put(KEY_ADDRESS, contact.getAddress());
        values.put(KEY_IMAGEURI, contact.getImgUri().toString());

        int rowsAffected = db.update(TABLE_CONTACTS, values, KEY_ID + "=?", new String[]{String.valueOf(contact.getId())});

        db.close();
        return rowsAffected;
    }

    public List<Contact> getAllContacts(){
        List<Contact> contacts = new ArrayList<Contact>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CONTACTS, null);
        if (cursor.moveToFirst()){
            do {
                contacts.add(new Contact(Integer.parseInt(cursor.getString(0)),cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), Uri.parse(cursor.getString(5)) ));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return contacts;
    }

}
