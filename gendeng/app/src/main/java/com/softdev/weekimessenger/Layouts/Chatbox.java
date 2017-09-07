package com.softdev.weekimessenger.Layouts;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconMultiAutoCompleteTextView;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;
import com.softdev.weekimessenger.*;
import com.softdev.weekimessenger.Activity.SelectDialogActivity;
import com.softdev.weekimessenger.Activity.ViewProfile;
import com.softdev.weekimessenger.Configuration.Config;
import com.softdev.weekimessenger.Handlers.AppHandler;
import com.softdev.weekimessenger.Handlers.ChatAdapter;
import com.softdev.weekimessenger.Handlers.DatabaseHandler;
import com.softdev.weekimessenger.Handlers.InboxAdapter;

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

public class Chatbox extends AppCompatActivity implements EmojiconGridFragment.OnEmojiconClickedListener, EmojiconsFragment.OnEmojiconBackspaceClickedListener {

    ImageButton btnEmoticons, btnImage;
    LinearLayout btnSend, btnCancel, chatForm;
    EmojiconMultiAutoCompleteTextView txtMessage;
    FrameLayout emojiconLayout;
    RecyclerView ChatView;
    ChatAdapter cAdapter;
    ArrayList<Message> messagesList;
    LinearLayoutManager layoutManager;
    DatabaseHandler dbHandler;
    SharedPreferences sharedPref;
    BroadcastReceiver mBroadcastReceiver;
    ProgressDialog progressDialog;
    Toolbar toolbar;
    Bitmap bitmap;
    int isGroup;
    int isMuted;
    int isBlocked;
    boolean isEmojiKeyboardShown = false, isMember;
    String group_id = "-1";
    String p_name;
    String p_username;
    String SELF, SELF_ID, bitmap_path;
    Menu menu;
    SimpleDateFormat crTime;
    Calendar c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbox);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        emojiconLayout= (FrameLayout) findViewById(R.id.emojicons);
        btnEmoticons = (ImageButton) findViewById(R.id.btnEmoticons);
        btnImage = (ImageButton) findViewById(R.id.btnImage);
        btnSend = (LinearLayout) findViewById(R.id.btnSend);
        btnCancel = (LinearLayout) findViewById(R.id.btnCancel);
        txtMessage = (EmojiconMultiAutoCompleteTextView) findViewById(R.id.txtMessage);
        chatForm = (LinearLayout) findViewById(R.id.form);
        ChatView = (RecyclerView) findViewById(R.id.viewChat);
        layoutManager = new LinearLayoutManager(getBaseContext());
        dbHandler = new DatabaseHandler(this);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SELF = AppHandler.getInstance().getDataManager().getString("username", null);
        SELF_ID = AppHandler.getInstance().getDataManager().getString("user", null);
        c = Calendar.getInstance();
        crTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        messagesList = new ArrayList<>();
        cAdapter = new ChatAdapter(this, messagesList, SELF, sharedPref.getBoolean("chat_showBubble", true), sharedPref.getBoolean("chat_showTimestamps", false), sharedPref.getString("chat_fontSize", "14"));

        ChatView.setLayoutManager(layoutManager);
        ChatView.setItemAnimator(new DefaultItemAnimator());
        ChatView.setAdapter(cAdapter);

        Intent intent = getIntent();
        isGroup = Integer.parseInt(intent.getStringExtra("isGroup"));
        if (isGroup == 1) {
            loadGroupInfo(intent.getStringExtra("group_id"));
        }
        else
        {
            loadUserInfo(intent.getStringExtra("username"));
        }
        dbHandler.MarkMessagesAsRead(group_id, ""+p_username);
        setResult(Activity.RESULT_OK, new Intent());
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.KEY_NOTIFICATIONS)) {
                    HandleNotification(intent);
                }
            }
        };

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strCreation = crTime.format(c.getTime());
                if (txtMessage.getText().toString().trim().isEmpty()) {
                    return;
                }

                if (isGroup == 0) {
                    Message msg;
                    if (bitmap != null) {
                        msg = new Message("-1", p_username, bitmap_path, strCreation);
                        msg.setMessageType(1);
                        msg.setStatus(1);
                    } else {
                        msg = new Message("-1", p_username, txtMessage.getText().toString(), strCreation);
                        msg.setMessageType(0);
                        msg.setStatus(1);
                    }
                    long id = dbHandler.AddMessage(msg);
                    if (id != -1) {
                        if (bitmap != null) {
                            messagesList.add(msg);
                            SendMessage("1", getStringImage(bitmap));
                            resetMessage();
                            cAdapter.notifyDataSetChanged();
                        } else {
                            messagesList.add(msg);
                            SendMessage("0", msg.getMessage());
                            resetMessage();
                            cAdapter.notifyDataSetChanged();
                        }
                    } else {
                        finish();
                    }
                } else {
                    Message msg;
                    if (bitmap != null) {
                        msg = new Message (group_id, SELF, bitmap_path, strCreation);
                        msg.setMessageType(1);
                        msg.setStatus(1);
                    }
                    else {
                        msg = new Message (group_id, SELF, txtMessage.getText().toString(), strCreation);
                        msg.setMessageType(0);
                        msg.setStatus(1);
                    }
                    long id = dbHandler.AddMessage(msg);
                    if (id != -1) {
                        if (bitmap != null) {
                            messagesList.add(msg);
                            SendGroupMessage("1", getStringImage(bitmap));
                            resetMessage();
                            cAdapter.notifyDataSetChanged();
                        }
                        else {
                            messagesList.add(msg);
                            SendGroupMessage("0", msg.getMessage());
                            resetMessage();
                            cAdapter.notifyDataSetChanged();
                        }
                    }
                    else {
                        finish();
                    }
                }
                dbHandler.MarkMessagesAsRead(group_id, "" + p_username);
                scrollToLast();
            }
        });

        btnEmoticons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEmojiKeyboardShown = !isEmojiKeyboardShown;
                showEmojiconFragment(isEmojiKeyboardShown);
                if (isEmojiKeyboardShown) {
                    btnEmoticons.setImageResource(R.drawable.ic_keyboard);
                    txtMessage.requestFocus();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(txtMessage.getWindowToken(), 0);
                } else {
                    btnEmoticons.setImageResource(R.drawable.emoticons_button);
                    txtMessage.requestFocus();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(txtMessage, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        txtMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEmojiconFragment(false);
                btnEmoticons.setImageResource(R.drawable.emoticons_button);
            }
        });

        txtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtMessage.getText().toString().trim().isEmpty()) {
                    btnSend.setVisibility(View.GONE);
                } else {
                    btnSend.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        txtMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (sharedPref.getBoolean("chat_enterSend", true)) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        btnSend.performClick();
                        return true;
                    }
                }
                return false;
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetMessage();
            }
        });

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });
        LoadMessages();
    }

    private void resetMessage() {
        txtMessage.setText(null);
        txtMessage.setEnabled(true);
        btnCancel.setVisibility(View.GONE);
        btnEmoticons.setVisibility(View.VISIBLE);
        btnSend.setVisibility(View.GONE);
        bitmap = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                bitmap_path = filePath.toString();
                txtMessage.setText("Press 'Send' to send this image.");
                txtMessage.setEnabled(false);
                btnCancel.setVisibility(View.VISIBLE);
                btnEmoticons.setVisibility(View.GONE);
                btnSend.setVisibility(View.VISIBLE);
            }
            catch (IOException ex) {
                Log.d("Chat", "" + ex.getMessage());
            }
        }
        else if (requestCode == 2 && resultCode == RESULT_OK) {
            ArrayList<String> arrayUsers = data.getStringArrayListExtra("selectedUsers");
            ArrayList<User> members = new ArrayList<>();
            for (String i : arrayUsers) {
                User u = dbHandler.GetUserInfo(i);
                members.add(u);
            }
            addMembers(members);
        }
        else if (requestCode == 3 && resultCode == RESULT_OK) {
            if (isGroup == 1) {
                loadGroupInfo(group_id);
            } else
            {
                loadUserInfo(p_username);
            }
            LoadMessages();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(Config.KEY_NOTIFICATIONS));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    private void HandleNotification(Intent intent) {
        int flag = intent.getIntExtra("flag", -1);

        if (flag == Config.PUSH_TYPE_GROUP)
        {
            String group_id = intent.getStringExtra("group_id");
            String msg_type = intent.getStringExtra("message_type");
            if (group_id != null && group_id.equals(group_id)) {
                LoadMessages();
                dbHandler.MarkMessagesAsRead(group_id, "");
                if (msg_type == "2") {
                    loadGroupInfo(group_id);
                }
            }
        }
        else if (flag == Config.PUSH_TYPE_USER) {
            String sender = intent.getStringExtra("sender");
            if (sender != null && sender.equals(p_username)) {
                LoadMessages();
                dbHandler.MarkMessagesAsRead("-1", p_username);
            }
        }
    }

    void SendGroupMessage(final String message_type, final String message) {
        StringRequest request = new StringRequest(Request.Method.POST, Config.GROUP_MESSAGE + group_id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {}
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("SendMessage()", "err: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("msg_type", message_type);
                params.put("message", message);
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

    void SendMessage(final String message_type, final String message) {
        StringRequest request = new StringRequest(Request.Method.POST, Config.PRIVATE_MESSAGE+p_username, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {}
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("SendMessage()", "err: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("msg_type", message_type);
                params.put("message", message);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        this.menu = menu;
        if (menu != null) {
            if (isGroup != 0) {
                menu.getItem(2).setVisible(false);
                menu.getItem(4).setVisible(true);
                menu.getItem(5).setVisible(true);
                menu.getItem(6).setVisible(true);
                if (!isMember) {
                    menu.getItem(1).setVisible(false);
                    menu.getItem(2).setVisible(false);
                    menu.getItem(4).setVisible(false);
                    menu.getItem(5).setVisible(false);
                    menu.getItem(6).setVisible(false);
                }
            }
            if (isGroup == 0) {
                menu.getItem(1).setTitle(isMuted == 1 ? "Unmute chat" : "Mute chat");
            }
            if (isBlocked == 1) {
                menu.getItem(2).setVisible(false);

            }
        }
        return true;
    }

    void loadUserInfo(final String username)
    {
        User user = dbHandler.GetUserInfo(username);
        String name = user.getName().isEmpty() ? user.getUsername() : user.getName();
        getSupportActionBar().setTitle(name);
        if(dbHandler.isBlocked(username))
        {
            btnEmoticons.setEnabled(false);
            btnSend.setEnabled(false);
            btnImage.setEnabled(false);
            txtMessage.setEnabled(false);
            txtMessage.setText(R.string.user_blocked);
            isBlocked = 1;
        }
        else { isBlocked = 0; }
        if(dbHandler.isMuted(group_id, username)) {
            isMuted = 1;
        }
        else {
            isMuted = 0;
        }

        p_name = name;
        p_username = username;

        if (!dbHandler.isUserExists(user.getUsername()) && isBlocked != 1) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("New user");
            builder.setMessage("This user is not in your friend list. Do you wanna add this person?");

            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    StringRequest request = new StringRequest(Request.Method.GET, Config.USER_INFO + username, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject obj = new JSONObject(response);
                                if (!obj.getBoolean("error")) {
                                    User newUser = new User(obj.getString("username"), obj.getString("email"),
                                            obj.getString("name"), obj.getString("icon"));
                                    newUser.setStatus(obj.getString("status"));
                                    dbHandler.AddUser(newUser);
                                    Toast.makeText(getApplicationContext(), "User has been added in your friend list.", Toast.LENGTH_SHORT).show();
                                    loadUserInfo(username);
                                }
                            } catch (JSONException ex) {
                                Log.d("Inbox", ex.getMessage());
                                Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Inbox", "err: " + error.getMessage());
                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
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
    }

    void loadGroupInfo(String group_id)
    {
        this.group_id = group_id;
        Group group = dbHandler.getGroupInfo(group_id);
        String name = group.getGroupName();
        isMember = group.getStatus();
        getSupportActionBar().setTitle(name);

        if(dbHandler.isMuted(group_id, p_username))
        { isMuted = 1; } else { isMuted = 0; }

        if (!isMember) {
            txtMessage.setText(R.string.not_member);
            btnEmoticons.setEnabled(false);
            txtMessage.setEnabled(false);
            btnImage.setEnabled(false);
        }
    }

    void LoadMessages()
    {
        messagesList.clear();
        messagesList.addAll(dbHandler.getAllMessages(group_id, p_username));
        cAdapter.notifyDataSetChanged();
        scrollToLast();
    }

    // Scrolling to bottom of the recycler view.
    void scrollToLast()
    {
        if (cAdapter.getItemCount() > 1) {
            ChatView.getLayoutManager().scrollToPosition(cAdapter.getItemCount() - 1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id)
        {
            case (R.id.action_view_profile):
            {
                Intent intent = new Intent(Chatbox.this, ViewProfile.class);
                intent.putExtra("isGroup", isGroup);
                intent.putExtra("username", p_username);
                intent.putExtra("group_id", group_id);
                startActivityForResult(intent, 3);
                break;
            }
            case (R.id.action_mute):
            {
                // Muting user using its username or group with its ID.
                if (isMuted != 1) {
                    if (isGroup != 1) {
                        dbHandler.MuteUser(p_username, "-1");
                        Toast.makeText(Chatbox.this, "User has been muted.", Toast.LENGTH_SHORT).show();
                        this.isMuted = 1;
                    } else {
                        dbHandler.MuteUser("", group_id);
                        Toast.makeText(Chatbox.this, "Group has been muted.", Toast.LENGTH_SHORT).show();
                        this.isMuted = 1;
                    }
                }
                else
                {
                    this.isMuted = 0;
                    dbHandler.UnmuteUser(p_username, group_id);
                    Toast.makeText(Chatbox.this, isGroup == 1 ? "Group has been unmuted." : "User has been unmuted.", Toast.LENGTH_SHORT).show();
                }
                menu.getItem(1).setTitle(isMuted == 1 ? "Unmute chat" : "Mute chat");
                break;
            }
            case (R.id.action_block):
            {
                // Blocking user using its username.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Block");
                builder.setMessage("Are you sure you want to block this user?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dbHandler.BlockUser(p_username);
                        Toast.makeText(getApplicationContext(), "User has been blocked.", Toast.LENGTH_LONG).show();
                        finish();
                        dialog.dismiss();
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
                break;
            }
            case (R.id.action_delete_chat):
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Delete conversation");
                builder.setMessage("Are you sure you want to delete this conversation?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dbHandler.DeleteConversation(group_id, p_username);
                        Toast.makeText(Chatbox.this, "Conversation deleted!", Toast.LENGTH_SHORT).show();
                        finish();
                        dialog.dismiss();
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
                break;
            }
            case (R.id.action_update_status): {
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);
                View mView = layoutInflaterAndroid.inflate(R.layout.input_dialog_box, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(mView);
                TextView dialogTitle = (TextView) mView.findViewById(R.id.dialogTitle);
                final EditText txtDescription = (EditText) mView.findViewById(R.id.editText);
                dialogTitle.setText("Change group description");
                txtDescription.setText(dbHandler.getGroupInfo(group_id).getGroupDescription().toString());
                builder.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateGroupDescription(txtDescription.getText().toString());
                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                break;
            }
            case (R.id.action_add_member):
            {
                Intent intent = new Intent(Chatbox.this, SelectDialogActivity.class);
                startActivityForResult(intent, 2);
                break;
            }
            case (R.id.action_leave_group):
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Leave group");
                builder.setMessage("Are you sure you want to leave this group? You can't undo this action and can only get back if an existing member adds you.");
                builder.setPositiveButton("LEAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final ProgressDialog pDialog = new ProgressDialog(Chatbox.this);
                        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        pDialog.setMessage("Leaving group...");
                        pDialog.setIndeterminate(true);
                        pDialog.setCanceledOnTouchOutside(false);
                        pDialog.setCancelable(false);
                        pDialog.show();
                        StringRequest request = new StringRequest(Request.Method.PUT, Config.GROUP_UPDATE + "group-leave", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject obj = new JSONObject(response);
                                    if (!obj.getBoolean("error")) {
                                        Message m = new Message(group_id, SELF , "You left the group", crTime.format(c.getTime()));
                                        m.setMessageType(2);
                                        m.setStatus(1);
                                        Group group = dbHandler.getGroupInfo(group_id);
                                        group.setStatus(false);
                                        dbHandler.AddMessage(m);
                                        dbHandler.AddGroup(group);
                                        cAdapter.notifyDataSetChanged();
                                        scrollToLast();
                                        dbHandler.MarkMessagesAsRead(group_id, "");
                                        finish();
                                    }
                                } catch (JSONException ex) {
                                    pDialog.dismiss();
                                    Log.e("Chatbox", "error: "+ex.getMessage());
                                    Toast.makeText(getApplicationContext(), "There was an error while leaving the group. Please try again later.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                pDialog.dismiss();
                                Log.e("Chatbox", "error: "+error.getMessage());
                                Toast.makeText(getApplicationContext(), "There was an error while leaving the group. Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("group_id", group_id);
                                params.put("username", SELF);
                                return params;
                            }

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                return AppHandler.getInstance().getAuthorization();
                            }
                        };
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
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(txtMessage, emojicon);
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(txtMessage);
    }

    private void showEmojiconFragment(boolean isVisible) {
        if (isVisible) {
            getSupportFragmentManager().beginTransaction().replace(R.id.emojicons, EmojiconsFragment.newInstance(false)).commit();
            emojiconLayout.setVisibility(View.VISIBLE);
        }
        else {
            emojiconLayout.setVisibility(View.GONE);
        }
    }

    public String getStringImage(Bitmap bmp){
        if (bmp == null)
            return "";

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, output);
        byte[] imageBytes = output.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    private void addMembers(final ArrayList<User> membersList) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Adding members...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        String membersStr = "";
        for (User u : membersList) {
            membersStr = membersStr + "'$member',".replace("$member", u.getUsername());
        }
        final String members = membersStr;
        StringRequest request = new StringRequest(Request.Method.PUT, Config.GROUP_CONFIG, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        for (User u : membersList) {
                            String strCreation = crTime.format(c.getTime());
                            Message m = new Message(group_id, SELF, "You added " + u.getName(), strCreation);
                            m.isReceived(0);
                            m.setStatus(1);
                            m.setMessageType(2);
                            dbHandler.AddMessage(m);
                        }
                        progressDialog.dismiss();
                        LoadMessages();
                    }
                } catch (JSONException ex) {
                    progressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Chatbox.this, "Unable to change group settings.", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("group_id", group_id);
                params.put("username", SELF);
                params.put("members", members);
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

    private void updateGroupDescription(final String status) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Updating group description...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        StringRequest request = new StringRequest(Request.Method.PUT, Config.GROUP_UPDATE + "group-status", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        if (obj.getString("status").equals(status)) {
                            String strCreation = crTime.format(c.getTime());
                            Message m = new Message(group_id, SELF, "You changed the description.", strCreation);
                            Group g = dbHandler.getGroupInfo(group_id);
                            g.setGroupDescription(status);
                            m.isReceived(0);
                            m.setStatus(1);
                            m.setMessageType(2);
                            dbHandler.AddGroup(g);
                            dbHandler.AddMessage(m);
                            LoadMessages();
                            progressDialog.dismiss();
                        }
                    }
                }
                catch (JSONException ex) {
                    Log.d("ChatBox", "JSONException: " + ex.getMessage());
                    progressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ChatBox", "ErrorResponse: " + error.getMessage());
                progressDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("group_id", group_id);
                params.put("username", SELF);
                params.put("groupstatus", status);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return AppHandler.getInstance().getAuthorization();
            }
        };
        AppHandler.getInstance().addToRequestQueue(request);
    }
}
