package com.example.diaryproject.account;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.diaryproject.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemViewHolder> {

    private ArrayList<Data> listData = new ArrayList<>();

    @NonNull
    @Override
    public RecyclerAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_list_item,parent,false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.ItemViewHolder holder, int position) {
        holder.onBind(listData.get(position));
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    void addItem(Data data){
        listData.add(data);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder{
        private TextView t1;
        private TextView t2;


        ItemViewHolder(View itemView){
            super(itemView);

            t1 = itemView.findViewById(R.id.item_context);
            t2 = itemView.findViewById(R.id.item_price);

        }

        void onBind(Data data){

            if(data.getContext().contains("지출")){
                t1.setBackgroundResource(R.color.blue);
                t2.setBackgroundResource(R.color.blue);
            } else{
                t1.setBackgroundResource(R.color.pink);
                t2.setBackgroundResource(R.color.pink);
            }
            t1.setText(data.getContext());
            t2.setText(data.getPrice());
        }

    }



}
