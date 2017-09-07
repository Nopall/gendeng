package com.softdev.weekimessenger.Activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.softdev.weekimessenger.Configuration.Config;
import com.softdev.weekimessenger.Group;
import com.softdev.weekimessenger.Handlers.AppHandler;
import com.softdev.weekimessenger.Handlers.DatabaseHandler;
import com.softdev.weekimessenger.Handlers.GroupMemberListAdapter;
import com.softdev.weekimessenger.Layouts.Chatbox;
import com.softdev.weekimessenger.Layouts.ItemsDivider;
import com.softdev.weekimessenger.Message;
import com.softdev.weekimessenger.R;
import com.softdev.weekimessenger.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateGroup extends AppCompatActivity {
    Toolbar toolbar;
    ProgressDialog progressDialog;
    DatabaseHandler dbHandler;
    CircleImageView groupImageView;
    EditText txtName, txtDescription, txtMembersUsername;
    Bitmap bitmap;
    String imagePath;
    ImageButton addBtn;
    ArrayList<User> members;
    RecyclerView listView;
    GroupMemberListAdapter gAdapter;
    LinearLayoutManager linearLayoutManager;
    MenuItem createItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setTitle("Create a new group");
        dbHandler = AppHandler.getInstance().getDBHandler();
        groupImageView = (CircleImageView) findViewById(R.id.icon);
        txtName = (EditText) findViewById(R.id.group_name);
        txtDescription = (EditText) findViewById(R.id.group_description);
        txtMembersUsername = (EditText) findViewById(R.id.editTxtUsername);
        addBtn = (ImageButton) findViewById(R.id.addBtn);
        groupImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        members = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        listView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        gAdapter = new GroupMemberListAdapter(getApplicationContext(), members, new GroupMemberListAdapter.ClickListener() {
            @Override
            public void onRemoveButtonClick(View view, int position) {
                final User u = members.get(position);
                members.remove(u);
                gAdapter.notifyItemRemoved(position);
                createItem.setEnabled(members.size() > 0);
            }
        });
        listView.setLayoutManager(linearLayoutManager);
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        listView.setItemAnimator(itemAnimator);
        listView.setAdapter(gAdapter);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!txtMembersUsername.getText().toString().trim().isEmpty()) {
                    if (dbHandler.isUserExists(txtMembersUsername.getText().toString().trim())) {
                        final User u = dbHandler.GetUserInfo(txtMembersUsername.getText().toString().trim());
                        if (!members.contains(u)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(CreateGroup.this);
                            builder.setTitle("Adding a new member");
                            builder.setMessage("Do you wanna add '$username' as a member of this group?".replace("$username", u.getName()));
                            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    members.add(u);
                                }
                            });
                            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    txtMembersUsername.setText("");
                                    dialog.dismiss();
                                }
                            });

                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    }
                }
                else {
                    Intent intent = new Intent(CreateGroup.this, SelectDialogActivity.class);
                    startActivityForResult(intent, 2);
                }
                createItem.setEnabled(members.size() > 0);
            }
        });

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.group_menu, menu);
//        return true;
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        createItem = menu.add(Menu.NONE, 1, 0, "Done");
        createItem.setIcon(R.drawable.ic_done);
        createItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        createItem.setEnabled(true);
        createItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                progressDialog = new ProgressDialog(CreateGroup.this);
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setMessage("Creating group...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();
                Toast.makeText(CreateGroup.this, "Sopo"+AppHandler.getInstance().getDataManager().getString("user", null), Toast.LENGTH_SHORT).show();
                createGroup(txtName.getText().toString(), txtDescription.getText().toString(), AppHandler.getInstance().getDataManager().getString("user", null), members);
                return false;
            }
        });
        return true;
    }

    private void createGroup(final String group_name, final String group_description, final String group_creator, final ArrayList<User> membersList)
    {
        String membersStr = "";
        for (User u : membersList) {
            membersStr = membersStr + "'$member',".replace("$member", u.getUsername());
        }
        final String members = membersStr;
        StringRequest request = new StringRequest(Request.Method.POST, Config.GROUP_CREATE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    progressDialog.setTitle("Finalizing settings...");
                    JSONObject obj = new JSONObject(response);
                    if(!obj.getBoolean("error")) {
                        Group group = new Group(obj.getString("CG"), group_name, obj.getString("icon"));
                        group.setGroupDescription(group_description);
                        group.setGroupMembers(membersList);
                        group.setStatus(true);
                        dbHandler.AddGroup(group);
                        Log.d("CreateGroup", "ismember " + group.getStatus());
                        progressDialog.dismiss();
                        Intent groupIntent = new Intent(CreateGroup.this, Chatbox.class);
                        groupIntent.putExtra("isGroup", "1");
                        groupIntent.putExtra("group_id", group.getGroupId());
                        startActivity(groupIntent);
                        finish();
                    }
                }
                catch (JSONException ex)
                {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                progressDialog.dismiss();
                Log.e("CreateGroup", error.toString());
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("group_name", group_name);
                params.put("group_icon", imagePath != null ? getStringImage(bitmap) : "null");
                params.put("group_description", group_description);
                params.put("group_members", members);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return AppHandler.getInstance().getAuthorization();
            }
        };
        AppHandler.getInstance().addToRequestQueue(request);
    }

    public String getStringImage(Bitmap bmp){
        if (bmp == null)
            return "null";

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
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
                groupImageView.setImageBitmap(bitmap);
            }
            catch (IOException ex) {
               Log.d("CreateGroup", "" + ex.getMessage());
            }
        }
        else if (requestCode == 2 && resultCode == RESULT_OK) {
            ArrayList<String> arrayUsers = data.getStringArrayListExtra("selectedUsers");
            for (String i : arrayUsers) {
                User u = dbHandler.GetUserInfo(i);
                if(!members.contains(u)) {
                    members.add(u);
                    gAdapter.notifyDataSetChanged();
                }
            }

            createItem.setEnabled(members.size() > 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return true;
    }
}
