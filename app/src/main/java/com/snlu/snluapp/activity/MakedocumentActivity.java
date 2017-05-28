package com.snlu.snluapp.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.snlu.snluapp.R;
import com.snlu.snluapp.item.DocumentItem;
import com.snlu.snluapp.item.SentenceItem;
import com.snlu.snluapp.item.WordItem;
import com.snlu.snluapp.util.SNLULog;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Hong Tae Joon on 2017-05-28.
 */

public class MakedocumentActivity extends AppCompatActivity{

    private DocumentItem document;
    private ArrayList<WordItem> item;
    private ArrayList<SentenceItem> sentenceItems;
    ListView lv;


    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        lv = (ListView)findViewById(R.id.summary_sentence);
        document = new DocumentItem();
        document.setNumber(getIntent().getStringExtra("documentNumber"));
        getSupportActionBar().setTitle("요약하기");
        loadDocumentInformation(document.getNumber());

    }
    private void loadDocumentInformation(String documentNumber) {
        JSONObject json = new JSONObject();
        try {
            json.put("documentNumber", documentNumber);
            SNLUVolley.getInstance(MakedocumentActivity.this).post("showDocument", json, requestDocumentListener);

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
                    lv.setAdapter(new SentenceAdapter(getApplicationContext(), sentenceItems));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

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
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null) convertView = LayoutInflater.from(context).inflate(R.layout.item_summary_sentence, null);
            TextView text = (TextView)convertView.findViewById(R.id.item_summary_sentence);
            text.setText(data.get(position).getSpeakerName()+":"+data.get(position).getSentence());
            return convertView;
        }
    }
}
