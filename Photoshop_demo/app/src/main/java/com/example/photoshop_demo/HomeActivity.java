package com.example.photoshop_demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 设置功能按钮点击事件
        findViewById(R.id.btn_ai_color).setOnClickListener(v -> {
            // AI追色功能
        });

        findViewById(R.id.btn_camera).setOnClickListener(v -> {
            // 相机模拟功能
        });

        findViewById(R.id.btn_effects).setOnClickListener(v -> {
            // 特效功能
        });

        findViewById(R.id.btn_collage).setOnClickListener(v -> {
            // 拼图功能 - 跳转到编辑页面
            Intent intent = new Intent(HomeActivity.this, EditActivity.class);
            startActivity(intent);
        });
    }
}

