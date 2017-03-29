package com.snlu.snluapp.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

/**
 * Created by Hong on 2016-11-19.
 */

public class SNLUVolley {
    private static SNLUVolley instance;
    private static String BASE_URL = "http://52.78.92.129:8000/";
    private RequestQueue requestQueue;
    private LruCache<String, Bitmap> cache;
    private ImageLoader imageLoader;
    private Context context;

    public SNLUVolley(Context context) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
        cache = new LruCache<>(20);
        imageLoader = new ImageLoader(requestQueue, new ImageLoader.ImageCache() {
            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });
    }

    public static SNLUVolley getInstance(Context context) {
        if(instance == null) instance = new SNLUVolley(context);
        return instance;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public void post(final String url, JSONObject json, Response.Listener<JSONObject> listener) {
        SNLULog.v("SNLUVolley post : { url: " + url + " , json: " + json.toString() + " }");
        requestQueue.add(new JsonObjectRequest(Request.Method.POST, BASE_URL + url, json, listener, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("SNLU_LOG", "SNLUVolley post error - " + url + " , detail : " + error);
                Toast.makeText(context, "서버로 부터 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    public void post(final String url, JSONObject json, final OnResponseListener onResponseListener) {
        SNLULog.v("SNLUVolley post : { url: " + url + " , json: " + json.toString() + " }");
        requestQueue.add(new JsonObjectRequest(Request.Method.POST, BASE_URL + url, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                SNLULog.v("SNLUVolley post response : { url: " + url + ", response :" + response.toString() + " }");
                onResponseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("SNLU_LOG", "SNLUVolley post error - " + url + " , detail : " + error);
                Toast.makeText(context, "서버로 부터 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    public void get(final String url, JSONObject json, Response.Listener<JSONObject> listener) {
        SNLULog.v("SNLUVolley get : { url: " + url + " , json: " + json.toString() + " }");
        requestQueue.add(new JsonObjectRequest(Request.Method.GET, url, json, listener, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("SNLU_TAG", "SNLUVolley get error - " + url + " , detail : " + error);
                Toast.makeText(context, "서버로 부터 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    public interface OnResponseListener {
        void onResponse(JSONObject response);
    }
}
