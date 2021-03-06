package com.alibaba.druid.pool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.alibaba.druid.util.JdbcUtils;

public class TestRollBack extends TestCase {

    private DruidDataSource dataSource;

    protected void setUp() throws Exception {

        dataSource = new DruidDataSource();
        dataSource.setUrl("jdbc:mysql://10.20.144.27/druid?useUnicode=true&characterEncoding=UTF-8");
        dataSource.setUsername("dragoon_test");
        dataSource.setPassword("dragoon_test");
        dataSource.setFilters("stat,trace,encoding");
        dataSource.setDefaultAutoCommit(false);

        createTable();
    }

    protected void tearDown() throws Exception {
        dropTable();
        dataSource.close();
    }

    public void test_druid() throws Exception {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();

            insert(conn, 1, "abc");
            insert(conn, 2, "1234567");
        } catch (Exception e) {
            conn.rollback();
        } finally {
            JdbcUtils.close(conn);
        }

        Assert.assertEquals(0, count());
    }

    public int count() throws Exception {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) FROM T_0");
            rs.next();
            return rs.getInt(1);
        } finally {
            JdbcUtils.close(rs);
            JdbcUtils.close(stmt);
            JdbcUtils.close(conn);
        }
    }

    private void insert(Connection conn, int id, String value) throws Exception {
        String sql = "insert into T_0 (ID, NAME) VALUES (?, ?)";

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.setString(2, value);
            stmt.execute();
        } finally {
            JdbcUtils.close(stmt);
        }
    }

    private void dropTable() throws Exception {
        String ddl = "DROP TABLE T_0";
        Connection conn = dataSource.getConnection();

        Statement stmt = conn.createStatement();
        stmt.execute(ddl);
        stmt.close();

        conn.close();
    }

    private void createTable() throws Exception {
        String ddl = "CREATE TABLE T_0 (ID INT PRIMARY KEY, NAME VARCHAR(5))";
        Connection conn = dataSource.getConnection();

        Statement stmt = conn.createStatement();
        stmt.execute(ddl);
        stmt.close();

        conn.close();
    }

}
