package com.Keepitsoft.wagertoolkit;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyAdapter extends BaseAdapter {
    Context context;
    int [] Images;
    String [] Names;
    LayoutInflater inflater;

    public MyAdapter (Context applicationContext,String [] Names,int [] Images){
        this.context = applicationContext;
        this.Images = Images;
        this.Names= Names;
        inflater = (LayoutInflater.from(applicationContext));
    }



    @Override
    public int getCount() {
        return Names.length;
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
    public View getView(int position, View view, ViewGroup parent) {
        view= inflater.inflate(R.layout.list_images, null);
        TextView NameG = (TextView) view.findViewById(R.id.text_flipper);
        ImageView ImageG = (ImageView) view.findViewById(R.id.image_flipper);
        NameG.setText(Names[position]);
        ImageG.setImageResource(Images[position]);
        return view;
    }
}
