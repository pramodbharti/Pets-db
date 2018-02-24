/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.example.android.pets;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.example.android.pets.data.PetsContract.PetsEntry;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //PetCursorAdapter global variable
    PetCursorAdapter petCursorAdapter;
    private static final int PET_LOADER = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        //Find the ListView which will be populated pet data.
        ListView listView = (ListView) findViewById(R.id.list);

        //Find and set empty view ont the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        //Setup an adapter to create a list item for each row of pet data in the Cursor.
        //There is no pet data yet (until the loader finishes ) so pass in null for the Cursor.
        petCursorAdapter = new PetCursorAdapter(this,null);
        listView.setAdapter(petCursorAdapter);
        //Setup the item click listeners
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Create a new intent to go to {@link EditorActivity.class}
                Intent intent = new Intent(CatalogActivity.this,EditorActivity.class);

                //Form the content URI that represents the specific pet that was clicked on,
                //by appending the id (passed as input to this method) onto the
                //{@link PetEntry#CONTENT_URI}.
                //For example, the URI would be "content://com.example.android.pets/pets/2"
                //if the pet with ID 2 was clicked on.
                Uri currentPetUri = ContentUris.withAppendedId(PetsEntry.CONTENT_URI,id);

                //Set the URI on the data field of the intent
                intent.setData(currentPetUri);

                //Launch the activity {@link EditorActivity} to display the data for the current pet.
                startActivity(intent);
            }
        });
        //Kick off the loader
        getLoaderManager().initLoader(PET_LOADER,null,this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }


    private void insertPet() {
        ContentValues values = new ContentValues();
        values.put(PetsEntry.COLUMN_NAME, "Toto");
        values.put(PetsEntry.COLUMN_BREED, "Terrier");
        values.put(PetsEntry.COLUMN_GENDER, 1);
        values.put(PetsEntry.COLUMN_WEIGHT, 7);

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
        // into the pets database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri uri = getContentResolver().insert(PetsEntry.CONTENT_URI,values);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                // Do nothing for now
                insertPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Helper method to delete all pets in the database.
     */
    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(PetsEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Define a projection that specifies the columns from the table we care about.
        String [] projection = {
                PetsEntry._ID,
                PetsEntry.COLUMN_NAME,
                PetsEntry.COLUMN_BREED
        };
        //This loader will execute the ContentProvider's query method on a background thread.
        return new CursorLoader(this, //Parent activity content
                PetsEntry.CONTENT_URI,       //Provider content URI to query
                projection,                  //Columns to include in the resulting Cursor
                null,                //No selection clause
                null,             //No selection arguments
                null);               //Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Update {@link PetCursorAdapter} with this new cursor containing updated pet data
        petCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    // Callback called when the data needs to be deleted
        petCursorAdapter.swapCursor(null);
    }
}
