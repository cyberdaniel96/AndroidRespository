package com.example.johnn.lodgingservicesystemstudent;


import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import domain.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {


    private List<Message> ml;
    private static ClickListener clickListener;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView userName;
        public TextView time;
        public TextView content;
        public ImageView picture;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            userName = v.findViewById(R.id.userNameTv);
            time = v.findViewById(R.id.timeTv);
            content = v.findViewById(R.id.contentTv);
            picture = v.findViewById(R.id.profilePicIv);

        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(getAdapterPosition(), view);
        }
    }

    public MessageAdapter(List<Message> MessageList) {
        ml = MessageList;
    }


    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MessageAdapter.ViewHolder holder, int position) {
        Message m = ml.get(position);
        if(m.getUserId().equals(m.getLodgingId())){
            holder.userName.setText(Html.fromHtml(m.getName() +"<font color='#000000'>(Owner)</font>"));
        }else{
            holder.userName.setText(m.getName());
        }

        holder.time.setText(m.getSentTime());
        holder.content.setText(m.getContent());
        Glide.with(holder.itemView.getContext())
                .load(m.getImage())
                .transform(new CircleTransform(holder.itemView.getContext()))
                .override(50,50)
                .into(holder.picture);
    }

    @Override
    public int getItemCount() {
        return ml.size();
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        MessageAdapter.clickListener = clickListener;
    }


    public interface ClickListener {
        void onItemClick(int position, View v);
    }
}
