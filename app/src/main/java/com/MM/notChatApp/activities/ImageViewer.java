package com.MM.notChatApp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.MM.notChatApp.R;
import com.MM.notChatApp.adapters.ImageViewerAdapter;

import java.util.ArrayList;

public class ImageViewer extends AppCompatActivity {

    ViewPager viewPager;
    ArrayList<String> photosList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        photosList = new ArrayList<>();
        viewPager = findViewById(R.id.viewPager);

        Intent intent = getIntent();
        photosList = intent.getStringArrayListExtra("list");
        ImageViewerAdapter imageViewerAdapter = new ImageViewerAdapter(this,
                photosList);
        viewPager.setAdapter(imageViewerAdapter);
    }
}
