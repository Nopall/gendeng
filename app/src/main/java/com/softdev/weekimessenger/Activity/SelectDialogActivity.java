package com.softdev.weekimessenger.Activity;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.softdev.weekimessenger.Handlers.AppHandler;
import com.softdev.weekimessenger.Handlers.DatabaseHandler;
import com.softdev.weekimessenger.Handlers.SelectDialogAdapter;
import com.softdev.weekimessenger.Layouts.ItemsDivider;
import com.softdev.weekimessenger.R;
import com.softdev.weekimessenger.User;

import java.util.ArrayList;

public class SelectDialogActivity extends AppCompatActivity {

    SelectDialogAdapter dAdapter;
    ArrayList<User> friendsList;
    DatabaseHandler dbHandler;
    LinearLayoutManager linearLayoutManager;
    RecyclerView listView;
    Toolbar toolbar;
    TextView txtNF;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_dialog);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(Activity.RESULT_CANCELED, intent);
                finish();
            }
        });
        friendsList = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        dbHandler = AppHandler.getInstance().getDBHandler();
        listView = (RecyclerView) findViewById(R.id.recyclerView);
        txtNF = (TextView) findViewById(R.id.txtEmpty);
        dAdapter = new SelectDialogAdapter(getApplicationContext(), friendsList, new SelectDialogAdapter.eventListener() {
            @Override
            public void onCheckedChanged(boolean isChecked) {
                SelectDialogActivity.this.toolbar.setSubtitle("$count selected".replace("$count",""+ dAdapter.getSelectedItemsCount()));
            }
        });
        listView.setLayoutManager(linearLayoutManager);
        listView.addItemDecoration(new ItemsDivider(getApplicationContext()));
        listView.setAdapter(dAdapter);

        LoadUsers();
    }

    private void LoadUsers() {
        ArrayList friends = dbHandler.getAllUsers();
        friendsList.clear();
        for (int i = 0; i < friends.size(); i++) {
            User u = (User) friends.get(i);
            if (!dbHandler.isBlocked(u.getUsername())) {
                friendsList.add(u);
            }
        }
        dAdapter.notifyDataSetChanged();
        if (friendsList.size() != 0) {
            txtNF.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        } else {
            txtNF.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem doneItem = menu.add(Menu.NONE, 1, 0, "Done");
        doneItem.setIcon(R.drawable.ic_done);
        doneItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        doneItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent();
                intent.putExtra("selectedUsers", dAdapter.getSelectedUsers());
                Log.d("Dialog", "Returning values...");
                setResult(Activity.RESULT_OK, intent);
                finish();
                return true;
            }
        });
        return true;
    }
}
