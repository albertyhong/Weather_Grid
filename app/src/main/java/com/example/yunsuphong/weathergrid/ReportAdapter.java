package com.example.yunsuphong.weathergrid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by yunsuphong on 5/10/15.
 */
//Adapter creates view after view after view and hands it to an adapter view
public class ReportAdapter extends BaseAdapter {
    Context context;
    String[] date;
    int[] humidity;
    int[] temperature;

    public ReportAdapter(Context context, String[] date, int[] humidity, int[] temperature) {
        this.context = context;
        this.date = date;
        this.humidity = humidity;
        this.temperature = temperature;
    }

    //Every adapter requires these methods
    @Override
    public int getCount() {
        return date.length;
    }

    //Every adapter requires these methods
    @Override
    public Object getItem(int position) {
        return null;
    }

    //Every adapter requires these methods
    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout linearLayout;
        //check if 2nd argument is a view, but if it's a linear layout. if it's a view that can be re-used, the 2nd argument will be null
        if (convertView != null) {
            linearLayout = (LinearLayout)convertView;
        } else {
            LayoutInflater inflater = LayoutInflater.from(context);
            linearLayout = (LinearLayout)inflater.inflate(R.layout.gridview_item, null);
        }

        TextView textView = (TextView)linearLayout.findViewById(R.id.title);
        textView.setText(date[position]);
        textView = (TextView) linearLayout.findViewById(R.id.humidity);
        textView.setText("Humidity " + humidity[position] + "%");
        textView = (TextView) linearLayout.findViewById(R.id.temperature);
        textView.setText("Max temp " + temperature[position] + "\u00B0 F");
        return linearLayout;
    }
}
