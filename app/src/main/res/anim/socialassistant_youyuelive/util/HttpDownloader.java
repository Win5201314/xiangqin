package com.socialassistant_youyuelive.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Administrator on 2017/5/25.
 */

//下载文本或者语音文件
public class HttpDownloader {

    private final static String SWORD = "SWORD";

    public String download(String urlStr) {

        URL url = null;
        StringBuffer sb = new StringBuffer();
        String line = null;
        BufferedReader buffer = null;

        try {
            //创建一个URL对象
            url = new URL(urlStr);

            //创建一个Http连接
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();

            //使用IO流读取数据
            buffer = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

            while ((line = buffer.readLine()) != null) {
                sb.append(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                buffer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    //返回-1下载文件失败，返回0下载成功，返回1则是文件已存在
    public int downFile(String urlStr, String path, String fileName) {
        InputStream inputStream = null;

        FileUtils fileUtils = new FileUtils();
        if (fileUtils.isFileExist(fileName)) {
            return 1;
        } else {
            try {
                inputStream = getInputSteamFromUrl(urlStr);
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
            File resultFile = fileUtils.write2SDFromInput(path, fileName, inputStream, size);
            if (resultFile == null) {
                return -1;
            }
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int size = 0;

    private InputStream getInputSteamFromUrl(String urlStr) throws MalformedURLException, IOException {
        URL url = null;
        url = new URL(urlStr);
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        InputStream inputStream = urlConn.getInputStream();
        size = urlConn.getContentLength();
        return inputStream;
    }

}
