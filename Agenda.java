package comdiegocano.agendas;

import java.sql.SQLException;
import java.util.ArrayList;

public class Agenda {

    // Instancia del repositorio que maneja todas las operaciones con la base de datos
    private RepositorioAgenda repo = new RepositorioAgenda();

    /**
     * Agrega una nueva persona a la agenda junto con sus direcciones y teléfonos.
     *
     * @param nombre nombre de la persona
     * @param direcciones lista de direcciones asociadas a la persona
     * @param telefonos lista de teléfonos asociados a la persona
     */
    public void agregarPersona(String nombre, ArrayList<String> direcciones, ArrayList<String> telefonos) {
        try {
            // Inserta la persona en la base de datos y obtiene su ID
            int personaId = repo.insertarPersona(nombre);

            // Inserta cada teléfono asociado a la persona
            for (String tel : telefonos) {
                repo.insertarTelefono(personaId, tel);
            }

            // Inserta cada dirección y relaciona con la persona
            for (String dir : direcciones) {
                int dirId = repo.insertarDireccion(dir);
                repo.relacionarDireccion(personaId, dirId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Consulta todas las personas registradas en la agenda junto con
     * sus direcciones y teléfonos.
     *
     * @return lista de objetos Persona con toda su información
     */
    public ArrayList<Persona> consultarPersonas() {
        try {
            return repo.consultarPersonas();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene todas las direcciones asociadas a una persona específica.
     *
     * @param personaId el identificador único de la persona
     * @return lista de direcciones de la persona
     */
    public ArrayList<String> obtenerDirecciones(int personaId) {
        try {
            return repo.obtenerDirecciones(personaId);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Modifica el nombre y las direcciones de una persona existente.
     *
     * @param id el identificador único de la persona
     * @param nuevoNombre el nuevo nombre que se asignará
     * @param nuevasDirecciones lista de nuevas direcciones que reemplazarán a las anteriores
     */
    public void modificarPersona(int id, String nuevoNombre, ArrayList<String> nuevasDirecciones) {
        try {
            repo.modificarPersona(id, nuevoNombre, nuevasDirecciones);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Elimina una persona de la agenda junto con sus teléfonos y direcciones.
     *
     * @param id el identificador único de la persona a eliminar
     */
    public void eliminarPersona(int id) {
        try {
            repo.eliminarPersona(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifica si una persona existe en la agenda según su ID.
     *
     * @param id el identificador único de la persona
     * @return true si la persona existe, false en caso contrario
     */
    public boolean existePersona(int id) {
        try {
            return repo.existePersona(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Modifica los teléfonos de una persona en la agenda.
     * Primero elimina los antiguos y luego inserta los nuevos.
     *
     * @param personaId el identificador único de la persona
     * @param nuevosTelefonos lista de nuevos teléfonos a asignar
     */
    public void modificarTelefonos(int personaId, ArrayList<String> nuevosTelefonos) {
        try {
            repo.modificarTelefonos(personaId, nuevosTelefonos);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
