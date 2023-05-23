package GUI;

import collections.Flat;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class CanvasManager {
    private Canvas canvas;
    private List<Flat> objects;
    private HashMap<Integer, Color> userColorMap = new HashMap<>();
    private Random rand = new Random();
    private Image houseImage;
    private TableView<Flat> table;

    public CanvasManager(List<Flat> objects, TableView<Flat> table, int width, int height) {
        this.canvas = new Canvas(width, height);
        this.table = table;
        this.objects = objects;
        setupObjectListeners();
        loadHouseImage();
        drawObjects();
    }

    private void loadHouseImage() {
        try {
            File imageFile = new File("Images/House.png");
            String absolutePath = imageFile.getAbsolutePath();
            houseImage = new Image(new FileInputStream(absolutePath));

        } catch (Exception e) {
            System.out.println("Failed to load house image: " + e.getMessage());
        }
    }

    private void drawObjects() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw grid lines
        int gridSize = 20;
        gc.setStroke(Color.LIGHTGRAY);
        for (int x = 0; x < canvas.getWidth(); x += gridSize) {
            gc.strokeLine(x, 0, x, canvas.getHeight());
        }
        for (int y = 0; y < canvas.getHeight(); y += gridSize) {
            gc.strokeLine(0, y, canvas.getWidth(), y);
        }

        // Draw X and Y axis lines
        gc.setStroke(Color.GRAY);
        gc.strokeLine(0, canvas.getHeight() / 2, canvas.getWidth(), canvas.getHeight() / 2); // X axis
        gc.strokeLine(canvas.getWidth() / 2, 0, canvas.getWidth() / 2, canvas.getHeight()); // Y axis

        // Draw objects
        for (Flat object : objects) {
            double x = canvas.getWidth() / 2 + object.getCoordinates().getX();
            double y = canvas.getHeight() / 2 - object.getCoordinates().getY();
            Color color = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), 1);
            gc.save(); // Save the current state of the graphics context
            gc.setGlobalAlpha(0.5); // Set the opacity to 0.5
            gc.setFill(color);
            double houseWidth = object.getArea();
            double houseHeight = object.getArea();
            gc.drawImage(houseImage, x - (houseWidth / 2), y - houseHeight, houseWidth, houseHeight);
            gc.restore(); // Restore the previous state of the graphics context
        }

    }

    private void setupObjectListeners() {
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            double x = event.getX();
            double y = event.getY();
            for (Flat object : objects) {
                double objX = canvas.getWidth() / 2 + object.getCoordinates().getX();
                double objY = canvas.getHeight() / 2 - object.getCoordinates().getY();
                double houseWidth = 30;
                double houseHeight = 30;
                if (x >= objX - (houseWidth / 2) && x <= objX + (houseWidth / 2)
                        && y >= objY - houseHeight && y <= objY) {
                    showObjectInfo(object, table);
                    break;
                }
            }
        });
    }

    private void showObjectInfo(Flat object, TableView<Flat> table) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Object Information");
        dialog.setHeaderText(object.getName());

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        StringBuilder content = new StringBuilder();
        content.append("ID: ").append(object.getId()).append("\n")
                .append("Owner: ").append(object.getOwnerId()).append("\n")
                .append("Coordinates: (").append(object.getCoordinates().getX()).append(", ")
                .append(object.getCoordinates().getY()).append(")\n")
                .append("Area: ").append(object.getArea()).append("\n")
                .append("Number of Rooms: ").append(object.getNumberOfRooms()).append("\n")
                .append("Time to Metro on Foot: ").append(object.getTimeToMetroOnFoot()).append("\n")
                .append("Time to Metro by Transport: ").append(object.getTimeToMetroByTransport()).append("\n")
                .append("Furnish: ").append(object.getFurnish()).append("\n")
                .append("House Name: ").append(object.getHouse().getName()).append("\n")
                .append("House Year: ").append(object.getHouse().getYear()).append("\n")
                .append("House Number of Floors: ").append(object.getHouse().getNumberOfFloors()).append("\n")
                .append("House Number of Lifts: ").append(object.getHouse().getNumberOfLifts());

        Label contentLabel = new Label(content.toString());

        Button selectButton = new Button("Edit");
        selectButton.setOnAction(event -> {
            table.getSelectionModel().select(object);
            table.scrollTo(object);
            dialog.close();
        });

        dialogPane.setContent(new VBox(contentLabel, selectButton));

        dialogPane.getScene().getWindow().setOnCloseRequest(e -> {
            table.getSelectionModel().clearSelection();
        });

        dialog.showAndWait();
    }
    public Canvas getCanvas() {
            return canvas;
    }
}