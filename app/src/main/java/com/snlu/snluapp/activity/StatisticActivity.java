package com.snlu.snluapp.activity;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
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
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import com.github.mikephil.charting.utils.ViewPortHandler;
import com.snlu.snluapp.R;
import com.snlu.snluapp.item.DocumentItem;
import com.snlu.snluapp.util.SNLULog;
import com.snlu.snluapp.util.SNLUVolley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
public class StatisticActivity extends AppCompatActivity implements OnSeekBarChangeListener{

    private static String TAG = "StatisticActivity";
    private int[] yData;
    private String[] xData;
    private DocumentItem document;
    protected BarChart barChart;
    BarDataSet barDataSet;
    BarData barData;
    private TextView tvX;
    private SeekBar seekBarX;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistc);

        getSupportActionBar().setTitle("단어 통계");

        document = new DocumentItem();
        document.setNumber(getIntent().getStringExtra("documentNumber"));

        tvX = (TextView)findViewById(R.id.tvXMax);

        seekBarX = (SeekBar)findViewById(R.id.seekBar);

        barChart = (BarChart) findViewById(R.id.statistic_bar_chart);
        barChart.setMaxVisibleValueCount(60);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setDrawGridBackground(false);
        barChart.getLegend().setEnabled(false);
        barChart.setPinchZoom(true);

        seekBarX.getProgress();
        seekBarX.setOnSeekBarChangeListener(this);

            findViewById(R.id.icon_minus).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (seekBarX.getProgress() > 1) {
                        seekBarX.setProgress(seekBarX.getProgress() - 1);
                        tvX.setText("" + (seekBarX.getProgress()));
                        setData(seekBarX.getProgress());
                        barChart.invalidate();
                    }
                }
            });

            findViewById(R.id.icon_plus).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (seekBarX.getProgress() > 0) {
                        seekBarX.setProgress(seekBarX.getProgress() + 1);
                        tvX.setText("" + (seekBarX.getProgress()));
                        setData(seekBarX.getProgress());
                        barChart.invalidate();
                    }
                }
            });

        requestStatistic();
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

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
        tvX.setText(""+(seekBarX.getProgress()+1));
        setData(seekBarX.getProgress()+1);
        barChart.invalidate();
    }

    private void setData(int count){

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
seekBarX.setMax(yData.length);
        barDataSet = new BarDataSet(yEntry, "");
        barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barData = new BarData(barDataSet);
        barData.setBarWidth(0.9f);

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.setVisibleXRangeMaximum(6);
        barChart.invalidate();
    }
    public void onStartTrackingTouch(SeekBar seekBar){}
    public void onStopTrackingTouch(SeekBar seekBar){}
}