package com.snlu.snluapp.activity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.DataSet;
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
    //PieChart pieChart;
    protected BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistc);

        document = new DocumentItem();
        document.setNumber(getIntent().getStringExtra("documentNumber"));

        barChart = (BarChart) findViewById(R.id.statistic_bar_chart);

        barChart.setMaxVisibleValueCount(60);
        barChart.setDrawBarShadow(true);
        barChart.setDrawValueAboveBar(true);

        requestStatistic();
    }
  /*          super.onCreate(savedInstanceState);
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
            yEntrys.add(new PieEntry(yData[i], xData[i]));

        // drawPieChart();
        PieDataSet dataSet = new PieDataSet(yEntrys,"");
        dataSet.setSliceSpace(3);
        dataSet.setSelectionShift(5);

        legend.setEnabled(true);
        PieData data = new PieData(dataSet);

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
    }*/

    private void requestStatistic() {
        JSONObject json = new JSONObject();
        try {
            json.put("documentNumber", document.getNumber());
            SNLUVolley.getInstance(this).post("analyze", json, new SNLUVolley.OnResponseListener() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.v("TAG", response.toString());
                    try {
                        int result = response.getInt("result");
                        if (result == 0) {
                            String str = response.getString("data");
                            JSONArray array = new JSONArray(str);

                            int cnt = array.length();
                            yData = new float[cnt];
                            xData = new String[cnt];
                            for (int i = 0; i < cnt; i++) {
                                yData[i] = array.getJSONObject(i).getInt("count");
                            }
                            for (int i = 0; i < cnt; i++) {
                                xData[i] = array.getJSONObject(i).getString("name");
                            }
                            addDataSet();

                            int count = array.getJSONObject(0).getInt("count");
                            String name = array.getJSONObject(0).getString("name");
                        } else {
                            Log.v("TAG", "error");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addDataSet() {
        BarDataSet dataSet;
        BarData data;
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);

        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setLabelCount(8, false);
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxis.setSpaceTop(15f);
        yAxis.setAxisMinimum(0f);

        Legend l = barChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.CIRCLE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        ArrayList<BarEntry> yEntrys = new ArrayList<BarEntry>();
       for (int i = 0; i < yData.length; i++) {
            yEntrys.add(new BarEntry(yData[i],i));
       }
       for(int i = 0; i<yData.length;i++) {
           dataSet = new BarDataSet(yEntrys,xData[i]);
       }
       data = new BarData(xAxis,dataSet);
       barChart.setData(data);
    }
}