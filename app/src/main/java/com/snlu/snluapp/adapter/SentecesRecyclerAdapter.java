package com.snlu.snluapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.snlu.snluapp.R;
import com.snlu.snluapp.item.SentenceItem;

import java.util.ArrayList;

/**
 * Created by Hong Tae Joon on 2017-04-13.
 */

public class SentecesRecyclerAdapter extends RecyclerView.Adapter {
    private Context context;
    private ArrayList<SentenceItem> sentenceItems;

    public SentecesRecyclerAdapter(Context context, ArrayList<SentenceItem> sentenceItems) {
        this.context = context;
        this.sentenceItems = sentenceItems;
    }

    public void addItem(SentenceItem item) {
        sentenceItems.add(item);
        notifyItemInserted(sentenceItems.size() - 1);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_sentence, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder vh = (ViewHolder)holder;
        SentenceItem item = sentenceItems.get(position);
        vh.textName.setText(item.getSpeakerName() + ":");
        vh.textSentence.setText(item.getSentence());
        String time = item.getSpeakTime();
        vh.textTime.setText(String.format("%s시 %s분 %s초", time.substring(11,13), time.substring(14,16), time.substring(17,19)));
    }

    @Override
    public int getItemCount() {
        return sentenceItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textName, textTime, textSentence;
        public ViewHolder(View itemView) {
            super(itemView);
            textName = (TextView)itemView.findViewById(R.id.text_name);
            textTime = (TextView)itemView.findViewById(R.id.text_time);
            textSentence = (TextView)itemView.findViewById(R.id.text_sentence);
        }
    }
}
