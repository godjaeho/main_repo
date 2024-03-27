package com.almasb.fxglgames.platformer;


import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.GameView;
import com.almasb.fxgl.app.scene.LoadingScene;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.app.scene.Viewport;
import com.almasb.fxgl.core.util.LazyValue;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.input.view.KeyView;
import com.almasb.fxgl.input.virtual.VirtualButton;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxglgames.platformer.LevelEndScene;
import com.almasb.fxglgames.platformer.MainLoadingScene;
import com.almasb.fxglgames.platformer.PlatformerFactory;
import com.almasb.fxglgames.platformer.PlayerButtonHandler;

import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.util.concurrent.TimeUnit;
import javafx.scene.input.MouseButton;

//인태님 임포트

import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsWorld;

import javafx.application.Platform;
// import javax.swing.SwingUtilities;

// import java.sql.Connection;
// import java.sql.Connection;
// import java.sql.Connection;
// 일단 미사용
import java.util.Map;
import static com.almasb.fxgl.dsl.FXGL.*;
import com.almasb.fxgl.dsl.FXGL;

import com.almasb.fxgl.core.serialization.Bundle;

import com.almasb.fxgl.net.Server;
import javafx.scene.control.CheckBox;
import javafx.util.Duration;


import static com.almasb.fxglgames.platformer.EntityType.*;
import static com.almasb.fxglgames.platformer.Config.*;
import com.almasb.fxgl.net.Client;



/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class PlatformerApp extends GameApplication {

    private Bundle playerPositionBundle = new Bundle("PlayerPosition");
    private Bundle playerPositionBundle2 = new Bundle("Player2Position");


    private static final int MAX_LEVEL = 5;
    private static final int STARTING_LEVEL = 0;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setSceneFactory(new SceneFactory() {
            @Override
            public LoadingScene newLoadingScene() {
                return new MainLoadingScene();
            }
        });
        settings.setApplicationMode(ApplicationMode.DEVELOPER);
    }

    private LazyValue<LevelEndScene> levelEndScene = new LazyValue<>(() -> new LevelEndScene());
    private static Entity player;
    private static Entity player2;
    private Entity player3;
    private Entity player4;
    private Entity player5;
    private Entity player6;
    // private Client client;

    /**********************전광판에 띄울 추가 코드******************/
    private Text text1;
    private Text text2;

    private static int playerID;
    private static Client<Bundle> client;
    
    
    @Override
    protected void initInput() {


        onBtnDown(MouseButton.PRIMARY, () -> shoot());
        getInput().addAction(new UserAction("Left") {
            @Override
            protected void onAction() {
                player.getComponent(PlayerComponent.class).left();
                
            }

            @Override
            protected void onActionEnd() {
                player.getComponent(PlayerComponent.class).stop();
            }
        }, KeyCode.A, VirtualButton.LEFT);

        getInput().addAction(new UserAction("Right") {
            @Override
            protected void onAction() {
                player.getComponent(PlayerComponent.class).right();
                // if (connection != null) {
                //     connection.send("Right"); // Connection 객체를 통해 메시지 전송
                // }
            }

            @Override
            protected void onActionEnd() {
                player.getComponent(PlayerComponent.class).stop();
            }
        }, KeyCode.D, VirtualButton.RIGHT);

        getInput().addAction(new UserAction("Jump") {
            @Override
            protected void onActionBegin() {
                player.getComponent(PlayerComponent.class).jump();
                // if (connection != null) {
                //     connection.send("Jump"); // Connection 객체를 통해 메시지 전송
                // }
            }
        }, KeyCode.W, VirtualButton.A);

        getInput().addAction(new UserAction("Use") {
            @Override
            protected void onActionBegin() {
                getGameWorld().getEntitiesByType(BUTTON)
                        .stream()
                        .filter(btn -> btn.hasComponent(CollidableComponent.class) && player.isColliding(btn))
                        .forEach(btn -> {
                            btn.removeComponent(CollidableComponent.class);

                            Entity keyEntity = btn.getObject("keyEntity");
                            keyEntity.setProperty("activated", true);

                            KeyView view = (KeyView) keyEntity.getViewComponent().getChildren().get(0);
                            view.setKeyColor(Color.RED);

                            makeExitDoor();
                        });
            }
        }, KeyCode.E, VirtualButton.B);
    }

    private void shoot() {
        spawn("bullet", player.getPosition().add(30, -5));
     //   Entity bulletEntity = spawn("bullet", player.getPosition().add(30, -5));
    //    Point2D bulletPosition = bulletEntity.getPosition();
               
    
        }



    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("level", STARTING_LEVEL);
        vars.put("levelTime", 0.0);
        vars.put("score", 0);
        vars.put("hp", PLAYER_HP); // hp 부여
        vars.put("secondaryCharge", 0); // hp UI(초록색 원)
        vars.put("lives", 1); // 플레이어 목숨 부여
        vars.put("kills", 0);

    }

    @Override
    protected void onPreInit() {
        getSettings().setGlobalMusicVolume(0.25);
        loopBGM("BGM_dash_runner.wav");
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new PlatformerFactory());

        player = null;
        player2 = null;
        player3 = null;
        player4 = null;
        player5 = null;
        player6 = null;

        playerID = 2;

        nextLevel();

        // player must be spawned after call to nextLevel, otherwise player gets removed
        // before the update tick _actually_ adds the player to game world
        player = spawn("player", 50, 50);
        player2 = spawn("player", 50, 60 );
        // player3 = spawn("player", 50, 70);
        // player4 = spawn("player", 50, 80 );
        // player5 = spawn("player", 50, 90);
        // player6 = spawn("player", 50, 100 );
        set("player", player);
        set("player2", player2);

        // set("player3", player3);
        // set("player4", player4);
        // set("player5", player5);
        // set("player6", player6);

       
        

        spawn("background");


/***************************hp 코드 *************************************/
getWorldProperties().<Integer>addListener("hp", (prev, now) -> {
    if (now > PLAYER_HP){
        set("hp", PLAYER_HP);

    }

    if (now <= 0) {
        System.out.println("killed by timeout");
        set("hp", PLAYER_HP);
    }

});

if (IS_TIME_HP_PENALTY) {
    run(() -> inc("hp", TIME_PENALTY), PENALTY_INTERVAL);
}

getWorldProperties().<Integer>addListener("secondaryCharge", (prev, now) -> {
    if (now > MAX_CHARGES_SECONDARY)
        set("secondaryCharge", MAX_CHARGES_SECONDARY);
});
/******************************************************************************/
/***************************목숨 코드 *************************************/
getWorldProperties().<Integer>addListener("lives", (prev, now) -> {
    if (now == 0)
        System.out.println("남은 목숨이 없습니다!!");
});
/******************************************************************************/


/**********************스코어 표시************************************/
        text1 = getUIFactoryService().newText("PLAYER 1 남은 목숨: " + geti("lives") //score로직 추가 필요
                , Color.RED, 30.0);
        text2 = getUIFactoryService().newText("PLAYER 2 남은 목숨: " + geti("lives") //score로직 추가 필요
                , Color.BLUE, 30.0);
        text1.setTranslateX(100);
        text1.setTranslateY(100);
        getGameScene().addUINode(text1);
        text2.setTranslateX(100);
        text2.setTranslateY(150);
        getGameScene().addUINode(text2);
/**********************스코어 표시************************************/


        Viewport viewport = getGameScene().getViewport();
        viewport.setBounds(-1500, 0, 250 * 70, getAppHeight());
        viewport.bindToEntity(player, getAppWidth() / 2, getAppHeight() / 2);
        viewport.setLazy(true);


        client = FXGL.getNetService().newTCPClient("192.168.0.40", 55555);
        
        client.setOnConnected(connection -> {
            
            var pos = player.getPosition();
                var bundle = new Bundle("PlayerPosition");
                bundle.put("PI", playerID);
                bundle.put("x", pos.getX());
                bundle.put("y", pos.getY());

                // bundle.put("x", 100);
                // bundle.put("y", 50);
            
            connection.send(bundle);


            connection.addMessageHandlerFX((conn, message) -> {
                // 서버로부터 메시지를 받았을 때 처리할 로직
                try {
                    Bundle bdd = (Bundle) message;
            
                    int enemyID = bdd.get("PI");
                    if (enemyID == 2) {
                        double receivedx = bdd.get("x");
                        double receivedy = bdd.get("y");
                        System.out.println("이닛게임 들어와");
                        System.out.print(receivedx);
                        System.out.print(receivedy);

                        player2.getComponent(PhysicsComponent.class).overwritePosition(new Point2D(receivedx-20, receivedy-20));
                    }
                } catch (Exception e) {
                    // 예외 발생 시 처리할 로직
                    e.printStackTrace(); // 또는 다른 처리를 수행할 수 있습니다.
                }

            });
        });
        client.connectAsync();

        // //매 프레임마다 플레이어의 위치를 서버로 전송
        // runOnce(() -> {
        //     if (player != null) {
        //         var pos = player.getPosition();
        //         var bundle = new Bundle("PlayerPosition");
        //         // bundle.put("x", pos.getX());
        //         // bundle.put("y", pos.getY());

        //         bundle.put("x", 100);
        //         bundle.put("y", 50);

        // //         var bundle = new Bundle();
        // // bundle.put("key", 1); // 여기서 "key"는 임의로 지정한 문자열이며, 원하는 대로 변경할 수 있습니다.
        
        //         client.send(bundle);
        //     }
        // }, Duration.seconds(0.1));  // 0.1초마다 플레이어의 위치를 전송
    }
    

    @Override
    protected void initPhysics() {
        getPhysicsWorld().setGravity(0, 300);
        getPhysicsWorld().addCollisionHandler(new PlayerButtonHandler());

        PhysicsWorld physicsWorld = getPhysicsWorld();

        physicsWorld.addCollisionHandler(new CollisionHandler(BULLET, PLAYER) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity player) {
                bullet.removeFromWorld();
                player.removeFromWorld();
                
            }
        });






        onCollisionOneTimeOnly(PLAYER, EXIT_SIGN, (player, sign) -> {
            var texture = texture("exit_sign.png").brighter();
            texture.setTranslateX(sign.getX() + 9);
            texture.setTranslateY(sign.getY() + 13);

            var gameView = new GameView(texture, 150);

            getGameScene().addGameView(gameView);

            runOnce(() -> getGameScene().removeGameView(gameView), Duration.seconds(1.6));
        });

        onCollisionOneTimeOnly(PLAYER, EXIT_TRIGGER, (player, trigger) -> {
            makeExitDoor();
        });

        onCollisionOneTimeOnly(PLAYER, DOOR_BOT, (player, door) -> {
            levelEndScene.get().onLevelFinish();

            // the above runs in its own scene, so fade will wait until
            // the user exits that scene
            getGameScene().getViewport().fade(() -> {
                nextLevel();
            });
        });

        onCollisionOneTimeOnly(PLAYER, MESSAGE_PROMPT, (player, prompt) -> {
            prompt.setOpacity(1);

            despawnWithDelay(prompt, Duration.seconds(4.5));
        });

        onCollisionBegin(PLAYER, KEY_PROMPT, (player, prompt) -> {
            String key = prompt.getString("key");

            var entity = getGameWorld().create("keyCode", new SpawnData(prompt.getX(), prompt.getY()).put("key", key));
            spawnWithScale(entity, Duration.seconds(1), Interpolators.ELASTIC.EASE_OUT());

            runOnce(() -> {
                despawnWithScale(entity, Duration.seconds(1), Interpolators.ELASTIC.EASE_IN());
            }, Duration.seconds(2.5));
        });
    }

    private void makeExitDoor() {
        var doorTop = getGameWorld().getSingleton(DOOR_TOP);
        var doorBot = getGameWorld().getSingleton(DOOR_BOT);

        doorBot.getComponent(CollidableComponent.class).setValue(true);

        doorTop.setOpacity(1);
        doorBot.setOpacity(1);
    }

    private void nextLevel() {
        if (geti("level") == MAX_LEVEL) {
            showMessage("You finished the demo!");
            return;
        }

        inc("level", +1);

        setLevel(geti("level"));
    }

    @Override
    protected void initUI() {
        if (isMobile()) {
            var dpadView = getInput().createVirtualDpadView();
            var buttonsView = getInput().createXboxVirtualControllerView();

            addUINode(dpadView, 0, getAppHeight() - 290);
            addUINode(buttonsView, getAppWidth() - 280, getAppHeight() - 290);
        }
/********************************* 게임 시간 보여주는 기능 추가 ****************************/
        var textUserTime = getUIFactoryService().newText("", Color.WHITE, 30.0);
        textUserTime.setTranslateX(100);
        textUserTime.setTranslateY(50);
        getGameScene().addUINode(textUserTime);

        run(() -> {
            Duration userTime = Duration.seconds(getd("levelTime"));
            textUserTime.setText(String.format("TIME: %.0f sec", PLAY_TIME - userTime.toSeconds()));
        }, Duration.seconds(0.1)); // 0.1초마다 업데이트
    }
/********************************* 게임 시간 보여주는 기능 추가 ****************************/


    @Override
    protected void onUpdate(double tpf) {
        inc("levelTime", tpf);

        

            // player2.setPosition(50.0, player.getPosition().getY()- 15.0);
          
            //player2.setPosition((double)100.0, (double)50.0);

        client.setOnConnected(connection -> {
            
            var pos = player.getPosition();
                
            playerPositionBundle.put("PI", playerID);
            playerPositionBundle.put("x", pos.getX());
            playerPositionBundle.put("y", pos.getY());

                // bundle.put("x", 100);
                // bundle.put("y", 50);
                // 클라이언트에 번들 잘 찍히는지. 
            // System.out.println(playerPositionBundle);
            connection.send(playerPositionBundle);


            connection.addMessageHandlerFX((conn, message) -> {
                // 서버로부터 메시지를 받았을 때 처리할 로직
                System.out.println(message);
             

                // System.out.println("메세지임");
                try{
                Bundle playerPositionBundle = (Bundle) message;
            
                
                
                if ((int)playerPositionBundle.get("PI") == 2) {
                    double  receivedx = (double)playerPositionBundle.get("x");
                    double  receivedy = (double)playerPositionBundle.get("y");
        
                    // player2 엔티티의 위치를 업데이트
                    // System.out.print("플레이어2 조정하는중");
                    // System.out.print("x" + receivedx);
                    // System.out.print(" y  "+ receivedy);
                    // System.out.println(" ");
                    
                    
                    // Platform.runLater(() -> {
                    //     player2.setPosition((double)receivedx, (double)receivedy);
                    //     //System.out.println(player2.getPosition().getX());
                    // });

                    Platform.runLater(() -> {
                        player2.getComponent(PhysicsComponent.class).overwritePosition(new Point2D(receivedx-20, receivedy-20));
                    });
                    
        
                }
                // player2.setPosition(100, 50);
            }catch (Exception e) {
                // 예외 발생 시 처리할 로직
                e.printStackTrace(); // 또는 다른 처리를 수행할 수 있습니다.
            }

            });


        });
        client.connectAsync();
        // player2.setPosition((double)receivedx, (double)receivedy);

        if (player.getY() > getAppHeight()) {
            onPlayerDied();
        }

        /**********************게임시간 종료 로직 **********************************************/
        Duration elapsedTime = Duration.seconds(getd("levelTime"));

        if (elapsedTime.greaterThanOrEqualTo(Duration.seconds(PLAY_TIME))) {
            //showMessage("Player?" +"  Win!");
            levelEndScene.get().onLevelFinish();

            // the above runs in its own scene, so fade will wait until
            // the user exits that scene
            //FXGL.getGameController().startNewGame();

            //getGameController().exit();

        }

/**************************************게임시간 종료 로직*******************************/

    }


    public void onPlayerDied() {
//        setLevel(geti("level"));
        if (player != null) {
            int curUserLife = geti("lives"); // 죽기 직전 남은 목숨 개수

            //남은 목숨이 1인 경우 => 더이상 리스폰하지 않음
            if (curUserLife <= 1) {
                text1.setText("PLAYER 1 남은 목숨: " + 0);
                System.out.println("남은 목숨 개수 0");
                levelEndScene.get().onLevelFinish();
            }
            //남은 목숨이 2이상인 경우 => 위치 재조정, hp초기화, 목숨 감소
            else {
                // 전광판 남은 목숨 변경
                text1.setText("PLAYER 1 남은 목숨: " + (curUserLife - 1));

                // 위치 재조정
                player.getComponent(PhysicsComponent.class).overwritePosition(new Point2D(50, 50));
                player.setZIndex(Integer.MAX_VALUE);

                //hp 초기화
                set("hp", PLAYER_HP);

                //목숨 감소
                inc("lives", -1);
            }
        }
    }

    private void setLevel(int levelNum) {
        if (player != null) {
            player.getComponent(PhysicsComponent.class).overwritePosition(new Point2D(50, 50));
            player.setZIndex(Integer.MAX_VALUE);
        }

        set("levelTime", 0.0);

        Level level = setLevelFromMap("tmx/level" + levelNum  + ".tmx");

        var shortestTime = level.getProperties().getDouble("star1time");

        var levelTimeData = new LevelEndScene.LevelTimeData(shortestTime * 2.4, shortestTime*1.3, shortestTime);

        set("levelTimeData", levelTimeData);
    }

    public static void main(String[] args) {
        launch(args);
    }
}