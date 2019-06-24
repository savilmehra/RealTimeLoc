package com.easy_locater.realtimeloc;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class UsersOfAppActivity extends AppCompatActivity implements ContactsAdapter.ContactsAdapterListener {
    private Button btnGenerateOTP, btnSignIn;
    private EditText etPhoneNumber, etOTP, countryCode;
    private String phoneNumber, otp, phoneComplete;
    private FirebaseAuth auth;
    private HashMap<String, String> contactHas;
    private Map<String, Object> databaseHas;
    private List<String> fromDatabase;
    private List<String> contactsList;
    private List<String> userWIthAppInstalled;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    private String verificationCode;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private ContactsAdapter mAdapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_users_activity);
        initToolbar();
        findViews();
        final ArrayList Userlist = new ArrayList<String>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {

            contactsList = new ArrayList<String>();

            contactsList = new TinyDB(this).getListString("contactlist");
            contactHas = new TinyDB(this).getHashMap("hashmap");

            if (contactsList != null && contactHas != null
                    && (contactsList.size() > 0) && !contactHas.isEmpty()) {
                recyclerView = findViewById(R.id.recycler_view);

                mAdapter = new ContactsAdapter(UsersOfAppActivity.this, contactsList, UsersOfAppActivity.this, contactHas);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(mAdapter);

            } else {
                progressBar.setVisibility(View.VISIBLE);
                FetchUrl FetchUrl = new FetchUrl();
                FetchUrl.execute();
            }
        }


    }


    private void findViews() {
        progressBar = (ProgressBar) findViewById(R.id.circularProgressBar);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                contactsList = new ArrayList<String>();

                contactsList = new TinyDB(this).getListString("contactlist");
                contactHas = new TinyDB(this).getHashMap("hashmap");

                if (contactsList != null && contactHas != null
                        && (contactsList.size() > 0) && !contactHas.isEmpty()) {
                    recyclerView = findViewById(R.id.recycler_view);

                    mAdapter = new ContactsAdapter(UsersOfAppActivity.this, contactsList, UsersOfAppActivity.this, contactHas);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    recyclerView.setLayoutManager(mLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(mAdapter);

                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    FetchUrl FetchUrl = new FetchUrl();
                    FetchUrl.execute();
                }
            } else {
                Toast.makeText(this, "Until you dont grant the permission, we cannot display Contacts", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private List<String> getContactList() {

        final List<String> hm = new ArrayList<String>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));


                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        if (phoneNo.length() > 10) {
                            phoneNo = phoneNo.substring(phoneNo.length() - 10);
                        }
                        hm.add(phoneNo);

                    }
                    pCur.close();
                }
            }
        }
        if (cur != null) {
            cur.close();
        }
        return hm;
    }


    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, List<String>> {

        @Override
        protected List<String> doInBackground(String... pp) {

            List<String> gg = new ArrayList<String>();
            try {

                gg = getContactList();
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return gg;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);

            if (result != null) {

                contactsList = result;

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
                ref.addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                // Result will be holded Here
                                Map<String, String> td = (HashMap<String, String>) dataSnapshot.getValue();
                                if (td != null) {
                                    fromDatabase = new ArrayList<String>(td.keySet());
                                    HashMap<String, String> tt = new HashMap<String, String>();
                                    for (Map.Entry<String, String> entry : td.entrySet()) {
                                        String key = entry.getKey();
                                        String UserId = (String) entry.getValue();
                                        tt.put(key, UserId);
                                    }

                                    if (contactsList != null && fromDatabase != null) {
                                        fromDatabase.retainAll(contactsList);
                                        recyclerView = findViewById(R.id.recycler_view);
                                        mAdapter = new ContactsAdapter(UsersOfAppActivity.this, fromDatabase, UsersOfAppActivity.this, tt);
                                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                                        recyclerView.setLayoutManager(mLayoutManager);
                                        recyclerView.setItemAnimator(new DefaultItemAnimator());
                                        recyclerView.setAdapter(mAdapter);
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //handle databaseError
                            }
                        });
            }


        }
    }

    @Override
    public void onContactSelected(String contact) {
        Toast.makeText(getApplicationContext(), "Selected:", Toast.LENGTH_LONG).show();
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
    private void whiteNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    /* @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.menu_main, menu);

         // Associate searchable configuration with the SearchView
         SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
         searchView = (SearchView) menu.findItem(R.id.action_search)
                 .getActionView();
         searchView.setSearchableInfo(searchManager
                 .getSearchableInfo(getComponentName()));
         searchView.setMaxWidth(Integer.MAX_VALUE);

         // listening to search query text change
         searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
             @Override
             public boolean onQueryTextSubmit(String query) {
                 // filter recycler view when query submitted
                 mAdapter.getFilter().filter(query);
                 return false;
             }

             @Override
             public boolean onQueryTextChange(String query) {
                 // filter recycler view when text is changed
                 mAdapter.getFilter().filter(query);
                 return false;
             }
         });
         return true;
     }*/
    private void initToolbar() {
        android.support.v7.widget.Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    public List<String> getNumber(ContentResolver cr) {
        try {
            Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                contactsList.add(phoneNumber);
            }
            phones.close();// close cursor
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contactsList;
    }
}