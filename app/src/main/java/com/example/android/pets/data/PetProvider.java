package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.example.android.pets.data.PetsContract.PetsEntry;

/**
 * Created by pramod on 30/1/18.
 */

public class PetProvider extends ContentProvider {

    public static final String LOG_TAG = PetProvider.class.getSimpleName();
    public static final int PETS = 100;
    public static final int PET_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(PetsContract.CONTENT_AUTHORITY, PetsContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetsContract.CONTENT_AUTHORITY, PetsContract.PATH_PETS + "/#", PET_ID);
    }

    private PetsSQLiteHelper sqLiteHelper;

    @Override
    public boolean onCreate() {
        sqLiteHelper = new PetsSQLiteHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = sqLiteHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                cursor = db.query(PetsContract.PetsEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PET_ID:
                selection = PetsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(PetsEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Can not query unknown URI " + uri);
        }
        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was selected for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for" + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */

    @Nullable
    private Uri insertPet(Uri uri, ContentValues contentValues) {
        //Check that the name is not null
        String name = contentValues.getAsString(PetsEntry.COLUMN_NAME);
        if (name == null)
            throw new IllegalArgumentException("Pet requires a name");
        //Check that the gender is valid
        Integer gender = contentValues.getAsInteger(PetsEntry.COLUMN_GENDER);
        if (gender == null || !PetsEntry.isValidGender(gender))
            throw new IllegalArgumentException("Pet requires a valid gender");
        // If the weight is provided, check that it's greater than or equal to 0 kg
        Integer weight = contentValues.getAsInteger(PetsEntry.COLUMN_WEIGHT);
        if (weight != null && weight < 0)
            throw new IllegalArgumentException("Pet requires a valid weight");

        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();
        // Insert the new pet with the given values
        long id = db.insert(PetsEntry.TABLE_NAME, null, contentValues);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        //Notify all listeners that the data has changed for the pet content URI
        //uri : content://com.example.android.pets/pets
        getContext().getContentResolver().notifyChange(uri,null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                selection = PetsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Error in updating record");
        }
    }

    private int updatePet(Uri uri, ContentValues contentValues, String selection, String selectionArgs[]) {
        // If the {@link PetEntry#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        if (contentValues.containsKey(PetsEntry.COLUMN_NAME)) {
            String name = contentValues.getAsString(PetsEntry.COLUMN_NAME);
            if (name == null)
                throw new IllegalArgumentException("Pet requires a name");
        }

        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (contentValues.containsKey(PetsEntry.COLUMN_GENDER)) {
            Integer gender = contentValues.getAsInteger(PetsEntry.COLUMN_GENDER);
            if (gender == null || !PetsEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (contentValues.containsKey(PetsEntry.COLUMN_WEIGHT)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer weight = contentValues.getAsInteger(PetsEntry.COLUMN_WEIGHT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database

        if (contentValues.size() == 0)
            return 0;

        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

        //Perform the update on the database and get the number of rows affected
        int rowsUpdated =  db.update(PetsEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        //If 1 or more rows were updated, then notify all listeners that the data at the given
        //URI has changed
        if(rowsUpdated >= 0)
            getContext().getContentResolver().notifyChange(uri,null);
        // Returns the number of database rows affected by the update statement
        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase db = sqLiteHelper.getWritableDatabase();

        //Track the number of rows that were deleted.
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = db.delete(PetsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PET_ID:
                // Delete a single row given by the ID in the URI
                selection = PetsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(PetsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        //If 1 or more rows were deleted, then notify all notify all listeners that the data at the
        //given URI changed
        if (rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri,null);
        return rowsDeleted;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetsEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
