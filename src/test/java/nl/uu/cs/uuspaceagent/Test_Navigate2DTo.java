package nl.uu.cs.uuspaceagent;

import eu.iv4xr.framework.mainConcepts.* ;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.* ;
import nl.uu.cs.aplib.utils.Pair;
import org.junit.jupiter.api.Test;
//import spaceEngineers.transport.SocketReaderWriterKt;

import static nl.uu.cs.aplib.AplibEDSL.* ;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static nl.uu.cs.uuspaceagent.PrintInfos.showWOMAgent;
import static nl.uu.cs.uuspaceagent.TestUtils.console;
import static nl.uu.cs.uuspaceagent.TestUtils.loadSE;

/**
 * Testing 2D grid-based auto-navigation using the navigate2DToTAC tactic.
 */
public class Test_Navigate2DTo {

    /**
     * Auto-navigate to a given point.
     */
    public Pair<TestAgent,GoalStructure> test_navigate2DTo(Vec3 destination) throws InterruptedException {
        console("*** start test...") ;
        var agentAndState = loadSE("myworld-3 with open door") ;
        TestAgent agent = agentAndState.fst ;
        UUSeAgentState state = agentAndState.snd ;
        Thread.sleep(1000);
        state.updateState(state.agentId);

        // agent start location should be around: <10.119276,-5.0025,55.681934>
        //  orientationForward: <-0.043967947,-2.0614608E-4,0.9990329> ... so looking towards z-axis
        console(showWOMAgent(state.worldmodel));

        var sqAgent = state.navgrid.gridProjectedLocation(state.worldmodel.position) ;
        var sqDestination = state.navgrid.gridProjectedLocation(destination) ;
        var centerOfSqDestination = state.navgrid.getSquareCenterLocation(sqDestination) ;

        //float dth = 1.3f * Grid2DNav.SQUARE_SIZE ;
        //final float distance_to_sq_threshold = dth*dth ;

        GoalStructure G = goal("close to destination")
                .toSolve((Pair<Vec3,Vec3> positionAndOrientation) -> {
                    //var currentAgentSq = st.grid2D.gridProjectedLocation(st.wom.position) ;
                    //return currentAgentSq.equals(sqDestination) ;
                    var pos = positionAndOrientation.fst ;
                    return Vec3.sub(centerOfSqDestination,pos).lengthSq() <= UUTacticLib.THRESHOLD_SQUARED_DISTANCE_TO_SQUARE ;
                })
                .withTactic(UUTacticLib.navigateToTAC(destination))
                .lift() ;

        agent.setGoal(G) ;

        int turn= 0 ;
        while(G.getStatus().inProgress()) {
            console(">> [" + turn + "] " + showWOMAgent(state.worldmodel));
            agent.update();
            //Thread.sleep(50);
            turn++ ;
            if (turn >= 1400) break ;
        }

        //SocketReaderWriterKt.closeIfCloseable(state.env().getController());
        TestUtils.closeConnectionToSE(state);
        return new Pair<>(agent,G) ;
    }

    /**
     * Test navigating to a very close square. Mainly to see if the agent turning in the
     * right direction.
     */
    //@Test
    public void test_nav_to_veryclose_square() throws InterruptedException {
        // agent start location should be around: <9.549925,-5.0025005,54.149185>
        Vec3 dest = new Vec3(9,-5,56) ;
        var agent_and_goal = test_navigate2DTo(dest) ;
        var G = agent_and_goal.snd ;
        G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
    }

    /**
     * Destination lies in a straight and clear line from the agent.
     */
    //@Test
    public void test1() throws InterruptedException {
        // this is just before the buttons-panel
        Vec3 dest = new Vec3(10,-5,68) ;
        var agent_and_goal = test_navigate2DTo(dest) ;
        var G = agent_and_goal.snd ;
        G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
    }

    /**
     * Reachable, but the agent will have to bend a bit.
     */
    //@Test
    public void test2() throws InterruptedException {
        // just before the buttons-panel
        Vec3 dest = new Vec3(12f,-5,70) ; // buttons-panel dimension seems to be incorrect, it gets in a way
        var agent_and_goal = test_navigate2DTo(dest) ;
        var G = agent_and_goal.snd ;
        G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
    }

    /**
     * Navigate to a position some distance before a door. The agent has to make a U-turn to pass
     * over a sticking wall.
     */
    //@Test
    public void test3() throws InterruptedException {
        // This is a position in front of a sliding-door. It is reachable from the
        // agent's start position.
        Vec3 dest = new Vec3(19,-5,65) ;
        var agent_and_goal = test_navigate2DTo(dest) ;
        var G = agent_and_goal.snd ;
        G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
    }

    /**
     * Navigate to a position beyond an open door. This is to test the ability of the 2D
     * grid nav to recognize inner corridors of a door.
     */
    @Test
    public void test4() throws InterruptedException {
        // This is a position in front of a sliding-door. It is reachable from the
        // agent's start position.
        Vec3 dest = new Vec3(19,-5,58) ;
        var agent_and_goal = test_navigate2DTo(dest) ;
        var G = agent_and_goal.snd ;
        G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
    }

}
