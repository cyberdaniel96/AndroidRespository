package com.example.johnn.lodgingservicesystemstudent;

import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.List;
import java.util.function.Function;

import domain.Message;
import domain.PrivateChat;

public class PrivateChatAdapter extends RecyclerView.Adapter<PrivateChatAdapter.ViewHolder>{
    private String loggedID;
    private static ClickListener clickListener;
    private int position;
    private List<Message> ml;
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView sentContent;
        public TextView sentTime;


        public TextView receiveContent;
        public TextView receiveTime;
        public TextView receiverID;
        public ImageView picture;


        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            sentContent = v.findViewById(R.id.sent_content);
            sentTime = v.findViewById(R.id.sent_time);

            receiveContent = v.findViewById(R.id.text_message_body);
            receiveTime = v.findViewById(R.id.text_message_time);
            receiverID = v.findViewById(R.id.text_message_name);
            picture = v.findViewById(R.id.image_message_profile);
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(getAdapterPosition(), view);
        }
    }


    public PrivateChatAdapter(List<Message> MessageList) {
        ml = MessageList;
    }

    //
    @Override
    public PrivateChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Message m = (PrivateChat)ml.get(position);
        View v = null;
        System.out.println(loggedID.equals(((PrivateChat) m).getReceiverID()));

        if(loggedID.equals(((PrivateChat) m).getReceiverID())){
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.private_message_sent, parent, false);
        }else{

            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.private_message_receive, parent, false);

        }

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PrivateChatAdapter.ViewHolder holder, int position) {
        Message m = (PrivateChat)ml.get(position);
        this.position = position;
        if(loggedID.equals(((PrivateChat) m).getSenderID())){
            holder.sentContent.setText(m.getContent());
            holder.sentTime.setText(m.getSentTime());
        }

        if(loggedID.equals(((PrivateChat) m).getReceiverID())){

            holder.receiveContent.setText("hhiuh");
        }
    }

    public void setLoggedID(String id){
        this.loggedID = id;
    }

    @Override
    public int getItemCount() {
        return ml.size();
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        PrivateChatAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

}
