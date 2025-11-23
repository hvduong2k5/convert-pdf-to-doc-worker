package com.laptrinhmang.service;

import com.laptrinhmang.bean.FileEntity;
import com.laptrinhmang.bean.Status;
import com.laptrinhmang.dao.FileDAO;

public class FileService {
    public boolean updateStatus(FileEntity fileEntity, Status status) {
        return new FileDAO().updateStatusFile(status, fileEntity.getId());
    }
    public boolean update(FileEntity fileEntity) {
        return new FileDAO().updateFile(fileEntity);
    }
}
