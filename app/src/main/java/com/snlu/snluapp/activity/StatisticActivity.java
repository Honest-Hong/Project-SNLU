package com.snlu.snluapp.activity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.snlu.snluapp.R;
import com.snlu.snluapp.item.DocumentItem;
import com.snlu.snluapp.util.SNLULog;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
public class StatisticActivity extends AppCompatActivity {

    private static String TAG = "StatisticActivity";
    private float[] yData;
    private String[] xData;
    private DocumentItem document;
    PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistc);
        Log.d(TAG,"onCreate starting to create chart");

        document = new DocumentItem();
        document.setNumber(getIntent().getStringExtra("documentNumber"));

        pieChart=(PieChart)findViewById(R.id.statistic_pie_chart);
        pieChart.setHoleRadius(20f);
        pieChart.setCenterText("단어통계");
        pieChart.setCenterTextSize(10);
        pieChart.setDrawEntryLabels(true);
        pieChart.setUsePercentValues(true);
        pieChart.setRotationEnabled(true);

        requestStatistic();
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
                            int cnt = array.length();
                            yData = new float[cnt];
                            xData = new String[cnt];
                            for(int i=0; i<cnt ;i++){
                                yData[i]=array.getJSONObject(i).getInt("count");
                            }
                            for(int i =0; i<cnt; i++){
                                xData[i] = array.getJSONObject(i).getString("name");
                            }
                            addDataSet();
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
    private void addDataSet() {
        Log.d(TAG,"addDataSet started");

        Legend legend = pieChart.getLegend();

        ArrayList<PieEntry> yEntrys = new ArrayList<PieEntry>();
        for(int i = 0; i<yData.length;i++)
            yEntrys.add(new PieEntry(yData[i], i));

        // drawPieChart();
        PieDataSet dataSet = new PieDataSet(yEntrys,"");
        dataSet.setSliceSpace(3);
        dataSet.setSelectionShift(5);

        ArrayList<String> xEntrys = new ArrayList<String>();
        for(int i=0; i<xData.length;i++)
            xEntrys.add(xData[i]);

        legend.setEnabled(true);
        PieData data = new PieData();

        //add colors to dataset
        ArrayList<Integer> colors = new ArrayList<Integer>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);

        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        pieChart.setData(data);

        pieChart.invalidate();

        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);

        //add legend to chart
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(7);
        legend.setYEntrySpace(5);
        legend.setYOffset(0f);
    }
}