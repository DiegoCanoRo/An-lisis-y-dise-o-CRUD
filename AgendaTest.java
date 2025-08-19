
import comdiegocano.agendas.Agenda;
import comdiegocano.agendas.Persona;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;

public class AgendaTest {

    //test para agregar y eliminar una persona en la base de datos
    @Test
    void testAgregarYEliminarPersona() {
        //se crea una instancia de Agenda
        Agenda agenda = new Agenda();
        ArrayList<String> telefonos = new ArrayList<>();
        telefonos.add("6662345");
        
        // agregar persona con nombre, dirección y teléfonos
        agenda.agregarPersona("Diegito", "Av Juan Álvarez 1", telefonos);
        
         // consultar todas las personas de la base de datos
        ArrayList<Persona> personas = agenda.consultarPersonas();
        
        //se busca la persona recien agregada
        Persona encontrada = personas.stream()
                .filter(p -> p.getNombre().equals("Diegito"))
                .findFirst()
                .orElse(null);
        
        //verificar que la persona fue agregada correctamente
        assertNotNull(encontrada, "La persona debe haberse agregado a la BD");
        assertTrue(encontrada.getTelefonos().contains("6662345"));
        
        //eliminar la persona 
        agenda.eliminarPersona(encontrada.getId());
        
        
        //verificar que fue eliminada correctamente
        personas = agenda.consultarPersonas();
        boolean existe = personas.stream()
                .anyMatch(p -> p.getId() == encontrada.getId());

        assertFalse(existe, "La persona debe haberse eliminado de la BD");
    }
    
    
    //test para verificar que si se modifican los datos de una persona
    @Test
    void testModificarPersona() {
        Agenda agenda = new Agenda();
        ArrayList<String> telefonos = new ArrayList<>();
        telefonos.add("777-1234");
        
        
        //se agrega a una persona con sus datos
        agenda.agregarPersona("ModificarTest", "Calle 123", telefonos);
        
        //se busca la persona
        ArrayList<Persona> personas = agenda.consultarPersonas();
        Persona p = personas.stream()
                .filter(persona -> persona.getNombre().equals("ModificarTest"))
                .findFirst().orElse(null);

        assertNotNull(p);

        // modifica nombre y dirección
        agenda.modificarPersona(p.getId(), "NombreModificado", "DireccionModificada");
        
        //se busca la persona modificada para verificar que se aplicaron los cambios
        personas = agenda.consultarPersonas();
        Persona modificado = personas.stream()
                .filter(persona -> persona.getId() == p.getId())
                .findFirst().orElse(null);

        assertEquals("NombreModificado", modificado.getNombre());
        assertEquals("DireccionModificada", modificado.getDireccion());

        // Limpiar
        agenda.eliminarPersona(p.getId());
    }
    
    //test para modificar los teléfonos asociados a una ID
    @Test
    void testModificarTelefonos() {
        Agenda agenda = new Agenda();
        ArrayList<String> telefonos = new ArrayList<>();
        
        //se crea una persona y se le agrega un teléfono
        telefonos.add("888-1111");
        agenda.agregarPersona("TelTest", "Av 2", telefonos);
        
        //se obtiene a la persona recien agregada
        ArrayList<Persona> personas = agenda.consultarPersonas();
        Persona p = personas.stream()
                .filter(persona -> persona.getNombre().equals("TelTest"))
                .findFirst().orElse(null);

        assertNotNull(p);

        //se crean nuevos teléfonos para agregarselos a la persona
        ArrayList<String> nuevosTelefonos = new ArrayList<>();
        nuevosTelefonos.add("999-2222");
        nuevosTelefonos.add("999-3333");
        agenda.modificarTelefonos(p.getId(), nuevosTelefonos);
        
        
        //se verifica si se aplicaron los cambios
        personas = agenda.consultarPersonas();
        Persona actualizado = personas.stream()
                .filter(persona -> persona.getId() == p.getId())
                .findFirst().orElse(null);

        assertEquals(2, actualizado.getTelefonos().size());
        assertTrue(actualizado.getTelefonos().contains("999-2222"));
        assertTrue(actualizado.getTelefonos().contains("999-3333"));

        // limpia y elimina a la persona
        agenda.eliminarPersona(p.getId());
    }

    @Test
    void testExistePersona() {
        Agenda agenda = new Agenda();
        ArrayList<String> telefonos = new ArrayList<>();
        telefonos.add("555-0000");
        
        //se agrega una persona para verificar si existe
        agenda.agregarPersona("ExisteTest", "Calle 9", telefonos);
        
        //se busca a la persona y se verifica si existe
        ArrayList<Persona> personas = agenda.consultarPersonas();
        Persona p = personas.stream()
                .filter(persona -> persona.getNombre().equals("ExisteTest"))
                .findFirst().orElse(null);

        assertNotNull(p);
        assertTrue(agenda.existePersona(p.getId()));

        // limpia y elimina a la persona
        agenda.eliminarPersona(p.getId());
        
        //se verifica que ya no exista
        assertFalse(agenda.existePersona(p.getId()));
    }
}
