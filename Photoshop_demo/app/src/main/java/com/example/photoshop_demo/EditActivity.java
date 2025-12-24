package com.example.photoshop_demo;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class EditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> {
            finish();
        });

        // 撤销按钮
        findViewById(R.id.btn_undo).setOnClickListener(v -> {
            // 撤销操作
        });

        // 更多选项按钮
        findViewById(R.id.btn_more).setOnClickListener(v -> {
            // 更多选项
        });

        // AI创意按钮
        findViewById(R.id.btn_ai_create).setOnClickListener(v -> {
            // AI创意功能
        });

        // 导出按钮
        findViewById(R.id.btn_export).setOnClickListener(v -> {
            // 导出功能
        });

        // 底部工具栏按钮
        findViewById(R.id.btn_tool_color_adjust).setOnClickListener(v -> {
            // AI色彩调节
        });

        findViewById(R.id.btn_tool_ai_color).setOnClickListener(v -> {
            // AI追色
        });

        findViewById(R.id.btn_tool_filter).setOnClickListener(v -> {
            // 滤镜
        });

        findViewById(R.id.btn_tool_balance).setOnClickListener(v -> {
            // 白平衡
        });

        findViewById(R.id.btn_tool_shadow).setOnClickListener(v -> {
            // 影调
        });
    }
}

