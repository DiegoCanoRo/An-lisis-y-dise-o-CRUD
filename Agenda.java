package comdiegocano.agendas;

import java.sql.*;
import java.util.ArrayList;

public class Agenda {

    private static final String URL = "jdbc:mariadb://localhost:3307/agenda";
    private static final String USER = "usuario1";
    private static final String PASSWORD = "superpassword";

    /**
     * Agrega una nueva persona a la base de datos junto con sus números de
     * teléfono.
     *
     * @param nombre
     * @param direccion
     * @param telefonos lista de teléfonos asociados a la persona
     */
    public void agregarPersona(String nombre, String direccion, ArrayList<String> telefonos) {
        //para insertar los datos de la persona
        String sqlPersona = "INSERT INTO Personas (nombre, direccion) VALUES (?, ?)";

        //para insertar los telefonos de la persona
        String sqlTelefono = "INSERT INTO Telefonos (personaId, telefono) VALUES (?, ?)";

        //abre conexion con la base de datos
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD); PreparedStatement psPersona = conn.prepareStatement(sqlPersona, Statement.RETURN_GENERATED_KEYS)) {

            //se les asigna valores a los parametros
            psPersona.setString(1, nombre);
            psPersona.setString(2, direccion);
            //ejecuta la inserción en la tabla personas
            psPersona.executeUpdate();

            //se obtiene el ID automatico para la persona insertada
            ResultSet rs = psPersona.getGeneratedKeys();
            int personaId = 0;
            if (rs.next()) {
                personaId = rs.getInt(1);
            }

            //prepara la sentencia para agregar los números de télefono de la persona
            try (PreparedStatement psTel = conn.prepareStatement(sqlTelefono)) {
                //recorre la lista de télefonos y los agrega a la base de datos
                for (String tel : telefonos) {
                    psTel.setInt(1, personaId);
                    psTel.setString(2, tel);
                    psTel.executeUpdate();
                }
            }

            //System.out.println("Persona agregada con éxito: " + nombre);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Consulta todas las personas registradas en la base de datos junto con sus
     * teléfonos.
     *
     * @return un ArrayList de objetos Persona con sus datos y teléfonos
     */
    public ArrayList<Persona> consultarPersonas() {
        //esta lista almacenara las personas recuperadas de la base de datos
        ArrayList<Persona> personas = new ArrayList<>();

        //consulta sql para obtener a las personas y sus telefonos asignados a su
        //id
        String sqlPersonas = "SELECT * FROM Personas";
        String sqlTelefonos = "SELECT telefono FROM Telefonos WHERE personaId = ?";

        //se abre la conexión a la base de datos y ejecuta la consulta
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlPersonas)) {

            //se recorre el resultado de la consulta de personas
            while (rs.next()) {
                //se obtiene el id de la persona
                int id = rs.getInt("id");

                //se obtiene su nombre
                String nombre = rs.getString("nombre");

                //su dirección
                String direccion = rs.getString("direccion");

                //se crea un objeto Persona con los datos obtnenidos
                Persona p = new Persona(id, nombre, direccion);

                //se prepara la sentencia para obtener los telefonos de esa persona
                try (PreparedStatement psTel = conn.prepareStatement(sqlTelefonos)) {

                    //se le asigna el id al parámetro
                    psTel.setInt(1, id);

                    //ejecuta la consulta de los teléfonos
                    ResultSet rsTel = psTel.executeQuery();

                    //recorre los resultados y agrega los teléfonos al objeto persona
                    while (rsTel.next()) {
                        p.agregarTelefono(rsTel.getString("telefono"));
                    }
                }

                personas.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return personas;
    }

    /**
     * Modifica los datos de una persona
     *
     * @param id el identificador unico de la persona a modificar
     * @param nuevoNombre el nuevo nombre que se asignara a la persona
     * @param nuevaDireccion la nueva dirección que se asignara a la persona
     */
    public void modificarPersona(int id, String nuevoNombre, String nuevaDireccion) {
        //consulta sql para actualizar los datos segun el id de la persona
        String sql = "UPDATE Personas SET nombre = ?, direccion = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD); PreparedStatement ps = conn.prepareStatement(sql)) {

            //asigna los valores a los parametros
            ps.setString(1, nuevoNombre);
            ps.setString(2, nuevaDireccion);
            ps.setInt(3, id);

            //ejecuta la actualización
            ps.executeUpdate();

//            int filas = ps.executeUpdate();
//            if (filas > 0) {
//                System.out.println("Persona modificada con exito ID: " + id);
//            } else {
//                System.out.println("No se encontró persona con ID: " + id);
//            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Elimina una persona de la base de datos junto con sus teléfonos.
     *
     * @param id el identificador único de la persona a eliminar
     */
    public void eliminarPersona(int id) {
        //consulta sql para eliminar los telefonos asociados a una id
        String sqlTelefonos = "DELETE FROM Telefonos WHERE personaId = ?";
        String sqlPersona = "DELETE FROM Personas WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD); PreparedStatement psTel = conn.prepareStatement(sqlTelefonos); PreparedStatement psPer = conn.prepareStatement(sqlPersona)) {

            //se asignan los parametros
            psTel.setInt(1, id);
            //se elimina los telefonos 
            psTel.executeUpdate();

            psPer.setInt(1, id);
            //se ejecuta la eliminación de la persona
            psPer.executeUpdate();
//            int filas = psPer.executeUpdate();
//
//            if (filas > 0) {
//                System.out.println("Persona eliminada con exito ID: " + id);
//            } else {
//                System.out.println("No se encontró persona con ID: " + id);
//            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Verifica si una persona existe en la base de datos según su ID.
     *
     * @param id el identificador único de la persona
     * @return true si la persona existe, false si no
     */
    public boolean existePersona(int id) {
        //consulta sql para contar cuantas personas hay con el id ingresado
        String sql = "SELECT COUNT(*) FROM Personas WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD); PreparedStatement ps = conn.prepareStatement(sql)) {
            //se le asigna el parametro
            ps.setInt(1, id);

            //se ejecuta la consulta y se guarda el resultado
            ResultSet rs = ps.executeQuery();

            //si es mayor a 0 entonces si existe esa persona
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Modifica los teléfonos de una persona en la base de datos, primero
     * elimina los teléfonos antiguos y luego inserta los nuevos.
     *
     * @param personaId el identificador único de la persona
     * @param nuevosTelefonos lista de nuevos teléfonos que se asignarán a la
     * persona
     */
    public void modificarTelefonos(int personaId, ArrayList<String> nuevosTelefonos) {
        String sqlBorrar = "DELETE FROM Telefonos WHERE personaId = ?";
        String sqlInsertar = "INSERT INTO Telefonos (personaId, telefono) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD); PreparedStatement psBorrar = conn.prepareStatement(sqlBorrar); PreparedStatement psInsertar = conn.prepareStatement(sqlInsertar)) {

            // Borrar teléfonos antiguos
            psBorrar.setInt(1, personaId);
            psBorrar.executeUpdate();

            // Insertar los nuevos
            for (String tel : nuevosTelefonos) {
                psInsertar.setInt(1, personaId);
                psInsertar.setString(2, tel);
                psInsertar.executeUpdate();
            }

            System.out.println("Teléfonos modificados con éxito para ID: " + personaId);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
