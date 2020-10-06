package DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DB {

    private final static String username = "sa";
    private final static String password = "123";
    private final static String database = "DOGADJAJI";
    public final static String serverName = "localhost";
    private final static int port = 1433;

    private final static String connectionString = "jdbc:sqlserver://" + serverName + ":" + port + ";"
            + "database=" + database
            + ";user=" + username
            + ";password=" + password;

    private static DB db = null;

    private Connection connection;

    private DB(){
        try {
            connection = DriverManager.getConnection(connectionString);
        }
        catch(SQLException ex){
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static DB getInstance(){
        if (db == null){
            System.out.println("Connecting to server: " + DB.serverName);
            System.out.println("database: " + DB.database);
            System.out.println("passphrase: " + DB.password);
            db = new DB();
        }
        return db;
    }


    public Connection getConnection(){
        return connection;
    }
}
