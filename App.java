package comdiegocano.agendas;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Optional;

public class App extends Application {

    private Agenda agenda = new Agenda();
    private TextArea areaTexto = new TextArea();

    @Override
    public void start(Stage stage) {
        // Configurar área de texto
        areaTexto.setEditable(false);
        areaTexto.setStyle("-fx-font-size: 16px; -fx-font-family: 'Consolas';");
        actualizarPanel();

        //crear los botones
        Button botonAgregar = new Button("Agregar");
        Button botonEliminar = new Button("Eliminar");
        Button botonModificar = new Button("Modificar");

        botonAgregar.setPrefSize(120, 40);
        botonEliminar.setPrefSize(120, 40);
        botonModificar.setPrefSize(120, 40);

        String estiloBoton = "-fx-font-size: 18px; -fx-font-family: 'Arial';";
        botonAgregar.setStyle(estiloBoton);
        botonEliminar.setStyle(estiloBoton);
        botonModificar.setStyle(estiloBoton);

        //eventos para los botones
        botonAgregar.setOnAction(e -> agregarPersona());
        botonEliminar.setOnAction(e -> eliminarPersona());
        botonModificar.setOnAction(e -> modificarPersona());

        //layout botones
        HBox cajaBotones = new HBox(20, botonAgregar, botonEliminar, botonModificar);
        cajaBotones.setStyle("-fx-alignment: center; -fx-padding: 20px;");

        //layout principal
        BorderPane layout = new BorderPane();
        layout.setCenter(areaTexto);
        layout.setBottom(cajaBotones);

        Scene scene = new Scene(layout, 900, 600);
        stage.setTitle("Agenda");
        stage.setScene(scene);
        stage.show();
    }

    //actualiza el panel de texto con la información de las tablas de la base
    //de datos
    private void actualizarPanel() {
        areaTexto.clear();
        ArrayList<Persona> personas = agenda.consultarPersonas();
        if (personas.isEmpty()) {
            areaTexto.setText("No hay registros.");
        } else {
            for (Persona p : personas) {
                areaTexto.appendText("ID: " + p.getId() + ", Nombre: " + p.getNombre()
                        + ", Dirección: " + p.getDireccion() + "\n  Teléfonos: " + p.getTelefonos() + "\n\n");
            }
        }
    }

    //pide ingresar los datos para agregar una persona en la base de datos
    private void agregarPersona() {
        TextInputDialog dialogNombre = new TextInputDialog();
        dialogNombre.setHeaderText("Ingrese el nombre:");
        Optional<String> nombre = dialogNombre.showAndWait();

        if (nombre.isEmpty()) {
            return;
        }

        TextInputDialog dialogDireccion = new TextInputDialog();
        dialogDireccion.setHeaderText("Ingrese la dirección:");
        Optional<String> direccion = dialogDireccion.showAndWait();
        if (direccion.isEmpty()) {
            return;
        }

        TextInputDialog dialogTelefonos = new TextInputDialog();
        dialogTelefonos.setHeaderText("Ingrese los teléfonos separados por coma:");
        Optional<String> telefonosStr = dialogTelefonos.showAndWait();
        if (telefonosStr.isEmpty()) {
            return;
        }

        String[] telArray = telefonosStr.get().split(",");
        ArrayList<String> telefonos = new ArrayList<>();
        for (String t : telArray) {
            telefonos.add(t.trim());
        }

        agenda.agregarPersona(nombre.get(), direccion.get(), telefonos);
        actualizarPanel();
    }

    //pide ingresar el ID de una persona para eliminarla de la base de datos
    private void eliminarPersona() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Ingrese el ID de la persona a eliminar:");
        Optional<String> idStr = dialog.showAndWait();
        if (idStr.isEmpty()) {
            return;
        }

        try {
            int id = Integer.parseInt(idStr.get());

            if (!agenda.existePersona(id)) {
                mostrarAlerta("No existe persona con ID: " + id);
                return;
            }

            agenda.eliminarPersona(id);
            actualizarPanel();
        } catch (NumberFormatException e) {
            mostrarAlerta("ID inválido");
        }
    }

    //pide el id para modificar los datos como el nombre, dirección y teléfonos
    private void modificarPersona() {
        TextInputDialog dialogId = new TextInputDialog();
        dialogId.setHeaderText("Ingrese el ID de la persona a modificar:");
        Optional<String> idStr = dialogId.showAndWait();
        if (idStr.isEmpty()) {
            return;
        }

        try {
            int id = Integer.parseInt(idStr.get());

            if (!agenda.existePersona(id)) {
                mostrarAlerta("No existe persona con ID: " + id);
                return;
            }

            TextInputDialog dialogNombre = new TextInputDialog();
            dialogNombre.setHeaderText("Ingrese el nuevo nombre:");
            Optional<String> nuevoNombre = dialogNombre.showAndWait();
            if (nuevoNombre.isEmpty()) {
                return;
            }

            TextInputDialog dialogDireccion = new TextInputDialog();
            dialogDireccion.setHeaderText("Ingrese la nueva dirección:");
            Optional<String> nuevaDireccion = dialogDireccion.showAndWait();
            if (nuevaDireccion.isEmpty()) {
                return;
            }

            TextInputDialog dialogTelefonos = new TextInputDialog();
            dialogTelefonos.setHeaderText("Ingrese los nuevos teléfonos separados por coma:");
            Optional<String> telefonosStr = dialogTelefonos.showAndWait();
            if (telefonosStr.isEmpty()) {
                return;
            }

            ArrayList<String> listaTelefonos = new ArrayList<>();
            for (String t : telefonosStr.get().split(",")) {
                listaTelefonos.add(t.trim());
            }

            agenda.modificarPersona(id, nuevoNombre.get(), nuevaDireccion.get());
            agenda.modificarTelefonos(id, listaTelefonos);

            actualizarPanel();

        } catch (NumberFormatException e) {
            mostrarAlerta("ID inválido");
        }
    }

    //muestra un mensaje de alerta para indicar errores o fallos al agregar, eliminar o modificar
    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setHeaderText(mensaje);
        alerta.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}
