package com.softdev.weekimessenger.Handlers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.softdev.weekimessenger.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlockedUsersAdapter extends RecyclerView.Adapter<BlockedUsersAdapter.ViewHolder> {

    ArrayList<DatabaseHandler.blockedUsers> blockedUsersList;
    Context mContext;

    public BlockedUsersAdapter(Context context, ArrayList<DatabaseHandler.blockedUsers> users, ClickListener listener) {
        this.mContext = context;
        this.blockedUsersList = users;
        this.onClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.blocked_list_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DatabaseHandler.blockedUsers users = blockedUsersList.get(position);
        holder.username.setText(users.getUsername());
        holder.blockDate.setText(users.getBlockDate());
        holder.icon.setImageResource(R.drawable.default_user);
    }

    @Override
    public int getItemCount() {
        return blockedUsersList.size();
    }

    public ClickListener onClickListener;
    public interface ClickListener {
        void onRemoveBlockButtonClicked(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username, blockDate;
        public CircleImageView icon;
        public Button unblockButton;
        public ViewHolder(View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.username);
            blockDate = (TextView) itemView.findViewById(R.id.blockDate);
            icon = (CircleImageView) itemView.findViewById(R.id.userIcon);
            unblockButton = (Button) itemView.findViewById(R.id.unBlockBtn);

            unblockButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onRemoveBlockButtonClicked(v, getAdapterPosition());
                }
            });
        }
    }
}