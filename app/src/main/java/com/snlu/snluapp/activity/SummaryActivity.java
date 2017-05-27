package com.snlu.snluapp.activity;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.snlu.snluapp.R;
import com.snlu.snluapp.dialog.SNLUAlertDialog;
import com.snlu.snluapp.item.DocumentItem;
import com.snlu.snluapp.item.SentenceItem;
import com.snlu.snluapp.item.WordItem;
import com.snlu.snluapp.util.SNLULog;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SummaryActivity extends AppCompatActivity {
    private DocumentItem document;
    private ArrayList<WordItem> item;
    private ArrayList<SentenceItem> sentenceItems;
    private ArrayList<SentenceItem> searchedSentence;
    private ProgressDialog loagindDialog; // 로딩화면
    ListView lv,lv2;
    EditText et;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        getSupportActionBar().setTitle("요약하기");
        document = new DocumentItem();
        document.setNumber(getIntent().getStringExtra("documentNumber"));
        lv = (ListView)findViewById(R.id.summary_word);
        lv2 = (ListView)findViewById(R.id.summary_sentence);
        et=(EditText)findViewById(R.id.summary_docu);
        loadDocumentInformation(document.getNumber());
        createThreadAndDialog(); // 로딩만들기
        requestStatistic(); //단어뽑기

        findViewById(R.id.summary_docu).setOnDragListener(new MyDragListener());
        // set
    }

    private void loadDocumentInformation(String documentNumber) {
        JSONObject json = new JSONObject();
        try {
            json.put("documentNumber", documentNumber);
            SNLUVolley.getInstance(SummaryActivity.this).post("showDocument", json, requestDocumentListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private SNLUVolley.OnResponseListener requestDocumentListener = new SNLUVolley.OnResponseListener() {
        @Override
        public void onResponse(JSONObject response) {
            try {
                SNLULog.v(response.toString());
                String result = response.getString("result");
                if (result.equals("0")) {
                    JSONArray array = response.getJSONArray("data");
                    sentenceItems = new ArrayList<>();
                    for(int i=0; i<array.length(); i++) {
                        SentenceItem item2 = new SentenceItem();
                        item2.setSentence(array.getJSONObject(i).getString("sentence"));
                        sentenceItems.add(item2);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    void createThreadAndDialog() { //로딩만들기
        loagindDialog = ProgressDialog.show(this, "단어 목록",
                "불러오는 중.....", true, false);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
            }
        });
        thread.start();
    }
    class MyDragListener implements View.OnDragListener {

        @Override
        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    et.setBackgroundColor(Color.parseColor("#5cf78282"));
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    et.requestFocus();
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DROP:

                    EditText ed = (EditText)findViewById(R.id.summary_docu);
                    ed.setText(ed.getText().toString()+" "+event.getClipData().getItemAt(0).getText().toString());
                    et.setSelection(et.length());
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    et.setBackgroundColor(Color.parseColor("#FFFFFF"));
                default:
                    break;
            }
            return true;
        }
    }

    private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            lv2.setSelector( new PaintDrawable( Color.parseColor("#FFB6C1")));
            lv.setSelector( new PaintDrawable( Color.parseColor("#FFB6C1")));
                searchedSentence= new ArrayList<>();
            for(int i = 0; i<sentenceItems.size(); i++) {
                if(sentenceItems.get(i).getSentence().contains(item.get(position).getName()))
                {
                    searchedSentence.add(sentenceItems.get(i));
                }
            }
            lv2.setAdapter(new SentenceAdapter(getApplicationContext(), searchedSentence));
        }
    }

    private void requestStatistic(){
        JSONObject json = new JSONObject();
        try{
            json.put("documentNumber",document.getNumber());
            SNLUVolley.getInstance(this).post("analyze", json, new SNLUVolley.OnResponseListener() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.v("TAG", response.toString());
                    try {
                        int result = response.getInt("result");
                        if(result==0) {
                            String str = response.getString("data");
                            JSONArray array = new JSONArray(str);
                            item = new ArrayList<>();
                            for(int i =0;i<array.length();i++) {
                                item.add(new WordItem(array.getJSONObject(i).getString("name")));
                            }
                            lv.setAdapter(new WordAdapter(getApplicationContext(), item));
                            lv.setOnItemClickListener( new ListViewItemClickListener() );

                            loagindDialog.dismiss();
                        } else {
                            Log.v("TAG", "error");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }
    class WordAdapter extends BaseAdapter {
        Context context;
        ArrayList<WordItem> data;
        public WordAdapter(Context context, ArrayList<WordItem> data) {
            this.context = context;
            this.data = data;
        }

        @Override
        public int getCount() {
            if(data == null) return 0;
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null) convertView = LayoutInflater.from(context).inflate(R.layout.item_summary_sentence, null);
            TextView text = (TextView)convertView.findViewById(R.id.item_summary_sentence);
            text.setText(data.get(position).getName());
            return convertView;
        }
    }
    class SentenceAdapter extends BaseAdapter {
        Context context;
        ArrayList<SentenceItem> data;
        public SentenceAdapter(Context context, ArrayList<SentenceItem> data) {
            this.context = context;
            this.data = data;
        }

        @Override
        public int getCount() {
            if(data == null) return 0;
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if(convertView==null) convertView = LayoutInflater.from(context).inflate(R.layout.item_summary_sentence, null);
            TextView text = (TextView)convertView.findViewById(R.id.item_summary_sentence);
            text.setText(searchedSentence.get(position).getSentence());
            convertView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        ClipData copy = ClipData.newPlainText("text", data.get(position).getSentence());
                        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                        v.startDrag(copy, shadowBuilder, v, 0);

                        return true;
                    } else {
                        return false;
                    }
                }
            });
            return convertView;
        }
    }
}
