package com.snlu.snluapp.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.snlu.snluapp.R;
import com.snlu.snluapp.item.DocumentItem;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Hong Tae Joon on 2017-04-13.
 */

public class DocumentsRecyclerAdapter extends RecyclerView.Adapter {
    private Context context;
    private ArrayList<DocumentItem> documentItems;
    private OnItemClickListener onItemClickListener;

    public DocumentsRecyclerAdapter(Context context, ArrayList<DocumentItem> documentItems, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.documentItems = documentItems;
        this.onItemClickListener = onItemClickListener;
    }

    public DocumentItem getItem(int position) {
        return documentItems.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_document, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder vh = (ViewHolder)holder;
        vh.textTitle.setText(documentItems.get(position).getTitle());
        String date = documentItems.get(position).getDate();
        vh.textDate.setText(String.format("(%s년 %s월 %s일 %s시 %s분에 진행함)", date.substring(0,4), date.substring(5,7), date.substring(8,10), date.substring(11,13), date.substring(14,16)));
        vh.documentItem = documentItems.get(position);
    }

    @Override
    public int getItemCount() {
        return documentItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public DocumentItem documentItem;
        public TextView textTitle, textDate;
        public LinearLayout linearLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            textTitle = (TextView)itemView.findViewById(R.id.text_title);
            textDate = (TextView)itemView.findViewById(R.id.text_date);
            linearLayout = (LinearLayout)itemView.findViewById(R.id.linear_layout);
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(documentItems.indexOf(documentItem));
                }
            });
            linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new AlertDialog.Builder(context)
                            .setTitle("알림")
                            .setMessage("정말로 " + documentItem.getTitle() + "를(을) 삭제하시겠습니까?")
                            .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    JSONObject param = new JSONObject();
                                    try {
                                        param.put("documentNumber", documentItem.getNumber());
                                        param.put("roomNumber", documentItem.getRoomNumber());
                                        SNLUVolley.getInstance(context).post("documentDelete", param, new SNLUVolley.OnResponseListener() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                try {
                                                    int result = response.getInt("result");
                                                    if(result == 0) {
                                                        Toast.makeText(context, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                                        documentItems.remove(documentItem);
                                                        notifyDataSetChanged();
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                    return true;
                }
            });
        }
    }
}
