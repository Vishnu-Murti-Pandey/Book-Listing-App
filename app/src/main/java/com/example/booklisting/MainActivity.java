package com.example.booklisting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import android.app.LoaderManager;
import android.content.Loader;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Book>> {

    /**
     * Tag for log messages
     */
    private static final String LOG_TAG = BookLoader.class.getName();

    /**
     * Constant value for the earthquake loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int BOOK_LOADER_ID = 1;

    /*
     * list view
     */
    ListView booklistView;


    /**
     * url for google books api
     */
    private String mUrlRequestGoogleBooks = "";


    //to check weather internet is connected or not
    private boolean isConnected;


    /** TextView that is displayed when the list is empty */
    private TextView emptyTextView;

    /**
     * Adapter for the list of earthquakes
     */
    private BookAdapter adapter;

    /**
     * Progress bar for ProgressView
     */
    private View circleProgressBar;

    /**
     * View for search View
     */
    private SearchView searchViewField;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Declaration and initialization ConnectivityManager for checking internet connection
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);


         //At the beginning check the connection with internet and save result to (boolean) variable isConnected
         //Checking if network is available
         //If TRUE - work with LoaderManager
         //If FALSE - hide loading spinner and show emptyStateTextView
        checkConnection(connectivityManager);

        //find the listView to set adapter
        booklistView = findViewById(R.id.list);

        //get the adapter from custom Book Adapter class
        adapter = new BookAdapter(this, new ArrayList<Book>());

        //set the adapter to listView
        booklistView.setAdapter(adapter);

        //find the reference of empty textView
        emptyTextView = findViewById(R.id.empty_view);
        booklistView.setEmptyView(emptyTextView);

        //circle progress
        circleProgressBar = findViewById(R.id.loading_spinner);

        // search Button
        Button searchButton = findViewById(R.id.search_button);

        //searchViewField
        searchViewField = findViewById(R.id.search_view_field);
        searchViewField.onActionViewExpanded();
        searchViewField.setIconified(true);
        searchViewField.setQueryHint("Enter a book title");

        if(isConnected) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(BOOK_LOADER_ID, null, this);
            Log.v(LOG_TAG, "loader created init loader");
        }
        else {

            // Progress bar mapping
            Log.v(LOG_TAG, "INTERNET connection status: " + String.valueOf(isConnected) + ". Sorry dude, no internet - no data :(");

            // Otherwise, display error
            // First, hide loading bar so error message will be visible
            circleProgressBar.setVisibility(GONE);

            // Update empty state with no connection error message
            emptyTextView.setText(R.string.no_internet_connection);
        }

        // Set an item click listener on the Search Button, which sends a request to
        // Google Books API based on value from Search View
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkConnection(connectivityManager);

                if(isConnected) {
                    //Update URL and restart loader to displaying new result of searching
                    updateQueryUrl(searchViewField.getQuery().toString());
                    Log.v(LOG_TAG, "first time url get now restart the loader");
                    restartLoader();
                }
                else {
                    //clear the adapter;
                    adapter.clear();
                    //Set EmptyTextView to VISIBLE;
                    emptyTextView.setVisibility(View.VISIBLE);
                    // ...and display message: "No internet connection."
                    emptyTextView.setText(R.string.no_internet_connection);
                }
            }
        });

        booklistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // find the current book which clicked On
                Book currentBook = adapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                assert currentBook != null;
                Uri buyBookUri = Uri.parse(currentBook.getUrlBook());

                //create a new intent to buy book
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, buyBookUri);

                //start activity to open browser to get the book
                startActivity(websiteIntent);
            }
        });

    }

    public void checkConnection(ConnectivityManager connectivityManager) {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            isConnected = true;

            Log.v(LOG_TAG, "INTERNET connection status: " + String.valueOf(isConnected) + ". It's time to play with LoaderManager :)");

        }
        else {
            isConnected = false;
        }
    }

    /**
     * Check if query contains spaces if YES replace these with PLUS sign
     *
     * @param searchValue - user data from SearchView
     */
    private String updateQueryUrl(String searchValue) {

        Log.v(LOG_TAG, "update query url function");

        if (searchValue.contains(" ")) {
            searchValue = searchValue.replace(" ", "+");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("https://www.googleapis.com/books/v1/volumes?q=").append(searchValue).append("&filter=paid-ebooks&maxResults=40");
        mUrlRequestGoogleBooks = sb.toString();
        return mUrlRequestGoogleBooks;
    }


    @Override
    public Loader<List<Book>> onCreateLoader(int id, Bundle bundle) {
        // Create a new loader for the given URL
        Log.v(LOG_TAG, "onCreateLoader works fine");
        updateQueryUrl(searchViewField.getQuery().toString());
        return new BookLoader(this, mUrlRequestGoogleBooks);
    }


    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> books) {

        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_spinner);
        loadingIndicator.setVisibility(GONE);

        // Set empty state text to display "No earthquakes found."
        emptyTextView.setText(R.string.no_books);

        // Clear the adapter of previous earthquake data
        adapter.clear();

        // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (books != null && !books.isEmpty()) {
            adapter.addAll(books);
        }
        Log.v(LOG_TAG, "onFinishLoader works fine");

    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {

        // Loader reset, so we can clear out our existing data.
        adapter.clear();
    }

    public void restartLoader() {
        emptyTextView.setVisibility(GONE);
        circleProgressBar.setVisibility(View.VISIBLE);
        getLoaderManager().restartLoader(BOOK_LOADER_ID, null, MainActivity.this);
    }


}