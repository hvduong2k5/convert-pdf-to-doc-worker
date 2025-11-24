package com.laptrinhmang;

import com.google.gson.Gson;
import com.laptrinhmang.bean.FileEntity;
import com.laptrinhmang.service.RedisService;
import com.laptrinhmang.threadpool.TaskExecutor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private static final String WATERMARK = "Evaluation Warning";

    public static void main(String[] args) {

        RedisService redisService = new RedisService();
        while (Thread.currentThread().isAlive()) {
            try {
                FileEntity fileEntity = null;
                try {
                     fileEntity = redisService.pop();
                }catch(Exception e ){
                    System.out.println("lỗi khi pop queue");
                    continue;
                }
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