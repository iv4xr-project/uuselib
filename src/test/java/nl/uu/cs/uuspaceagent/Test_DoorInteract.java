package nl.uu.cs.uuspaceagent;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Vec3;
import spaceEngineers.controller.useobject.UseObjectExtensions;
import spaceEngineers.model.Block;
import spaceEngineers.model.CharacterObservation;
import spaceEngineers.model.DoorBase;
import spaceEngineers.model.ToolbarLocation;

import static nl.uu.cs.uuspaceagent.TestUtils.loadSE;

/**
 * For trying to open a door directly using primitives from SE.
 */
public class Test_DoorInteract {

    @Test
    public void test_doorInteraction() throws InterruptedException {

    	var state = loadSE("myworld-3 atdoor").snd;

        state.updateState(state.agentId);
        Thread.sleep(50);
        state.updateState(state.agentId);
        
        WorldEntity agentInfo = state.worldmodel.elements.get(state.agentId) ;
        System.out.println(">>>> navgrid-origin " + state.navgrid.origin) ;
        System.out.println("** Agent @" + state.worldmodel.position) ;
        System.out.println("** Agent's info: " + PrintInfos.showWorldEntity(agentInfo));
        
        CharacterObservation cobs = state.env().getController().getObserver().observe() ;
        // this get the block that the agent is looking at (it should be looking at a door):
        Block targetBlock = cobs.getTargetBlock() ;
        assertTrue(targetBlock != null) ;
        System.out.println("=== target block: " + targetBlock.getId());
        
        // we get the world-entity representation of that target block. 
        // Note: The block/door is however stored as a sub-object, so you can't 
        // just get it straight from worldmodel.elements. 
        WorldEntity target = SEBlockFunctions.findWorldEntity(state.worldmodel,targetBlock.getId()) ;
        assertTrue(target != null) ;
        System.out.println("** door state 1: " + PrintInfos.showWorldEntity(target));

        assertTrue(target.id.equals(cobs.getTargetBlock().getId())) ;
        assertTrue(target.getProperty("blockType").equals("LargeBlockSlideDoor")) ;
        // check that the door is initially close:
        assertTrue(! target.getBooleanProperty("isOpen")) ;
                
        // open the door:
        UseObjectExtensions useUtil = new UseObjectExtensions(state.env().getController().getSpaceEngineers()) ;
        useUtil.openIfNotOpened((DoorBase) targetBlock);

        Thread.sleep(2000);
        state.updateState(state.agentId);

        // check that the door is now open:
        target = SEBlockFunctions.findWorldEntity(state.worldmodel,targetBlock.getId()) ;
        assertTrue(target != null) ;
        System.out.println("** door state 2: " + PrintInfos.showWorldEntity(target));
        assertTrue(target.getBooleanProperty("isOpen")) ;
        TestUtils.closeConnectionToSE(state);
    }
    
    @Test
    public void test_pathfinding_throughDoor() throws InterruptedException {

        var state = loadSE("myworld-3 atdoor").snd;

        state.updateState(state.agentId);
        Thread.sleep(50);
        state.updateState(state.agentId);
        
        WorldEntity agentInfo = state.worldmodel.elements.get(state.agentId) ;
        System.out.println(">>>> navgrid-origin " + state.navgrid.origin) ;
        System.out.println("** Agent @" + state.worldmodel.position) ;
        System.out.println("** Agent's info: " + PrintInfos.showWorldEntity(agentInfo));
        
        CharacterObservation cobs = state.env().getController().getObserver().observe() ;
        // this get the block that the agent is looking at (it should be looking at a door):
        Block targetBlock = cobs.getTargetBlock() ;
        assertTrue(targetBlock != null) ;
        System.out.println("=== target block: " + targetBlock.getId());
        
        // we get the world-entity representation of that target block. 
        // Note: The block/door is however stored as a sub-object, so you can't 
        // just get it straight from worldmodel.elements. 
        WorldEntity target = SEBlockFunctions.findWorldEntity(state.worldmodel,targetBlock.getId()) ;
        assertTrue(target != null) ;
        System.out.println("** door state 1: " + PrintInfos.showWorldEntity(target));
        // the door should be closed initially:
        assertTrue(! target.getBooleanProperty("isOpen")) ;
        
        // This is a location behind the door
        var destination = new Vec3(18.8f , -5f , 60f) ;
        DPos3 agentSq = state.navgrid.gridProjectedLocation(state.worldmodel.position) ;
        DPos3 destinationSq = state.navgrid.gridProjectedLocation(destination) ;
        List<DPos3> path = state.pathfinder.findPath(state.navgrid, agentSq, destinationSq)  ;
        // check that there is no navigation path to that location behind the door:
        assertTrue(path == null) ;
        
        // open the door:
        UseObjectExtensions useUtil = new UseObjectExtensions(state.env().getController().getSpaceEngineers()) ;
        useUtil.openIfNotOpened((DoorBase) targetBlock);

        Thread.sleep(2000);
        state.updateState(state.agentId);

        // the door should now be now open:
        target = SEBlockFunctions.findWorldEntity(state.worldmodel,targetBlock.getId()) ;
        assertTrue(target != null) ;
        System.out.println("** door state 2: " + PrintInfos.showWorldEntity(target));
        assertTrue(target.getBooleanProperty("isOpen")) ;
        
        // And there should now exist a navigation path to that location, because the door is
        // now open:
        path = state.pathfinder.findPath(state.navgrid, agentSq, destinationSq)  ;
        assertTrue(path != null) ;
        System.out.println(">>>> path: " + path) ;
        TestUtils.closeConnectionToSE(state);
    }
}
