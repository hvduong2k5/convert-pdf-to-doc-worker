package com.laptrinhmang.service;

import com.laptrinhmang.util.MinioUtil;
import io.minio.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class MinioService {
    private final MinioClient client;
    private final String bucket = "convert";

    public MinioService() {
        this.client = MinioUtil.getClient();
        createBucketIfNotExists();
    }

    // =======================
    // CREATE BUCKET IF NOT EXISTS
    // =======================
    private void createBucketIfNotExists() {
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

    // =======================
    // UPLOAD FILE
    // =======================
    public String upload(File file) throws Exception {

        client.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(file.getName())
                        .stream(new FileInputStream(file), file.length(), -1)
                        .contentType("application/octet-stream")
                        .build()
        );

        return file.getName();
    }

    // =======================
    // DOWNLOAD TO TEMP FILE
    // =======================
    public File download(String fileName) throws Exception {

        InputStream stream = client.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(fileName)
                        .build()
        );

        File tmp = new File(System.getProperty("java.io.tmpdir"), fileName);

        java.nio.file.Files.copy(stream, tmp.toPath(), REPLACE_EXISTING);

        stream.close();
        return tmp;
    }

    // =======================
    // DELETE FILE
    // =======================
    public void delete(String fileName) throws Exception {
        client.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(fileName)
                        .build()
        );
    }

    // =======================
    // CHECK EXISTS
    // =======================
    public boolean exists(String fileName) {
        try {
            client.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(fileName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
