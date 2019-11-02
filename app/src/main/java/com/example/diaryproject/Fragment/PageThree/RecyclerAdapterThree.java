package com.example.diaryproject.Fragment.PageThree;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.diaryproject.Fragment.Data;
import com.example.diaryproject.Fragment.PageOne.RecyclerAdapter;
import com.example.diaryproject.Fragment.PageTwo.RecyclerAdapterTwo;
import com.example.diaryproject.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerAdapterThree extends RecyclerView.Adapter<RecyclerAdapterThree.ItemViewHolderThree> {

    private ArrayList<NoteData> listDataNote = new ArrayList<>();

    @NonNull
    @Override
    public ItemViewHolderThree onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pagethree_item_list,parent,false);
        return new ItemViewHolderThree(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolderThree holder, int position) {
        holder.onBind(listDataNote.get(position));
    }

    @Override
    public int getItemCount() {
        return listDataNote.size();
    }

    void addItem(NoteData data){
        listDataNote.add(data);
    }


    class ItemViewHolderThree extends RecyclerView.ViewHolder{
        private TextView t1;
        private TextView t2;
        private TextView t3;


        ItemViewHolderThree(View itemView){
            super(itemView);

            t1 = itemView.findViewById(R.id.send_id);
            t2 = itemView.findViewById(R.id.content_note);
            t3 = itemView.findViewById(R.id.recv_date);

        }

        void onBind(NoteData data){
            t1.setText(data.getNickname() + "님이 보냈습니다");
            t2.setText(data.getContent());
            t3.setText(data.getDate());
        }

    }
}
