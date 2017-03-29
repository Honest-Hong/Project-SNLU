package com.snlu.snluapp.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.snlu.snluapp.item.PhoneNumItem;

import com.snlu.snluapp.R;
import com.snlu.snluapp.util.SNLULog;
import com.snlu.snluapp.util.SNLUPermission;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.snlu.snluapp.R.drawable.icon_delete;
import static com.snlu.snluapp.R.drawable.icon_plus;

public class AddUserActivity extends AppCompatActivity implements TextWatcher {
    static final int REQUEST_READ_CONTACTS = 100;
    private TextView textAdd;
    private EditText editSearch;
    private String roomNumber;
    private ArrayList<PhoneNumItem> selectedItems;
    private ArrayList<PhoneNumItem> phoneNumItems;
    private ArrayList<PhoneNumItem> searchItems;
    private ListView listView;
    private AddUserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        getSupportActionBar().setTitle("회의자 초대");
        textAdd = (TextView)findViewById(R.id.add_user_btn);
        editSearch = (EditText)findViewById(R.id.edit_search);
        listView = (ListView)findViewById(R.id.add_user_listview);

        adapter = new AddUserAdapter(this);
        listView.setAdapter(adapter);
        phoneNumItems = new ArrayList<>();
        selectedItems = new ArrayList<>();

        textAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addUser();
            }
        });
        editSearch.addTextChangedListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhoneNumItem item = (PhoneNumItem)adapter.getItem(position);
                View convertView = adapter.getView(position, view, null);
                ImageView imageView = (ImageView)convertView.findViewById(R.id.item_number_imageView);
                if(item.isSelected()){
                    imageView.setImageResource(icon_plus);
                    item.setSelected(false);
                    selectedItems.remove(item);
                }else{
                    imageView.setImageResource(icon_delete);
                    item.setSelected(true);
                    selectedItems.add(item);
                }
                textAdd.setText(String.format("%d 명을 회의실로 초대합니다.", selectedItems.size()));
            }
        });
        if(SNLUPermission.checkPermission(this, Manifest.permission.READ_CONTACTS, REQUEST_READ_CONTACTS)) getContactList();
    }

    @Override
    public void onBackPressed() {
        if(editSearch.getText().toString().length() > 0) {
            editSearch.setText("");
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(s.toString().equals("")) {
            adapter.setData(phoneNumItems);
        } else {
            searchItems = new ArrayList<>();
            for(int i=0; i<phoneNumItems.size(); i++) {
                PhoneNumItem target = phoneNumItems.get(i);
                if(target.getName().contains(s.toString()) || target.getPhoneNum().contains(s.toString()))
                    searchItems.add(phoneNumItems.get(i));
            }
            adapter.setData(searchItems);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==REQUEST_READ_CONTACTS && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getContactList();
        }
    }

    // 주소록의 목록을 불러오기 위한 쿼리
    private Cursor getURI() {
        Uri people = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER};
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        return getContentResolver().query(people, projection, null, null, sortOrder);
    }

    // 전화번호 형식을 바꿔주는 함수
    private String formatPhoneNumber(String phoneNumber) {
        phoneNumber = phoneNumber.replaceAll("-","");
        String ret =  phoneNumber.substring(0,3);
        // 10자리 이상 (핸드폰 전화번호)
        if(phoneNumber.length() > 10) ret += "-" + phoneNumber.substring(3,7) + "-" + phoneNumber.substring(7);
        // 10자리 미만 (일반 전화번호)
        else ret += "-" + phoneNumber.substring(3,6) + "-" + phoneNumber.substring(6);
        return ret;
    }

    ProgressDialog dialog;
    public void getContactList() {
        dialog = ProgressDialog.show(this, "", "사용자를 불러오는 중입니다.", false, false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = getURI();
                int size = cursor.getCount();
                String[] name = new String[size];
                String[] phoneNum = new String[size];
                int count = 0;

                if (cursor.moveToFirst()) {
                    int indexID = cursor.getColumnIndex("_id");
                    do {
                        int id = cursor.getInt(indexID);
                        String phoneChk = cursor.getString(2);
                        if (phoneChk.equals("1")) {
                            Cursor phone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);
                            while (phone.moveToNext()) {
                                phoneNum[count] = formatPhoneNumber(phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                            }
                        }
                        name[count] = cursor.getString(1);
                        phoneNumItems.add(new PhoneNumItem(phoneNum[count], name[count], false));
                        count++;
                    } while (cursor.moveToNext() || count > size);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setData(phoneNumItems);
                        dialog.dismiss();
                    }
                });
            }
        }).start();
    }

    public class AddUserAdapter extends BaseAdapter {
        Context context;
        ArrayList<PhoneNumItem> data;

        public AddUserAdapter(Context context) {
            this.context = context;
            data = new ArrayList<>();
        }

        public void setData(ArrayList<PhoneNumItem> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
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
            if(convertView == null) convertView = LayoutInflater.from(context).inflate(R.layout.item_number, null);

            ImageView imageView = (ImageView)convertView.findViewById(R.id.item_number_imageView);
            if(data.get(position).isSelected()) {
                imageView.setImageResource(R.drawable.icon_delete);
            } else {
                imageView.setImageResource(R.drawable.icon_plus);
            }
            TextView textName = (TextView)convertView.findViewById(R.id.item_number_name);
            textName.setText(data.get(position).getName());
            TextView textPhone = (TextView)convertView.findViewById(R.id.item_number_phone);
            textPhone.setText(data.get(position).getPhoneNum());

            return convertView;
        }
    }

    private void addUser() {
        try {
            JSONObject json = new JSONObject();
            roomNumber = getIntent().getStringExtra("roomNumber");
            json.put("roomNumber",roomNumber);
            String str = "[";
            for (int i = 0; i < selectedItems.size(); i++) {
                if(i!=0) str += ",";
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("phoneNumber", selectedItems.get(i).getPhoneNum().replace("-", ""));
                str += jsonObject.toString();
            }
            str += "]";
            JSONArray jsonArray = new JSONArray(str);
            json.put("phoneNumbers", jsonArray);
            SNLULog.v(jsonArray.toString());
            SNLUVolley.getInstance(AddUserActivity.this).post("userAdd", json, new Response.Listener<JSONObject>() {
                public void onResponse(JSONObject response) {
                    finish();
                }
            });
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }
}