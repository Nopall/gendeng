package com.softdev.weekimessenger.Handlers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.softdev.weekimessenger.Message;
import com.softdev.weekimessenger.R;
import com.softdev.weekimessenger.User;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    Context mContext;
    ArrayList<Message> messagesList;
    String SELF, fontSize, today;
    Boolean showBubble, showStamp;
    public ChatAdapter(Context context, ArrayList<Message> messagesList, String self, boolean showBubble, boolean showStamp, String fontSize) {
        this.mContext = context;
        this.messagesList = messagesList;
        this.SELF = self;
        this.showBubble = showBubble;
        this.showStamp = showStamp;
        this.fontSize = fontSize;

        Calendar calendar = Calendar.getInstance();
        this.today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == 5) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatmodel_right, parent, false);
        }
        else if (viewType == 10) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatmodel_right_image, parent, false);
        }
        else if (viewType == 20) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatmodel_middle, parent, false);
        }
        else if (viewType == 15) {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatmodel_left_image, parent, false);
        }
        else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatmodel_left, parent, false);
        }

        return new ViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messagesList.get(position);
        if (msg.getMessageType() == 2) {
            return 20;
        }
        else if (msg.getMessageType() == 1 && msg.isReceived()) {
            return 15;
        }
        else if (msg.getMessageType() == 1) {
            return 10;
        }
        else if (!msg.isReceived()) {
            return 5;
        }
        else {
            return 0;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User sender = AppHandler.getInstance().getDBHandler().GetUserInfo(messagesList.get(position).getSender());
        Message msg = messagesList.get(position);

        if (msg.getMessageType() != 2) {
            if (!showBubble)
                holder.icon.setVisibility(View.GONE);

            if (showStamp) {
                holder.timestamp.setVisibility(View.VISIBLE);
            } else {
                holder.timestamp.setVisibility(View.INVISIBLE);
            }

            if (holder.sender != null) {
                if (!msg.getGroupID().equals("-1")) {
                    holder.sender.setVisibility(View.VISIBLE);
                    if (!sender.getName().equals("")) {
                        holder.sender.setText(sender.getName());
                    }
                } else {
                    holder.sender.setVisibility(View.INVISIBLE);
                }
            }

            if (msg.getMessageType() == 1) {
                Picasso.with(mContext).load(msg.getMessage()).fit().into(holder.imageView);
            } else {
                holder.message.setTextSize(Float.parseFloat(fontSize));
                holder.message.setText(messagesList.get(position).getMessage());
            }

            holder.timestamp.setText(getTimeStamp(msg.getCreation(), msg.isReceived()));
            if (msg.isReceived()) {
                if (sender.getIcon().trim().isEmpty() || sender.getIcon().equals("null")) {
                    holder.icon.setImageResource(R.drawable.default_user);
                } else {
                    Picasso.with(mContext).load(sender.getIcon()).error(R.drawable.default_user).placeholder(R.drawable.default_user).into(holder.icon);
                }
            } else {
                Picasso.with(mContext).load(AppHandler.getInstance().getDataManager().getString("icon", "null")).error(R.drawable.default_user).placeholder(R.drawable.default_user)
                        .placeholder(R.drawable.default_user).into(holder.icon);
            }
        } else {
            holder.message.setText(msg.getMessage());
        }
    }

    public String getTimeStamp(String stamp, boolean isReceived) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (isReceived) { format.setTimeZone(TimeZone.getTimeZone("UTC")); }
        String timestamp = "";

        today = today.length() < 2 ? "0" + today : today;

        try {
            Date date = format.parse(stamp);
            SimpleDateFormat todayFormat = new SimpleDateFormat("dd");
            String dateToday = todayFormat.format(date);
            format = dateToday.equals(today) ? new SimpleDateFormat("hh:mm a") : new SimpleDateFormat("dd LLL, hh:mm a");
            timestamp = format.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timestamp;
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView message, timestamp, sender;
        public CircleImageView icon;
        public ImageView status, imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            message = (TextView) itemView.findViewById(R.id.txtMessage);
            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
            icon = (CircleImageView) itemView.findViewById(R.id.icon);
            sender = (TextView) itemView.findViewById(R.id.sender);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
        }
    }
}
