package com.example.demo19;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

public class DigitalWhiteboard extends Application 
{
    private GraphicsContext gc;
    private Canvas canvas;
    private ColorPicker colorPicker;
    private Stack<WritableImage> undoStack, redoStack;
    private MediaPlayer mediaPlayer;

    @Override
    public void start(Stage primaryStage) 
    {
        primaryStage.setTitle("🖌️ Digital Whiteboard");

        canvas = new Canvas(1200, 800);
        gc = canvas.getGraphicsContext2D();
        setupDrawing();

        colorPicker = new ColorPicker(Color.BLACK);
        undoStack = new Stack<>();
        redoStack = new Stack<>();

        Button clearBtn = createButton("🧹 Clear", e -> clearCanvas());
        Button saveBtn = createButton("💾 Save", e -> saveCanvas(primaryStage));
        Button textBtn = createButton("📝 Add Text", e -> addText());
        Button imgBtn = createButton("🖼️ Add Image", e -> addImage(primaryStage));
        Button videoBtn = createButton("🎥 Add Video", e -> addVideo(primaryStage));
        Button musicBtn = createButton("🎵 Add Music", e -> addMusic(primaryStage));
        Button undoBtn = createButton("↩️ Undo", e -> undo());
        Button redoBtn = createButton("↪️ Redo", e -> redo());

        
        HBox controlPanel = new HBox(10, colorPicker, clearBtn, saveBtn, textBtn, imgBtn, videoBtn, musicBtn, undoBtn, redoBtn);
        controlPanel.getStyleClass().add("hbox");

        BorderPane root = new BorderPane();
        root.setTop(controlPanel);
        root.setCenter(canvas);
        root.getStyleClass().add("root");

        Scene scene = new Scene(root, 1200, 900);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Button createButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> event) 
    {
        Button button = new Button(text);
        button.setOnAction(event);
        return button;
    }

    private void setupDrawing() {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(3.0);
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            saveState();
            gc.setStroke(colorPicker.getValue());
            gc.beginPath();
            gc.moveTo(e.getX(), e.getY());
            gc.stroke();
        });
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();
        });
    }

    private void clearCanvas() {
        saveState();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void saveCanvas(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            WritableImage image = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
            canvas.snapshot(null, image);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addText() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Text");
        dialog.setHeaderText("Enter text to add to the canvas:");
        dialog.setContentText("Text:");
        dialog.showAndWait().ifPresent(text -> {
            saveState();
            gc.setFill(colorPicker.getValue());
            gc.fillText(text, 50, 50);
        });
    }

    private void addImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            saveState();
            gc.drawImage(image, 100, 100);
        }
    }

    private void addVideo(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*.mov"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Media media = new Media(file.toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            MediaView mediaView = new MediaView(mediaPlayer);
            mediaView.setFitWidth(canvas.getWidth());
            mediaView.setFitHeight(canvas.getHeight());
            ((BorderPane) stage.getScene().getRoot()).setCenter(mediaView);
            mediaPlayer.play();
        }
    }

    private void addMusic(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();
        }
    }

    private void saveState() {
        WritableImage snapshot = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, snapshot);
        undoStack.push(snapshot);
        redoStack.clear();
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(canvas.snapshot(null, null));
            WritableImage previousState = undoStack.pop();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.drawImage(previousState, 0, 0);
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(canvas.snapshot(null, null));
            WritableImage nextState = redoStack.pop();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.drawImage(nextState, 0, 0);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
