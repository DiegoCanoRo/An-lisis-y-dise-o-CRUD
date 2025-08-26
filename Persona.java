package comdiegocano.agendas;

import java.util.ArrayList;

public class Persona {

    private int id;
    private String nombre;
    private ArrayList<String> direcciones;
    private ArrayList<String> telefonos;

    public Persona(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
        this.direcciones = new ArrayList<>();
        this.telefonos = new ArrayList<>();
    }

    public void agregarDireccion(String direccion) {
        direcciones.add(direccion);
    }

    public void agregarTelefono(String numero) {
        telefonos.add(numero);
    }

    // getters
    public String getNombre() {
        return nombre;
    }

    public int getId() {
        return id;
    }

    public ArrayList<String> getDirecciones() {
        return direcciones;
    }

    public ArrayList<String> getTelefonos() {
        return telefonos;
    }

    // setters
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

}
