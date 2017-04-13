package com.snlu.snluapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.snlu.snluapp.R;
import com.snlu.snluapp.item.DocumentItem;

import java.util.ArrayList;

/**
 * Created by Hong Tae Joon on 2017-04-13.
 */

public class DocumentsRecyclerAdapter extends RecyclerView.Adapter {
    private Context context;
    private ArrayList<DocumentItem> documentItems;
    private OnItemClickListener onItemClickListener;

    public DocumentsRecyclerAdapter(Context context, ArrayList<DocumentItem> documentItems, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.documentItems = documentItems;
        this.onItemClickListener = onItemClickListener;
    }

    public DocumentItem getItem(int position) {
        return documentItems.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_document, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder vh = (ViewHolder)holder;
        vh.textView.setText(documentItems.get(position).getDate());
        vh.linearLayout.setTag(position);
    }

    @Override
    public int getItemCount() {
        return documentItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public LinearLayout linearLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView)itemView.findViewById(R.id.text_date);
            linearLayout = (LinearLayout)itemView.findViewById(R.id.linear_layout);
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick((int)v.getTag());
                }
            });
        }
    }
}
