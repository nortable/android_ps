package com.example.photoshop_demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 滑动删除回调
 * 原因：为RecyclerView提供滑动删除功能
 */
public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    
    private ProjectAdapter adapter;
    private Drawable deleteIcon;
    private ColorDrawable background;
    private int backgroundColor = Color.parseColor("#FF0000");
    private Paint clearPaint;
    private Context context;
    
    public SwipeToDeleteCallback(Context context, ProjectAdapter adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.context = context;
        this.adapter = adapter;
        
        // 初始化删除图标
        deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
        if (deleteIcon != null) {
            deleteIcon.setTint(Color.WHITE);
        }
        
        // 初始化背景
        background = new ColorDrawable(backgroundColor);
        
        // 初始化清除画笔（用于透明效果）
        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }
    
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                         @NonNull RecyclerView.ViewHolder viewHolder,
                         @NonNull RecyclerView.ViewHolder target) {
        // 不支持拖拽
        return false;
    }
    
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // 获取滑动的项目位置
        int position = viewHolder.getAdapterPosition();
        // 通知adapter删除
        adapter.deleteProject(position);
    }
    
    @Override
    public void onChildDraw(@NonNull Canvas c,
                           @NonNull RecyclerView recyclerView,
                           @NonNull RecyclerView.ViewHolder viewHolder,
                           float dX, float dY,
                           int actionState,
                           boolean isCurrentlyActive) {
        
        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getBottom() - itemView.getTop();
        boolean isCanceled = dX == 0f && !isCurrentlyActive;
        
        if (isCanceled) {
            // 滑动取消，清除画布
            clearCanvas(c, itemView.getRight() + dX, (float) itemView.getTop(),
                       (float) itemView.getRight(), (float) itemView.getBottom());
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }
        
        // 绘制红色背景
        if (dX < 0) {
            // 左滑
            background.setBounds(
                itemView.getRight() + (int) dX,
                itemView.getTop(),
                itemView.getRight(),
                itemView.getBottom()
            );
        } else {
            // 右滑
            background.setBounds(
                itemView.getLeft(),
                itemView.getTop(),
                itemView.getLeft() + (int) dX,
                itemView.getBottom()
            );
        }
        background.draw(c);
        
        // 绘制删除图标
        if (deleteIcon != null) {
            int iconMargin = (itemHeight - deleteIcon.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + iconMargin;
            int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
            
            if (dX < 0) {
                // 左滑 - 图标在右边
                int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                int iconRight = itemView.getRight() - iconMargin;
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            } else {
                // 右滑 - 图标在左边
                int iconLeft = itemView.getLeft() + iconMargin;
                int iconRight = iconLeft + deleteIcon.getIntrinsicWidth();
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            }
            
            deleteIcon.draw(c);
        }
        
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
    
    /**
     * 清除画布
     */
    private void clearCanvas(Canvas c, Float left, Float top, Float right, Float bottom) {
        c.drawRect(left, top, right, bottom, clearPaint);
    }
}

