package com.softdev.weekimessenger.Handlers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.softdev.weekimessenger.Group;
import com.softdev.weekimessenger.Inbox;
import com.softdev.weekimessenger.R;
import com.softdev.weekimessenger.User;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> {

    ArrayList<Inbox> inboxList;
    Context mContext;

    public InboxAdapter(Context context, ArrayList<Inbox> inbox) {
        this.mContext = context;
        this.inboxList = inbox;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.inbox_list_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Inbox inbox = inboxList.get(position);
        if (!inbox.getGroupID().equals("-1")) {
            Group group = AppHandler.getInstance().dbHandler.getGroupInfo(inbox.getGroupID());
            holder.title.setText(group.getGroupName());
            if (inbox.getType() == 1) { holder.lastMessage.setText("Image"); }
            else { holder.lastMessage.setText(inbox.getLastMessage()); }
            holder.timestamp.setText(getTimeStamp(inbox.getTimeStamp(), inbox.isReceived()));
            if(group.getGroupIcon().trim().isEmpty() || group.getGroupIcon().equals("null")) { holder.icon.setImageResource(R.drawable.default_user); }
            else { Picasso.with(mContext).load(group.getGroupIcon()).error(R.drawable.default_user).placeholder(R.drawable.default_user).into(holder.icon); }
        }
        else {
            User user = AppHandler.getInstance().dbHandler.GetUserInfo(inbox.getName());
            holder.title.setText(user.getName());
            if (inbox.getType() == 1) { holder.lastMessage.setText("Image"); }
            else { holder.lastMessage.setText(inbox.getLastMessage()); }
            holder.timestamp.setText(getTimeStamp(inbox.getTimeStamp(), inbox.isReceived()));
            if(user.getIcon().trim().isEmpty() || user.getIcon().equals("null")) { holder.icon.setImageResource(R.drawable.default_user); }
            else { Picasso.with(mContext).load(user.getIcon()).error(R.drawable.default_user).placeholder(R.drawable.default_user).into(holder.icon); }
        }

        if (inbox.getMessageCounts() > 0) {
            holder.count.setText(String.valueOf(inbox.getMessageCounts()));
            holder.count.setVisibility(View.VISIBLE);
        } else { holder.count.setVisibility(View.GONE); }
    }


    private String getTimeStamp(String time, boolean isReceived) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (isReceived) { format.setTimeZone(TimeZone.getTimeZone("UTC")); }
        String timestamp = "";
        try {
            Date date = format.parse(time);
            timestamp = DateUtils.getRelativeDateTimeString(mContext, date.getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0).toString();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return timestamp;
    }

    @Override
    public int getItemCount() {
        return inboxList.size();
    }

    public void animate(List<com.softdev.weekimessenger.Inbox> inbox) {
        applyAndAnimateRemovals(inbox);
        applyAndAnimateAdditions(inbox);
        applyAndAnimateMovedItems(inbox);
    }

    private void applyAndAnimateRemovals(List<com.softdev.weekimessenger.Inbox> inbox) {
        for (int i = inboxList.size() - 1; i >= 0; i--) {
            final com.softdev.weekimessenger.Inbox model = inboxList.get(i);
            if (!inbox.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<com.softdev.weekimessenger.Inbox> inbox) {
        for (int i = 0, count = inbox.size(); i < count; i++) {
            final com.softdev.weekimessenger.Inbox model = inbox.get(i);
            if (!inboxList.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<com.softdev.weekimessenger.Inbox> inbox) {
        for (int toPosition = inbox.size() - 1; toPosition >= 0; toPosition--) {
            final com.softdev.weekimessenger.Inbox model = inbox.get(toPosition);
            final int fromPosition = inboxList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public com.softdev.weekimessenger.Inbox removeItem(int position) {
        final Inbox model = inboxList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, com.softdev.weekimessenger.Inbox inbox) {
        inboxList.add(position, inbox);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final Inbox inbox = inboxList.remove(fromPosition);
        inboxList.add(toPosition, inbox);
        notifyItemMoved(fromPosition, toPosition);
    }

    public interface ClickListener {
        void onClick(View view, int position);
        void onLongClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title, lastMessage, timestamp, count;
        public ImageView icon;
        public CheckBox checkBox;
        public ViewHolder(View itemView)
        {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.listLayoutName);
            lastMessage = (TextView) itemView.findViewById(R.id.listLayoutmessage);
            timestamp = (TextView) itemView.findViewById(R.id.listLayoutimestamp);
            count = (TextView) itemView.findViewById(R.id.listLayoutcount);
            icon = (ImageView) itemView.findViewById(R.id.listLayouticon);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
        }
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private InboxAdapter.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final InboxAdapter.ClickListener clickListener) {
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
