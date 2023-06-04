package GUI;

import collections.Coordinates;
import collections.Flat;
import collections.Furnish;
import collections.House;
import commands.Info;
import commands.InfoPacket;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LongStringConverter;
import managers.CommandExecutor;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.*;

import static managers.ConverterChecker.*;

public class MainGUI extends Application {

    private ObservableList<Flat> objects = FXCollections.observableArrayList();
    private Label itemCountLabel;
    private Label yourItemCountLabel;
    private CommandExecutor executor = new CommandExecutor();
    private boolean isVisualizerOpened = false;

    public MainGUI() throws IOException {
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login");

        // Welcome label
        Label welcomeLabel = new Label("Welcome");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 30));

        // Login and password section
        TextField loginField = new TextField();
        loginField.setPromptText("Login");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        VBox loginPasswordBox = new VBox(10, loginField, passwordField);
        TitledPane loginPasswordPane = new TitledPane("Credentials", loginPasswordBox);
        loginPasswordPane.setCollapsible(false);

        // Login and Register buttons
        Button loginButton = new Button("Login");
        loginButton.setPrefSize(170, 30);
        loginButton.setOnAction(event -> {
            if (loginField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                showError("Both fields must be filled in!", "Try again.");
            } else {
                InfoPacket credentials = new InfoPacket("/login", null);
                credentials.setLogin(loginField.getText());
                credentials.setPassword(passwordField.getText());

                executor.setLogin(credentials.getLogin());
                executor.setPass(credentials.getPassword());

                InfoPacket loginPacket = executor.request(credentials);

                if (loginPacket.getCmd().equals("/login")){
                    executor.setUserID(Integer.parseInt(loginPacket.getArg()));
                    showMainApp(primaryStage);

                } else {
                    showError(loginPacket.getCmd(), "Try again.");
                }

            }
        });

        Button registerButton = new Button("Register");
        registerButton.setPrefSize(170, 30);
        registerButton.setOnAction(event -> {
            // Your register event handling code here
        });

        HBox buttons = new HBox(15, loginButton, registerButton);
        buttons.setAlignment(Pos.CENTER);

        VBox layout = new VBox(30, welcomeLabel, loginPasswordPane, buttons);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        primaryStage.setScene(new Scene(layout, 800, 600));
        primaryStage.show();
    }

    private void showInfo(String title, String header, String context){

        System.out.println(title + header + context);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(context);

        alert.showAndWait();
    }

    private void showError(String header, String context) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(header);
        alert.setContentText(context);
        alert.showAndWait();
    }

    private void showCreateObjectWindow() {
        Stage stage = new Stage();
        stage.setTitle("Create Object");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        TextField[] fields = {
                createFieldWithLabel(grid, "Name", 0),
                createFieldWithLabel(grid, "X coordination", 1),
                createFieldWithLabel(grid, "Y coordination", 2),
                createFieldWithLabel(grid, "Area", 3),
                createFieldWithLabel(grid, "Number of rooms", 4),
                createFieldWithLabel(grid, "Time to metro on foot", 5),
                createFieldWithLabel(grid, "Time to metro by transport", 6),
                createFieldWithLabel(grid, "Furnish [NONE, DESIGNER, FINE, LITTLE]", 7),
                createFieldWithLabel(grid, "House name", 8),
                createFieldWithLabel(grid, "House year", 9),
                createFieldWithLabel(grid, "Number of floors", 10),
                createFieldWithLabel(grid, "Number of lifts", 11)
        };

        Button createButton = new Button("Create");
        createButton.setOnAction(event -> {
            for(TextField field: fields){
                if(field.getText().isBlank()){
                    Label label = (Label) grid.getChildren().stream()
                            .filter(node -> Objects.equals(GridPane.getRowIndex(node), GridPane.getRowIndex(field)))
                            .filter(node -> GridPane.getColumnIndex(node) == 0)
                            .findFirst()
                            .orElse(null);

                    try {
                        assert label == null;
                    } catch (AssertionError e) {
                        showError(String.format("%s is empty", label.getText()), "Please fill the input");
                        return;
                    }
                }
            }

            if(!(isFloat(fields[1].getText())) || !(isDouble(fields[2].getText())) || !(isInteger(fields[3].getText())) || !(isInteger(fields[4].getText())) || !(isFloat(fields[5].getText())) || !(isDouble(fields[6].getText())) || !(isLong(fields[9].getText())) || !(isLong(fields[10].getText())) || !(isInteger(fields[11].getText()))) {
                showError("Error when formatting fields", "Check fields for validness");
                return;
            }

            if(!(isFurnish(fields[7].getText().toUpperCase(Locale.ROOT)))){
                showError("Error when formatting Furnish", "It must be one of [NONE, DESIGNER, FINE, LITTLE]");
                return;
            }

            System.out.println("True");


            Flat flat = new Flat(
                    objects.get(objects.size() - 1).getId() + 1,
                    executor.getUserID(),
                    fields[0].getText(),
                    new Coordinates(
                            Long.parseLong(fields[1].getText()),
                            Double.parseDouble(fields[2].getText())
                    ),
                    ZonedDateTime.now(),
                    Integer.parseInt(fields[3].getText()),
                    Integer.parseInt(fields[4].getText()),
                    Float.parseFloat(fields[5].getText()),
                    Double.parseDouble(fields[6].getText()),
                    Furnish.valueOf(fields[7].getText().toUpperCase(Locale.ROOT)),
                    new House(
                            fields[8].getText(),
                            Long.parseLong(fields[9].getText()),
                            Long.parseLong(fields[10].getText()),
                            Integer.parseInt(fields[11].getText())
                    )
            );

            objects.add(flat);

            InfoPacket addPacket = new InfoPacket("add", null);
            addPacket.setFlat(flat);

            executor.request(addPacket);

            // stage.close();

        });
        createButton.setPrefSize(150, 20);
        GridPane.setConstraints(createButton, 1, 12);
        grid.getChildren().add(createButton);

        Scene scene = new Scene(grid, 700, 700);
        stage.setScene(scene);
        stage.show();
    }

    private TextField createFieldWithLabel(GridPane grid, String labelText, int rowIndex) {
        TextField field = new TextField();
        field.setPrefSize(200, 30);
        Label label = new Label(labelText);
        label.setFont(new Font("Arial", 20));

        GridPane.setConstraints(label, 0, rowIndex);
        GridPane.setConstraints(field, 1, rowIndex);

        grid.getChildren().addAll(label, field);
        return field;
    }

    private void requestInfo(ActionEvent event) {

        Map<String, String> commands = new HashMap<>() {
            {
                put("create object", "add");
                put("remove head", "remove_head");
                put("average metro time", "average_of_time_to_metro_by_transport");
                put("count metro time", "count_by_time_to_metro_on_foot");
            }
        };

        Button clickedButton = (Button) event.getSource();
        String cmd = clickedButton.getText().toLowerCase(Locale.ROOT);

        if(commands.containsKey(cmd)){
            cmd = commands.get(cmd);
        }

        InfoPacket inf = executor.request(new InfoPacket(cmd, null));
        System.out.println(inf);
        showInfo(cmd, inf.getCmd(), inf.getCmd());

    }

    private void requestChange(TableColumn.CellEditEvent event) { // TODO: FIX

        Flat newFlat = (Flat) event.getRowValue();

        System.out.println(newFlat);

        InfoPacket upd = new InfoPacket("update", Integer.toString(newFlat.getId()));
        upd.setFlat(newFlat);

        InfoPacket inf = executor.request(upd);

        // Check

        System.out.println(inf);

    }


    private void showMainApp(Stage primaryStage) {
        Label usernameLabel = new Label(executor.getLogin());
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        TitledPane usernamePane = new TitledPane("Username", usernameLabel);
        usernamePane.setCollapsible(false);


        InfoPacket DB = executor.request(new InfoPacket("/loadDB", null));
        System.out.println(DB.getDB().size());
        objects.addAll(DB.getDB());


        TableView<Flat> table = new TableView<>();
        itemCountLabel = new Label("Total objects: " + objects.size());

        yourItemCountLabel = new Label("Your objects: " + "0"); // TODO:
        yourItemCountLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

        itemCountLabel = new Label("Total objects: " + objects.size());
        itemCountLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

        TitledPane itemCountPane = new TitledPane("Info", new VBox(itemCountLabel, yourItemCountLabel));
        itemCountPane.setCollapsible(false);

        Button Create_ObjectButton = new Button("Create object");
        Create_ObjectButton.setOnAction(event -> {
            showCreateObjectWindow();
        });

        Button helpButton = new Button("Help");
        helpButton.setOnAction(event -> {

            Flat f = new Flat(ZonedDateTime.now().getSecond(), 1, "1", new Coordinates(ZonedDateTime.now().getSecond() * 10, 30), ZonedDateTime.now(), 30, 1, (float) 1.1, 1.1, Furnish.DESIGNER, new House("1", (long) 1, 1, 1));

            objects.add(f);

            System.out.println(objects);
        });

        Button infoButton = new Button("Info");
        infoButton.setOnAction(this::requestInfo);

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(event -> {
            Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationDialog.setTitle("Confirmation");
            confirmationDialog.setHeaderText("Confirm Clear");
            confirmationDialog.setContentText("Are you sure you want to clear all elements?");

            Optional<ButtonType> result = confirmationDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                requestInfo(event);
            }
        });

        Button execute_scriptButton = new Button("Execute_script?");
        execute_scriptButton.setOnAction(this::requestInfo);

        Button headButton = new Button("Head"); // Popup first
        headButton.setOnAction(this::requestInfo);

        Button remove_headButton = new Button("Remove Head"); // Pop first
        remove_headButton.setOnAction(this::requestInfo);

        // TODO:

        Button remove_lowerButton = new Button("Remove lower");
        remove_lowerButton.setOnAction(this::requestInfo);

        Button count_by_time_to_metro_on_footButtn = new Button("Count metro time");
        count_by_time_to_metro_on_footButtn.setOnAction(event -> {
            showCreateObjectWindow();
        });

        //

        Button average_of_time_to_metro_by_transportButton = new Button("Average metro time");
        average_of_time_to_metro_by_transportButton.setOnAction(event -> {
            showCreateObjectWindow();
        });

        Button visualize = new Button("Visualize");
        visualize.setOnAction(event -> {
            if (!isVisualizerOpened) {
                isVisualizerOpened = true;

                CanvasManager canvasManager = new CanvasManager(objects, table, 1920, 1080);
                VBox vbox = new VBox(canvasManager.getCanvas());

                Stage newWindow = new Stage();
                newWindow.setTitle("New Window");
                newWindow.setOnCloseRequest(e -> isVisualizerOpened = false);

                Scene secondScene = new Scene(vbox, 1000, 1000);
                newWindow.setScene(secondScene);
                newWindow.initModality(Modality.NONE);
                newWindow.setMaximized(true);
                newWindow.show();
            }
        });


        VBox buttonsGroup = new VBox(Create_ObjectButton, helpButton, infoButton, clearButton, execute_scriptButton, headButton, remove_headButton, remove_lowerButton, average_of_time_to_metro_by_transportButton, count_by_time_to_metro_on_footButtn, visualize);

        for(Node butt: buttonsGroup.getChildren()){
            butt.setStyle("-fx-background-color: #347d89; -fx-text-fill: white;");
        }

        buttonsGroup.setSpacing(10);
        buttonsGroup.setPadding(new Insets(10));

        TitledPane buttonsPane = new TitledPane("Commands", buttonsGroup);
        buttonsPane.setCollapsible(true);
        buttonsPane.setExpanded(false);

        /* TODO: FIX
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Flat item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                } else {
                    System.out.println(item.getOwnerId() + " " + executor.getUserID());
                    if (item.getOwnerId() == executor.getUserID()) {
                        setStyle("-fx-background-color: lightblue;");
                    }
                }
            }
        });
         */

        table.setItems(null);
        table.layout();
        table.setItems(objects);

        table.setOnKeyPressed(keyEvent -> {
            final Flat selectedItem = table.getSelectionModel().getSelectedItem();
            if (selectedItem != null && keyEvent.getCode().equals(KeyCode.DELETE)) {
                objects.remove(selectedItem);
            }
        });

        TableColumn<Flat, Integer> idColumn = new TableColumn<>("ID");
        TableColumn<Flat, Integer> ownerIdColumn = new TableColumn<>("Owner");
        TableColumn<Flat, String> nameColumn = new TableColumn<>("Name");

        TableColumn<Flat, Long> xColumn = new TableColumn<>("X");
        TableColumn<Flat, Double> yColumn = new TableColumn<>("Y");

        TableColumn<Flat, Timestamp> creationDateColumn = new TableColumn<>("Creation");
        TableColumn<Flat, Integer> areaColumn = new TableColumn<>("Area");
        TableColumn<Flat, Integer> numberOfRoomsColumn = new TableColumn<>("NumberOfRooms");
        TableColumn<Flat, Float> timeToMetroOnFootColumn = new TableColumn<>("TimeToMetroOnFoot");
        TableColumn<Flat, Double> timeToMetroByTransport = new TableColumn<>("TimeToMetroByTransport");
        TableColumn<Flat, Furnish> furnishColumn = new TableColumn<>("Furnish");

        TableColumn<Flat, String> houseNameColumn = new TableColumn<>("HouseName");
        TableColumn<Flat, Long> houseYearColumn = new TableColumn<>("HouseYear");
        TableColumn<Flat, Long> houseNumberOfFloorsColumn = new TableColumn<>("HouseNumberOfFloorsColumn");
        TableColumn<Flat, Integer> houseNumberOfLiftsColumn = new TableColumn<>("HouseNumberOfLiftsColumn");

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        ownerIdColumn.setCellValueFactory(new PropertyValueFactory<>("ownerId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        xColumn.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().getCoordinates().getX()).asObject());
        yColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getCoordinates().getY()).asObject());
        creationDateColumn.setCellValueFactory(cellData -> {
            ZonedDateTime zdt = cellData.getValue().getCreationDate();
            Timestamp timestamp = Timestamp.from(zdt.toInstant());
            return new SimpleObjectProperty<>(timestamp);
        });
        areaColumn.setCellValueFactory(new PropertyValueFactory<>("area"));
        numberOfRoomsColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfRooms"));
        timeToMetroOnFootColumn.setCellValueFactory(new PropertyValueFactory<>("timeToMetroOnFoot"));
        timeToMetroByTransport.setCellValueFactory(new PropertyValueFactory<>("timeToMetroByTransport"));
        furnishColumn.setCellValueFactory(new PropertyValueFactory<>("furnish"));
        houseNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHouse().getName()));
        houseYearColumn.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().getHouse().getYear()).asObject());
        houseNumberOfFloorsColumn.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().getHouse().getNumberOfFloors()).asObject());
        houseNumberOfLiftsColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getHouse().getNumberOfLifts()).asObject());

        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> {
            Flat flat = event.getRowValue();
            if (flat.getOwnerId() == executor.getUserID()) {
                flat.setName(event.getNewValue());
                requestChange(event);
            } else {
                nameColumn.getTableView().refresh();
                showError("Not enough rights.", "You don't have permission to modify this object.");
                event.consume();
            }
        });

        xColumn.setCellFactory(TextFieldTableCell.forTableColumn(new LongStringConverter()));
        xColumn.setOnEditCommit(event -> {
            Flat flat = event.getRowValue();
            flat.getCoordinates().setX(event.getNewValue());
            requestChange(event);
        });

        yColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        yColumn.setOnEditCommit(event -> {
            Flat flat = event.getRowValue();
            flat.getCoordinates().setY(event.getNewValue());
            requestChange(event);
        });

        areaColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        areaColumn.setOnEditCommit(event -> {
            Flat flat = event.getRowValue();
            flat.setArea(event.getNewValue());
            requestChange(event);
        });

        numberOfRoomsColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        numberOfRoomsColumn.setOnEditCommit(event -> {
            Flat flat = event.getRowValue();
            if (flat.getOwnerId() == executor.getUserID()) {
                flat.setNumberOfRooms(event.getNewValue());
                requestChange(event);
            } else {
                event.consume();
            }
        });

        timeToMetroOnFootColumn.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));
        timeToMetroOnFootColumn.setOnEditCommit(event -> {
            Flat flat = event.getRowValue();
            flat.setTimeToMetroOnFoot(event.getNewValue());
            requestChange(event);
        });

        timeToMetroByTransport.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        timeToMetroByTransport.setOnEditCommit(event -> {
            Flat flat = event.getRowValue();
            flat.setTimeToMetroByTransport(event.getNewValue());
            requestChange(event);
        });

        furnishColumn.setCellFactory(ComboBoxTableCell.forTableColumn(Furnish.values()));
        furnishColumn.setOnEditCommit(event -> {
            Flat flat = event.getRowValue();
            flat.setFurnish(event.getNewValue());
            requestChange(event);
        });

        houseNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        houseNameColumn.setOnEditCommit(event -> {
            Flat flat = event.getRowValue();
            flat.getHouse().setName(event.getNewValue());
            requestChange(event);
        });

        houseYearColumn.setCellFactory(TextFieldTableCell.forTableColumn(new LongStringConverter()));
        houseYearColumn.setOnEditCommit(event -> {
            Flat flat = event.getRowValue();
            flat.getHouse().setYear(event.getNewValue());
            requestChange(event);
        });

        houseNumberOfFloorsColumn.setCellFactory(TextFieldTableCell.forTableColumn(new LongStringConverter()));
        houseNumberOfFloorsColumn.setOnEditCommit(event -> {
            Flat flat = event.getRowValue();
            flat.getHouse().setNumberOfFloors(event.getNewValue());
            requestChange(event);
        });

        houseNumberOfLiftsColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        houseNumberOfLiftsColumn.setOnEditCommit(event -> {
            Flat flat = event.getRowValue();
            flat.getHouse().setNumberOfLifts(event.getNewValue());
            requestChange(event);
        });

        table.setEditable(true);


        table.getColumns().addAll(idColumn, ownerIdColumn, nameColumn, xColumn, yColumn, creationDateColumn, areaColumn, numberOfRoomsColumn, timeToMetroOnFootColumn, timeToMetroByTransport, furnishColumn, houseNameColumn, houseYearColumn, houseNumberOfFloorsColumn, houseNumberOfLiftsColumn);

        for(TableColumn column: table.getColumns()){
            column.setPrefWidth(120);
        }
        idColumn.setPrefWidth(60);
        ownerIdColumn.setPrefWidth(60);

        VBox leftPanel = new VBox(usernamePane, itemCountPane, buttonsPane);
        leftPanel.setSpacing(20);
        leftPanel.setPadding(new Insets(10, 10, 10, 10));

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(20));
        layout.setLeft(leftPanel);
        layout.setCenter(table);

        primaryStage.setScene(new Scene(layout, 1920, 1080));
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void updateItemCount() {
        itemCountLabel.setText("Total objects: " + objects.size());

    }
    public void run(){
        launch();
    }

}