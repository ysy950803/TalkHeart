package com.ysy.talkheart.utils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Shengyu Yao on 2016/11/29.
 */

public class UpdateChecker {

    private static final String dbDriver = "com.mysql.jdbc.Driver";
    private static final String dbUrl = "jdbc:mysql://IP:3306/dbName";
    private static final String dbUser = "root";
    private static final String dbPass = "123456";

    private Connection conn = null;
    private ResultSet rs = null;
    private Statement st = null;
    private PreparedStatement pst = null;
    private CallableStatement cst = null;

    public Connection getConn() {
        try {
            Class.forName(dbDriver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPass); // 注意是三个参数
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public void closeConn() {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (pst != null) {
            try {
                pst.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (cst != null) {
            try {
                cst.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {  // 关闭连接对象
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public int codeSelect(String sql) {
        int code = -1;
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                code = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return code;
    }

    public String downloadUrlSelect(String sql) {
        String url = "";
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                url = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return url;
    }
}
