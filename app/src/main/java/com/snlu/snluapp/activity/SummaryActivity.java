package com.snlu.snluapp.activity;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.snlu.snluapp.R;
import com.snlu.snluapp.item.DocumentItem;
import com.snlu.snluapp.item.RoomItem;
import com.snlu.snluapp.item.WordItem;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SummaryActivity extends AppCompatActivity {
    private DocumentItem document;
    private ArrayList<WordItem> item;
    ListView lv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        document = new DocumentItem();
        document.setNumber(getIntent().getStringExtra("documentNumber"));
        lv = (ListView)findViewById(R.id.summary_word);
        requestStatistic();
        // set
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
                            item = new ArrayList<WordItem>();
                            for(int i =0;i<array.length();i++) {
                                item.add(new WordItem(array.getJSONObject(i).getString("name")));
                            }
                            lv.setAdapter(new WordAdapter(getApplicationContext(), item));
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
            if(convertView==null) convertView = LayoutInflater.from(context).inflate(R.layout.item_summary_word, null);
            TextView text = (TextView)convertView.findViewById(R.id.item_summary_word);
            text.setText(data.get(position).getName());
            return convertView;
        }

        public void setData(ArrayList<WordItem> data) {
            this.data = data;
            notifyDataSetChanged();
        }
    }
}
