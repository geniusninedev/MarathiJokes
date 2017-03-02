package com.geniusnine.android.marathijokes;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.clockbyte.admobadapter.AdmobRecyclerAdapterWrapper;
import com.google.android.gms.ads.MobileAds;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Content extends AppCompatActivity {

    private MobileServiceClient mClient;
    private MobileServiceSyncTable<MarathiJokesContent> mobileServiceSyncTable;
    private ContentAdapter contentAdapter;
    private RecyclerView recyclerViewContents;


    private ArrayList<MarathiJokesContent> items = new ArrayList<>();
    AdmobRecyclerAdapterWrapper adapterWrapper;
    Timer updateAdsTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        MobileAds.initialize(getApplicationContext(), getString(R.string.test_admob_app_id));
        fetchDataFromAzure();
        initUpdateAdsTimer();
    }

    private void fetchDataFromAzure() {
        Intent intent = getIntent();
        String cat = intent.getStringExtra("category");


        try {
            // Create the Mobile Service Client instance, using the provided

            // Mobile Service URL and key
            mClient = new MobileServiceClient("https://geniusnineapps.azurewebsites.net", this);

            // Extend timeout from default of 10s to 20s
            mClient.setAndroidHttpClientFactory(new OkHttpClientFactory() {
                @Override
                public OkHttpClient createOkHttpClient() {
                    OkHttpClient client = new OkHttpClient();
                    client.setReadTimeout(20, TimeUnit.SECONDS);
                    client.setWriteTimeout(20, TimeUnit.SECONDS);
                    return client;
                }
            });


            mobileServiceSyncTable = mClient.getSyncTable("MarathiJokesContent", MarathiJokesContent.class);
            initLocalStore().get();


            showAll(cat);


        } catch (MalformedURLException e) {

        } catch (Exception e) {

        }
    }


    private AsyncTask<Void, Void, Void> initLocalStore() throws MobileServiceLocalStoreException, ExecutionException, InterruptedException {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceSyncContext syncContext = mClient.getSyncContext();

                    if (syncContext.isInitialized())
                        return null;

                    SQLiteLocalStore localStore = new SQLiteLocalStore(mClient.getContext(), "OfflineStore", null, 1);

                    Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("category", ColumnDataType.String);
                    tableDefinition.put("content", ColumnDataType.String);

                    localStore.defineTable("MarathiJokesContent", tableDefinition);

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


    public void showAll(String cat) {
        final String categoryId = cat;
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override

            protected Void doInBackground(Void... params) {
                try {

                    sync().get();
                    Query query = QueryOperations.field("category").eq(categoryId);
                    final List<MarathiJokesContent> results = mobileServiceSyncTable.read(query).get();

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            items.clear();
                            for (MarathiJokesContent item : results) {
                                items.add(item);
                            }
                            dataBinder();
                        }
                    });
                } catch (Exception exception) {
                    //createAndShowDialog(exception, "Error");
                }
                return null;
            }
        };
        //runAsyncTask(task);
        task.execute();
    }

    private AsyncTask<Void, Void, Void> sync() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    MobileServiceSyncContext syncContext = mClient.getSyncContext();
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

    private void dataBinder() {
        recyclerViewContents = (RecyclerView) findViewById(R.id.recyclerViewContent);
        recyclerViewContents.setLayoutManager(new LinearLayoutManager(this));
        contentAdapter = new ContentAdapter(this, items);
        adapterWrapper = new AdmobRecyclerAdapterWrapper(this, getString(R.string.test_admob_app_id));
        adapterWrapper.setAdapter(contentAdapter);
        adapterWrapper.setLimitOfAds(100);
        adapterWrapper.setNoOfDataBetweenAds(1);
        adapterWrapper.setFirstAdIndex(2);
        recyclerViewContents.setAdapter(adapterWrapper);
    }

    private void initUpdateAdsTimer() {
        updateAdsTimer = new Timer();
        updateAdsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapterWrapper.requestUpdateAd();
                    }
                });
            }
        }, 60 * 1000, 60 * 1000);
    }
}
