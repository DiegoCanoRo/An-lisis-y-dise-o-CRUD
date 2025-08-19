package comdiegocano.agendas;

import java.util.ArrayList;

public class Persona {

    private int id;
    private String nombre;
    private String direccion;
    private ArrayList<String> telefonos;

    public Persona(int id, String nombre, String direccion) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        telefonos = new ArrayList<>();
    }

    public void agregarTelefono(String numero) {
        telefonos.add(numero);
    }

    //getters
    public String getNombre() {
        return nombre;
    }

    public int getId() {
        return id;
    }

    public String getDireccion() {
        return direccion;
    }

    public ArrayList<String> getTelefonos() {
        return telefonos;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDireccion(String Direccion) {
        this.direccion = direccion;
    }

}//
