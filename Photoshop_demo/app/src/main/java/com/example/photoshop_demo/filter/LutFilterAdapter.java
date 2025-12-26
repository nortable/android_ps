package com.example.photoshop_demo.filter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.photoshop_demo.R;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LUT滤镜适配器
 */
public class LutFilterAdapter extends RecyclerView.Adapter<LutFilterAdapter.ViewHolder> {
    
    private List<LutFilter> filters;
    private Bitmap previewSource;  // 用于生成预览的源图（小尺寸）
    private int selectedPosition = 0;
    private OnFilterSelectedListener listener;
    private Map<Integer, Bitmap> previewCache;  // 预览图缓存
    
    public interface OnFilterSelectedListener {
        void onFilterSelected(LutFilter filter, int position);
    }
    
    public LutFilterAdapter(List<LutFilter> filters, Bitmap previewSource) {
        this.filters = filters;
        this.previewSource = previewSource;
        this.previewCache = new HashMap<>();
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.filter_item, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LutFilter filter = filters.get(position);
        
        // 设置滤镜名称
        holder.filterName.setText(filter.getName());
        
        // 生成或获取缓存的预览图
        if (previewCache.containsKey(position)) {
            holder.filterPreview.setImageBitmap(previewCache.get(position));
        } else {
            // 异步生成预览图
            generatePreview(holder, filter, position);
        }
        
        // 选中状态
        boolean isSelected = position == selectedPosition;
        holder.selectedBorder.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        holder.itemView.setAlpha(isSelected ? 1.0f : 0.7f);
        
        // 点击事件
        holder.itemView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onFilterSelected(filter, position);
            }
        });
    }
    
    /**
     * 异步生成预览图
     */
    private void generatePreview(ViewHolder holder, LutFilter filter, int position) {
        new Thread(() -> {
            Bitmap preview;
            
            if (filter.getId().equals("identity")) {
                // 原图直接使用
                preview = previewSource;
            } else {
                // 应用LUT生成预览
                preview = LutFilterManager.getInstance(holder.itemView.getContext())
                    .applyFilter(previewSource, filter, 1.0f);
            }
            
            // 缓存预览图
            previewCache.put(position, preview);
            
            // 回到主线程更新UI
            holder.itemView.post(() -> {
                // 检查holder是否还在绑定这个位置
                if (holder.getAdapterPosition() == position) {
                    holder.filterPreview.setImageBitmap(preview);
                }
            });
        }).start();
    }
    
    @Override
    public int getItemCount() {
        return filters.size();
    }
    
    public void setOnFilterSelectedListener(OnFilterSelectedListener listener) {
        this.listener = listener;
    }
    
    /**
     * 获取当前选中的滤镜
     */
    public LutFilter getSelectedFilter() {
        if (selectedPosition >= 0 && selectedPosition < filters.size()) {
            return filters.get(selectedPosition);
        }
        return null;
    }
    
    /**
     * 重置选中状态
     */
    public void resetSelection() {
        int oldPosition = selectedPosition;
        selectedPosition = 0;
        notifyItemChanged(oldPosition);
        notifyItemChanged(selectedPosition);
    }
    
    /**
     * 释放预览图缓存
     */
    public void releaseCache() {
        for (Bitmap preview : previewCache.values()) {
            if (preview != null && !preview.isRecycled() && preview != previewSource) {
                preview.recycle();
            }
        }
        previewCache.clear();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView filterPreview;
        TextView filterName;
        View selectedBorder;
        
        ViewHolder(View view) {
            super(view);
            filterPreview = view.findViewById(R.id.filter_preview);
            filterName = view.findViewById(R.id.filter_name);
            selectedBorder = view.findViewById(R.id.selected_border);
        }
    }
}

