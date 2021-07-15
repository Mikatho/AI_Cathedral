package JavaFX;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import JavaFX.ki.SmartyAI;
import JavaFX.ki.cathedral.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Main extends Application implements Runnable {

    private Game game;
    private Text scenetitle;
    private Text score;
    private Text lastBuilding;
    VBox vbox = new VBox();

    SmartyAI ai_one;

    String ai = "Grün";
    String enemyAi = "Schwarz";

    Button buttonGreen;
    Button buttonBlack;

    Text xAxesText = new Text("X: 0");
    Text yAxesText = new Text("Y: 0");
    Text[] xAxesDesc = new Text[10];
    Text[] yAxesDesc = new Text[10];

    Button buttonRight = new Button("\u25B6");
    Button buttonLeft = new Button("\u25C0");
    Button buttonDown = new Button("\u2B07");
    Button buttonUp = new Button("\u2B06");
    Button buttonRotate = new Button("\u27F2");
    Button buttonConfirm = new Button("Bestätigen");
    Button undoButton = new Button("Undo");


    Button buttonForfeit = new Button("Skippen");

    boolean playerChosen = false;
    boolean cathredralPlaced = false;

    @Override
    public void start(Stage primaryStage) {

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25, 25, 25, 25));

        scenetitle = new Text("Wähle aus wer anfängt.");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        gridPane.add(scenetitle, 0, 0, 2, 1);

        score = new Text("Score:");
        score.setFont(Font.font("Tahoma", FontWeight.NORMAL, 15));
        gridPane.add(score, 1, 13);

        lastBuilding = new Text("Last Building:");
        lastBuilding.setFont(Font.font("Tahoma", FontWeight.NORMAL, 15));
        gridPane.add(lastBuilding, 1, 14);

        game = new Game();
        ai_one = new SmartyAI();

        buttonGreen = new Button(ai);
        buttonBlack = new Button(enemyAi);

        buttonGreen.setMinWidth(120);
        buttonBlack.setMinWidth(120);

        buttonRight.setMinWidth(60);
        buttonLeft.setMinWidth(60);
        buttonDown.setMinWidth(60);
        buttonUp.setMinWidth(60);
        buttonRotate.setMinWidth(60);
        buttonConfirm.setMinWidth(80);
        buttonForfeit.setMinWidth(40);
        undoButton.setMinWidth(40);

        buttonGreen.setOnMouseClicked(mouseEvent -> {
            // UNSERE AI SOLL ZUG MACHEN
            if (!playerChosen) {
                playerChosen = true;

                buttonBlack.setDisable(true);
            } else {
                aiTurn(game, ai_one);
                if (!cathredralPlaced) cathredralPlaced = true;

                undoButton.setDisable(false);

                buttonGreen.setDisable(true);
                buttonBlack.setDisable(false);

                cleanUpControlButtons(gridPane);
            }

            updateUI(primaryStage, gridPane, game);
        });

        buttonBlack.setOnMouseClicked(mouseEvent -> {
            // GEGNER AI SOLL ZUG MACHEN
            if (!playerChosen) {
                playerChosen = true;

                buttonGreen.setDisable(true);
            } else {
                aiTurn(game, ai_one);
                if (!cathredralPlaced) cathredralPlaced = true;

                undoButton.setDisable(false);

                buttonGreen.setDisable(false);
                buttonBlack.setDisable(true);

                cleanUpControlButtons(gridPane);
            }

            updateUI(primaryStage, gridPane, game);
        });

        undoButton.setOnMouseClicked(mouseEvent -> {
            if (cathredralPlaced) game.undoLastTurn();

            if (game.getTurns() == 1) {
                cathredralPlaced = false;
                undoButton.setDisable(true);
            }

            buttonGreen.setDisable(!buttonGreen.isDisabled());
            buttonBlack.setDisable(!buttonBlack.isDisabled());

            updateUI(primaryStage, gridPane, game);
        });

        gridPane.add(buttonGreen, 0, 1);
        gridPane.add(buttonBlack, 1, 1);

        gridPane.add(undoButton, 0, 2);
        undoButton.setDisable(true);

        //gridPane.setGridLinesVisible(true);

        primaryStage.setTitle("WhiskeyAI");
        primaryStage.setScene(new Scene(gridPane, 1200, 800));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    private void aiTurn(Game game, SmartyAI smartyAI) {
        try {
            Placement tempPlace = smartyAI.takeTurn(game);
            if (tempPlace == null) {
                game.forfeitTurn();
            } else {
                game.takeTurn(tempPlace);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUI(Stage ui, GridPane gridPane, Game game) {

        if (game.getCurrentPlayer() == Color.Black) {
            scenetitle.setText(ai + " ist am Zug!");
        } else {
            scenetitle.setText(enemyAi + " ist am Zug!");
        }

        Color[][] currentBoard = game.lastTurn().getBoard().getBoardAsColorArray();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                gridPane.add(getBoardFieldRect(currentBoard[x][y]), y + 3, x + 2);
            }
        }

        createBuildingButtons(gridPane, ui);

        createAxesDescription(gridPane);

        score.setText(getCurrentScore(game));

        try {
            if (cathredralPlaced) lastBuilding.setText("Last Building: " + game.lastTurn().getAction().getBuilding().getName());
        } catch (Exception ignored) {
        }

        ui.show();
    }

    private String getCurrentScore(Game game) {
        String rScore;
        rScore = "Score: \n";
        rScore += ai + ": ";
        rScore += game.score().get(Color.Black);
        rScore += "\n";
        rScore += enemyAi + ": ";
        rScore += game.score().get(Color.White);
        rScore += "\n";

        return rScore;
    }

    private Rectangle getBoardFieldRect(Color color) {
        Rectangle rect = new Rectangle();
        rect.setWidth(30);
        rect.setHeight(30);
        switch (color) {
            case None:
                rect.setFill(javafx.scene.paint.Color.LIGHTPINK);
                break;
            case Black:
                rect.setFill(javafx.scene.paint.Color.BLACK);
                break;
            case Black_Owned:
                rect.setFill(javafx.scene.paint.Color.DARKGRAY);
                break;
            case White:
                rect.setFill(javafx.scene.paint.Color.GREEN);
                break;
            case White_Owned:
                rect.setFill(javafx.scene.paint.Color.LIGHTGREEN);
                break;
            case Blue:
                rect.setFill(javafx.scene.paint.Color.BLUE);
                break;
        }
        return rect;
    }

    private void createBuildingButtons(GridPane gridPane, Stage ui) {
        gridPane.getChildren().remove(vbox);
        vbox = new VBox();

        List<Building> sortedBuildings = game.getPlaceableBuildingsByColor(game.getCurrentPlayer()).stream()
                .sorted(Comparator.comparingInt(Building::getBuildingsize).reversed()).collect(Collectors.toList());

        for (Building building : sortedBuildings) {

            Button btn = new Button(building.getName());
            btn.setOnMouseClicked(mouseEvent -> humanPlayerTurn(building, gridPane, ui));
            btn.setMinWidth(100);
            vbox.getChildren().add(btn);
        }
        gridPane.add(vbox, 13, 2, 13, vbox.getChildren().size() + 2);
    }

    private void createAxesDescription(GridPane gridPane) {
        for (int i=0; i<10; i++) {
            gridPane.getChildren().remove(xAxesDesc[i]);
            gridPane.getChildren().remove(yAxesDesc[i]);

            xAxesDesc[i] = new Text(Integer.toString(i));
            GridPane.setHalignment(xAxesDesc[i], HPos.CENTER);

            yAxesDesc[i] = new Text(Integer.toString(i));

            gridPane.add(xAxesDesc[i], i + 3, 12);
            gridPane.add(yAxesDesc[i], 2, i + 2);
        }
    }

    private void humanPlayerTurn(Building building, GridPane gridPane, Stage ui) {
        cleanUpControlButtons(gridPane);

        xAxesText = new Text("X: 0");
        yAxesText = new Text("Y: 0");

        final int[] x = {0};
        final int[] y = {0};
        final Direction[] direction = {Direction._0};


        buttonRight.setOnMouseClicked(mouseEvent -> {
            if (x[0] < 9) x[0]++;
            testPlace(new Placement(x[0], y[0], direction[0], building), gridPane, ui, buttonConfirm);
            xAxesText.setText("X: " + x[0]);
        });
        buttonLeft.setOnMouseClicked(mouseEvent -> {
            if (x[0] > 0) x[0]--;
            testPlace(new Placement(x[0], y[0], direction[0], building), gridPane, ui, buttonConfirm);
            xAxesText.setText("X: " + x[0]);
        });
        buttonDown.setOnMouseClicked(mouseEvent -> {
            if (y[0] < 9) y[0]++;
            testPlace(new Placement(x[0], y[0], direction[0], building), gridPane, ui, buttonConfirm);
            yAxesText.setText("Y: " + y[0]);
        });
        buttonUp.setOnMouseClicked(mouseEvent -> {
            if (y[0] > 0) y[0]--;
            testPlace(new Placement(x[0], y[0], direction[0], building), gridPane, ui, buttonConfirm);
            yAxesText.setText("Y: " + y[0]);

        });

        buttonRotate.setOnMouseClicked(mouseEvent -> {
            switch (direction[0]) {
                case _0:
                    direction[0] = Direction._90;
                    break;
                case _90:
                    direction[0] = Direction._180;
                    break;
                case _180:
                    direction[0] = Direction._270;
                    break;
                case _270:
                    direction[0] = Direction._0;
                    break;
            }
            testPlace(new Placement(x[0], y[0], direction[0], building), gridPane, ui, buttonConfirm);
        });

        buttonForfeit.setOnMouseClicked(mouseEvent -> {
            game.forfeitTurn();
            cleanUpControlButtons(gridPane);
            buttonGreen.setDisable(false);
            buttonBlack.setDisable(true);

        });

        buttonConfirm.setOnMouseClicked(mouseEvent -> {
            game.takeTurn(new Placement(x[0], y[0], direction[0], building));
            cleanUpControlButtons(gridPane);
            if (!cathredralPlaced) {
                cathredralPlaced = true;
            }
            undoButton.setDisable(false);

            buttonGreen.setDisable(!buttonGreen.isDisabled());
            buttonBlack.setDisable(!buttonBlack.isDisabled());

            updateUI(ui, gridPane, game);

            System.out.println(game.getTurns());
        });

        centerControlButtons();

        gridPane.add(buttonRight, 29, 3);
        gridPane.add(buttonLeft, 27, 3);
        gridPane.add(xAxesText, 30, 3);

        gridPane.add(buttonDown, 28, 4);
        gridPane.add(buttonUp, 28, 2);
        gridPane.add(yAxesText, 28, 1);

        gridPane.add(buttonRotate, 28, 3);
        gridPane.add(buttonConfirm, 27, 5);
        gridPane.add(buttonForfeit, 27, 6);
    }

    private void cleanUpControlButtons(GridPane gridPane) {
        gridPane.getChildren().remove(buttonRight);
        gridPane.getChildren().remove(buttonLeft);
        gridPane.getChildren().remove(buttonDown);
        gridPane.getChildren().remove(buttonUp);
        gridPane.getChildren().remove(buttonRotate);
        gridPane.getChildren().remove(buttonConfirm);
        gridPane.getChildren().remove(xAxesText);
        gridPane.getChildren().remove(yAxesText);
        gridPane.getChildren().remove(buttonForfeit);
    }

    private void centerControlButtons() {
        GridPane.setHalignment(yAxesText, HPos.CENTER);
        GridPane.setHalignment(buttonRight, HPos.CENTER);
        GridPane.setHalignment(buttonLeft, HPos.CENTER);
        GridPane.setHalignment(buttonDown, HPos.CENTER);
        GridPane.setHalignment(buttonUp, HPos.CENTER);
        GridPane.setHalignment(buttonRotate, HPos.CENTER);

        GridPane.setHalignment(yAxesText, HPos.CENTER);
    }

    private void testPlace(Placement placement, GridPane gridPane, Stage ui, Button confirm) {
        Game testGame = game.copy();
        boolean b = testGame.takeTurn(placement);
        updateUI(ui, gridPane, testGame);

        confirm.setDisable(!b);
    }

    @Override
    public void run() {
    }
}
