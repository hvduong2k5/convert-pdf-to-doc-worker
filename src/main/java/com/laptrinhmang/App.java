package com.laptrinhmang;

import com.google.gson.Gson;
import com.laptrinhmang.bean.FileEntity;
import com.laptrinhmang.bean.Status;
import com.laptrinhmang.service.ConvertService;
import com.laptrinhmang.service.MinioService;
import com.laptrinhmang.service.RedisService;
import com.laptrinhmang.threadpool.TaskExecutor;
import com.laptrinhmang.util.DBUtil;
import com.laptrinhmang.util.RedisUtil;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.FileFormat;
import org.apache.poi.xwpf.usermodel.*;

import java.io.*;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private static final String WATERMARK = "Evaluation Warning";

    public static void main(String[] args) {

        RedisService redisService = new RedisService();
        while (Thread.currentThread().isAlive()) {
            try {

                FileEntity fileEntity = redisService.pop(RedisUtil.getQueueName());
                System.out.println(new Gson().toJson(fileEntity));
                if (fileEntity != null) {
                    executorService.submit(new TaskExecutor(fileEntity));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}