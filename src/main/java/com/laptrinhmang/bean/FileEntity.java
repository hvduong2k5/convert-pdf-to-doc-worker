package com.laptrinhmang.bean;

import java.time.LocalDateTime;

public class FileEntity {
    private int id;
    private int userId;
    private String name;
    private String link_pdf;
    private String link_doc;
    private long size;
    private Status status;
    private String created_at;

    public FileEntity(){}
    public FileEntity(int id, int userId, String name, String linkPdf, String linkDoc, long size, Status status, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.link_pdf = linkPdf;
        this.link_doc = linkDoc;
        this.size = size;
        this.status = status;
        this.created_at = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink_pdf() {
        return link_pdf;
    }

    public void setLink_pdf(String link_pdf) {
        this.link_pdf = link_pdf;
    }

    public String getLink_doc() {
        return link_doc;
    }

    public void setLink_doc(String link_doc) {
        this.link_doc = link_doc;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
