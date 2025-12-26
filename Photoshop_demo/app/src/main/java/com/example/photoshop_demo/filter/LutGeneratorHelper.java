package com.example.photoshop_demo.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * LUT生成辅助工具
 * 用于在开发时生成并保存LUT文件
 * 
 * 使用方法：
 * 1. 在HomeActivity或EditActivity的onCreate中调用 generateAndSaveAllLuts()
 * 2. 运行应用，LUT文件会保存到 Pictures/Photoshop_demo/luts/
 * 3. 从设备中复制这些PNG文件到项目的 app/src/main/assets/luts/ 文件夹
 * 4. 删除或注释掉调用代码
 */
public class LutGeneratorHelper {
    
    /**
     * 生成并保存所有LUT文件
     * 注意：仅在开发时使用！
     */
    public static void generateAndSaveAllLuts(Context context) {
        new Thread(() -> {
            try {
                // 创建保存目录
                File lutDir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "Photoshop_demo/luts"
                );
                if (!lutDir.exists()) {
                    lutDir.mkdirs();
                }
                
                // 生成并保存所有LUT
                saveLut(LutGenerator.generateIdentityLut(), 
                    new File(lutDir, "identity.png"));
                saveLut(LutGenerator.generateGrayscaleLut(), 
                    new File(lutDir, "grayscale.png"));
                saveLut(LutGenerator.generateWarmLut(), 
                    new File(lutDir, "warm.png"));
                saveLut(LutGenerator.generateCoolLut(), 
                    new File(lutDir, "cool.png"));
                saveLut(LutGenerator.generateVintageLut(), 
                    new File(lutDir, "vintage.png"));
                saveLut(LutGenerator.generateVividLut(), 
                    new File(lutDir, "vivid.png"));
                saveLut(LutGenerator.generateRomanticLut(), 
                    new File(lutDir, "romantic.png"));
                saveLut(LutGenerator.generateCinematicLut(), 
                    new File(lutDir, "cinematic.png"));
                
                android.util.Log.d("LutGenerator", 
                    "所有LUT已保存到: " + lutDir.getAbsolutePath());
                android.util.Log.d("LutGenerator", 
                    "请从设备复制这些文件到项目的 app/src/main/assets/luts/ 文件夹");
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * 保存LUT到文件
     */
    private static void saveLut(Bitmap lut, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            lut.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
        }
        lut.recycle();
    }
}

