package com.snlu.snluapp.activity;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.snlu.snluapp.R;

import java.util.ArrayList;

public class CreateRoomActivity extends AppCompatActivity implements View.OnClickListener{
    private RecyclerView recyclerView;
    private ListAdapter adapter;
    private InviteItem searchItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);
        getSupportActionBar().setTitle("회의방 생성");

        findViewById(R.id.button_create).setOnClickListener(this);
        findViewById(R.id.button_search).setOnClickListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        ArrayList<InviteItem> testItems = new ArrayList<>();
        testItems.add(new InviteItem("01020779637", "강동우"));
        testItems.add(new InviteItem("01067180803", "윤수환"));
        testItems.add(new InviteItem("01043247755", "박정원"));
        adapter = new ListAdapter(testItems);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.button).setOnClickListener(this);
        ImageView button = (ImageView)findViewById(R.id.button);
        button.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.icon_plus));
        setSearchResult(new InviteItem("01055966432", "홍태준"));
    }

    private void setSearchResult(InviteItem item) {
        searchItem = item;
        ImageView imageView = (ImageView)findViewById(R.id.image_view);
        TextView textView = (TextView)findViewById(R.id.text_view);
        ImageView button = (ImageView)findViewById(R.id.button);
        if(item == null) {
            imageView.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.INVISIBLE);
            button.setVisibility(View.INVISIBLE);
        } else {
            imageView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);
            textView.setText(item.userId + "\t\t"+ item.userName);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_create:
                break;
            case R.id.button_search:
                break;
            case R.id.button:
                // 회의자를 리사이클러 뷰에 추가하는 과정
                adapter.addItem(searchItem);
                setSearchResult(null);
                break;
        }
    }

    class ListAdapter extends RecyclerView.Adapter implements View.OnClickListener {
        private ArrayList<InviteItem> data;

        public ListAdapter(ArrayList<InviteItem> data) {
            this.data = data;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.item_invite, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ViewHolder vh = (ViewHolder)holder;
            vh.textView.setText(data.get(position).userId + "\t\t" + data.get(position).userName);
            vh.button.setTag(position);
            vh.button.setOnClickListener(this);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @Override
        public void onClick(View v) {
            int pos = (int)v.getTag();
            data.remove(pos);
            notifyItemRemoved(pos);
        }

        public void addItem(InviteItem item) {
            data.add(item);
            notifyItemInserted(data.size() - 1);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public TextView textView;
            public ImageView button;

            public ViewHolder(View view) {
                super(view);
                textView = (TextView)view.findViewById(R.id.text_view);
                button = (ImageView)view.findViewById(R.id.button);
                button.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.icon_delete));
            }
        }
    }

    class InviteItem {
        public String userId, userName;
        public InviteItem(String userId, String userName) {
            this.userId = userId;
            this.userName = userName;
        }
    }
}
