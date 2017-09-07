package com.softdev.weekimessenger.Handlers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.softdev.weekimessenger.R;
import com.softdev.weekimessenger.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {

    ArrayList<User> friendList;
    Context mContext;

    public FriendListAdapter(Context context, ArrayList<User> list, ClickListener listener)
    {
        mContext = context;
        friendList = list;
        onClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list_layout_2, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        User user = friendList.get(position);
        holder.name.setText(user.getName());
        holder.status.setText(user.getStatus());
        if(user.getIcon().trim().isEmpty() || user.getIcon().equals("null")) { holder.icon.setImageResource(R.drawable.default_user); }
        else { Picasso.with(mContext).load(user.getIcon()).error(R.drawable.default_user).placeholder(R.drawable.default_user).into(holder.icon); }
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public ClickListener onClickListener;
    public interface ClickListener {
        void onMessageButtonClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, status;
        public CircleImageView icon;
        public Button messageBtn;

        public ViewHolder(View itemView)
        {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.listLayoutName);
            status = (TextView) itemView.findViewById(R.id.listLayoutStatus);
            icon = (CircleImageView) itemView.findViewById(R.id.listLayoutIcon);
            messageBtn = (Button) itemView.findViewById(R.id.sendMessageBtn);

            messageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onMessageButtonClick(v, getAdapterPosition());
                }
            });
        }
    }
}
