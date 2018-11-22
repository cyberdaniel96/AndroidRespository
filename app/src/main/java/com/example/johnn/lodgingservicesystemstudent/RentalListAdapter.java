package com.example.johnn.lodgingservicesystemstudent;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import domain.Lodging;
import domain.Rental;


public class RentalListAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<Rental> list;

    TextView txtIssueDate, txtDueDate, txtStatus, txtAmount;

    private OnClickListener onClickListener;

    public RentalListAdapter(Context context, List<Rental> list) {
        this.context = context;
        this.list = list;
    }

    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            txtIssueDate = (TextView)itemView.findViewById(R.id.txtIssueDate);
            txtDueDate = (TextView)itemView.findViewById(R.id.txtDueDate);
            txtStatus = (TextView)itemView.findViewById(R.id.txtStatus);
            txtAmount = (TextView)itemView.findViewById(R.id.txtAmount);
        }

        void bind(Rental r){
            txtIssueDate.setText(r.getIssueDate());
            txtDueDate.setText(r.getDueDate());
            txtStatus.setText(r.getStatus());
            txtAmount.setText(r.getTotalAmount()+"");
        }

        @Override
        public void onClick(View v) {
            onClickListener.onClick(v, getAdapterPosition());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_rental_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Rental rental= (Rental) list.get(position);
        ((ViewHolder) holder).bind(rental);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setOnClickListener(OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    interface OnClickListener{
        void onClick(View v, int position);
    }
}
