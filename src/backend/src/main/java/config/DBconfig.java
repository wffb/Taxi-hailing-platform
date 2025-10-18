package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBconfig {

     public static final String DB_URL = "jdbc:postgresql://a.oregon-postgres.render.com:5432/db_90007_hajimi_db_0920?sslmode=require";
     public static final String DB_USER = "db_90007_hajimi_db_0920_user";
     public static final String DB_PASSWORD = "00EE5M35q2ufzvTDusD18SB1YD8qA4Uv";

     static {
          try { Class.forName("org.postgresql.Driver"); } catch (ClassNotFoundException e) { throw new RuntimeException(e); }
     }

     public static Connection getConnection() throws SQLException {
          System.out.println("[DB] URL=" + DB_URL + ", USER=" + DB_USER);
          return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
     }
}
