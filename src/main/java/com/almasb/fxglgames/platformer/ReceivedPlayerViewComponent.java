//이 페이지 전체 추가함
package com.almasb.fxglgames.platformer;

import com.almasb.fxgl.dsl.components.view.ChildViewComponent;
import javafx.beans.binding.Bindings;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;

import static com.almasb.fxgl.dsl.FXGL.getip;
import static com.almasb.fxglgames.platformer.Config.*;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class ReceivedPlayerViewComponent extends ChildViewComponent {

    public ReceivedPlayerViewComponent() {
        super(36, 36, false);

        var arcHP = new Arc(-20, -13, 25, 25, -90, 0);
        arcHP.setStroke(Color.RED.brighter());
        arcHP.setStrokeWidth(2.5);
        arcHP.setFill(null);
        arcHP.lengthProperty().bind(
                getip("hp2").multiply(-360.0).divide(PLAYER_HP)
        );

        var arcSP = new Arc(0, 0, 40, 40, -90, 0);
        arcSP.setStroke(Color.LIGHTBLUE.brighter().brighter());
        arcSP.setStrokeWidth(3.5);
        arcSP.setFill(null);
        arcSP.lengthProperty().bind(
                getip("secondaryCharge").multiply(-360.0).divide(MAX_CHARGES_SECONDARY)
        );
        arcSP.opacityProperty().bind(
                Bindings.when(getip("secondaryCharge").lessThan(MAX_CHARGES_SECONDARY))
                        .then(0.15)
                        .otherwise(1.0)
        );

        getViewRoot().getChildren().addAll(arcHP, arcSP);
    }
}