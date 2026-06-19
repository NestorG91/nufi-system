package com.nufi.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Utilidad reusable para indicar al usuario que una acción está en proceso.
 * <p>
 * Patrón típico de uso:
 * <pre>
 *     LoadingUtil.ejecutar(
 *             owner,
 *             "Cargando cosechas...",
 *             () -> db.consultaPesada(),  // ← se ejecuta en hilo aparte
 *             resultado -> tabla.setItems(resultado) // ← en hilo FX
 *     );
 * </pre>
 * Mientras corre la tarea muestra un overlay modal con un reloj animado y
 * un mensaje. El cursor del nodo dueño se pone en WAIT.
 */
public final class LoadingUtil {

    private LoadingUtil() { }

    /**
     * Cambia el cursor del nodo a reloj de espera mientras dura una acción.
     * Usar para operaciones rápidas (&lt; 1 seg) que no justifican un overlay.
     */
    public static void conCursorEspera(Node nodo, Runnable accion) {
        if (nodo == null || nodo.getScene() == null) {
            accion.run();
            return;
        }
        Scene scene = nodo.getScene();
        Cursor original = scene.getCursor();
        scene.setCursor(Cursor.WAIT);
        try {
            accion.run();
        } finally {
            scene.setCursor(original);
        }
    }

    /**
     * Ejecuta una acción en un hilo aparte mostrando un overlay con reloj
     * mientras dura. El callback {@code onSuccess} recibe el resultado en el
     * hilo de JavaFX.
     */
    public static <T> void ejecutar(Node owner,
                                    String mensaje,
                                    Supplier<T> trabajo,
                                    Consumer<T> onSuccess) {

        Stage overlay = construirOverlay(owner, mensaje);

        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                return trabajo.get();
            }
        };

        task.setOnSucceeded(e -> {
            overlay.close();
            if (onSuccess != null) onSuccess.accept(task.getValue());
        });
        task.setOnFailed(e -> {
            overlay.close();
            Throwable ex = task.getException();
            System.out.println("❌ LoadingUtil: " +
                    (ex != null ? ex.getMessage() : "error desconocido"));
        });

        overlay.show();
        Thread t = new Thread(task, "nufi-loading-thread");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Versión sin retorno: ejecuta acción y al terminar llama a {@code onDone}
     * en el hilo de JavaFX.
     */
    public static void ejecutar(Node owner,
                                String mensaje,
                                Runnable trabajo,
                                Runnable onDone) {
        ejecutar(owner, mensaje, () -> { trabajo.run(); return null; },
                v -> { if (onDone != null) onDone.run(); });
    }

    // =========================================
    // Construcción del overlay modal
    // =========================================
    private static Stage construirOverlay(Node owner, String mensaje) {
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(56, 56);
        spinner.setStyle("-fx-progress-color:#2d6a4f;");

        Label lbl = new Label(
                (mensaje == null || mensaje.isBlank()) ?
                        "⏳ Procesando..." : "⏳ " + mensaje);
        lbl.setStyle(
                "-fx-text-fill:#2d6a4f;" +
                        "-fx-font-size:14px;" +
                        "-fx-font-weight:bold;");

        VBox caja = new VBox(14, spinner, lbl);
        caja.setStyle(
                "-fx-background-color:white;" +
                        "-fx-padding:24 32;" +
                        "-fx-background-radius:12;" +
                        "-fx-border-color:#d4e8d4;" +
                        "-fx-border-radius:12;" +
                        "-fx-border-width:1;" +
                        "-fx-effect:dropshadow(gaussian," +
                        "rgba(0,0,0,0.18), 18, 0, 0, 4);");
        caja.setAlignment(javafx.geometry.Pos.CENTER);

        StackPane root = new StackPane(caja);
        root.setStyle("-fx-background-color:rgba(0,0,0,0.18);");

        Scene scene = new Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

        Stage stage = new Stage(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);
        if (owner != null && owner.getScene() != null
                && owner.getScene().getWindow() != null) {
            stage.initOwner(owner.getScene().getWindow());
        }
        stage.setScene(scene);
        stage.setResizable(false);

        // Centrar respecto a la ventana propietaria
        Platform.runLater(() -> {
            if (stage.getOwner() instanceof Stage padre) {
                stage.setX(padre.getX() + padre.getWidth()/2  - 130);
                stage.setY(padre.getY() + padre.getHeight()/2 - 80);
            }
        });
        return stage;
    }
}
