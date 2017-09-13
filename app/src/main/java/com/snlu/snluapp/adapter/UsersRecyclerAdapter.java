package com.snlu.snluapp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.snlu.snluapp.R;
import com.snlu.snluapp.item.UserItem;

import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Hong Tae Joon on 2017-04-13.
 */

public class UsersRecyclerAdapter extends RecyclerView.Adapter {
    private Context context;
    private ArrayList<UserItem> userItems;
    private OnItemClickListener onItemClickListener;
    private boolean isChief;
    private Handler imageHandler;

    public UsersRecyclerAdapter(Context context, ArrayList<UserItem> userItems, OnItemClickListener onItemClickListener, boolean isChief) {
        this.context = context;
        this.userItems = userItems;
        this.onItemClickListener = onItemClickListener;
        this.isChief = isChief;
        imageHandler = new Handler();
    }

    public UserItem getItem(int position) {
        return userItems.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ViewHolder vh = (ViewHolder)holder;
        if(!userItems.get(position).getImagePath().equals("null")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(userItems.get(position).getImagePath());
                        final Bitmap bitmap = BitmapFactory.decodeStream(url.openStream());
                        imageHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                vh.imageView.setImageBitmap(bitmap);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        imageHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                vh.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_user));
                            }
                        });
                    }
                }
            }).start();
        }
        vh.textName.setText(userItems.get(position).getName());

        if(userItems.get(position).isSelected()) {
            vh.textId.setText(String.format("(%s)(방장)", userItems.get(position).getId()));
            vh.imageManager.setVisibility(View.VISIBLE);
        }
        else {
            vh.textId.setText("(" + userItems.get(position).getId() + ")");
            vh.imageManager.setVisibility(View.GONE);
            vh.linearLayout.setOnClickListener(onUserClickListener);
            vh.linearLayout.setTag(position);
        }
    }

    @Override
    public int getItemCount() {
        return userItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textName, textId;
        public ImageView imageView, imageManager;
        public LinearLayout linearLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            textName = (TextView)itemView.findViewById(R.id.text_name);
            textId = (TextView)itemView.findViewById(R.id.text_id);
            imageView = (ImageView)itemView.findViewById(R.id.image_view);
            imageManager = (ImageView)itemView.findViewById(R.id.button_manager);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.linear_layout);
        }
    }

    private View.OnClickListener onUserClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isChief) {
                onItemClickListener.onItemClick((int) v.getTag());
            }
        }
    };
}
