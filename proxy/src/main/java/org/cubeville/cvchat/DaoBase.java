package org.cubeville.cvchat;

import java.util.Properties;

import java.sql.Connection;
import java.sql.DriverManager;
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
                try { connection.close(); } catch(Exception ignored) {}
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

        connection = null;
        try {
            connection = DriverManager.getConnection(url, info);
        }
        catch(SQLException e) {
            System.out.println("Can't open SQL connection to " + url);
            throw new RuntimeException("SQL connection error!");
        }
    }

    
}
