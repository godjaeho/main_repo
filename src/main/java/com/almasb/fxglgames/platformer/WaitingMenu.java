//package com.almasb.fxglgames.platformer;
//
//import com.almasb.fxgl.app.scene.FXGLMenu;
//import com.almasb.fxgl.app.scene.GameSubScene;
//import com.almasb.fxgl.app.scene.MenuType;
//import com.almasb.fxgl.dsl.FXGL;
//import com.almasb.fxgl.entity.level.tiled.TMXLevelLoader;
//import com.almasb.fxgl.texture.ImagesKt;
//import com.almasb.fxgl.ui.FXGLButton;
//import com.almasb.fxgl.ui.FXGLScrollPane;
//import com.almasb.fxgl.ui.FontType;
//import javafx.scene.Group;
//import javafx.scene.Node;
//import javafx.scene.Parent;
//import javafx.scene.control.Button;
//import javafx.scene.control.ScrollPane;
//import javafx.scene.control.TextField;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.input.MouseButton;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.Pane;
//import javafx.scene.layout.VBox;
//import javafx.scene.paint.Color;
//import javafx.scene.paint.CycleMethod;
//import javafx.scene.paint.LinearGradient;
//import javafx.scene.paint.Stop;
//import javafx.scene.shape.Rectangle;
//import javafx.scene.shape.StrokeType;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//import static com.almasb.fxgl.dsl.FXGL.*;
//
//public class WaitingMenu extends FXGLMenu {
//
//    String playerID = null;
//    //Timeline countdown = null;
//
//    public WaitingMenu() {
//        super(MenuType.MAIN_MENU);
//
//        var readyButton = new FXGLButton("Ready");
//        var idField = new TextField();
//
//
//        readyButton.setOnMouseClicked(e -> {
//            if (e.getButton() == MouseButton.PRIMARY) {
//                if(true) {
//                    playerID = idField.getText();
//                    System.out.println("테스트용 출력 PlayerN : " + playerID);  //테스트용 코드
//                    fireNewGame();
//                }
//            }
//        });
//
//
//        idField.setPromptText("Enter your ID");
//
//        var vbox = new VBox(10, readyButton, idField);
//        vbox.setTranslateX(FXGL.getAppWidth() / 2.0 - 100);
//        vbox.setTranslateY(FXGL.getAppHeight() / 2.0 - 50);
//
//        getContentRoot().getChildren().addAll(vbox);
//
//    }
//
//}
package com.almasb.fxglgames.platformer;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.ui.FXGLButton;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import com.almasb.fxgl.app.scene.MenuType;


public class WaitingMenu extends FXGLMenu {

    private TextField idField = null;
    private Timeline countdownTimer = null;
    public String playerID = null;

    public WaitingMenu() {
        super(MenuType.MAIN_MENU);

        var readyButton = new FXGLButton("Ready");
        readyButton.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                playerID = idField.getText();
                System.out.println("테스트용 출력 PlayerN : " + playerID);  //테스트용 코드
                startCountdown(); // Ready 버튼 클릭 시 카운트 다운 시작
            }
        });

        idField = new TextField();
        idField.setPromptText("Enter your ID");

        var vbox = new VBox(10, readyButton, idField);
        vbox.setTranslateX(FXGL.getAppWidth() / 2.0 - 100);
        vbox.setTranslateY(FXGL.getAppHeight() / 2.0 - 50);

        getContentRoot().getChildren().addAll(vbox);
    }

    private void startCountdown() {
        var countdownLabel = FXGL.getUIFactoryService().newText("3", Color.BLACK, 80.0);
        countdownLabel.setTranslateX(FXGL.getAppWidth() / 2.0 - 25);
        countdownLabel.setTranslateY(FXGL.getAppHeight() / 2.0 - 100);
        countdownLabel.setManaged(false);

        countdownTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    int countdown = Integer.parseInt(countdownLabel.getText());
                    countdown--;

                    if (countdown == 0) {
                        countdownTimer.stop();
                        countdownLabel.setText("");
                        startGame(); // 카운트 다운 종료 후 게임 시작
                    } else {
                        countdownLabel.setText(String.valueOf(countdown));
                    }
                })
        );

        countdownTimer.setCycleCount(3);
        countdownTimer.play();

        getContentRoot().getChildren().add(countdownLabel);
    }

    private void startGame() {
        fireNewGame();
    }

//    public String getPlayerId() {
//        return playerId;
//    }
}
