package com.laptrinhmang.service;

import com.laptrinhmang.util.MinioUtil;
import io.minio.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class MinioService {
    private final MinioClient client;

    public MinioService() {
        this.client = MinioUtil.getClient();
        createBucketIfNotExists("pdf");
        createBucketIfNotExists("doc");
    }


    private void createBucketIfNotExists(String bucket) {
        try {
            boolean exists = client.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucket)
                            .build()
            );

            if (!exists) {
                client.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucket)
                                .build()
                );
                System.out.println("[MinioService] Bucket created: " + bucket);
            }

        } catch (Exception e) {
            throw new RuntimeException("Cannot verify/create bucket: " + bucket, e);
        }
    }


    public void upload(String bucket, String objectName, InputStream is, long size, String contentType) throws Exception {
        MinioUtil.getClient().putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .stream(is, size, -1)
                        .contentType(contentType)
                        .build()
        );
    }


    public File download(String bucket, String objectName) throws Exception {

        File tempFile = File.createTempFile("minio-download-", ".tmp");
        try (InputStream in = client.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .build()
        )) {
            Files.copy(in, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile;
    }


    public void delete(String bucket, String objectName) throws Exception {
        client.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .build()
        );
    }


    public boolean exists(String bucket, String objectName) {
        try {
            client.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
