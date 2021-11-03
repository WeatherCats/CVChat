package org.cubeville.cvchat;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class DaoBase
{
    String dbUser;
    String dbPassword;
    String dbDatabase;

    Connection connection;

    public DaoBase(String dbUser, String dbPassword, String dbDatabase) {
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.dbDatabase = dbDatabase;
    }

    protected Connection getConnection() {
        if(connection != null) {
            try {
                Statement statement = connection.createStatement();
                statement.execute("SELECT 1");
                return connection;
            }
            catch(SQLException e) {
                try { connection.close(); } catch(Exception ed) {}
            }
        }
        openConnection();
        return connection;
    }

    private void openConnection() {
        Properties info = new Properties();
        info.put("autoReconnect", "false");
        info.put("user", dbUser);
        info.put("password", dbPassword);
        info.put("useUnicode", "true");
        info.put("characterEncoding", "utf8");
        String url = "jdbc:mysql://localhost:3306/" + dbDatabase + "?useSSL=false";

        System.out.println("Connecting to database " + url + " with user = " + dbUser + " and password " +  dbPassword);
        
        connection = null;
        try {
            connection = DriverManager.getConnection(url, info);
        }
        catch(SQLException e) {
            System.out.println("Can't open SQL connection!");
            throw new RuntimeException("SQL connection error!");
        }
    }

    
}
