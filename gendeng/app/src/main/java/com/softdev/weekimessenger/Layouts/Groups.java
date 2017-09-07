package com.softdev.weekimessenger.Layouts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.softdev.weekimessenger.Group;
import com.softdev.weekimessenger.Handlers.AppHandler;
import com.softdev.weekimessenger.Handlers.DatabaseHandler;
import com.softdev.weekimessenger.Handlers.GroupsAdapter;
import com.softdev.weekimessenger.R;

import java.util.ArrayList;

public class Groups extends Fragment {
    SwipeRefreshLayout swipeLayout;
    GroupsAdapter gAdapter;
    RecyclerView groupsView;
    ArrayList<Group> groupsList;
    DatabaseHandler dbHandler;
    LinearLayoutManager layoutManager;
    TextView status;
    public Groups() { }
    public static Groups newInstance() { return new Groups(); }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_groups, container, false);
        groupsList = new ArrayList<>();
        gAdapter = new GroupsAdapter(getContext(), groupsList);
        groupsView = (RecyclerView) v.findViewById(R.id.groupsView);
        dbHandler = AppHandler.getInstance().getDBHandler();
        layoutManager = new LinearLayoutManager(getContext());
        status = (TextView) v.findViewById(R.id.no_group_txtView);
        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeLayout);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LoadGroups();
            }
        });
        groupsView.setLayoutManager(layoutManager);
        groupsView.addItemDecoration(new ItemsDivider(getContext()));
        groupsView.setItemAnimator(new DefaultItemAnimator());
        groupsView.setAdapter(gAdapter);

        groupsView.addOnItemTouchListener(new GroupsAdapter.RecyclerTouchListener(getContext(), groupsView, new GroupsAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Group group = groupsList.get(position);
                Intent intent = new Intent(getContext(), Chatbox.class);
                intent.putExtra("isGroup", "1");
                Log.d("Groups", group.getGroupId());
                intent.putExtra("group_id", group.getGroupId());
                startActivityForResult(intent, 1);
                LoadGroups();
            }

            @Override
            public void onLongClick(View view, int position) {}
        }));

        swipeLayout.setRefreshing(true);
        // Loading groups.
        LoadGroups();
        return v;
    }

    public void Refresh() { swipeLayout.setRefreshing(true); LoadGroups(); }

    void LoadGroups()
    {
        groupsList.clear();
        groupsList.addAll(dbHandler.getAllGroups());
        gAdapter.notifyDataSetChanged();

        if(groupsList.size() > 0) {
            status.setVisibility(View.GONE);
            groupsView.setVisibility(View.VISIBLE);
        }
        else
        {
            status.setVisibility(View.VISIBLE);
            groupsView.setVisibility(View.VISIBLE);
        }
        swipeLayout.setRefreshing(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            LoadGroups();
        }
    }
}
