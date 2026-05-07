package monitoring.bd;

import monitoring.logs.SentinelLogs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class BD {
    private static final String URL = "jdbc:sqlite:sentinelmon.db";

    public static Connection getConnection() throws SQLException{
        return DriverManager.getConnection(URL);
    }
    public static void init(){
        try(Connection con = getConnection(); Statement stmt = con.createStatement()){
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS plugins (
                    id TEXT PRIMARY KEY,
                    version TEXT,
                    descripcion TEXT,
                    proveedor TEXT,
                    jar_path TEXT
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS settings (
                    clave TEXT PRIMARY KEY,
                    valor TEXT
                )
            """);
        } catch (SQLException e){
            SentinelLogs.error("Error iniciando BD "+e.getMessage());
        }
    }
}
