package com.snlu.snluapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.snlu.snluapp.R;
import com.snlu.snluapp.item.SentenceItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Hong Tae Joon on 2017-04-13.
 */

public class SentencesAdapter extends RecyclerView.Adapter {
    private Context context;
    private ArrayList<SentenceItem> sentenceItems;
    private String userId;
    private String managerId;

    public SentencesAdapter(Context context, ArrayList<SentenceItem> sentenceItems, String userId, String managerId) {
        this.context = context;
        this.sentenceItems = sentenceItems;
        this.userId = userId;
        this.managerId = managerId;
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
        Log.i("HTJ", userId + "///" + item.getSpeakerPhoneNumber());
        if(userId.equals(item.getSpeakerPhoneNumber())) {
            vh.linear.setGravity(Gravity.END);
        } else {
            vh.linear.setGravity(Gravity.START);
        }
        vh.textName.setText(item.getSpeakerName());
        if(position > 0 && sentenceItems.get(position - 1).getSpeakerName().equals(item.getSpeakerName())) {
            vh.textName.setVisibility(View.GONE);
            vh.imageManager.setVisibility(View.GONE);
        }
        else {
            vh.textName.setVisibility(View.VISIBLE);
            if(managerId.equals(item.getSpeakerPhoneNumber())) {
                vh.imageManager.setVisibility(View.VISIBLE);
            }
        }
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
        @BindView(R.id.text_name) TextView textName;
        @BindView(R.id.text_time) TextView textTime;
        @BindView(R.id.text_sentence) TextView textSentence;
        @BindView(R.id.parent) LinearLayout linear;
        @BindView(R.id.image_manager) ImageView imageManager;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
