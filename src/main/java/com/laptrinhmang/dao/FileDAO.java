package com.laptrinhmang.dao;

import com.laptrinhmang.bean.Status;
import com.laptrinhmang.bean.FileEntity;
import com.laptrinhmang.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FileDAO {
    private Connection con = null;

    public FileDAO() {
        this.con = DBUtil.getConnection();
    }

    public List<FileEntity> getAllFiles(int user_id) {
        String sql = "SELECT * FROM files WHERE user_id = ?";
        List<FileEntity> ListForUser = new ArrayList<>();
        try(
                PreparedStatement ps = con.prepareStatement(sql)
        ){
            ps.setInt(1, user_id);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                ListForUser.add(new FileEntity(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("link_pdf"),
                        rs.getString("link_doc"),
                        rs.getLong("size"),
                        Status.valueOf(rs.getString("status")),
                        rs.getObject("created_at", java.time.LocalDateTime.class).toString()
                ));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ListForUser;
    }

    public FileEntity getFileById(int file_id) {
        String sql = "SELECT * FROM files WHERE id = ?";
        FileEntity fileE = null;
        try(
                PreparedStatement ps = con.prepareStatement(sql)
        ){
            ps.setInt(1, file_id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                fileE = new FileEntity(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("link_pdf"),
                        rs.getString("link_doc"),
                        rs.getLong("size"),
                        Status.valueOf(rs.getString("status")),
                        rs.getObject("created_at", java.time.LocalDateTime.class).toString()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileE;
    }

    public int addFile(FileEntity file) {
        int id = -1;
        String sql = "INSERT INTO files (user_id, name, link_pdf, link_doc, size, status) VALUES (?,?,?,?,?,?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, file.getUserId());
            ps.setString(2, file.getName());
            ps.setString(3, file.getLink_pdf());
            ps.setString(4, file.getLink_doc());
            ps.setLong(5, file.getSize());
            ps.setString(6, file.getStatus().name());

            int row = ps.executeUpdate();

            if (row > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        id = rs.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    public int migrateFile(int old_id, int new_id) {
        String sql = "UPDATE files SET user_id = ? WHERE user_id = ? ";
        try(
                PreparedStatement ps = con.prepareStatement(sql)
        ){
            ps.setInt(1, new_id);
            ps.setInt(2, old_id);

            int row = ps.executeUpdate();
            return row;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean updateFile (FileEntity file) {
        String sql = "UPDATE files SET user_id = ?, name = ?, link_pdf = ?, link_doc = ?, size = ?, status = ? WHERE id = ? ";
        try(
                PreparedStatement ps = con.prepareStatement(sql);
        ){
            ps.setInt(1, file.getUserId());
            ps.setString(2, file.getName());
            ps.setString(3, file.getLink_pdf());
            ps.setString(4, file.getLink_doc());
            ps.setLong(5, file.getSize());
            ps.setString(6, file.getStatus().name());
            ps.setInt(7, file.getId());

            int row = ps.executeUpdate();

            return row > 0;

        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStatusFile(Status status, int id) {
        String sql = "UPDATE files SET status = ? WHERE id = ? ";
        try(
                PreparedStatement ps = con.prepareStatement(sql)
        ){
            ps.setString(1, status.toString());
            ps.setInt(2, id);

            int row = ps.executeUpdate();

            return (row>0);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
