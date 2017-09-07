package com.softdev.weekimessenger.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.softdev.weekimessenger.Configuration.Config;
import com.softdev.weekimessenger.Group;
import com.softdev.weekimessenger.Handlers.AppHandler;
import com.softdev.weekimessenger.Handlers.DatabaseHandler;
import com.softdev.weekimessenger.Handlers.GroupMemberListAdapter;
import com.softdev.weekimessenger.Message;
import com.softdev.weekimessenger.R;
import com.softdev.weekimessenger.User;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ViewProfile extends AppCompatActivity {

    Toolbar toolbar;
    CollapsingToolbarLayout cBar;
    ImageView header;
    DatabaseHandler dbHandler;
    FloatingActionButton editActionButton;
    TextView txtStatus, txtEmail, txtJoined, txtUsername;
    ProgressDialog pDialog;
    ArrayList<User> members;
    RecyclerView listView;
    GroupMemberListAdapter gAdapter;
    LinearLayoutManager linearLayoutManager;
    CardView groupMembersCard, statusCard, emailCard, joinedCard, usernameCard;
    Bitmap bitmap;
    Menu menu;
    int isGroup;
    String p_username = "";
    String p_group_id;
    String SELF, SELF_ID, imagePath;
    boolean isMember;
    SimpleDateFormat crTime;
    Calendar c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        toolbar = (Toolbar) findViewById(R.id.anim_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        cBar = (CollapsingToolbarLayout) findViewById(R.id.cBar);
        header = (ImageView) findViewById(R.id.header);
        dbHandler = AppHandler.getInstance().getDBHandler();
        editActionButton = (FloatingActionButton) findViewById(R.id.editButton);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        txtJoined = (TextView) findViewById(R.id.txtJoined);
        txtUsername = (TextView) findViewById(R.id.txtUsername);
        members = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        listView = (RecyclerView) findViewById(R.id.recyclerView);
        statusCard = (CardView) findViewById(R.id.cardview);
        emailCard = (CardView) findViewById(R.id.cardview1);
        joinedCard = (CardView) findViewById(R.id.cardview2);
        groupMembersCard = (CardView) findViewById(R.id.cardview3);
        usernameCard = (CardView) findViewById(R.id.cardview4);
        c = Calendar.getInstance();
        crTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        gAdapter = new GroupMemberListAdapter(getApplicationContext(), members, new GroupMemberListAdapter.ClickListener() {
            @Override
            public void onRemoveButtonClick(View view, int position) {
                final User u = members.get(position);
                final int pos = position;
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewProfile.this);
                builder.setTitle("Kick member");
                builder.setMessage("Are you sure you want to kick $user?".replace("$user", u.getName()));
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pDialog.setMessage("Please wait...");
                        pDialog.setIndeterminate(true);
                        pDialog.setCanceledOnTouchOutside(false);
                        pDialog.setCancelable(false);
                        pDialog.show();
                        // Kick
                        StringRequest request = new StringRequest(Request.Method.PUT, Config.GROUP_UPDATE + "group-kick", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject obj = new JSONObject(response);
                                    if (!obj.getBoolean("error")) {
                                        String strCreation = crTime.format(c.getTime());
                                        members.remove(u);
                                        gAdapter.notifyItemRemoved(pos);
                                        Message m = new Message(p_group_id, SELF, "You kicked $user".replace("$user", u.getName()), strCreation);
                                        dbHandler.RemoveGroupMember(p_group_id, u.getUsername());
                                        m.isReceived(0);
                                        m.setStatus(1);
                                        m.setMessageType(2);
                                        dbHandler.AddMessage(m);
                                        setResult(Activity.RESULT_OK, new Intent());
                                        pDialog.dismiss();
                                    } else {
                                        Log.e("ViewProfile", "" + obj);
                                        pDialog.dismiss();
                                    }
                                } catch (Exception ex) {
                                    pDialog.dismiss();
                                    Log.e("ViewProfile", "error: " + ex.getMessage());
                                    Toast.makeText(getApplicationContext(), "There was an error while updating your status. Please try again later.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                pDialog.dismiss();
                                Log.e("ViewProfile", "error: " + error.getMessage());
                                Toast.makeText(getApplicationContext(), "There was an error while updating your status. Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("username", SELF);
                                params.put("group_id", p_group_id);
                                params.put("groupkick", u.getUsername());
                                return params;
                            }

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                return AppHandler.getInstance().getAuthorization();
                            }
                        };

                        int socketTimeout = 0;
                        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

                        request.setRetryPolicy(policy);
                        AppHandler.getInstance().addToRequestQueue(request);
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
        listView.setLayoutManager(linearLayoutManager);
        listView.setAdapter(gAdapter);
        pDialog = new ProgressDialog(this);
        Intent intent = getIntent();
        isGroup = intent.getIntExtra("isGroup", -1);
        SELF = AppHandler.getInstance().getDataManager().getString("username", "null");
        SELF_ID = AppHandler.getInstance().getDataManager().getString("user", "null");
        if (isGroup == 1) {
            p_group_id = intent.getStringExtra("group_id");
            editActionButton.setVisibility(View.VISIBLE);
            LoadGroupInfo(p_group_id);
            LoadGroupInformation();
        } else {
            p_username = intent.getStringExtra("username");
            editActionButton.setVisibility(View.GONE);
            LoadUserInfo(p_username);
        }
    }

    void LoadUserInfo(String username) {
        if (dbHandler.isUserExists(username) && !dbHandler.isBlocked(username)) {
            User u = dbHandler.GetUserInfo(username);
            cBar.setTitle(u.getName());
            txtEmail.setText(u.getEmail());
            txtStatus.setText(u.getStatus());
            txtJoined.setText(u.getCreation());
            if (u.getIcon().trim().isEmpty() || u.getIcon().equals("null")) {
                header.setImageResource(R.drawable.bg_profile);
            } else {
                Picasso.with(this).load(u.getIcon()).error(R.drawable.bg_profile).placeholder(R.drawable.bg_profile).into(header);
            }
            statusCard.setVisibility(View.VISIBLE);
            emailCard.setVisibility(View.VISIBLE);
            joinedCard.setVisibility(View.VISIBLE);
            LoadUserInformation(u);
        } else if (p_username.equals(SELF)) {
            txtEmail.setText(AppHandler.getInstance().getDataManager().getString("email", null));
            txtStatus.setText(AppHandler.getInstance().getDataManager().getString("status", null));
            txtJoined.setText(AppHandler.getInstance().getDataManager().getString("created_At", null));
            statusCard.setVisibility(View.VISIBLE);
            emailCard.setVisibility(View.VISIBLE);
            joinedCard.setVisibility(View.VISIBLE);
            cBar.setTitle(AppHandler.getInstance().getDataManager().getString("name", "Unknown"));
            String icon = AppHandler.getInstance().getDataManager().getString("icon", "null");
            if (icon.trim().isEmpty() || icon.equals("null")) {
                header.setImageResource(R.drawable.bg_profile);
            } else {
                Picasso.with(this).load(icon).error(R.drawable.bg_profile).placeholder(R.drawable.bg_profile).into(header);
            }
        } else {
            txtUsername.setText(p_username);
            usernameCard.setVisibility(View.VISIBLE);
            cBar.setTitle(p_username);
        }
    }

    void LoadGroupInfo(String group_id) {
        final Group group = dbHandler.getGroupInfo(group_id);
        cBar.setTitle(group.getGroupName());
        cBar.setContentDescription(group.getGroupDescription());
        isMember = group.getStatus();
        txtStatus.setText(group.getGroupDescription());
        if (!txtStatus.getText().toString().trim().isEmpty()) {
            statusCard.setVisibility(View.VISIBLE);
        }
        if (group.getGroupIcon().trim().isEmpty() || group.getGroupIcon().equals("null")) {
            header.setImageResource(R.drawable.bg_profile);
        } else {
            Picasso.with(this).load(group.getGroupIcon()).error(R.drawable.bg_profile).placeholder(R.drawable.bg_profile).into(header);
        }
        if (group.getGroupMembers().size() > 0) {
            members.clear();
            members.addAll(group.getGroupMembers());
            gAdapter.notifyDataSetChanged();
            groupMembersCard.setVisibility(View.VISIBLE);
        } else {
            groupMembersCard.setVisibility(View.GONE);
        }
        if (!dbHandler.getGroupInfo(p_group_id).getStatus()) {
            gAdapter.showRemoveButton(false);
        }
        editActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(ViewProfile.this);
                View mView = layoutInflaterAndroid.inflate(R.layout.input_dialog_box, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewProfile.this);
                builder.setView(mView);
                TextView dialogTitle = (TextView) mView.findViewById(R.id.dialogTitle);
                final EditText txtName = (EditText) mView.findViewById(R.id.editText);
                dialogTitle.setText("Change group name");
                txtName.setText(group.getGroupName());
                builder.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pDialog.setMessage("Updating group name...");
                        pDialog.setIndeterminate(true);
                        pDialog.setCanceledOnTouchOutside(false);
                        pDialog.setCancelable(false);
                        pDialog.show();
                        StringRequest request = new StringRequest(Request.Method.PUT, Config.GROUP_UPDATE + "group-name", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject obj = new JSONObject(response);
                                    if (!obj.getBoolean("error")) {
                                        if (obj.getString("name").equals(txtName.getText().toString())) {
                                            group.setGroupName(obj.getString("name"));
                                            String strCreation = crTime.format(c.getTime());
                                            Message m = new Message(group.getGroupId(), SELF, "You changed the group name.", strCreation);
                                            m.isReceived(0);
                                            m.setStatus(1);
                                            m.setMessageType(2);
                                            dbHandler.AddMessage(m);
                                            dbHandler.AddGroup(group);
                                            LoadGroupInfo(group.getGroupId());
                                            setResult(Activity.RESULT_OK, new Intent());
                                            pDialog.dismiss();
                                            Toast.makeText(getApplicationContext(), "Group name has been updated.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Unable to update group name.", Toast.LENGTH_SHORT).show();
                                            pDialog.dismiss();
                                        }
                                    }
                                } catch (JSONException ex) {
                                    pDialog.dismiss();
                                    Log.e("ViewProfile", "error: " + ex.getMessage());
                                    Toast.makeText(getApplicationContext(), "There was an error while updating group name. Please try again later.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                pDialog.dismiss();
                                Log.e("ViewProfile", "error: " + error.getMessage());
                                Toast.makeText(getApplicationContext(), "There was an error while updating group name. Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("group_id", group.getGroupId());
                                params.put("username", SELF);
                                params.put("groupname", txtName.getText().toString());
                                return params;
                            }

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                return AppHandler.getInstance().getAuthorization();
                            }
                        };
                        int socketTimeout = 0;
                        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

                        request.setRetryPolicy(policy);
                        AppHandler.getInstance().addToRequestQueue(request);
                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (p_username.equals(SELF) || isGroup == 1 && isMember) {
            getMenuInflater().inflate(R.menu.profile_menu, menu);
            this.menu = menu;
            editActionButton.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case (R.id.profile_picture_update): {
                showFileChooser();
                break;
            }
            case (R.id.action_done): {
                if (isGroup != 1) {
                    updatePicture();
                } else {
                    updateGroupPicture();
                }
                break;
            }
            case (R.id.action_cancel): {
                if (isGroup == 1) {
                    LoadGroupInfo(p_group_id);
                } else {
                    LoadUserInfo(p_username);
                }
                menu.getItem(0).setVisible(false);
                menu.getItem(1).setVisible(false);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void updatePicture() {
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage("Updating picture...");
        pDialog.setIndeterminate(true);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.setCancelable(false);
        pDialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, Config.USER_UPDATE_ICON, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        AppHandler.getInstance().getDataManager().setString("icon", obj.getString("image_path"));
                        pDialog.dismiss();
                        menu.getItem(0).setVisible(false);
                        menu.getItem(1).setVisible(false);
                        LoadUserInfo(p_username);
                        setResult(Activity.RESULT_OK, new Intent());
                    } else {
                        pDialog.dismiss();
                        menu.getItem(0).setVisible(false);
                        menu.getItem(1).setVisible(false);
                        LoadUserInfo(p_username);
                        Log.d("ViewProfile", obj.toString());
                        Toast.makeText(ViewProfile.this, "Unable to update picture.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException ex) {
                    pDialog.dismiss();
                    menu.getItem(0).setVisible(false);
                    menu.getItem(1).setVisible(false);
                    LoadUserInfo(p_username);
                    Log.e("Settings", "error: " + ex.getMessage());
                    Toast.makeText(ViewProfile.this, "There was an error while updating picture. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                menu.getItem(0).setVisible(false);
                menu.getItem(1).setVisible(false);
                LoadUserInfo(p_username);
                Log.e("Settings", "error: " + error.getMessage());
                Toast.makeText(ViewProfile.this, "There was an error while updating picture. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", SELF);
                params.put("encoded", getStringImage(bitmap));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return AppHandler.getInstance().getAuthorization();
            }
        };
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        request.setRetryPolicy(policy);
        AppHandler.getInstance().addToRequestQueue(request);
    }

    public void updateGroupPicture() {
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage("Updating picture...");
        pDialog.setIndeterminate(true);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.setCancelable(false);
        pDialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, Config.GROUP_UPDATE + "icon", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        Group group = dbHandler.getGroupInfo(p_group_id);
                        String strCreation = crTime.format(c.getTime());
                        Message m = new Message(group.getGroupId(), SELF, "You changed the group photo.", strCreation);
                        m.isReceived(0);
                        m.setStatus(1);
                        m.setMessageType(2);
                        group.setGroupIcon(obj.getString("image_path"));
                        dbHandler.AddGroup(group);
                        dbHandler.AddMessage(m);
                        menu.getItem(0).setVisible(false);
                        menu.getItem(1).setVisible(false);
                        LoadGroupInfo(p_group_id);
                        setResult(Activity.RESULT_OK, new Intent());
                        pDialog.dismiss();
                    } else {
                        pDialog.dismiss();
                        menu.getItem(0).setVisible(false);
                        menu.getItem(1).setVisible(false);
                        LoadGroupInfo(p_group_id);
                        Log.d("ViewProfile", obj.toString());
                        Toast.makeText(ViewProfile.this, "Unable to update picture.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException ex) {
                    pDialog.dismiss();
                    menu.getItem(0).setVisible(false);
                    menu.getItem(1).setVisible(false);
                    LoadGroupInfo(p_group_id);
                    Log.e("Settings", "error: " + ex.getMessage());
                    Toast.makeText(ViewProfile.this, "There was an error while updating picture. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                menu.getItem(0).setVisible(false);
                menu.getItem(1).setVisible(false);
                LoadGroupInfo(p_group_id);
                Log.e("Settings", "error: " + error.getMessage());
                Toast.makeText(ViewProfile.this, "There was an error while updating picture. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", SELF);
                params.put("group_id", p_group_id);
                params.put("encoded", getStringImage(bitmap));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return AppHandler.getInstance().getAuthorization();
            }
        };
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        request.setRetryPolicy(policy);
        AppHandler.getInstance().addToRequestQueue(request);
    }

    public String getStringImage(Bitmap bmp) {
        if (bmp == null)
            return "null";

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, output);
        byte[] imageBytes = output.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imagePath = filePath.toString();
                header.setImageBitmap(bitmap);
                menu.getItem(0).setVisible(true);
                menu.getItem(1).setVisible(true);
            } catch (IOException ex) {
                Log.d("CreateGroup", "" + ex.getMessage());
            }
        }
    }

    private void LoadGroupInformation() {
        if (!dbHandler.getGroupInfo(p_group_id).getStatus())
            return;

        StringRequest request = new StringRequest(Request.Method.GET, Config.GROUP_INFO + p_group_id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    ArrayList<User> users = new ArrayList<>();
                    JSONObject obj = new JSONObject(response);
                    JSONArray membersArray = obj.getJSONArray("members");
                    Group group = new Group(obj.getString("group_id"), obj.getString("name"), obj.getString("icon"));
                    group.setGroupDescription(obj.getString("description"));
                    for (int i = 0; i < membersArray.length(); i++) {
                        JSONObject memberObj = membersArray.getJSONObject(i);
                        if (!memberObj.getString("username").equals(SELF)) {
                            User u = new User(memberObj.getString("username"));
                            if (!dbHandler.isGroupMember(group.getGroupId(), u.getUsername())) {
                                users.add(u);
                            }
                        }
                    }
                    group.setGroupMembers(users);
                    group.setStatus(true);
                    dbHandler.AddGroup(group);
                    LoadGroupInfo(p_group_id);
                    setResult(Activity.RESULT_OK, new Intent());
                } catch (JSONException ex) {
                    Log.e("ViewProfile", "Ex: " + ex.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("View", "ErrorResponse: " + error.getMessage());
            }
        });
        AppHandler.getInstance().addToRequestQueue(request);
    }

    private void LoadUserInformation(final User user) {
        StringRequest request = new StringRequest(Request.Method.GET, Config.USER_INFO + p_username, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        User u = new User(obj.getString("username"), obj.getString("email"), obj.getString("name"), obj.getString("icon"), obj.getString("status"));
                        u.setCreation(obj.getString("created_At"));
                        if (!user.getStatus().equals(u.getStatus()) || !user.getIcon().equals(u.getIcon()) || !user.getName().equals(u.getName())) {
                            dbHandler.AddUser(u);
                            LoadUserInfo(u.getUsername());
                            setResult(Activity.RESULT_OK, new Intent());
                        }
                    }
                } catch (JSONException ex) {
                    Log.e("ViewProfile", "Ex: " + ex.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ViewProfile", "ErrorResponse: " + error.getMessage());
            }
        });
        AppHandler.getInstance().addToRequestQueue(request);
    }
}
