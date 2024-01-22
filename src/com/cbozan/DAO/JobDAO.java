package com.cbozan.DAO;

import com.cbozan.Entity.Employer;
import com.cbozan.Entity.Job;
import com.cbozan.Entity.Worker;
import com.cbozan.Exception.EntityException;

import javax.swing.*;
import javax.swing.plaf.nimbus.State;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executor;

public class JobDAO {
    private HashMap<Integer, Job> cache = new HashMap<>();
    private boolean usingCache = true;

    private JobDAO(){list();}

    public Job findById(int id){
        if(!usingCache)
            list(); // Nếu cache đang không được sử dụng thì ta gọi cái list để khởi tạo một cái cache mới
        if(cache.containsKey(id)){
            return cache.get(id);
        }
        return null;
    }

    public void refresh(){
        setUsingCache(false);
        list();
        setUsingCache(true);
    }

    public List<Job> list(Employer employer){
        List<Job> listJob  = new ArrayList<>();

        Connection conn;
        Statement st;
        ResultSet rs;
        String query = "select * from job where employer_id=" + employer.getId()+";";

        try {
            conn = DB.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery(query);

            Job job;
            Job.JobBuilder builder;

            while (rs.next()){
                builder = new Job.JobBuilder();
                builder.setId(rs.getInt("id"));
                builder.setEmployer_id(rs.getInt("employer_id"));
                builder.setPrice_id(rs.getInt("price_id"));
                builder.setTitle(rs.getString("title"));
                builder.setDescription(rs.getString("description"));
                builder.setDate(rs.getTimestamp("date"));

                try{
                    job = builder.build();
                    listJob.add(job);
                    cache.put(job.getId(), job);
                }catch(EntityException e){
                    showEntityException(e, "Job adding error");
                }
            }
        }catch(SQLException e){
            showSQLException(e);
        }

        return listJob;
    }
    public List<Job> list(){
        List<Job> list = new ArrayList<>();

        if(!cache.isEmpty() && usingCache){
            for(Map.Entry<Integer, Job> obj : cache.entrySet()){
                list.add(obj.getValue());
            }
            return list;
        }

        cache.clear();

        Connection conn;
        Statement st;
        ResultSet rs;
        String query = "select * from job;";

        try{
            conn = DB.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery(query);

            Job.JobBuilder builder;
            Job job;

            while(rs.next()){
                builder = new Job.JobBuilder();
                builder.setId(rs.getInt("id"));
                builder.setEmployer_id(rs.getInt("employer_id"));
                builder.setPrice_id(rs.getInt("price_id"));
                builder.setTitle(rs.getString("title"));
                builder.setDescription(rs.getString("description"));
                builder.setDate(rs.getTimestamp("date"));

                try{
                    job = builder.build();
                    list.add(job);
                    cache.put(job.getId(), job);
                }catch(EntityException e){
                    showEntityException(e,"Id: " + rs.getInt("id") + ", Title: " + rs.getString("title"));
                }

            }
        }catch(SQLException e){
            showSQLException(e);
        }
        return list;
    }


    //CREATE------------------------------------------------------------------------------------
    public boolean create(Job job){
        if(!createControl(job))
            return false;

        cache.clear();

        Connection conn;
        PreparedStatement pst;
        String query = "insert into job (employer_id, price_id, title, description) values (?,?,?,?);";
        int rs = 0;

        try{
            conn = DB.getConnection();
            pst = conn.prepareStatement(query);
            pst.setInt(1, job.getEmployer().getId());
            pst.setInt(2, job.getPrice().getId());
            pst.setString(3, job.getTitle());
            pst.setString(4, job.getDescription());
            rs = pst.executeUpdate();

            if(rs!=0){
                String query1 = "select * from job order by id desc limit 1;";
                ResultSet resultSet = conn.createStatement().executeQuery(query1);
                while(resultSet.next()){
                    Job.JobBuilder builder = new Job.JobBuilder();
                    builder.setId(resultSet.getInt("id"));
                    builder.setId(resultSet.getInt("id"));
                    builder.setEmployer_id(resultSet.getInt("employer_id"));
                    builder.setPrice_id(resultSet.getInt("price_id"));
                    builder.setTitle(resultSet.getString("title"));
                    builder.setDescription(resultSet.getString("description"));
                    builder.setDate(resultSet.getTimestamp("date"));

                    try{
                        Job obj = builder.build();
                        cache.put(obj.getId(), obj);
                    }catch(EntityException e){
                        showEntityException(e, "ID : " + resultSet.getInt("id") + " Title : " + resultSet.getString("title"));
                    }
                }
            }
        }catch(SQLException e){
            showSQLException(e);
        }

        return rs > 0;
    }
    private boolean createControl(Job job){
        for(Map.Entry<Integer, Job> obj : cache.entrySet()) {
            if(obj.getValue().getTitle().equals(job.getTitle()) && obj.getValue().getId() != job.getId()) {
                DB.ERROR_MESSAGE = obj.getValue().getTitle() + " have already existed!";
                return false;
            }
        }
        return true;
    }


    //UPDATE--------------------------------------------------------------------------------------------
    public boolean update(Job job){
        if(!updateControl(job))
            return false;

        Connection connection;
        PreparedStatement pst;
        int rs = 0;
        String query = "update job set employer_id=?, price_id=?, title=?, description=? where id=?";

        try{
            connection = DB.getConnection();
            pst = connection.prepareStatement(query);
            pst.setInt(1, job.getEmployer().getId());
            pst.setInt(2, job.getPrice().getId());
            pst.setString(3, job.getTitle());
            pst.setString(4, job.getDescription());
            pst.setInt(5, job.getId());

            rs = pst.executeUpdate();
            if(rs != 0){
                cache.put(job.getId(), job);
            }

        }catch(SQLException e){
            showSQLException(e);
        }

        return rs > 0;
    }
    private boolean updateControl(Job job){
        for(Map.Entry<Integer, Job> obj : cache.entrySet()){
            if(obj.getValue().getTitle().equals(job.getTitle())){
                DB.ERROR_MESSAGE = job.getTitle() + " have already existed!";
                return false;
            }
        }
        return true;
    }


    //DELETE--------------------------------------------------------------------------------------------
    public boolean delete(Job job){
        Connection connection;
        Statement st;
        String query="delete from job where id=" + job.getId() + ";";
        int result=0;

        try{
            connection = DB.getConnection();
            st = connection.createStatement();
            result = st.executeUpdate(query);
            if(result != 0){
                cache.remove(job.getId());
            }
        }catch(SQLException e){
            showSQLException(e);
        }

        return result > 0;
    }

    //INSTANCE---------------------------------------------------------------------------------------------
    private static class JobDAOHelper {
        private static final JobDAO instance = new JobDAO();
    }

    public static JobDAO getInstance() {
        return JobDAOHelper.instance;
    }


    //SET USING CACHE------------------------------------------------------------------------------------
    public boolean isUsingCache(){
        return usingCache;
    }
    private void setUsingCache(boolean usingCache){
        this.usingCache = usingCache;
    }


    //EXCEPTION------------------------------------------------------------------------------------
    private void showSQLException(SQLException e){
        String message = e.getErrorCode()
                + "\n" + e.getMessage()
                + "\n" + e.getLocalizedMessage()
                + "\n" + e.getCause();
        JOptionPane.showMessageDialog(null, message);
    }
    private void showEntityException(EntityException e, String msg){
        String message = msg + " not add "
                + "\n" + e.getMessage()
                + "\n" + e.getLocalizedMessage()
                + "\n" + e.getCause();
        JOptionPane.showMessageDialog(null, message);
    }
}
