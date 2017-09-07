package com.softdev.weekimessenger.Activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.softdev.weekimessenger.Configuration.Config;
import com.softdev.weekimessenger.Handlers.AppHandler;
import com.softdev.weekimessenger.Handlers.DatabaseHandler;
import com.softdev.weekimessenger.Handlers.UserListAdapter;
import com.softdev.weekimessenger.Layouts.ItemsDivider;
import com.softdev.weekimessenger.Message;
import com.softdev.weekimessenger.R;
import com.softdev.weekimessenger.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AddFriend extends AppCompatActivity {
    UserListAdapter aAdapter;
    ArrayList<User> usersList;
    DatabaseHandler dbHandler;
    LinearLayoutManager layoutManager;
    TextView txtStatus;
    ProgressBar progressBar;
    RecyclerView listView;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        usersList = new ArrayList<>();
        layoutManager = new LinearLayoutManager(getApplicationContext());
        dbHandler = AppHandler.getInstance().getDBHandler();
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        listView = (RecyclerView) findViewById(R.id.friendView);
        aAdapter = new UserListAdapter(getApplicationContext(), usersList, new UserListAdapter.ClickListener() {
            @Override
            public void onAddButtonPress(View view, int position) {
                final int pos = position;
                final User u = usersList.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(AddFriend.this);
                builder.setTitle(u.getName());
                builder.setMessage("Do you want to add '$user' in your friend list?".replace("$user", u.getName()));

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        // Showing progress dialog to let user know about the background task.
                        ProgressDialog pDialog = new ProgressDialog(AddFriend.this);
                        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pDialog.setMessage("Adding '$user' in your friend list...".replace("$user", u.getName()));
                        pDialog.setIndeterminate(true);
                        pDialog.setCanceledOnTouchOutside(false);
                        pDialog.show();

                        dbHandler.AddUser(u);
                        pDialog.hide();
                        Toast.makeText(getApplicationContext(), "$user is now in your friend list.".replace("$user", u.getName()), Toast.LENGTH_LONG).show();
                        usersList.remove(u);
                        aAdapter.notifyItemRemoved(pos);

                        Intent intent = new Intent(Config.FRIENDS_UPDATED);
                        LocalBroadcastManager.getInstance(AddFriend.this).sendBroadcast(intent);
                        Log.d("AddFriend", "Updated!");
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        txtStatus.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        listView.setLayoutManager(layoutManager);
        listView.setAdapter(aAdapter);
    }

    void SearchFromServer(String toFind)
    {
        progressBar.setVisibility(View.VISIBLE);
        txtStatus.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        usersList.clear();
        StringRequest request = new StringRequest(Request.Method.GET, Config.USERS_DIRECTORY + toFind, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error"))
                    {
                        JSONArray users = obj.getJSONArray("users");
                        if(users.length() != 0) {
                            for(int i = 0; i < users.length(); i++)
                            {
                                JSONObject user = users.getJSONObject(i);
                                if (!dbHandler.isUserExists(user.getString("username")) && !user.getString("username").equals(AppHandler.getInstance().getDataManager().getString("username", null))) {
                                    String iconStr = user.getString("icon");
                                    User userObj = new User(user.getString("username"), user.getString("email"), user.getString("name"),
                                            iconStr);
                                    userObj.setStatus(user.getString("status"));
                                    userObj.setCreation(user.getString("created_At"));
                                    usersList.add(userObj);
                                }
                            }
                            aAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                        } else {
                            txtStatus.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            listView.setVisibility(View.GONE);
                        }
                    }
                }
                catch (JSONException ex)
                {
                    txtStatus.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                    Log.d("SearchFromServer(ex)", ex.getMessage());
                    Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                txtStatus.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                Log.d("SearchFromServer(er)", "err: "+error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        AppHandler.getInstance().addToRequestQueue(request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        final MenuItem searchMenu = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenu);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                SearchFromServer(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }
}
