package com.example.diaryproject.Fragment;

import android.content.ClipData;
import android.provider.ContactsContract;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.diaryproject.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemViewHolder> {

  private ArrayList<Data> listData = new ArrayList<>();


    @NonNull
    @Override
    public RecyclerAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pageone_item_list,parent,false);
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
        private TextView t3;

        ItemViewHolder(View itemView){
            super(itemView);

            t1 = itemView.findViewById(R.id.title_listitem);
            t2 = itemView.findViewById(R.id.nickname_listitem);
            t3 = itemView.findViewById(R.id.date_listitem);
        }

        void onBind(Data data){
            t1.setText(data.getTitle());
            t2.setText(data.getNickname());
            t3.setText(data.getDate());
        }

    }

}
