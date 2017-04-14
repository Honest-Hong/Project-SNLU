package com.snlu.snluapp.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.snlu.snluapp.R;
import com.snlu.snluapp.item.SentenceItem;

import java.util.ArrayList;

/**
 * Created by Hong Tae Joon on 2017-04-13.
 */

public class SentencesDetailAdapter extends RecyclerView.Adapter {
    private Context context;
    private ArrayList<SentenceItem> sentenceItems;
    private int editedPosition;
    private EditText editText;
    private OnEditListener editListener;
    private ArrayList<SentenceItem> searchItems;
    private String keyword;

    public SentencesDetailAdapter(Context context, OnEditListener editListener) {
        this.context = context;
        this.sentenceItems = new ArrayList<>();
        this.editListener = editListener;
        editedPosition = -1;
        editText = null;
        searchItems = null;
        keyword = "";
    }

    public void setSearchKeyword(String keyword) {
        this.keyword = keyword;
        if (keyword.equals("")) searchItems = null;
        else {
            searchItems = new ArrayList<>();
            for (int i = 0; i < sentenceItems.size(); i++) {
                SentenceItem target = sentenceItems.get(i);
                if(target.getSentence().contains(keyword)) {
                    SentenceItem item = new SentenceItem();
                    item.setSpeakerPhoneNumber(target.getSpeakerPhoneNumber());
                    item.setSpeakerName(target.getSpeakerName());
                    item.setSpeakTime(target.getSpeakTime());
                    item.setSentence(target.getSentence());
                    searchItems.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public int getEditedPosition() {
        return editedPosition;
    }

    public void setEditedPosition(int editedPosition) {
        this.editedPosition = editedPosition;
        notifyItemChanged(editedPosition);
    }

    @Nullable
    public String getEditedText() {
        if(editText != null) return editText.getText().toString();
        else return null;
    }

    public void edit() {
        sentenceItems.get(editedPosition).setSentence(editText.getText().toString());
        returnEditedPosition();
    }

    public void returnEditedPosition() {
        notifyItemChanged(editedPosition);
        this.editedPosition = -1;
        this.editText = null;
    }

    public void setSentenceItems(ArrayList<SentenceItem> sentenceItems) {
        this.sentenceItems = sentenceItems;
        notifyDataSetChanged();
    }

    public void addItem(SentenceItem item) {
        sentenceItems.add(item);
        notifyItemInserted(sentenceItems.size() - 1);
    }

    public SentenceItem getItem(int position) {
        if(searchItems == null) return sentenceItems.get(position);
        else return searchItems.get(position);
    }

    public void clear() {
        sentenceItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_sentence, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SentenceItem item;
        if(searchItems == null) item = sentenceItems.get(position);
        else item = searchItems.get(position);
        ViewHolder vh = (ViewHolder)holder;
        vh.textName.setText(item.getSpeakerName() + ":");
        if(editedPosition == position) {
            vh.textSentence.setVisibility(View.GONE);
            vh.editSentence.setVisibility(View.VISIBLE);
            vh.editSentence.setText(item.getSentence());
            vh.editSentence.requestFocus();
            editText = vh.editSentence;
        } else {
            vh.textSentence.setVisibility(View.VISIBLE);
            if(keyword.equals(""))
                vh.textSentence.setText(item.getSentence());
            else
                vh.textSentence.setText(Html.fromHtml(item.getSentence().replaceAll(keyword, "<font color='red'>" + keyword + "</font>")));
            vh.editSentence.setVisibility(View.GONE);
        }
        String time = item.getSpeakTime();
        vh.textTime.setText(String.format("%s시 %s분 %s초", time.substring(11,13), time.substring(14,16), time.substring(17,19)));
        vh.linearLayout.setTag(position);
    }

    @Override
    public int getItemCount() {
        if(searchItems == null) return sentenceItems.size();
        else return searchItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textName, textTime, textSentence;
        public EditText editSentence;
        public LinearLayout linearLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            textName = (TextView)itemView.findViewById(R.id.text_name);
            textTime = (TextView)itemView.findViewById(R.id.text_time);
            textSentence = (TextView)itemView.findViewById(R.id.text_sentence);
            editSentence = (EditText)itemView.findViewById(R.id.edit_sentence);
            linearLayout = (LinearLayout)itemView.findViewById(R.id.linear_layout);
            linearLayout.setOnLongClickListener(listener);
        }
        private View.OnLongClickListener listener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = (int)v.getTag();
                if(editedPosition != -1) returnEditedPosition();
                setEditedPosition(position);
                editListener.onEdit();
                return true;
            }
        };
    }
}
