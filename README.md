# PullRefresh
#一个支持下拉刷新和上拉刷新的控件，并且支持自定义下拉头和上拉头，可选择只使用下拉头或者只使用上拉头
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-android--PullRefresh-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1084)
# Demo
<p>
   <img src="https://raw.githubusercontent.com/ylqhust/PullRefresh/master/pullrefresh.mp4" width="320" alt="点击观看视频"/>
</p>
##下拉刷新
<p>
  <img src="https://github.com/ylqhust/PullRefresh/blob/master/pulldown.png">
</p>
##上拉刷新
<p>
  <img src="https://github.com/ylqhust/PullRefresh/blob/master/pullup.png">
</p>
# Usage
导入三个文件  

	1.PullRefresh.java  
	2.HeaderHolder.java  
	3.FooterHolder.java  


自定义header.xml和footer.xml

使用的地方  


    <com.ylqhust.pullrefresh.PullRefresh
        android:id="@+id/pullrefresh"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">
        <include layout="@layout/header"/>
        <ListView
            android:id="@+id/listview"
            android:layout_width="match_parent"
            android:layout_height="match_parent">
        </ListView>
        <include layout="@layout/footer"/>
    </com.ylqhust.pullrefresh.PullRefresh>
    
代码部分  

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
        //使用头部和尾部
        pullRefresh.UseAll(callBack,header, listView,footer, new SimpleHeaderHolder(this), new SimpleFooterHolder(this),task );
        //只使用头部
        //pullRefresh.UseHeader(callBack,header,listView,new SimpleHeaderHolder(this),task);
        //只使用尾部
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


