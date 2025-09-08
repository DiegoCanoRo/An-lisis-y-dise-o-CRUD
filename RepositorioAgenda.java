
package comdiegocano.agendas;

import java.sql.*;
import java.util.ArrayList;

public class RepositorioAgenda {

    private static final String URL = "jdbc:mariadb://localhost:3307/agenda";
    private static final String USER = "usuario1";
    private static final String PASSWORD = "superpassword";

    /**
     * Inserta una nueva persona en la base de datos.
     *
     * @param nombre nombre de la persona
     * @return el ID autogenerado de la persona insertada
     */
    public int insertarPersona(String nombre) throws SQLException {
        String sql = "INSERT INTO Personas (nombre) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    /**
     * Inserta un nuevo teléfono asociado a una persona.
     *
     * @param personaId identificador de la persona
     * @param telefono número telefónico
     */
    public void insertarTelefono(int personaId, String telefono) throws SQLException {
        String sql = "INSERT INTO Telefonos (personaId, telefono) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, personaId);
            ps.setString(2, telefono);
            ps.executeUpdate();
        }
    }

    /**
     * Inserta una nueva dirección en la base de datos.
     *
     * @param direccion texto de la dirección
     * @return el ID autogenerado de la dirección insertada
     */
    public int insertarDireccion(String direccion) throws SQLException {
        String sql = "INSERT INTO Direcciones (direccion) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, direccion);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    /**
     * Inserta la relación entre una persona y una dirección.
     *
     * @param personaId identificador de la persona
     * @param direccionId identificador de la dirección
     */
    public void relacionarDireccion(int personaId, int direccionId) throws SQLException {
        String sql = "INSERT INTO PersonaDireccion (personaId, direccionId) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, personaId);
            ps.setInt(2, direccionId);
            ps.executeUpdate();
        }
    }

    /**
     * Consulta todas las personas registradas en la base de datos junto con sus teléfonos y direcciones.
     *
     * @return un ArrayList de objetos Persona con sus datos completos
     */
    public ArrayList<Persona> consultarPersonas() throws SQLException {
        ArrayList<Persona> personas = new ArrayList<>();

        String sqlPersonas = "SELECT id, nombre FROM Personas";
        String sqlTelefonos = "SELECT telefono FROM Telefonos WHERE personaId = ?";
        String sqlDirecciones = "SELECT d.direccion FROM Direcciones d "
                + "INNER JOIN PersonaDireccion pd ON d.id = pd.direccionId "
                + "WHERE pd.personaId = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlPersonas)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                Persona p = new Persona(id, nombre);

                try (PreparedStatement psTel = conn.prepareStatement(sqlTelefonos)) {
                    psTel.setInt(1, id);
                    ResultSet rsTel = psTel.executeQuery();
                    while (rsTel.next()) {
                        p.agregarTelefono(rsTel.getString("telefono"));
                    }
                }

                try (PreparedStatement psDir = conn.prepareStatement(sqlDirecciones)) {
                    psDir.setInt(1, id);
                    ResultSet rsDir = psDir.executeQuery();
                    while (rsDir.next()) {
                        p.agregarDireccion(rsDir.getString("direccion"));
                    }
                }

                personas.add(p);
            }
        }
        return personas;
    }

    /**
     * Obtiene todas las direcciones asociadas a una persona.
     *
     * @param personaId identificador de la persona
     * @return lista de direcciones
     */
    public ArrayList<String> obtenerDirecciones(int personaId) throws SQLException {
        ArrayList<String> direcciones = new ArrayList<>();
        String sql = "SELECT d.direccion FROM Direcciones d "
                + "INNER JOIN PersonaDireccion pd ON d.id = pd.direccionId "
                + "WHERE pd.personaId = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, personaId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                direcciones.add(rs.getString("direccion"));
            }
        }
        return direcciones;
    }

    /**
     * Modifica los datos de una persona (nombre y direcciones).
     *
     * @param id identificador de la persona
     * @param nuevoNombre nuevo nombre
     * @param nuevasDirecciones lista de nuevas direcciones
     */
    public void modificarPersona(int id, String nuevoNombre, ArrayList<String> nuevasDirecciones) throws SQLException {
        String sqlUpdatePersona = "UPDATE Personas SET nombre = ? WHERE id = ?";
        String sqlBorrarRelaciones = "DELETE FROM PersonaDireccion WHERE personaId = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement psUpdate = conn.prepareStatement(sqlUpdatePersona);
             PreparedStatement psBorrar = conn.prepareStatement(sqlBorrarRelaciones)) {

            psUpdate.setString(1, nuevoNombre);
            psUpdate.setInt(2, id);
            psUpdate.executeUpdate();

            psBorrar.setInt(1, id);
            psBorrar.executeUpdate();

            for (String dir : nuevasDirecciones) {
                int dirId = insertarDireccion(dir);
                relacionarDireccion(id, dirId);
            }
        }
    }

    /**
     * Elimina una persona junto con sus teléfonos.
     *
     * @param id identificador de la persona
     */
    public void eliminarPersona(int id) throws SQLException {
        String sqlTelefonos = "DELETE FROM Telefonos WHERE personaId = ?";
        String sqlPersona = "DELETE FROM Personas WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement psTel = conn.prepareStatement(sqlTelefonos);
             PreparedStatement psPer = conn.prepareStatement(sqlPersona)) {

            psTel.setInt(1, id);
            psTel.executeUpdate();

            psPer.setInt(1, id);
            psPer.executeUpdate();
        }
    }

    /**
     * Verifica si existe una persona en la base de datos.
     *
     * @param id identificador de la persona
     * @return true si existe, false si no
     */
    public boolean existePersona(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Personas WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    /**
     * Modifica los teléfonos de una persona, reemplazando los existentes.
     *
     * @param personaId identificador de la persona
     * @param nuevosTelefonos lista de nuevos teléfonos
     */
    public void modificarTelefonos(int personaId, ArrayList<String> nuevosTelefonos) throws SQLException {
        String sqlBorrar = "DELETE FROM Telefonos WHERE personaId = ?";
        String sqlInsertar = "INSERT INTO Telefonos (personaId, telefono) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement psBorrar = conn.prepareStatement(sqlBorrar);
             PreparedStatement psInsertar = conn.prepareStatement(sqlInsertar)) {

            psBorrar.setInt(1, personaId);
            psBorrar.executeUpdate();

            for (String tel : nuevosTelefonos) {
                psInsertar.setInt(1, personaId);
                psInsertar.setString(2, tel);
                psInsertar.executeUpdate();
            }
        }
    }
}
