package com.example.photoshop_demo;

import android.graphics.Bitmap;
import java.util.Stack;

/**
 * 编辑历史管理
 * 实现撤销/重做功能
 */
public class EditHistory {
    private Stack<Bitmap> undoStack = new Stack<>();
    private Stack<Bitmap> redoStack = new Stack<>();
    private int maxSize = 10;  // 最多保存10步
    
    /**
     * 保存当前状态
     * @param bitmap 要保存的图片状态
     */
    public void pushState(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        
        // 复制Bitmap（避免原图被修改）
        Bitmap copy = bitmap.copy(
            bitmap.getConfig() != null ? bitmap.getConfig() : Bitmap.Config.ARGB_8888, 
            true
        );
        undoStack.push(copy);
        
        // 限制栈大小，防止内存溢出
        if (undoStack.size() > maxSize) {
            Bitmap oldest = undoStack.remove(0);
            if (oldest != null && !oldest.isRecycled()) {
                oldest.recycle();
            }
        }
        
        // 清空重做栈（新操作会清除重做历史）
        clearRedoStack();
    }
    
    /**
     * 撤销
     * @param current 当前图片
     * @return 上一步的图片，如果没有返回null
     */
    public Bitmap undo(Bitmap current) {
        if (undoStack.isEmpty()) {
            return null;
        }
        
        // 当前状态压入重做栈
        if (current != null && !current.isRecycled()) {
            Bitmap copy = current.copy(
                current.getConfig() != null ? current.getConfig() : Bitmap.Config.ARGB_8888,
                true
            );
            redoStack.push(copy);
        }
        
        // 从撤销栈弹出
        return undoStack.pop();
    }
    
    /**
     * 重做
     * @param current 当前图片
     * @return 重做后的图片，如果没有返回null
     */
    public Bitmap redo(Bitmap current) {
        if (redoStack.isEmpty()) {
            return null;
        }
        
        // 当前状态压入撤销栈
        if (current != null && !current.isRecycled()) {
            Bitmap copy = current.copy(
                current.getConfig() != null ? current.getConfig() : Bitmap.Config.ARGB_8888,
                true
            );
            undoStack.push(copy);
        }
        
        // 从重做栈弹出
        return redoStack.pop();
    }
    
    /**
     * 检查是否可以撤销
     * @return true if can undo
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }
    
    /**
     * 检查是否可以重做
     * @return true if can redo
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
    
    /**
     * 清空所有历史
     */
    public void clear() {
        clearUndoStack();
        clearRedoStack();
    }
    
    /**
     * 清空撤销栈
     */
    private void clearUndoStack() {
        while (!undoStack.isEmpty()) {
            Bitmap bitmap = undoStack.pop();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }
    
    /**
     * 清空重做栈
     */
    private void clearRedoStack() {
        while (!redoStack.isEmpty()) {
            Bitmap bitmap = redoStack.pop();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }
    
    /**
     * 获取撤销栈大小
     */
    public int getUndoStackSize() {
        return undoStack.size();
    }
    
    /**
     * 获取重做栈大小
     */
    public int getRedoStackSize() {
        return redoStack.size();
    }
}

