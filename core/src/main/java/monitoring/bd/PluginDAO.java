package monitoring.bd;

import monitoring.logs.SentinelLogs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// clase DAO para acceder a la tabla plugins de la BD
// nos permite guardar, borrar y listar plugins instalados
public class PluginDAO {

    // guarda un plugin en la BD
    // si ya existe lo actualiza
    public static void guardar(String id, String version, String descripcion, String proveedor, String jarPath) {
        String sql = "INSERT OR REPLACE INTO plugins (id, version, descripcion, proveedor, jar_path) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = BD.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.setString(2, version);
            ps.setString(3, descripcion);
            ps.setString(4, proveedor);
            ps.setString(5, jarPath);
            ps.executeUpdate();

            SentinelLogs.info("Plugin guardado en BD: " + id);
        } catch (SQLException e) {
            SentinelLogs.error("Error guardando plugin en BD: " + e.getMessage());
        }
    }

    // elimina un plugin de la BD por su id
    public static void eliminar(String id) {
        String sql = "DELETE FROM plugins WHERE id = ?";
        try (Connection con = BD.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.executeUpdate();

            SentinelLogs.info("Plugin eliminado de BD: " + id);
        } catch (SQLException e) {
            SentinelLogs.error("Error eliminando plugin de BD: " + e.getMessage());
        }
    }

    // comprueba si un plugin existe en la BD
    public static boolean existe(String id) {
        String sql = "SELECT COUNT(*) FROM plugins WHERE id = ?";
        try (Connection con = BD.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            SentinelLogs.error("Error comprobando plugin en BD: " + e.getMessage());
        }
        return false;
    }

    // devuelve todos los plugins guardados en la BD
    // cada fila es un PluginBD con los datos que guardamos
    public static List<PluginBD> listarTodos() {
        List<PluginBD> lista = new ArrayList<>();
        String sql = "SELECT id, version, descripcion, proveedor, jar_path FROM plugins";
        try (Connection con = BD.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PluginBD plugin = new PluginBD(
                        rs.getString("id"),
                        rs.getString("version"),
                        rs.getString("descripcion"),
                        rs.getString("proveedor"),
                        rs.getString("jar_path"));
                lista.add(plugin);
            }
        } catch (SQLException e) {
            SentinelLogs.error("Error listando plugins de BD: " + e.getMessage());
        }
        return lista;
    }

    // record para almacenar los datos de un plugin de la BD
    public record PluginBD(
            String id,
            String version,
            String descripcion,
            String proveedor,
            String jarPath) {

        // pasa de "driver-update" a "Driver Update" para mostrar en la tienda
        public String nombreTienda() {
            return normalizar(id.replace("-", " "));
        }

        private String normalizar(String texto) {
            if (texto == null || texto.isBlank()) {
                return texto;
            }
            StringBuilder sb = new StringBuilder();
            for (String temp : texto.split(" ")) {
                if (!temp.isEmpty()) {
                    sb.append(Character.toUpperCase(temp.charAt(0)))
                            .append(temp.substring(1).toLowerCase())
                            .append(" ");
                }
            }
            return sb.toString().trim();
        }
    }
}
