package com.example.diaryproject.Diary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.diaryproject.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ComRecyclerAdapter extends RecyclerView.Adapter<ComRecyclerAdapter.ItemViewHolderCom> {

    private ArrayList<CommentData> ComlistData = new ArrayList<>();

    @NonNull
    @Override
    public ComRecyclerAdapter.ItemViewHolderCom onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item_list,parent,false);
        return new ComRecyclerAdapter.ItemViewHolderCom(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComRecyclerAdapter.ItemViewHolderCom holder, int position) {
        holder.onBind(ComlistData.get(position));
    }

    @Override
    public int getItemCount() {
        return ComlistData.size();
    }

    void addItem(CommentData data){
        ComlistData.add(data);
    }

    void resetItem() { ComlistData.clear();}


    class ItemViewHolderCom extends RecyclerView.ViewHolder{
        private TextView t1;
        private TextView t2;


        ItemViewHolderCom(View itemView){
            super(itemView);

            t1 = itemView.findViewById(R.id.comment_itemnick);
            t2 = itemView.findViewById(R.id.comment_item);

        }

        void onBind(CommentData data){
            t1.setText(data.getNick());
            t2.setText(data.getComment());
        }

    }

}
