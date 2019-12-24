package com.example.demo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demo.retrofit_rr.CreateFileRequestData;
import com.example.demo.retrofit_rr.RequestAPIs;
import com.example.demo.retrofit_rr.ResponseData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetectAct extends AppCompatActivity {

    List<String> listTitle;
    List<ResponseData> listData;
    ResponseData initDat;

    ExpandableListAdapter expandableListAdapter;
    ExpandableListView expandableListView;

    EditText incEdt;
    Button submitBtn, playBtn;
    MediaPlayer mp;
    Boolean isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);


        Intent intent = getIntent();
        isPlaying = false;

        initDat = new ResponseData();
        initDat.setLabels(intent.getStringArrayExtra("dat-labels"));
        initDat.setStartTimes(intent.getFloatArrayExtra("dat-starts"));
        initDat.setEndTimes(intent.getFloatArrayExtra("dat-ends"));
        initDat.setUrl(intent.getStringExtra("dat-url"));

        listTitle = new ArrayList<>();
        listData = new ArrayList<>();


        incEdt = findViewById(R.id.pure_increaseNumber);
        submitBtn = findViewById(R.id.pure_submitBtn);
        playBtn = findViewById(R.id.playBtn);

        TableLayout table = findViewById(R.id.displayResultAct_tableLayout_main);

        float[] startTimes = initDat.getStartTimes();
        float[] endTimes = initDat.getEndTimes();
        String[] labels = initDat.getLabels();
        int len = labels.length;


        expandableListView  = findViewById(R.id.expandableListView);

        expandableListAdapter = new DisplayDataAdapt(getApplicationContext(), listTitle, listData, isPlaying);
        expandableListView.setAdapter(expandableListAdapter);

        NestedScrollView view =findViewById(R.id.scrollView);
        view.setNestedScrollingEnabled(true);
        for (int i = 0; i < len; i++) {
            TableRow tr = new TableRow(getApplicationContext());

            String colorCode;
            if (labels[i].equals("Normal")) {
                colorCode = "#001f3f";
            } else {
                colorCode = "#d90000";
            }
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            tr.setLayoutParams(layoutParams);

            TextView tv1 = new TextView(getApplicationContext());
            tv1.setText(startTimes[i] + " s");
            tv1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT, 1f));
            tv1.setGravity(Gravity.CENTER);
            tv1.setTextColor(Color.parseColor(colorCode));
            tr.addView(tv1);

            TextView tv2 = new TextView(getApplicationContext());
            tv2.setText(endTimes[i] + " s");
            tv2.setTextColor(Color.parseColor(colorCode));
            tv2.setGravity(Gravity.CENTER);
            tv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT, 1f));
            tr.addView(tv2);

            TextView tv3 = new TextView(getApplicationContext());
            tv3.setText(labels[i]);
            tv3.setTextColor(Color.parseColor(colorCode));
            tv3.setGravity(Gravity.CENTER);
            tv3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT, 1f));
            tr.addView(tv3);

            table.addView(tr);
        }

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final float inc = Float.parseFloat(incEdt.getText().toString());
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(GlobalVarialbles.hostname)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    retrofit.create(RequestAPIs.class)
                            .createFile(new CreateFileRequestData(inc, "", -1))
                            .enqueue(new Callback<ResponseData>() {
                                @Override
                                public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                                    int number = listData.size()+1;
                                    ResponseData rs = response.body();
                                    rs.setIcrease(inc+"");
                                    rs.setFilter(null);
                                    rs.setNoise(null);
                                    listTitle.add("New file number "+number);
                                    listData.add(rs);
                                    Log.d("size", listData.size()+"");
                                }

                                @Override
                                public void onFailure(Call<ResponseData> call, Throwable t) {

                                }
                            });
                }

                catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), "Wrong data type", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlaying) {
                    Log.d("url", initDat.getUrl());
                    mp = new MediaPlayer();
                    try {
                        mp.setDataSource(initDat.getUrl());

                        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                isPlaying = true;
                                playBtn.setText("pause");
                                mp.start();
                            }
                        });

                        mp.prepareAsync();

                        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                isPlaying = false;
                                playBtn.setText("play file");
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


                else {
                    if (mp != null) {
                        mp.stop();
                        mp.release();
                        mp = null;
                        isPlaying = false;
                        playBtn.setText("play file");
                    }
                }
            }
        });

    }


}
