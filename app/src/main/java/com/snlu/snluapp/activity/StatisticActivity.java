package com.snlu.snluapp.activity;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.github.mikephil.charting.utils.ViewPortHandler;
import com.snlu.snluapp.R;
import com.snlu.snluapp.item.DocumentItem;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StatisticActivity extends AppCompatActivity {
    private int[] yData;
    private String[] xData;
    private DocumentItem document;
    @BindView(R.id.statistic_bar_chart) BarChart barChart;
    @BindView(R.id.text_amount) TextView textAmount;
    BarDataSet barDataSet;
    BarData barData;
    private ProgressDialog progressDialog;
    private int amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistc);
        ButterKnife.bind(this);

        document = new DocumentItem();
        document.setNumber(getIntent().getStringExtra("documentNumber"));

        barChart = (BarChart) findViewById(R.id.statistic_bar_chart);
        barChart.setMaxVisibleValueCount(60);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setDrawGridBackground(false);
        barChart.getLegend().setEnabled(false);
        barChart.setPinchZoom(true);
        barChart.setDescription(null);

        requestStatistic();
    }

    @OnClick(R.id.button_back)
    public void onBack() {
        finish();
    }

    @OnClick({R.id.button_minus, R.id.button_plus})
    public void onClickControl(View v) {
        switch(v.getId()) {
            case R.id.button_minus:
                if (amount > 1) {
                    amount--;
                    setData(amount);
                }
                break;
            case R.id.button_plus:
                if (amount < xData.length - 1) {
                    amount++;
                    setData(amount);
                }
                break;
        }
    }

    private void requestStatistic() {
        progressDialog = ProgressDialog.show(this, "로딩중", "단어 통계를 불러오는중입니다.");
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
                            yData = new int[cnt];
                            xData = new String[cnt];
                            for (int i = 0; i < cnt; i++) {
                                yData[i] = array.getJSONObject(i).getInt("count");
                            }
                            for (int i = 0; i < cnt; i++) {
                                xData[i] = array.getJSONObject(i).getString("name");
                            }
                            addDataSet();
                            amount = xData.length / 2;
                            setData(amount);
                        } else {
                            Log.v("TAG", "error");
                        }
                        if(progressDialog != null) progressDialog.dismiss();

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
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return xData[(int)value];
            }
        });

        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxis.setAxisMinimum(0f);
        yAxis.setDrawGridLines(false);
        yAxis.setDrawAxisLine(false);
        yAxis.setDrawLabels(false);

        barChart.getAxisRight().setEnabled(false);

        setData(5);
    }
    private void setData(int count){
        textAmount.setText(amount + "");
        ArrayList<BarEntry> yEntry = new ArrayList<BarEntry>();
        barChart.setData(barData);

        if(yData.length<count) {
            for (int i = 0; i < yData.length; i++) {
                yEntry.add(new BarEntry(i, yData[i]));
            }
        }
        else{
            for(int i =0; i<count;i++)
                yEntry.add(new BarEntry(i,yData[i]));
        }

        barDataSet = new BarDataSet(yEntry, "");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barData = new BarData(barDataSet);
        barData.setBarWidth(0.9f);
        barData.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return (int)value + "개";
            }
        });

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.setVisibleXRangeMaximum(5);
        barChart.invalidate();
    }
}