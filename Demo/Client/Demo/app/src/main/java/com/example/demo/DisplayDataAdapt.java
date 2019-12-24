package com.example.demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demo.retrofit_rr.CreateFileRequestData;
import com.example.demo.retrofit_rr.RequestAPIs;
import com.example.demo.retrofit_rr.ResponseData;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DisplayDataAdapt extends BaseExpandableListAdapter {
    private Context context;
    private List<String> listTitle;
    private List<ResponseData> listData;
    private Boolean isPLaying;
    private MediaPlayer mp;

    public DisplayDataAdapt(Context context, List<String> listTitle, List<ResponseData> listData, Boolean isPLaying) {
        this.context = context;
        this.listTitle = listTitle;
        this.listData = listData;
        this.isPLaying = isPLaying;
    }

    @Override
    public int getGroupCount() {
        return listData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 3;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listTitle.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listData.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String title = (String) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_title, null);
        }

        TextView tv = convertView.findViewById(R.id.listTitle);
        tv.setText(title);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, final ViewGroup parent) {

        LayoutInflater li = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (childPosition == 1) {
            convertView = li.inflate(R.layout.list_data, null);

            TextView incrTxt = convertView.findViewById(R.id.data_in_de);
            TextView noiseTxt = convertView.findViewById(R.id.data_noise);
            TextView filterTxt = convertView.findViewById(R.id.data_filter);

            ResponseData dat = listData.get(groupPosition);

            if (dat.getIcrease()!= null) {
                incrTxt.setText("Increase: "+dat.getIcrease());
            }

            if (dat.getNoise() != null) {
                noiseTxt.setText("Noise: "+dat.getNoise());
            }

            if (dat.getFilter() != null) {
                filterTxt.setText("Filter: "+dat.getFilter());
            }

        }

        else if (childPosition == 2) {
            convertView = li.inflate(R.layout.table_data, null);
            ResponseData dat = listData.get(groupPosition);

            TableLayout table = convertView.findViewById(R.id.displayResultAct_tableLayout);
            float[] startTimes = dat.getStartTimes();
            float[] endTimes = dat.getEndTimes();
            String[] labels = dat.getLabels();
            int len = labels.length;
            for (int i = 0; i < len; i++) {
                TableRow tr = new TableRow(context);

                String colorCode;
                if (labels[i].equals("Normal")) {
                    colorCode = "#001f3f";
                }

                else {
                    colorCode = "#d90000";
                }

                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                tr.setLayoutParams(layoutParams);

                TextView tv1 = new TextView(context);
                tv1.setText(startTimes[i]+" s");
                tv1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT, 1f));
                tv1.setGravity(Gravity.CENTER);
                tv1.setTextColor(Color.parseColor(colorCode));
                tr.addView(tv1);

                TextView tv2 = new TextView(context);
                tv2.setText(endTimes[i]+" s");
                tv2.setTextColor(Color.parseColor(colorCode));
                tv2.setGravity(Gravity.CENTER);
                tv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT, 1f));
                tr.addView(tv2);

                TextView tv3 = new TextView(context);
                tv3.setText(labels[i]);
                tv3.setTextColor(Color.parseColor(colorCode));
                tv3.setGravity(Gravity.CENTER);
                tv3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT, 1f));
                tr.addView(tv3);

                table.addView(tr);
            }
        }

        else if (childPosition == 0) {
            convertView = li.inflate(R.layout.play_btn, null);

            final ResponseData dat = listData.get(groupPosition);

            final Button play = convertView.findViewById(R.id.button2);

            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isPLaying) {
                        Log.d("url", dat.getUrl());
                        mp = new MediaPlayer();
                        try {
                            mp.setDataSource(dat.getUrl());

                            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    isPLaying = true;
                                    play.setText("pause");
                                    mp.start();
                                }
                            });

                            mp.prepareAsync();

                            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    isPLaying = false;
                                    play.setText("play file");
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
                            isPLaying = false;
                            play.setText("play file");
                        }
                    }
                }
            });
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

}
