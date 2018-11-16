package com.socialassistant_youyuelive.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import android.os.Environment;

import com.alibaba.fastjson.JSONObject;

public class FileUtils {
    private String SDPATH;
    public static final String ORDERDIRECTORY = Environment.getExternalStorageDirectory() + "/.youyue/.order";

    public String getSDPATH() {
        return SDPATH;
    }

    public FileUtils() {
        //得到当前外部存储设备的目录
        // /SDCARD
        SDPATH = Environment.getExternalStorageDirectory() + "/";
    }

    /*
     * 在SD卡上创建文件
     */
    public File createSDFile(String fileName) throws IOException {
        File file = new File(SDPATH + fileName);
        file.createNewFile();
        return file;
    }

	/*
     * 在SD卡上创建目录
	 */

    public File createSDDir(String dirName) {
        File dir = new File(SDPATH + dirName);
        dir.mkdir();
        return dir;
    }

    /*
     * 判断SD卡上的文件夹是否存在
     */
    public boolean isFileExist(String fileName) {
        File file = new File(SDPATH + fileName);
        return file.exists();
    }

    /*
     * 将一个InputStream里面的数据写入到SD卡中
     */
    public File write2SDFromInput(String path, String fileName, InputStream input, int size) {
        File file = null;
        OutputStream output = null;
        try {
            //创建目录
            createSDDir(path);
            //创建文件
            file = createSDFile(path + fileName);
            //创建输出流
            output = new FileOutputStream(file);
            //创建缓冲区
            byte buffer[] = new byte[size];
            //写入数据
            int len1 = -1;
            while ((len1 = input.read(buffer)) != -1) {
                output.write(buffer, 0, len1);
                //清空缓存
                output.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //关闭输出流
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    // 字节流操作主要用于小文件读写
    public String readFileByByte(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            int length = inputStream.available();
            byte[] bytes = new byte[length];
            inputStream.read(bytes);
            // 类型默认使用"UTF-8"
            String content = new String(bytes, "UTF-8");
            return content;
        } catch (Exception e) {

        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    // 用字节流写入字符串到指定文件
    public boolean writeFileByByte(String filePath, String content) {
        FileOutputStream outputStream = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            outputStream = new FileOutputStream(file);
            byte[] bytes = content.getBytes();
            outputStream.write(bytes);
            return true;
        } catch (Exception e) {

        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    // 删除指定路径的文件
    public boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
            return true;
        }
        return false;
    }

    // 列出指定路径下的文件下的文件个数
    public int listFile(String filePath) {
        File file = new File(filePath);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            return files.length;
        }
        return 0;
    }

    // 列出指定文件夹下的第一个文件的订单参数
    public Map<String, Object> searchOrderFile(String filePath) {
        Map<String, Object> params = new HashMap<>();
        File file = new File(filePath);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files.length > 0) {
                for (File orderFile : files) {
                    String content = readFileByByte(orderFile.getAbsolutePath());
                    JSONObject jsonObject = JSONObject.parseObject(content);
                    params.put("consumer", jsonObject.getString("consumer"));
                    params.put("anchor", jsonObject.getString("anchor"));
                    params.put("userId", jsonObject.getString("userId"));
                    params.put("anchorId", jsonObject.getString("anchorId"));
                    params.put("pay", Math.abs(jsonObject.getLongValue("consume")));
                    params.put("recordIdString", jsonObject.getLongValue("timeStamp"));
                    return params;
                }
            }
        }
        return null;
    }
}
