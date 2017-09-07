package com.softdev.weekimessenger.Layouts;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.softdev.weekimessenger.Configuration.Config;
import com.softdev.weekimessenger.Handlers.AppHandler;
import com.softdev.weekimessenger.Handlers.BlockedUsersAdapter;
import com.softdev.weekimessenger.Handlers.DatabaseHandler;
import com.softdev.weekimessenger.R;

import java.util.ArrayList;

public class BlockedUsers extends AppCompatActivity {

    BlockedUsersAdapter bAdapter;
    ArrayList<DatabaseHandler.blockedUsers> usersList;
    DatabaseHandler dbHandler;
    LinearLayoutManager layoutManager;
    Toolbar toolbar;

    RecyclerView blockedView;
    TextView txtSum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_users);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setTitle("Blocked Users");
        usersList = new ArrayList<>();
        blockedView = (RecyclerView) findViewById(R.id.blockedView);
        dbHandler = AppHandler.getInstance().getDBHandler();
        layoutManager = new LinearLayoutManager(this);
        txtSum = (TextView) findViewById(R.id.txtSum);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);

        bAdapter = new BlockedUsersAdapter(this, usersList, new BlockedUsersAdapter.ClickListener() {
            @Override
            public void onRemoveBlockButtonClicked(View view, int position) {
                dbHandler.unBlock(usersList.get(position).getUsername());
                Toast.makeText(BlockedUsers.this, "$user has been unblocked.".replace("$user", usersList.get(position).getUsername()), Toast.LENGTH_SHORT).show();
                usersList.remove(position);
                bAdapter.notifyItemRemoved(position);
                Intent intent = new Intent(Config.FRIENDS_UPDATED);
                LocalBroadcastManager.getInstance(BlockedUsers.this).sendBroadcast(intent);
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        blockedView.setLayoutManager(layoutManager);
        blockedView.addItemDecoration(new ItemsDivider(this));
        blockedView.setItemAnimator(itemAnimator);
        blockedView.setAdapter(bAdapter);

        if(!usersList.isEmpty())
            usersList.clear();

        usersList.addAll(dbHandler.RetrieveBlockedUsers());
        if (usersList.size() > 0)
        {
            blockedView.setVisibility(View.VISIBLE);
            txtSum.setVisibility(View.GONE);
            bAdapter.notifyDataSetChanged();
        }
        else
        {
            blockedView.setVisibility(View.GONE);
            txtSum.setVisibility(View.VISIBLE);
        }
    }
}
