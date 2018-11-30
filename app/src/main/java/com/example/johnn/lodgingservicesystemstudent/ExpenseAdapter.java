package com.example.johnn.lodgingservicesystemstudent;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;



import java.util.List;

import domain.Expense;



public class ExpenseAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<Expense> list;

    TextView viewCategory, viewSubAmount;


    private OnClickListener onClickListener;

    public ExpenseAdapter(Context context, List<Expense> list) {
        this.context = context;
        this.list = list;
    }

    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            viewCategory = (TextView)itemView.findViewById(R.id.viewCategory);
            viewSubAmount = (TextView)itemView.findViewById(R.id.viewSubAmount);

        }

        void bind(Expense expense){
            viewCategory.setText(expense.getCategory());
            viewSubAmount.setText(expense.getAmount()+"");

        }

        @Override
        public void onClick(View v) {
            onClickListener.onClick(v, getAdapterPosition());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Expense expense= (Expense) list.get(position);
        ((ViewHolder) holder).bind(expense);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setOnClickListener(OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    interface OnClickListener{
        void onClick(View v, int positionm);
    }



}
