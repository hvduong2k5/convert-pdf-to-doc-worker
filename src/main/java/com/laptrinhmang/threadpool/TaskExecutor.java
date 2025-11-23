package com.laptrinhmang.threadpool;

import com.google.gson.Gson;
import com.laptrinhmang.App;
import com.laptrinhmang.bean.FileEntity;
import com.laptrinhmang.service.ConvertService;
import com.laptrinhmang.service.FileService;
import com.laptrinhmang.service.MinioService;
import com.laptrinhmang.service.RedisService;
import com.laptrinhmang.bean.Status;
import java.io.File;

public class TaskExecutor implements Runnable {
    private FileEntity fileEntity;
    private RedisService redisService;
    private MinioService minioService;
    private ConvertService convertService;
    private FileService fileService;
    public TaskExecutor(FileEntity fileEntity) {
        this.fileEntity = fileEntity;
        this.redisService = new RedisService();
        this.minioService = new MinioService();
        this.convertService = new ConvertService();
        this.fileService = new FileService();
    }

    @Override
    public void run() {
        try{
            File pdfFile = minioService.download(fileEntity.getLink_pdf());
            fileService.updateStatus(fileEntity, Status.PROCESSING);
            redisService.cacheSet(String.valueOf(fileEntity.getId()), fileEntity);
            File resultFile = new File(convertService.convertFullProcess(pdfFile,"A:/temp/" + fileEntity.getId()+"/"+fileEntity.getLink_pdf()));
            minioService.upload(resultFile);

            fileService.updateStatus(fileEntity, Status.SUCCESS);

            resultFile.delete();
            System.out.println("File " + fileEntity.getId() + " converted and uploaded to Minio");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
