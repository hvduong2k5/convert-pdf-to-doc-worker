package com.laptrinhmang.util;

import io.minio.MinioClient;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TemplateUtil {
    private static class DBUtil {
        private static final String DATABASE = "";
        private static final String USERNAME = "";
        private static final String PASSWORD = "";

        private static final String URL = "jdbc:mysql://localhost:3306/" + DATABASE
                + "?useUnicode=true&characterEncoding=UTF-8"
                + "&serverTimezone=UTC&autoReconnect=true";

        public static Connection getConnection() {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                return DriverManager.getConnection(URL, USERNAME, PASSWORD);
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                return null;
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
    }
    private static class MinioUtil {
        private static MinioClient client;
        private static final String ENDPOINT = "http://localhost:9000";
        private static final String ACCESS_KEY = "minioadmin";
        private static final String SECRET_KEY = "minioadmin";



        public static MinioClient getClient() {
            if (client == null) {
                client = MinioClient.builder()
                        .endpoint(ENDPOINT)
                        .credentials(ACCESS_KEY, SECRET_KEY)
                        .build();
            }
            return client;
        }
    }
    private static class RedisUtil {
        private static JedisPool pool;
        private static final String HOST = "localhost";
        private static final int PORT = 6379;
        private static final String QUEUE_NAME = "task_queue";
        private static final String CHANNEL_NAME = "task_channel";

        static {
            try {
                JedisPoolConfig config = new JedisPoolConfig();
                config.setMaxTotal(50);
                config.setMaxIdle(10);
                config.setMinIdle(2);
                config.setBlockWhenExhausted(true);
                pool = new JedisPool(config, HOST, PORT);

            } catch (Exception e) {
                System.err.println("Cannot initialize JedisPool: " + e.getMessage());
            }
        }
        public static String getChannelName() {
            return CHANNEL_NAME;
        }
        public static String getQueueName() {
            return QUEUE_NAME;
        }
        public static JedisPool getPool() {
            return pool;
        }
    }
}
