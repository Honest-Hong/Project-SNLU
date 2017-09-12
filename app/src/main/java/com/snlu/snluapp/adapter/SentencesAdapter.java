package com.snlu.snluapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.snlu.snluapp.R;
import com.snlu.snluapp.item.SentenceItem;

import java.util.ArrayList;

/**
 * Created by Hong Tae Joon on 2017-04-13.
 */

public class SentencesAdapter extends RecyclerView.Adapter {
    private Context context;
    private ArrayList<SentenceItem> sentenceItems;
    private String userId;

    public SentencesAdapter(Context context, ArrayList<SentenceItem> sentenceItems, String userId) {
        this.context = context;
        this.sentenceItems = sentenceItems;
        this.userId = userId;
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
        if(userId.equals(item.getSpeakerPhoneNumber())) {
            vh.linear.setGravity(Gravity.END);
        } else {
            vh.linear.setGravity(Gravity.START);
        }
        vh.textName.setText(item.getSpeakerName() + ":");
        if(position > 0 && sentenceItems.get(position - 1).getSpeakerName().equals(item.getSpeakerName()))
            vh.textName.setVisibility(View.GONE);
        else
            vh.textName.setVisibility(View.VISIBLE);
        vh.textSentence.setText(item.getSentence());
        String time = item.getSpeakTime();
        if(position > 0 && sentenceItems.get(position - 1).getSpeakTime().substring(11,16).equals(item.getSpeakTime().substring(11,16)))
            vh.textTime.setVisibility(View.GONE);
        else {
            vh.textTime.setVisibility(View.VISIBLE);
            vh.textTime.setText(String.format("%s시 %s분", time.substring(11, 13), time.substring(14, 16)));
        }
    }

    @Override
    public int getItemCount() {
        return sentenceItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textName, textTime, textSentence;
        public LinearLayout linear;
        public ViewHolder(View itemView) {
            super(itemView);
            linear = (LinearLayout) itemView;
            textName = (TextView)itemView.findViewById(R.id.text_name);
            textTime = (TextView)itemView.findViewById(R.id.text_time);
            textSentence = (TextView)itemView.findViewById(R.id.text_sentence);
        }
    }
}
