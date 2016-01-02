package com.ylqhust.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.ylqhust.pullrefresh.PullRefresh;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PullRefresh pullRefresh;
    private ListView listView;
    private Adapter adapter;
    private int tag = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pullRefresh = (PullRefresh) findViewById(R.id.pullrefresh);
        listView = (ListView) findViewById(R.id.listview);
        View header = findViewById(R.id.header);
        View footer = findViewById(R.id.footer);

        PullRefresh.CallBack callBack = new PullRefresh.CallBack() {
            @Override
            public boolean CanotPullDown() {
                if (listView.getFirstVisiblePosition() == 0) {
                    View first = listView.getChildAt(0);
                    if (first.getTop() == 0)
                        return true;
                }
                return false;
            }

            @Override
            public boolean CanotPullUp() {
                if (listView.getLastVisiblePosition() == listView.getCount() - 1) {
                    View last = listView.getChildAt(listView.getChildCount() - 1);
                    if (Math.abs(listView.getBottom() - last.getBottom()) < 2)
                        return true;
                }
                return false;
            }
        };
        PullRefresh.Task task = new PullRefresh.Task() {
            @Override
            public void HeaderTask() {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                HeaderTaskFinish();
                            }
                        });
                    }
                }.start();
            }

            @Override
            public void FooterTask() {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                FooterTaskFinish();
                            }
                        });
                    }
                }.start();
            }
        };
        pullRefresh.UseAll(callBack,header, listView,footer, new SimpleHeaderHolder(this), new SimpleFooterHolder(this),task );
        //pullRefresh.UseHeader(callBack,header,listView,new SimpleHeaderHolder(this),task);
        //pullRefresh.UseFooter(callBack,footer,listView,new SimpleFooterHolder(this),task);


        List<String> stringList = new ArrayList<String>();
        for(int i=0;i<20;i++){
            stringList.add("String:"+i);
        }

        adapter = new Adapter(this,stringList);
        listView.setAdapter(adapter);
    }

    private void HeaderTaskFinish() {
        if (tag%2==0){
            pullRefresh.headerEndSuccess();
            adapter.addDataToHeader();
            adapter.notifyDataSetChanged();
        }
        else{
            pullRefresh.headerEndFailed();
        }
        tag++;
    }

    private void FooterTaskFinish() {
        if (tag%2==0){
            pullRefresh.footerEndSuccess();
            adapter.addDataToFooter();
            adapter.notifyDataSetChanged();
        }
        else{
            pullRefresh.footerEndFailed();
        }
        tag++;
    }
}
