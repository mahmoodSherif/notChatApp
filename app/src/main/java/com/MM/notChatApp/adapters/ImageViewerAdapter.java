package com.MM.notChatApp.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ImageViewerAdapter extends PagerAdapter {
    private Context context;
    private ArrayList<String> photos = null;
    private String photo = "";

    public ImageViewerAdapter(Context context, ArrayList<String> photos) {
        this.context = context;
        this.photos = photos;
    }
    public ImageViewerAdapter(Context context,String photo) {
        this.context = context;
        this.photo = photo;
    }

    @Override
    public int getCount() {
        if(photos!=null)
        return photos.size();
        else
            return 1;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        if(photos!=null)
        {
            Glide.with(imageView.getContext())
                    .load(Uri.parse(photos.get(position)))
                    .into(imageView);
        }
        else {
            Glide.with(imageView.getContext())
                    .load(Uri.parse(photo))
                    .into(imageView);
        }
        container.addView(imageView,0);
        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ImageView) object);
    }
}
