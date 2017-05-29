package com.snlu.snluapp.activity;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.snlu.snluapp.R;
import com.snlu.snluapp.item.DocumentItem;
import com.snlu.snluapp.item.SentenceItem;
import com.snlu.snluapp.item.SummaryContentItem;
import com.snlu.snluapp.util.SNLULog;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SummaryActivity extends AppCompatActivity implements View.OnClickListener {
    private DocumentItem document;
    private ArrayList<SentenceItem> sentenceItems;
    private ProgressDialog loagindDialog; // 로딩화면
    private LinearLayout linearContent;
    private ArrayList<SummaryContentItem> contentItems;
    private ScrollView scrollView;
    ListView lv;
    LinearLayout llo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        getSupportActionBar().setTitle("요약하기");
        document = new DocumentItem();
        document.setNumber(getIntent().getStringExtra("documentNumber"));
        lv = (ListView)findViewById(R.id.summary_sentence);
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                ClipData copy = ClipData.newPlainText("text", sentenceItems.get(position).getSentence());
                copy.addItem(new ClipData.Item(sentenceItems.get(position).getSpeakerName()));
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                v.startDrag(copy, shadowBuilder, v, 0);
                return false;
            }
        });
        loadDocumentInformation(document.getNumber());
        createThreadAndDialog(); // 로딩만들기

        // 테스트 데이터
        contentItems = new ArrayList<>();
//        {
//            SummaryContentItem contentItem = new SummaryContentItem();
//            contentItem.setName("아이디어");
//            ArrayList<SentenceItem> sentenceItems = new ArrayList<>();
//            {
//                SentenceItem sentenceItem = new SentenceItem();
//                sentenceItem.setSentence("문장입니다.");
//                sentenceItem.setSpeakerName("홍태준");
//                sentenceItems.add(sentenceItem);
//            }
//            {
//                SentenceItem sentenceItem = new SentenceItem();
//                sentenceItem.setSentence("반갑습니다.");
//                sentenceItem.setSpeakerName("강동우");
//                sentenceItems.add(sentenceItem);
//            }
//            contentItem.setSentenceItems(sentenceItems);
//            contentItems.add(contentItem);
//        }
//        {
//            SummaryContentItem contentItem = new SummaryContentItem();
//            contentItem.setName("아이디어1 반응");
//            ArrayList<SentenceItem> sentenceItems = new ArrayList<>();
//            {
//                SentenceItem sentenceItem = new SentenceItem();
//                sentenceItem.setSentence("반응입니다.");
//                sentenceItem.setSpeakerName("윤수환");
//                sentenceItems.add(sentenceItem);
//            }
//            {
//                SentenceItem sentenceItem = new SentenceItem();
//                sentenceItem.setSentence("좋습니다.");
//                sentenceItem.setSpeakerName("박정원");
//                sentenceItems.add(sentenceItem);
//            }
//            contentItem.setSentenceItems(sentenceItems);
//            contentItems.add(contentItem);
//        }
//        {
//            SummaryContentItem contentItem = new SummaryContentItem();
//            contentItem.setName("아이디어2 반응");
//            ArrayList<SentenceItem> sentenceItems = new ArrayList<>();
//            {
//                SentenceItem sentenceItem = new SentenceItem();
//                sentenceItem.setSentence("별로입니다.");
//                sentenceItem.setSpeakerName("홍태준");
//                sentenceItems.add(sentenceItem);
//            }
//            {
//                SentenceItem sentenceItem = new SentenceItem();
//                sentenceItem.setSentence("좋습니다.");
//                sentenceItem.setSpeakerName("강동우");
//                sentenceItems.add(sentenceItem);
//            }
//            contentItem.setSentenceItems(sentenceItems);
//            contentItems.add(contentItem);
//        }
        linearContent = (LinearLayout)findViewById(R.id.linear_content);
        scrollView = (ScrollView)findViewById(R.id.scroll_view);
        makeContent(linearContent, contentItems);
        findViewById(R.id.button_add_folder).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_add_folder:
                addFolder(linearContent, contentItems);
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
                break;
        }
    }

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
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    llo.setVisibility(View.GONE);
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DROP:
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    llo.setVisibility(View.VISIBLE);
                default:
                    break;
            }
            return true;
        }
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
                        item2.setSpeakerName(array.getJSONObject(i).getString("name"));
                        sentenceItems.add(item2);
                    }
                    lv.setAdapter(new SummaryActivity.SentenceAdapter(getApplicationContext(), sentenceItems));
                    loagindDialog.dismiss();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_summary, menu);
        return super.onCreateOptionsMenu(menu);
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
            text.setText(data.get(position).getSpeakerName()+":"+data.get(position).getSentence());
            convertView.setTag(data.get(position));
            return convertView;
        }
    }

    public void makeContent(ViewGroup parent, ArrayList<SummaryContentItem> contentItems) {
        for(int i=0; i<contentItems.size(); i++) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_summary_content, parent, false);
            final EditText editName = (EditText)v.findViewById(R.id.edit_name);
            editName.setText(contentItems.get(i).getName());
            LinearLayout linearLayout = (LinearLayout)v.findViewById(R.id.linear_layout);
            makeSentences(linearLayout, contentItems.get(i).getSentenceItems());
            editName.setTag(linearLayout);
            editName.setOnDragListener(onDragListener);
            parent.addView(v);
        }
    }

    public void addFolder(ViewGroup parent, ArrayList<SummaryContentItem> contentItems) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_summary_content, parent, false);
        final EditText editName = (EditText)v.findViewById(R.id.edit_name);
        editName.setText("새 폴더");
        LinearLayout linearLayout = (LinearLayout)v.findViewById(R.id.linear_layout);
        editName.setTag(linearLayout);
        editName.setOnDragListener(onDragListener);
        parent.addView(v);
        contentItems.add(new SummaryContentItem("새 폴더", new ArrayList<SentenceItem>()));
    }

    public void makeSentences(ViewGroup parent, ArrayList<SentenceItem> sentenceItems) {
        for(int i=0; i<sentenceItems.size(); i++) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_summary_content_sentence, parent, false);
            TextView textName = (TextView)v.findViewById(R.id.text_name);
            textName.setText(sentenceItems.get(i).getSpeakerName());
            TextView textSentence = (TextView)v.findViewById(R.id.text_sentence);
            textSentence.setText(sentenceItems.get(i).getSentence());
            parent.addView(v);
        }
    }

    public void addSentence(ViewGroup parent, SentenceItem sentenceItem) {
        if(sentenceItem == null)
            return;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_summary_content_sentence, parent, false);
        TextView textName = (TextView)v.findViewById(R.id.text_name);
        textName.setText(sentenceItem.getSpeakerName());
        TextView textSentence = (TextView)v.findViewById(R.id.text_sentence);
        textSentence.setText(sentenceItem.getSentence());
        parent.addView(v);
        v.requestFocus();
    }

    private View.OnDragListener onDragListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            ViewGroup parent = (ViewGroup)v.getTag();
            EditText editName = (EditText)v;
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    editName.setBackgroundColor(ContextCompat.getColor(parent.getContext(), R.color.colorRedPink));
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    editName.setBackgroundColor(ContextCompat.getColor(parent.getContext(), R.color.colorPink));
                    break;
                case DragEvent.ACTION_DROP:
                    ClipData clipData = event.getClipData();
                    SentenceItem item = new SentenceItem();
                    item.setSentence(clipData.getItemAt(0).getText().toString());
                    item.setSpeakerName(clipData.getItemAt(1).getText().toString());
                    addSentence(parent, item);
                    Log.v("TEST", parent.toString());
                    Log.v("TEST", parent.getId() + "");
                    break;

                case DragEvent.ACTION_DRAG_ENDED:
                    editName.setBackgroundColor(ContextCompat.getColor(parent.getContext(), R.color.colorPink));
                default:
                    break;
            }
            return true;
        }
    };
}
