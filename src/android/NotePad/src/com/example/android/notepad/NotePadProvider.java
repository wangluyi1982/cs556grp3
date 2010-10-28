/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.notepad;

import com.example.android.notepad.NotePad.Notes;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
//import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
//import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Provides access to a database of notes. Each note has a title, the note
 * itself, a creation date and a modified data.
 */
public class NotePadProvider extends ContentProvider {

    private static final String TAG = "NotePadProvider";

    private static final String DATABASE_NAME = "note_pad.db";
    private static final int DATABASE_VERSION = 2;
    private static final String NOTES_TABLE_NAME = "Ontology";
    private static final String NOTES_RELATIONSHIPS = "Relationships";
    	

    private static HashMap<String, String> sNotesProjectionMap;

    private static final int NOTES = 1;
    private static final int NOTE_ID = 2;

    private static final UriMatcher sUriMatcher;

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        /*
         * Create 2 tables: Ontology and Relationships
         */
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + NOTES_TABLE_NAME + " ("
                    + Notes._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + Notes.TITLE + " TEXT UNIQUE);");
            
            db.execSQL("CREATE TABLE " + NOTES_RELATIONSHIPS + " ("
            		+ Notes.PARENT + " INTEGER,"
            		+ Notes.CHILD + " INTEGER,"
            		+ " PRIMARY KEY(PARENT, CHILD));");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
        case NOTES:
            qb.setTables(NOTES_TABLE_NAME);
            qb.setProjectionMap(sNotesProjectionMap);
            break;

        case NOTE_ID:
            qb.setTables(NOTES_TABLE_NAME);
            qb.setProjectionMap(sNotesProjectionMap);
            qb.appendWhere(Notes._ID + "=" + uri.getPathSegments().get(1));
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        /*
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = NotePad.Notes.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }*/

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, null);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case NOTES:
            return Notes.CONTENT_TYPE;

        case NOTE_ID:
            return Notes.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != NOTES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        
        // initially, values should have TITLE and PARENT as strings
        
        if (values.containsKey(NotePad.Notes.TITLE) == false) {
        	values.put(NotePad.Notes.TITLE, "new node");
        	// title is required!
        	//throw new SQLException("Failed to insert row into " + uri);
        }
        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        
        // no parent has been set, so no relationship required, just insert title into Ontology
        // note: if relationship is required, PARENT_TITLE has to be set in initialValues
        
        long cid = 0;
        
        if (values.containsKey(NotePad.Notes.TITLE) == false){
        	values.put(NotePad.Notes.TITLE, "<parent>,<child>");
        }
        cid = db.insert(NOTES_TABLE_NAME, Notes.NOTE, values);
       
       
        if (cid > 0) {
            Uri noteUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, cid);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }
    
    public ArrayList<String> getSiblings(String node){
    	// return the an ArrayList containing the siblings of a given node
    	// first find parent of the node
    	// after parent is found, find children of that parent
    	//mOpenHelper = new DatabaseHelper(getContext());
    	SQLiteDatabase db = mOpenHelper.getReadableDatabase();
    	ArrayList<String> Siblings = new ArrayList<String>(); // arraylist to store sibling titles
    	
    	long cid, pid;
    	
    	// get node id
    	Cursor c, x;
    	c = db.query(NOTES_TABLE_NAME, new String[] {"_ID"}, " title = '" + node + "'", null, null, null, null);
    	cid = c.getInt(1);
    	
    	// get parent id
    	c = db.query(NOTES_RELATIONSHIPS, new String[]{"PARENT"}, " CHILD = " + cid, null, null, null, null);
    	pid = c.getInt(1);
    	
    	// get parent's children
    	c = db.query(NOTES_RELATIONSHIPS, new String[]{"CHILD"}, " PARENT = " + pid, null, null, null, null);
    	
    	
    	// iterate through the cursor and store the sibling ids in the arraylist
    	int id;
    	c.moveToFirst();
    	while (c.isAfterLast() == false){
    		id = c.getInt(1);
    		// get title for id
    		x = db.query(NOTES_TABLE_NAME, new String[]{"title"}, " _ID = " + id, null, null, null, null);
    		if (x.getCount() != 0){
    			Siblings.add(x.getString(1));
    		}
    		c.moveToNext();
    	}
    	return Siblings;
    }
    
    public ArrayList<String> getChildren(String node){
    	// return the children of a node as an array
    	//mOpenHelper = new DatabaseHelper(getContext());
    	SQLiteDatabase db = mOpenHelper.getReadableDatabase();
    	ArrayList<String> Children = new ArrayList<String>();
    
    	// get node id
    	long cid;
    	Cursor c, x;
    	c = db.query(NOTES_TABLE_NAME, new String[] {"_id"}, " title = '" + node + "'", null, null, null, null);
    	cid = c.getInt(1);
    	
    	// get children of the node in the relationships table
    	c = db.query(NOTES_RELATIONSHIPS, new String[] {"CHILD"}, " Parent = " + cid, null, null, null, null);
    	
    	// store children ids
    	int id;
    	if (c.getCount() != 0){
    		c.moveToFirst();
    		while(c.isAfterLast() == false){
    			id = c.getInt(1);
    			// get id's title
    			x = db.query(NOTES_TABLE_NAME, new String[]{"title"}, "_ID = " + id, null, null, null, null);
    			if (x.getCount() != 0){
    				Children.add(x.getString(1));
    			}
    			c.moveToNext();
    		}
    	}
    	return Children;
    }
    

    @Override
	public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case NOTES:
            count = db.delete(NOTES_TABLE_NAME, where, whereArgs);
            break;

        case NOTE_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.delete(NOTES_TABLE_NAME, Notes._ID + "=" + noteId, null);
            db.delete(NOTES_RELATIONSHIPS, "Parent = " + noteId + " OR Child = " + noteId, null);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
    
    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        values.remove("modified");
        values.remove("note");
        int count;
        switch (sUriMatcher.match(uri)) {
        case NOTES:
            count = db.update(NOTES_TABLE_NAME, values, where, whereArgs);
            break;

        case NOTE_ID:
            String noteId = uri.getPathSegments().get(1);
            String nodename = (String) values.get("title");
            long parent_id, child_id;
            if (nodename.indexOf(",") > 0){
            	String parent_child[] = nodename.split(",");
            	//update with parent
            	values.clear();
            	values.put("TITLE", parent_child[0]);
            	count = db.update(NOTES_TABLE_NAME, values, Notes._ID + "=" + noteId, null);
            	parent_id = Integer.parseInt(noteId);
            	values.clear();
            	values.put("TITLE", parent_child[1]);
            	child_id = db.insert(NOTES_TABLE_NAME, null, values);
            	
            	//insert into relationships table
            	if (child_id > 0){
	            	values.clear();
	            	values.put("PARENT", parent_id);
	            	values.put("CHILD", child_id);
	            	db.insert(NOTES_RELATIONSHIPS, null, values);
            	}
            }else{
            	// no parent child so simply insert the node
            	count = db.update(NOTES_TABLE_NAME, values, Notes._ID + "=" + noteId, null);
            }
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(NotePad.AUTHORITY, "notes", NOTES);
        sUriMatcher.addURI(NotePad.AUTHORITY, "notes/#", NOTE_ID);

        sNotesProjectionMap = new HashMap<String, String>();
        sNotesProjectionMap.put(Notes._ID, Notes._ID);
        sNotesProjectionMap.put(Notes.TITLE, Notes.TITLE);
        sNotesProjectionMap.put(Notes.NOTE, Notes.NOTE);
        sNotesProjectionMap.put(Notes.CREATED_DATE, Notes.CREATED_DATE);
        sNotesProjectionMap.put(Notes.MODIFIED_DATE, Notes.MODIFIED_DATE);
    }

}
