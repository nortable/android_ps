package com.example.photoshop_demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 项目列表适配器
 * 原因：RecyclerView需要Adapter来显示列表数据
 */
public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private Context context;
    private List<EditProject> projects;
    private OnProjectClickListener listener;
    private OnProjectDeleteListener deleteListener;

    public interface OnProjectClickListener {
        void onProjectClick(EditProject project);
    }
    
    public interface OnProjectDeleteListener {
        void onProjectDelete(EditProject project, int position);
    }

    public ProjectAdapter(Context context, List<EditProject> projects) {
        this.context = context;
        this.projects = projects;
    }

    public void setOnProjectClickListener(OnProjectClickListener listener) {
        this.listener = listener;
    }
    
    public void setOnProjectDeleteListener(OnProjectDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        EditProject project = projects.get(position);
        holder.bind(project);
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public void updateProjects(List<EditProject> newProjects) {
        this.projects = newProjects;
        notifyDataSetChanged();
    }
    
    /**
     * 删除项目
     */
    public void deleteProject(int position) {
        if (position >= 0 && position < projects.size()) {
            EditProject project = projects.get(position);
            
            // 从列表移除
            projects.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, projects.size());
            
            // 通知HomeActivity处理删除逻辑
            if (deleteListener != null) {
                deleteListener.onProjectDelete(project, position);
            }
        }
    }
    
    /**
     * 恢复项目（撤销删除用）
     */
    public void restoreProject(EditProject project, int position) {
        projects.add(position, project);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, projects.size());
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView projectName;
        TextView projectTime;
        TextView projectStatus;
        TextView btnReedit;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.project_thumbnail);
            projectName = itemView.findViewById(R.id.project_name);
            projectTime = itemView.findViewById(R.id.project_time);
            projectStatus = itemView.findViewById(R.id.project_status);
            btnReedit = itemView.findViewById(R.id.btn_reedit);
        }

        public void bind(EditProject project) {
            // 加载缩略图
            loadThumbnail(project);

            // 设置项目名称（使用ID的后5位作为编号，确保唯一性）
            String projectId = project.getProjectId();
            String displayNumber = projectId.substring(Math.max(0, projectId.length() - 5));
            projectName.setText("编辑项目 #" + displayNumber);

            // 设置时间
            projectTime.setText(formatTime(project.getLastEditTime()));

            // 设置状态
            if (project.getEditedImagePath() != null && !project.getEditedImagePath().isEmpty()) {
                projectStatus.setText("已保存");
                projectStatus.setTextColor(context.getColor(R.color.teal_200));
            } else {
                projectStatus.setText("编辑中");
                projectStatus.setTextColor(context.getColor(R.color.yellow));
            }

            // 点击事件
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProjectClick(project);
                }
            });

            btnReedit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProjectClick(project);
                }
            });
        }

        private void loadThumbnail(EditProject project) {
            try {
                Bitmap bitmap = null;
                
                // 优先显示编辑后的图片
                if (project.getEditedImagePath() != null && !project.getEditedImagePath().isEmpty()) {
                    File file = new File(project.getEditedImagePath());
                    if (file.exists()) {
                        bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    }
                }
                
                // 如果没有编辑后的图片，显示原图
                if (bitmap == null && project.getOriginalImageUri() != null) {
                    Uri uri = Uri.parse(project.getOriginalImageUri());
                    bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                }
                
                if (bitmap != null) {
                    // 创建缩略图
                    Bitmap thumbnail = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
                    this.thumbnail.setImageBitmap(thumbnail);
                    if (bitmap != thumbnail) {
                        bitmap.recycle();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 加载失败，显示占位图
                thumbnail.setBackgroundColor(context.getColor(R.color.gray));
            }
        }

        private String formatTime(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            if (diff < 60 * 1000) {
                return "刚刚";
            } else if (diff < 60 * 60 * 1000) {
                return (diff / (60 * 1000)) + "分钟前";
            } else if (diff < 24 * 60 * 60 * 1000) {
                return (diff / (60 * 60 * 1000)) + "小时前";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            }
        }
    }
}


