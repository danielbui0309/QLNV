package com.cbozan.DAO;

import com.cbozan.Entity.Price;
import com.cbozan.Entity.Worktype;
import com.cbozan.Exception.EntityException;

import javax.swing.*;
import javax.xml.transform.Result;
import java.io.PipedReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PriceDAO {
    private HashMap<Integer, Price> cache = new HashMap<>();
    private boolean usingCache = true;
    private PriceDAO() {list();}
    public Price findById(int id){
        if(!usingCache)
            list();
        if(cache.containsKey(id))
            return cache.get(id);
        return null;
    }

    public List<Price> list(){
        List<Price> list = new ArrayList<>();

        if(!cache.isEmpty() && usingCache){
            for (Map.Entry<Integer, Price> obj : cache.entrySet())
                list.add(obj.getValue());
            return list;
        }

        cache.clear();

        Connection connection;
        Statement st;
        ResultSet rs;
        String query = "select * from price;";

        try{
            connection = DB.getConnection();
            st = connection.createStatement();
            rs = st.executeQuery(query);
            Price price;


            while(rs.next()){
                Price.PriceBuilder builder = new Price.PriceBuilder();
                builder.setId(rs.getInt("id"));
                builder.setFulltime(rs.getBigDecimal("fulltime"));
                builder.setHalftime(rs.getBigDecimal("halftime"));
                builder.setOvertime(rs.getBigDecimal("overtime"));
                builder.setDate(rs.getTimestamp("date"));

                try{
                    price = builder.build();
                    list.add(price);
                    cache.put(price.getId(), price);
                }catch (EntityException e){
                    showEntityException(e, "ID : " + rs.getInt("id"));
                }
            }

        }catch (SQLException e){
            showSQLException(e);
        }

        return list;
    }

    //CREATE---------------------------------------------------------------------------------------------
    public boolean create(Price price){
        if(!createControl(price))
            return false;

        Connection connection;
        PreparedStatement pst;
        int result = 0;
        String query = "insert into price (fulltime, halftime, overtime) values (?,?,?);";

        try{
            connection = DB.getConnection();
            pst=connection.prepareStatement(query);
            pst.setBigDecimal(1, price.getFulltime());
            pst.setBigDecimal(2, price.getHalftime());
            pst.setBigDecimal(3, price.getOvertime());

            result = pst.executeUpdate();

            if(result != 0){
                String query1 = "select * from price order by id desc limit 1";
                ResultSet rs = connection.createStatement().executeQuery(query1);
                while (rs.next()){
                    Price.PriceBuilder builder = new Price.PriceBuilder();
                    builder.setId(rs.getInt("id"));
                    builder.setFulltime(rs.getBigDecimal("fulltime"));
                    builder.setHalftime(rs.getBigDecimal("halftime"));
                    builder.setOvertime(rs.getBigDecimal("overtime"));
                    builder.setDate(rs.getTimestamp("date"));

                    try{
                        Price obj = builder.build();
                        cache.put(obj.getId(), obj);
                    }catch(EntityException e){
                        showEntityException(e, "ID : " + rs.getInt("id"));
                    }
                }

            }
        }catch (SQLException e){
        showSQLException(e);
        }

        return result > 0;
    }
    private boolean createControl(Price price){
        for(Map.Entry<Integer, Price> obj : cache.entrySet())
            if(obj.getValue().getFulltime().compareTo(price.getFulltime()) == 0
                    && obj.getValue().getHalftime().compareTo(price.getHalftime()) == 0
                    && obj.getValue().getOvertime().compareTo(price.getOvertime()) == 0){
                DB.ERROR_MESSAGE = "This price information record already exists.";
                return false;
            }
        return true;
    }


    //UPDATE---------------------------------------------------------------------------------------------
    public boolean update(Price price){
        if(!updateControl(price))
            return false;

        Connection connection;
        PreparedStatement pst;
        int rs = 0;
        String query = "update price set fulltime =?, halftime=?, overtime=? where id=?;";

        try{
            connection = DB.getConnection();
            pst = connection.prepareStatement(query);
            pst.setBigDecimal(1, price.getFulltime());
            pst.setBigDecimal(2, price.getHalftime());
            pst.setBigDecimal(3, price.getOvertime());

            rs = pst.executeUpdate();

            if(rs != 0){
                cache.put(price.getId(), price);
            }
        }catch (SQLException e){
            showSQLException(e);
        }

        return rs > 0;
    }
    private boolean updateControl(Price price){
        for(Map.Entry<Integer, Price> obj : cache.entrySet())
            if(obj.getValue().getFulltime().compareTo(price.getFulltime()) == 0
            && obj.getValue().getHalftime().compareTo(price.getHalftime()) == 0
            && obj.getValue().getOvertime().compareTo(price.getOvertime()) == 0){
                DB.ERROR_MESSAGE = "This price information record already exists.";
                return false;
            }
        return true;
    }

    //DELETE---------------------------------------------------------------------------------------------
    public boolean delete(Price price){
        Connection connection;
        Statement st;
        int result = 0;
        String query = "delete from price where id="+price.getId()+";";

        try{
            connection = DB.getConnection();
            st = connection.createStatement();
            result = st.executeUpdate(query);

            if(result != 0){
                cache.remove(price.getId());
            }
        }catch (SQLException e){
            showSQLException(e);
        }

        return result > 0;
    }


    //INSTANCE---------------------------------------------------------------------------------------------
    private static class PriceDAOHelper {
        private static final PriceDAO instance = new PriceDAO();
    }

    public static PriceDAO getInstance() {
        return PriceDAOHelper.instance;
    }


    //SET USING CACHE------------------------------------------------------------------------------------
    public void setUsingCache(boolean usingCache){ this.usingCache = usingCache;}
    public boolean isUsingCache(){return usingCache;}


    //EXCEPTION------------------------------------------------------------------------------------
    private void showEntityException(EntityException e, String msg){
        String message = msg + " not added" +
                "\n" + e.getMessage() + "\n" + e.getLocalizedMessage() + e.getCause();
        JOptionPane.showMessageDialog(null, message);
    }
    private void showSQLException(SQLException e){
        String message = e.getErrorCode() + "\n" + e.getMessage() + "\n" + e.getLocalizedMessage() + "\n" + e.getCause();
        JOptionPane.showMessageDialog(null, message);
    }
}
