package com.example.demo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.demo.retrofit_rr.InitFileRequestData;
import com.example.demo.retrofit_rr.RequestAPIs;
import com.example.demo.retrofit_rr.ResponseData;
import com.example.demo.retrofit_rr.ResponseLabelData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    TextView randSeed, fileNum;
    Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

        randSeed = findViewById(R.id.mainAct_randSeed);
        fileNum = findViewById(R.id.mainAct_fileNumber);
        submitBtn = findViewById(R.id.mainAct_genFIleBtn);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String randSeedStr = randSeed.getText().toString();
                String fileNumStr = fileNum.getText().toString();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(GlobalVarialbles.hostname)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                retrofit.create(RequestAPIs.class)
                        .initFile(new InitFileRequestData(Integer.parseInt(randSeedStr), Integer.parseInt(fileNumStr)))
                        .enqueue(new Callback<ResponseData>() {
                            @Override
                            public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                                ResponseData data = response.body();

                                Log.d("duration", data.getDuration()+"");

                                Intent intent = new Intent(getApplicationContext(), DetectAct.class);
                                intent.putExtra("dat-url", data.getUrl());
                                intent.putExtra("dat-labels", data.getLabels());
                                intent.putExtra("dat-starts", data.getStartTimes());
                                intent.putExtra("dat-ends", data.getEndTimes());

                                startActivity(intent);
                            }

                            @Override
                            public void onFailure(Call<ResponseData> call, Throwable t) {

                            }
                        });

            }
        });

    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.INTERNET}, 0);
        }
    }
}
