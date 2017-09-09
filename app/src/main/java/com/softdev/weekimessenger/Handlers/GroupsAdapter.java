package com.softdev.weekimessenger.Handlers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.softdev.weekimessenger.Group;
import com.softdev.weekimessenger.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> {

    ArrayList<Group> groupsList;
    Context mContext;

    public GroupsAdapter(Context context, ArrayList<Group> groups)
    {
        groupsList = groups;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_list_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Group group = groupsList.get(position);
        holder.group_name.setText(group.getGroupName());
        holder.group_description.setText(group.getGroupDescription());

        if (group.getMessageCounts() > 0) {
            holder.messagesCount.setText(""+group.getMessageCounts());
            holder.messagesCount.setVisibility(View.VISIBLE);
        }
        else {
            holder.messagesCount.setVisibility(View.GONE);
        }

        if(group.getGroupIcon().trim().isEmpty() || group.getGroupIcon().equals("null")) { holder.icon.setImageResource(R.drawable.avatar_group); }
        else { Picasso.with(mContext).load(group.getGroupIcon()).error(R.drawable.avatar_group).placeholder(R.drawable.avatar_group).into(holder.icon); }
    }

    @Override
    public int getItemCount() {
        return groupsList.size();
    }

    public interface ClickListener {
        void onClick(View view, int position);
        void onLongClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView group_name, group_description, messagesCount;
        public CircleImageView icon;
        public ViewHolder(View itemView) {
            super(itemView);
            group_name = (TextView) itemView.findViewById(R.id.groupName);
            group_description = (TextView) itemView.findViewById(R.id.groupDes);
            icon = (CircleImageView) itemView.findViewById(R.id.groupIcon);
            messagesCount = (TextView) itemView.findViewById(R.id.messagesCount);
        }
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private GroupsAdapter.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final GroupsAdapter.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}
