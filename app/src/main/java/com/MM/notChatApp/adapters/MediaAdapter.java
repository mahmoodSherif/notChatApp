package com.MM.notChatApp.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import static android.graphics.BitmapFactory.decodeFile;

public class MediaAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> photos;
    private GridView gridView;
    public MediaAdapter(Context c,ArrayList<String> images,GridView gv) {
        this.context = c;
        photos = images;
        gridView = gv;
    }

    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        Log.e("list::::",photos.get(0));
        ImageView imageView;
        if(view == null)
        {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams
                    .MATCH_PARENT, gridView.getWidth()/3));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(10, 10, 10, 10);
        }
        else {
            imageView = (ImageView) view;
        }
        Glide.with(imageView.getContext())
                .load(Uri.parse(photos.get(i)))
                .into(imageView);
        return imageView;
    }
}
