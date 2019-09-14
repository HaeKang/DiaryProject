package com.example.diaryproject.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.diaryproject.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerAdapterTwo extends RecyclerView.Adapter<RecyclerAdapterTwo.ItemViewHolderTwo> {

    private ArrayList<Data> listDataTwo = new ArrayList<>();


    @NonNull
    @Override
    public RecyclerAdapterTwo.ItemViewHolderTwo onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pageone_item_list,parent,false);
        return new RecyclerAdapterTwo.ItemViewHolderTwo(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapterTwo.ItemViewHolderTwo holder, int position) {
        holder.onBind(listDataTwo.get(position));
    }

    @Override
    public int getItemCount() {
        return listDataTwo.size();
    }

    void addItem(Data data){
        listDataTwo.add(data);
    }

    class ItemViewHolderTwo extends RecyclerView.ViewHolder{
        private TextView t1;
        private TextView t2;
        private TextView t3;


        ItemViewHolderTwo(View itemView){
            super(itemView);

            t1 = itemView.findViewById(R.id.comment_id);
            t2 = itemView.findViewById(R.id.nickname_listitem);
            t3 = itemView.findViewById(R.id.date_listitem);

        }

        void onBind(Data data){
            t1.setText(data.getTitle());
            t2.setText(data.getNickname());
            t3.setText(data.getPostid());
        }

    }
}
