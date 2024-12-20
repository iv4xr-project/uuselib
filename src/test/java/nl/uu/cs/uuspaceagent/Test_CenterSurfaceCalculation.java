package nl.uu.cs.uuspaceagent;

import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the calculation for finding the location of the center point of the surfaces of a block.
 */
public class Test_CenterSurfaceCalculation {

    @Test
    public void test1() throws InterruptedException {
        var agentAndState = TestUtils.loadSE("myworld-3") ;
        var agent = agentAndState.fst ;
        var state = agentAndState.snd ;
        Thread.sleep(1000);
        state.updateState(state.agentId);

        WorldEntity survivalKit = SEBlockFunctions.findClosestBlock(state.worldmodel,"SurvivalKitLarge", 10) ;

        assertTrue(survivalKit != null) ;
        assertTrue(survivalKit.getStringProperty("blockType").equals("SurvivalKitLarge")) ;
        assertTrue(Vec3.sub(survivalKit.position, state.worldmodel.position).length() <= 10) ;

        WorldEntity battery = SEBlockFunctions.findClosestBlock(state.worldmodel,"LargeBlockBatteryBlock", 8) ;
        assertTrue(battery == null) ;
        battery = SEBlockFunctions.findClosestBlock(state.worldmodel,"LargeBlockBatteryBlock", 20) ;
        assertTrue(battery.getStringProperty("blockType").equals("LargeBlockBatteryBlock")) ;
        assertTrue(Vec3.sub(battery.position, state.worldmodel.position).length() <= 20) ;

        Vec3 survivalKitFrontSidePoint  = SEBlockFunctions.getSideCenterPoint(survivalKit, SEBlockFunctions.BlockSides.FRONT,0) ;
        Vec3 survivalKitBackSidePoint  = SEBlockFunctions.getSideCenterPoint(survivalKit, SEBlockFunctions.BlockSides.BACK,0) ;
        Vec3 survivalKitRightSidePoint  = SEBlockFunctions.getSideCenterPoint(survivalKit, SEBlockFunctions.BlockSides.RIGHT,0) ;
        Vec3 survivalKitLeftSidePoint  = SEBlockFunctions.getSideCenterPoint(survivalKit, SEBlockFunctions.BlockSides.LEFT,0) ;
        System.out.println("** Survival-kit's FRONT's center: " + survivalKitFrontSidePoint) ;
        System.out.println("** Survival-kit's BACK's center: " + survivalKitBackSidePoint) ;
        System.out.println("** Survival-kit's RIGHT's center: " + survivalKitRightSidePoint) ;
        System.out.println("** Survival-kit's LEFT's center: " + survivalKitLeftSidePoint) ;
        Vec3 center = (Vec3) survivalKit.getProperty("centerPosition") ;
        assertEquals(survivalKitFrontSidePoint, new Vec3(center.x + 1.25f, center.y, center.z)) ;
        assertEquals(survivalKitBackSidePoint, new Vec3(center.x - 1.25f, center.y, center.z)) ;
        assertEquals(survivalKitRightSidePoint, new Vec3(center.x, center.y, center.z + 1.25f)) ;
        assertEquals(survivalKitLeftSidePoint, new Vec3(center.x, center.y, center.z - 1.25f)) ;
    }
}
