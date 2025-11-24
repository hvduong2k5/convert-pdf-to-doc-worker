package com.laptrinhmang.threadpool;

import com.google.gson.Gson;
import com.laptrinhmang.App;
import com.laptrinhmang.bean.FileEntity;
import com.laptrinhmang.service.ConvertService;
import com.laptrinhmang.service.FileService;
import com.laptrinhmang.service.MinioService;
import com.laptrinhmang.service.RedisService;
import com.laptrinhmang.bean.Status;
import com.laptrinhmang.util.RedisUtil;
import org.checkerframework.checker.units.qual.C;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.wml.P;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Random;

public class TaskExecutor implements Runnable {
    private FileEntity fileEntity;
    private RedisService redisService;
    private MinioService minioService;
    private ConvertService convertService;
    private FileService fileService;
    private String FOLDER_TEMP ;
    static private final String CONTENT_TYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public TaskExecutor(FileEntity fileEntity) {
        this.fileEntity = fileEntity;
        this.redisService = new RedisService();
        this.minioService = new MinioService();
        this.convertService = new ConvertService();
        this.fileService = new FileService();
        this.FOLDER_TEMP = "A:/temp/" + fileEntity.getId() + "/";
    }

    @Override
    public void run() {
        try{
            File tempFile = minioService.download("pdf", fileEntity.getLink_pdf());
            fileEntity.setStatus(Status.PROCESSING);
            fileService.update(fileEntity);
            redisService.cacheSet(String.valueOf(fileEntity.getId()), fileEntity);
            redisService.publish(RedisUtil.getChannelName(), fileEntity);
            System.out.println("File " + fileEntity.getId() + " is processing");

            File resultFile = convertService.convertFullProcess(tempFile, this.FOLDER_TEMP);
            InputStream resultStream = Files.newInputStream(resultFile.toPath());
            String linkDoc = fileEntity.getLink_pdf().replace(".pdf", ".docx");


            fileEntity.setLink_doc(linkDoc);
            minioService.upload("doc", fileEntity.getLink_doc(), resultStream, resultFile.length(), CONTENT_TYPE_DOCX);

            fileEntity.setStatus(Status.SUCCESS);
            fileService.update(fileEntity);
            redisService.cacheSet(String.valueOf(fileEntity.getId()), fileEntity);
            redisService.publish(RedisUtil.getChannelName(), fileEntity);

            resultStream.close();
            boolean delete = resultFile.delete();
            System.out.println("File " + fileEntity.getId() + " converted and uploaded to Minio");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
