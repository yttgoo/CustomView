package com.example.android.customview;

import android.os.Bundle;
import android.view.View;

import com.example.android.customview.view.CustomView;

import java.util.Random;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SimpleTextActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_text);
        final CustomView customView = findViewById(R.id.text);
        final Random random = new Random();
        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int i = random.nextInt(2000);
                customView.setNum(i);
            }
        });
    }
}
