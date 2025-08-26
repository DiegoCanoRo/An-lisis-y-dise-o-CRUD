package comdiegocano.agendas;

import java.sql.*;
import java.util.ArrayList;

public class Agenda {

    private static final String URL = "jdbc:mariadb://localhost:3307/agenda";
    private static final String USER = "usuario1";
    private static final String PASSWORD = "superpassword";

   /**
 * Agrega una nueva persona a la base de datos junto con sus números de
 * teléfono y direcciones.
 *
 * @param nombre nombre de la persona
 * @param direcciones lista de direcciones asociadas a la persona
 * @param telefonos lista de teléfonos asociados a la persona
 */
public void agregarPersona(String nombre, ArrayList<String> direcciones, ArrayList<String> telefonos) {
    //consulta sql para insertar una nueva persona (ya no incluye dirección)
    String sqlPersona = "INSERT INTO Personas (nombre) VALUES (?)";

    //para insertar los teléfonos de la persona
    String sqlTelefono = "INSERT INTO Telefonos (personaId, telefono) VALUES (?, ?)";

    //abre conexion con la base de datos
    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
         PreparedStatement psPersona = conn.prepareStatement(sqlPersona, Statement.RETURN_GENERATED_KEYS)) {

        //se asigna el nombre al parámetro
        psPersona.setString(1, nombre);
        //ejecuta la inserción en la tabla personas
        psPersona.executeUpdate();

        //se obtiene el ID autogenerado para la persona insertada
        ResultSet rs = psPersona.getGeneratedKeys();
        int personaId = 0;
        if (rs.next()) {
            personaId = rs.getInt(1);
        }

        //prepara la sentencia para agregar los números de teléfono
        try (PreparedStatement psTel = conn.prepareStatement(sqlTelefono)) {
            for (String tel : telefonos) {
                psTel.setInt(1, personaId);
                psTel.setString(2, tel);
                psTel.executeUpdate();
            }
        }

        //se agregan todas las direcciones usando el método existente
        for (String dir : direcciones) {
            agregarDireccion(personaId, dir);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    /**
     * Agrega una nueva dirección a la base de datos y la asocia con una persona.
     *
     * @param personaId el identificador único de la persona
     * @param direccion la dirección que se asignará a la persona
     */
    public void agregarDireccion(int personaId, String direccion) {
        //consulta sql para insertar una nueva dirección
        String sqlInsertDireccion = "INSERT INTO Direcciones (direccion) VALUES (?)";
        //consulta sql para asociar la dirección a una persona en la tabla intermedia
        String sqlInsertRelacion = "INSERT INTO PersonaDireccion (personaId, direccionId) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement psDir = conn.prepareStatement(sqlInsertDireccion, Statement.RETURN_GENERATED_KEYS)) {

            //se asigna el valor de la dirección al parámetro
            psDir.setString(1, direccion);
            //se ejecuta la inserción en la tabla Direcciones
            psDir.executeUpdate();

            //se obtiene el id autogenerado de la dirección insertada
            ResultSet rs = psDir.getGeneratedKeys();
            int direccionId = 0;
            if (rs.next()) {
                direccionId = rs.getInt(1);
            }

            //se prepara la sentencia para insertar la relación persona-dirección
            try (PreparedStatement psRel = conn.prepareStatement(sqlInsertRelacion)) {
                psRel.setInt(1, personaId);
                psRel.setInt(2, direccionId);
                psRel.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


   /**
 * Consulta todas las personas registradas en la base de datos junto con sus
 * teléfonos y direcciones.
 *
 * @return un ArrayList de objetos Persona con sus datos, teléfonos y direcciones
 */
public ArrayList<Persona> consultarPersonas() {
    //esta lista almacenará las personas recuperadas de la base de datos
    ArrayList<Persona> personas = new ArrayList<>();

    //consulta sql para obtener todas las personas (ya no se trae la columna direccion)
    String sqlPersonas = "SELECT id, nombre FROM Personas";
    //consulta sql para obtener los teléfonos de una persona según su id
    String sqlTelefonos = "SELECT telefono FROM Telefonos WHERE personaId = ?";
    //consulta sql para obtener las direcciones de una persona mediante la tabla intermedia
    String sqlDirecciones = "SELECT d.direccion FROM Direcciones d " +
                            "INNER JOIN PersonaDireccion pd ON d.id = pd.direccionId " +
                            "WHERE pd.personaId = ?";

    //se abre la conexión a la base de datos y ejecuta la consulta de personas
    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sqlPersonas)) {

        //se recorre el resultado de la consulta de personas
        while (rs.next()) {
            //se obtiene el id de la persona
            int id = rs.getInt("id");
            //se obtiene su nombre
            String nombre = rs.getString("nombre");

            //se crea un objeto Persona solo con id y nombre; direcciones y teléfonos se agregan abajo
            Persona p = new Persona(id, nombre);

            //se obtienen los teléfonos asociados a la persona
            try (PreparedStatement psTel = conn.prepareStatement(sqlTelefonos)) {
                psTel.setInt(1, id);
                ResultSet rsTel = psTel.executeQuery();
                while (rsTel.next()) {
                    p.agregarTelefono(rsTel.getString("telefono"));
                }
            }

            //se obtienen las direcciones asociadas a la persona
            try (PreparedStatement psDir = conn.prepareStatement(sqlDirecciones)) {
                psDir.setInt(1, id);
                ResultSet rsDir = psDir.executeQuery();
                while (rsDir.next()) {
                    p.agregarDireccion(rsDir.getString("direccion"));
                }
            }

            //se agrega la persona con sus datos completos a la lista
            personas.add(p);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return personas;
}

    

    /**
     * Obtiene todas las direcciones asociadas a una persona en la base de datos.
     *
     * @param personaId el identificador único de la persona
     * @return una lista con las direcciones asociadas a la persona
     */
    public ArrayList<String> obtenerDirecciones(int personaId) {
        //esta lista almacenará las direcciones recuperadas de la base de datos
        ArrayList<String> direcciones = new ArrayList<>();

        //consulta sql para obtener las direcciones relacionadas a la persona
        String sql = "SELECT d.direccion FROM Direcciones d " +
                     "INNER JOIN PersonaDireccion pd ON d.id = pd.direccionId " +
                     "WHERE pd.personaId = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            //se asigna el id de la persona al parámetro
            ps.setInt(1, personaId);

            //se ejecuta la consulta
            ResultSet rs = ps.executeQuery();

            //se recorren los resultados y se agregan a la lista
            while (rs.next()) {
                direcciones.add(rs.getString("direccion"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return direcciones;
    }


/**
 * Modifica los datos de una persona: actualiza su nombre y reemplaza todas sus
 * direcciones por las nuevas proporcionadas.
 *
 * @param id el identificador único de la persona a modificar
 * @param nuevoNombre el nuevo nombre que se asignará a la persona
 * @param nuevasDirecciones la nueva lista de direcciones que reemplazará a las existentes
 */
public void modificarPersona(int id, String nuevoNombre, ArrayList<String> nuevasDirecciones) {
    //consulta sql para actualizar solo el nombre de la persona
    String sqlUpdatePersona = "UPDATE Personas SET nombre = ? WHERE id = ?";
    //consulta sql para borrar todas las relaciones actuales persona-dirección
    String sqlBorrarRelaciones = "DELETE FROM PersonaDireccion WHERE personaId = ?";

    try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
         PreparedStatement psUpdate = conn.prepareStatement(sqlUpdatePersona);
         PreparedStatement psBorrar = conn.prepareStatement(sqlBorrarRelaciones)) {

        //actualizar nombre
        psUpdate.setString(1, nuevoNombre);
        psUpdate.setInt(2, id);
        psUpdate.executeUpdate();

        //borrar relaciones de direcciones antiguas
        psBorrar.setInt(1, id);
        psBorrar.executeUpdate();

        //insertar nuevas direcciones (se crean si no existían)
        for (String dir : nuevasDirecciones) {
            agregarDireccion(id, dir);
        }

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
