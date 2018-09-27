package com.example.johnn.lodgingservicesystemstudent;

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

public class PrivateChatAdapter extends RecyclerView.Adapter<PrivateChatAdapter.ViewHolder>{
    private static Functionalities listener;

    private List<Message> ml;
    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView sentContent;
        public TextView sentTime;


        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(new MyFunction());
            v.setOnLongClickListener(new MyFunction());
            sentContent = v.findViewById(R.id.sent_content);
            sentTime = v.findViewById(R.id.sent_time);
        }

        private class MyFunction implements View.OnClickListener, View.OnLongClickListener{

            @Override
            public void onClick(View view) {
                //listener.onItemClick(getAdapterPosition());
                System.out.println(getAdapterPosition());
            }

            @Override
            public boolean onLongClick(View view) {
                System.out.println("Long Press Testing");
                return true;
            }
        }


    }

    public interface Functionalities{
        void onItemClick(int position);
    }



    public PrivateChatAdapter(List<Message> MessageList) {
        ml = MessageList;
    }

    //
    @Override
    public PrivateChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.private_message_sent, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PrivateChatAdapter.ViewHolder holder, int position) {
        Message m = ml.get(position);

        holder.sentContent.setText(m.getContent());
        holder.sentTime.setText(m.getSentTime());


    }

    @Override
    public int getItemCount() {
        return ml.size();
    }


}
