package com.ylqhust.example;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by apple on 15/12/31.
 */
public class Adapter extends BaseAdapter {
    private List<String> strings;

    public LayoutInflater inflater;
    private Context c;

    public Adapter(Context context, List<String> strings) {
        this.inflater = LayoutInflater.from(context);
        this.strings = strings;
        this.c = context;
    }

    @Override
    public int getCount() {
        return strings.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.list_item,null);
        TextView textView = (TextView) view.findViewById(R.id.textview);
        textView.setText(strings.get(position));
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(c,strings.get(position),Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    public void addDataToHeader() {
        for(int i=0;i<3;i++)
            strings.add(0,System.currentTimeMillis()+"New");
    }

    public void addDataToFooter() {
        for(int i=0;i<3;i++)
            strings.add(System.currentTimeMillis()+"New");
    }
}
