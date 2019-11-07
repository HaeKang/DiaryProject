package com.example.diaryproject.Plan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.diaryproject.Fragment.Data;
import com.example.diaryproject.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PlanRecyclerAdapter extends RecyclerView.Adapter<PlanRecyclerAdapter.PlanItemViewHolder> {

    private ArrayList<PlanData> listData = new ArrayList<>();

    @NonNull
    @Override
    public PlanItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.plan_delete_item,parent,false);
        return new PlanRecyclerAdapter.PlanItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanItemViewHolder holder, int position) {
        holder.onBind(listData.get(position));
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    void addItem(PlanData data){
        listData.add(data);
    }

    class PlanItemViewHolder extends RecyclerView.ViewHolder{
        private TextView t1;

        public PlanItemViewHolder(View view){
            super(view);
            this.t1 = view.findViewById(R.id.delete_content_list);

        }

        void onBind(PlanData data){
            t1.setText(data.getContent());

        }

    }

}
