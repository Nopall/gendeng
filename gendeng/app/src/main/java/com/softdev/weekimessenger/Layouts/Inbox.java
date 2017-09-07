package com.softdev.weekimessenger.Layouts;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.softdev.weekimessenger.Configuration.Config;
import com.softdev.weekimessenger.Group;
import com.softdev.weekimessenger.Handlers.AppHandler;
import com.softdev.weekimessenger.Handlers.DatabaseHandler;
import com.softdev.weekimessenger.Handlers.InboxAdapter;
import com.softdev.weekimessenger.Message;
import com.softdev.weekimessenger.R;
import com.softdev.weekimessenger.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Inbox extends Fragment {

    InboxAdapter iAdapter;
    RecyclerView inboxView;
    TextView txtEmptyInbox;
    ArrayList<com.softdev.weekimessenger.Inbox> inboxList;
    DatabaseHandler dbHandler;
    LinearLayoutManager layoutManager;
    String SELF;
    public Inbox() {}
    public static Inbox newInstance()
    {
        return new Inbox();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void Refresh() { LoadInbox(); }

    public void filter(String toSearch)
    {
        if (!toSearch.trim().isEmpty()) {
            ArrayList<com.softdev.weekimessenger.Inbox> filteredItems = Filter(dbHandler.getAllMessages(), toSearch);
            inboxList.clear();
            inboxList.addAll(filteredItems);
            inboxView.scrollToPosition(0);
            iAdapter.notifyDataSetChanged();
        }
        else {
            LoadInbox();
        }
    }

    private ArrayList<com.softdev.weekimessenger.Inbox> Filter(ArrayList<com.softdev.weekimessenger.Inbox> list, String toSearch) {
        toSearch = toSearch.toLowerCase();
        final ArrayList<com.softdev.weekimessenger.Inbox> filteredItems = new ArrayList<>();
        for (com.softdev.weekimessenger.Inbox l : list) {
            final String item = (l.getGroupID() == "-1" ? l.getName() : dbHandler.getGroupInfo(l.getGroupID()).getGroupName()).toLowerCase();
            final String itemName = dbHandler.GetUserInfo(l.getName()).getName().toLowerCase();
            final String itemMessage = l.getLastMessage().toLowerCase();
            if (item.contains(toSearch.trim()) || itemName.contains(toSearch) || itemMessage.contains(toSearch)) {
                filteredItems.add(l);
            }
        }
        return filteredItems;
    }

    private void LoadInbox()
    {
        inboxList.clear();
        inboxList.addAll(dbHandler.getAllMessages());
        iAdapter.notifyDataSetChanged();

        if(inboxList.size() > 0)
        {
            txtEmptyInbox.setVisibility(View.GONE);
            inboxView.setVisibility(View.VISIBLE);
        }
        else
        {
            txtEmptyInbox.setVisibility(View.VISIBLE);
            inboxView.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_inbox, container, false);

        inboxList = new ArrayList<>();
        iAdapter = new InboxAdapter(getContext(), inboxList);
        inboxView = (RecyclerView) v.findViewById(R.id.inboxView);
        txtEmptyInbox = (TextView) v.findViewById(R.id.txtEmptyInbox);
        dbHandler = new DatabaseHandler(getContext());
        layoutManager = new LinearLayoutManager(getContext());
        SELF = AppHandler.getInstance().getDataManager().getString("user", null);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);

        inboxView.setLayoutManager(layoutManager);
        inboxView.addItemDecoration(new ItemsDivider(getContext()));
        inboxView.setItemAnimator(itemAnimator);
        inboxView.setAdapter(iAdapter);
        inboxView.addOnItemTouchListener(new InboxAdapter.RecyclerTouchListener(getContext(), inboxView, new InboxAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                com.softdev.weekimessenger.Inbox inbox = inboxList.get(position);
                Intent intent = new Intent(getContext(), Chatbox.class);
                intent.putExtra("isGroup", inbox.getGroupID().equals("-1") ? "0" : "1");
                intent.putExtra("group_id", inbox.getGroupID());
                intent.putExtra("username", inbox.getName());
                startActivityForResult(intent, 1);
                LoadInbox();
            }

            @Override
            public void onLongClick(View view, int position) {
                final com.softdev.weekimessenger.Inbox inbox = inboxList.get(position);
                final boolean isMuted = dbHandler.isMuted(inbox.getGroupID(), inbox.getName());
                final Group group = dbHandler.getGroupInfo(inbox.getGroupID());
                PopupMenu popup = new PopupMenu(getContext(), view);
                popup.getMenuInflater().inflate(R.menu.inbox_menu, popup.getMenu());
                popup.getMenu().getItem(1).setTitle(isMuted ? "Unmute conversation" : "Mute conversation");
                if (group != null) { popup.getMenu().getItem(1).setVisible(group.getStatus()); }
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case (R.id.menu_open_conversation):
                                Intent intent = new Intent(getContext(), Chatbox.class);
                                intent.putExtra("isGroup", inbox.getGroupID().equals("-1") ? "0" : "1");
                                intent.putExtra("group_id", inbox.getGroupID());
                                intent.putExtra("username", inbox.getName());
                                startActivity(intent);
                                dbHandler.MarkMessagesAsRead(inbox.getGroupID(), inbox.getName());
                                break;
                            case (R.id.menu_mute_conversation):
                                if (!isMuted) {
                                    dbHandler.MuteUser(inbox.getName(), inbox.getGroupID());
                                    Toast.makeText(getContext(), "Conversation has been muted.", Toast.LENGTH_SHORT).show();
                                } else {
                                    dbHandler.UnmuteUser(inbox.getName(), inbox.getGroupID());
                                    Toast.makeText(getContext(), "Conversation has been unmuted.", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case (R.id.menu_delete_conversation):
                                dbHandler.DeleteConversation(inbox.getGroupID(), inbox.getName());
                                Toast.makeText(getContext(), "Conversation has been deleted.", Toast.LENGTH_SHORT).show();
                                LoadInbox();
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        }));

        // Retrieving messages.
        LoadInbox();
        getActivity().invalidateOptionsMenu();
        return v;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            LoadInbox();
        }
    }
}
