package com.ysy.talkheart.utils;

/**
 * Created by Shengyu Yao on 2016/11/19.
 */

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DBProcessor {

    private static final String dbDriver = "com.mysql.jdbc.Driver";
    private static final String dbUrl = "jdbc:mysql://[IP]:3306/dbName";
    private static final String dbUser = "root";
    private static final String dbPass = "******";

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

    public String uidSelect(String sql) {
        String uid = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                uid = rs.getInt(1) + "";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return uid;
    }

    public String[] relationSelect(String sql) {
        String[] relation = new String[3];
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                relation[0] = rs.getInt(1) + "";
                relation[1] = rs.getInt(2) + "";
                relation[2] = rs.getInt(3) + "";
            }
        } catch (SQLException e) {
            relation[0] = "-2";
            e.printStackTrace();
        }
        return relation;
    }

    public int insert(String sql) {
        int i = 0;
        try {
            pst = conn.prepareStatement(sql);
            i = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("Duplicate"))
                i = -1;
        }
        return i; // 返回影响的行数，1为执行成功
    }

    public int delete(String sql) {
        int i = 0;
        try {
            st = conn.createStatement();
            i = st.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return i; // 如果返回的是1，则执行成功;
    }

    public int update(String sql) {
        int i = 0;
        try {
            pst = conn.prepareStatement(sql);
            i = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return i; // 返回影响的行数，1为执行成功
    }

    public int goodUpdate(String sql, String sql2) {
        int i = 0;
        try {
            pst = conn.prepareStatement(sql);
            i = pst.executeUpdate();
            pst = conn.prepareStatement(sql2);
            i = pst.executeUpdate() + i;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return i;
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

    public String[] loginSelect(String sql) { // select pw from user where username = ''
        String[] uid_pw = new String[2];
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                uid_pw[0] = rs.getInt(1) + "";
                uid_pw[1] = rs.getString(2) + "";
            }
        } catch (SQLException e) {
            uid_pw = null;
            e.printStackTrace();
        }
        return uid_pw;
    }

    public int rowSelect(String sql) {
        int row = -1;
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                row = rs.getRow() - 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return row; // -1~n-1
    }

    public List<List<String>> activeSelect(String sql) {
        List<List<String>> resList = new ArrayList<>();
        List<String> actid_col = new ArrayList<>();
        List<String> sendtime_col = new ArrayList<>();
        List<String> goodnum_col = new ArrayList<>();
        List<String> content_col = new ArrayList<>();
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                actid_col.add(rs.getInt(1) + ""); // actid
                sendtime_col.add(rs.getString(2)); // sendtime
                goodnum_col.add(rs.getInt(3) + ""); // goodnum
                content_col.add(rs.getString(4)); // content
            }
            resList.add(actid_col);
            resList.add(sendtime_col);
            resList.add(goodnum_col);
            resList.add(content_col);
        } catch (SQLException e) {
            resList = null;
            e.printStackTrace();
        }
        return resList;
    }

    public List<List<String>> homeActiveSelect(String sql) {
        List<List<String>> resList = new ArrayList<>();
        List<String> actid_col = new ArrayList<>();
        List<String> sex_col = new ArrayList<>();
        List<String> nickname_col = new ArrayList<>();
        List<String> sendtime_col = new ArrayList<>();
        List<String> content_col = new ArrayList<>();
        List<String> goodnum_col = new ArrayList<>();
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                actid_col.add(rs.getInt(1) + ""); // actid
                sex_col.add(rs.getInt(2) + ""); // sex
                nickname_col.add(rs.getString(3)); // nickname
                sendtime_col.add(rs.getString(4)); // sendtime
                content_col.add(rs.getString(5)); // content
                goodnum_col.add(rs.getInt(6) + ""); // goodnum
            }
            resList.add(actid_col);
            resList.add(sex_col);
            resList.add(nickname_col);
            resList.add(sendtime_col);
            resList.add(content_col);
            resList.add(goodnum_col);
        } catch (SQLException e) {
            resList = null;
            e.printStackTrace();
        }
        return resList;
    }

    public List<List<String>> goodSelect(String sql) {
        List<List<String>> resList = new ArrayList<>();
        List<String> actid_col = new ArrayList<>();
        List<String> isfav_col = new ArrayList<>();
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                actid_col.add(rs.getInt(1) + ""); // actid
                isfav_col.add(rs.getInt(2) + ""); // isfav
            }
            resList.add(actid_col);
            resList.add(isfav_col);
        } catch (SQLException e) {
            resList = null;
            e.printStackTrace();
        }
        return resList;
    }

    public String[] meInfoSelect(String sql) {
        String[] meInfo = new String[6];
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                meInfo[0] = rs.getInt(1) + ""; // sex
                meInfo[1] = rs.getString(2); // nickname
                meInfo[2] = rs.getString(3); // intro
                meInfo[3] = rs.getInt(4) + ""; // active_num
                meInfo[4] = rs.getInt(5) + ""; // watch_num
                meInfo[5] = rs.getInt(6) + ""; // fans_num
            }
        } catch (SQLException e) {
            meInfo[1] = "/(ㄒoㄒ)/~~";
            e.printStackTrace();
        }
        return meInfo;
    }

    public List<List<String>> searchUserSelect(String sql) {
        List<List<String>> resList = new ArrayList<>();
        List<String> nickname_col = new ArrayList<>();
        List<String> sex_col = new ArrayList<>();
        List<String> intro_col = new ArrayList<>();
        List<String> uid_col = new ArrayList<>();

        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                nickname_col.add(rs.getString(1));
                sex_col.add(rs.getInt(2) + "");
                intro_col.add(rs.getString(3));
                uid_col.add(rs.getInt(4) + "");
            }
            resList.add(nickname_col);
            resList.add(sex_col);
            resList.add(intro_col);
            resList.add(uid_col);
        } catch (SQLException e) {
            resList = null;
            e.printStackTrace();
        }
        return resList;
    }

    public List<List<String>> draftSelect(String sql) {
        List<List<String>> resList = new ArrayList<>();
        List<String> dftid_col = new ArrayList<>();
        List<String> savetime_col = new ArrayList<>();
        List<String> content_col = new ArrayList<>();
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                dftid_col.add(rs.getInt(1) + "");
                savetime_col.add(rs.getString(2));
                content_col.add(rs.getString(3));
            }
            resList.add(dftid_col);
            resList.add(savetime_col);
            resList.add(content_col);
        } catch (SQLException e) {
            resList = null;
            e.printStackTrace();
        }
        return resList;
    }

    public List<List<String>> markSelect(String sql) {
        List<List<String>> resList = new ArrayList<>();
        List<String> sex_col = new ArrayList<>();
        List<String> time_col = new ArrayList<>();
        List<String> nickname_col = new ArrayList<>();
        List<String> content_col = new ArrayList<>();
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                sex_col.add(rs.getInt(1) + "");
                time_col.add(rs.getString(2));
                nickname_col.add(rs.getString(3));
                content_col.add(rs.getString(4));
            }
            resList.add(sex_col);
            resList.add(time_col);
            resList.add(nickname_col);
            resList.add(content_col);
        } catch (SQLException e) {
            resList = null;
            e.printStackTrace();
        }
        return resList;
    }

    public String[] personInfoSelect(String sql, boolean isFromMe) {
        if (isFromMe) {
            String[] personInfo = new String[2];
            try {
                st = conn.createStatement();
                rs = st.executeQuery(sql);
                while (rs.next()) {
                    personInfo[0] = rs.getString(1); // school
                    personInfo[1] = rs.getString(2); // birthday
                }
            } catch (SQLException e) {
                personInfo[1] = "/(ㄒoㄒ)/~~";
                e.printStackTrace();
            }
            return personInfo;
        } else {
            String[] personInfo = new String[6];
            try {
                st = conn.createStatement();
                rs = st.executeQuery(sql);
                while (rs.next()) {
                    personInfo[0] = rs.getString(1); // school
                    personInfo[1] = rs.getString(2); // birthday
                    personInfo[2] = rs.getString(3); // intro
                    personInfo[3] = rs.getInt(4) + ""; // act_num
                    personInfo[4] = rs.getInt(5) + ""; // watch_num
                    personInfo[5] = rs.getInt(6) + ""; // fans_num
                }
            } catch (SQLException e) {
                personInfo[1] = "/(ㄒoㄒ)/~~";
                e.printStackTrace();
            }
            return personInfo;
        }
    }

    public List<List<String>> watchSelect(String sql, String sql2) {
        List<List<String>> resList = new ArrayList<>();
        List<String> uid_col = new ArrayList<>();
        List<String> sex_col = new ArrayList<>();
        List<String> nickname_col = new ArrayList<>();
        List<String> intro_col = new ArrayList<>();
        List<String> relation_col = new ArrayList<>();
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                if (rs.getInt(5) != -1 && rs.getInt(5) != 0) {
                    uid_col.add(rs.getInt(1) + "");
                    sex_col.add(rs.getInt(2) + "");
                    nickname_col.add(rs.getString(3));
                    intro_col.add(rs.getString(4));
                    relation_col.add(rs.getInt(5) + "");
                }
            }

            st = conn.createStatement();
            rs = st.executeQuery(sql2);
            while (rs.next()) {
                if (rs.getInt(5) != 1 && rs.getInt(5) != 0) {
                    uid_col.add(rs.getInt(1) + "");
                    sex_col.add(rs.getInt(2) + "");
                    nickname_col.add(rs.getString(3));
                    intro_col.add(rs.getString(4));
                    relation_col.add(rs.getInt(5) == 2 ? "2" : "1");
                }
            }

            resList.add(uid_col);
            resList.add(sex_col);
            resList.add(nickname_col);
            resList.add(intro_col);
            resList.add(relation_col);
        } catch (SQLException e) {
            resList = null;
            e.printStackTrace();
        }
        return resList;
    }

    public List<List<String>> fansSelect(String sql, String sql2) {
        List<List<String>> resList = new ArrayList<>();
        List<String> uid_col = new ArrayList<>();
        List<String> sex_col = new ArrayList<>();
        List<String> nickname_col = new ArrayList<>();
        List<String> intro_col = new ArrayList<>();
        List<String> relation_col = new ArrayList<>();
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                if (rs.getInt(5) != 1 && rs.getInt(5) != 0) {
                    uid_col.add(rs.getInt(1) + "");
                    sex_col.add(rs.getInt(2) + "");
                    nickname_col.add(rs.getString(3));
                    intro_col.add(rs.getString(4));
                    relation_col.add(rs.getInt(5) + "");
                }
            }

            st = conn.createStatement();
            rs = st.executeQuery(sql2);
            while (rs.next()) {
                if (rs.getInt(5) != -1 && rs.getInt(5) != 0) {
                    uid_col.add(rs.getInt(1) + "");
                    sex_col.add(rs.getInt(2) + "");
                    nickname_col.add(rs.getString(3));
                    intro_col.add(rs.getString(4));
                    relation_col.add(rs.getInt(5) == 2 ? "2" : "-1");
                }
            }

            resList.add(uid_col);
            resList.add(sex_col);
            resList.add(nickname_col);
            resList.add(intro_col);
            resList.add(relation_col);
        } catch (SQLException e) {
            resList = null;
            e.printStackTrace();
        }
        return resList;
    }
}
