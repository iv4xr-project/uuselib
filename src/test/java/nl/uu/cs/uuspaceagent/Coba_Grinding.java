package nl.uu.cs.uuspaceagent;

import eu.iv4xr.framework.mainConcepts.WorldEntity;
import org.junit.jupiter.api.Test;
import spaceEngineers.model.*;

import static nl.uu.cs.uuspaceagent.TestUtils.loadSE;

/**
 * For trying out grinding directly using primitives from SE.
 */
public class Coba_Grinding {

    @Test
    public void test() throws InterruptedException {

        var state = loadSE("myworld-3 in front of battery").snd;

        state.updateState(state.agentId);

        System.out.println("** Equiping grinder");
        state.env().equip(new ToolbarLocation(0,0));

        state.updateState(state.agentId);

        WorldEntity agentInfo = state.worldmodel.elements.get(state.agentId) ;
        System.out.println("** Agent's info: " + PrintInfos.showWorldEntity(agentInfo));

        /*
        System.out.println("======");
        CharacterObservation cobs = state.env().getController().getObserver().observe() ;
        if(cobs.getTargetBlock() != null) {
            System.out.println("=== target block: " + cobs.getTargetBlock().getId());
        }
        */

        WorldEntity target = SEBlockFunctions.findClosestBlock(state.worldmodel, "LargeBlockBatteryBlock", 10) ;
        String batteryId = target.id ;
        System.out.println("** target state: " + PrintInfos.showWorldEntity(target));

        System.out.println("** start grinding ");
        for(int k=0; k<1000; k++) {
            state.env().beginUsingTool();
            if(k % 200 == 0) {
                state.updateState(state.agentId);
                System.out.println(" >>> k=" + k) ;
                target = SEBlockFunctions.findWorldEntity(state.worldmodel,batteryId) ;
                if (target == null) {
                    System.out.println("** target is gone");
                    break ;
                }
                else {
                    System.out.println("** target state: " + PrintInfos.showWorldEntity(target));
                    //state.env().getController().getObserver().takeScreenshot("C:\\Users\\uprim\\AppData\\Roaming\\SpaceEngineers\\Screenshots\\shot" + k + ".png");
                    state.env().getController().getObserver().takeScreenshot("C:\\workshop\\projects\\iv4xr\\Screenshots\\shot" + k + ".png");
                    //state.env().getController().getObserver().takeScreenshot("C:\\workshop\\projects\\iv4xr\\shot" + k + ".png");
                }
            }
        }
        System.out.println("** stop grinding ");
        state.env().endUsingTool();

        //System.out.println("** Equiping hand :D ");
        //state.env().equip(new ToolbarLocation(9,0));


    }
}
