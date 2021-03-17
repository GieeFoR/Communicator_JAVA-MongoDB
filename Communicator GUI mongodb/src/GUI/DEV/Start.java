package GUI.DEV;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;


public class Start {
    public static VBox vBox = new VBox();

    public Start() {
        Stage primaryStage = new Stage();
        primaryStage.setTitle("DEV tools");
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);

        Group root = new Group();
        Scene scene = new Scene(root, 800, 600, Color.WHITE);
        TabPane tabPane = new TabPane();
        AnchorPane anchorPane = new AnchorPane();

        tabPane.getTabs().add(createTab("Clients"));
        tabPane.getTabs().add(createTab("Conversations"));
        tabPane.getTabs().add(createTab("Messages"));

        // bind to take available space
        anchorPane.prefHeightProperty().bind(scene.heightProperty());
        anchorPane.prefWidthProperty().bind(scene.widthProperty());

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(tabPane);
        borderPane.setPrefHeight(200);
        borderPane.prefWidthProperty().bind(scene.widthProperty());

        Line line = new Line();
        line.setStartX(0.);
        line.setStartY(200.);
        line.setEndY(200.);

        line.endXProperty().bind(scene.widthProperty());

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(vBox);

        anchorPane.getChildren().addAll(borderPane, scrollPane, line);

        AnchorPane.setTopAnchor(borderPane, 0.);
        AnchorPane.setLeftAnchor(borderPane, 0.);
        //AnchorPane.setRightAnchor(borderPane, 0.);

        AnchorPane.setTopAnchor(scrollPane, 200.);
        AnchorPane.setBottomAnchor(scrollPane, 0.);
        AnchorPane.setLeftAnchor(scrollPane, 10.);
        AnchorPane.setRightAnchor(scrollPane, 10.);

        root.getChildren().add(anchorPane);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Tab createTab(String tabName) {
        Tab tab = new Tab(tabName);
        tab.setClosable(false);

        switch (tabName) {
            case "Clients" -> new ClientTab(tab);
            case "Conversations" -> new ConversationTab(tab);
            case "Messages" -> new MessageTab(tab);
        }
        return tab;
    }
}
