package nl.uu.cs.uuspaceagent.construction;

import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.utils.Pair;
import nl.uu.cs.uuspaceagent.DPos3;
import nl.uu.cs.uuspaceagent.TestUtils;
import nl.uu.cs.uuspaceagent.UUSeAgentState;
import nl.uu.cs.uuspaceagent.UUTacticLib;

import org.junit.jupiter.api.Test;
import uuspaceagent.*;

import static nl.uu.cs.aplib.AplibEDSL.* ;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static nl.uu.cs.uuspaceagent.PrintInfos.showWOMAgent;
import static nl.uu.cs.uuspaceagent.TestUtils.console;
import static nl.uu.cs.uuspaceagent.TestUtils.loadSE;

public class Test_ComplexPathfinding {

    /**
     * Auto-navigate to a given point, opening closed doors along the way.
     */
    public Pair<TestAgent,GoalStructure> deployAgent(Vec3 destination) throws InterruptedException {
        console("*** start test...") ;
        var agentAndState = loadSE("islanddswithdoors") ;
        TestAgent agent = agentAndState.fst ;
        UUSeAgentState state = agentAndState.snd ;
        Thread.sleep(1000);
        state.updateState(state.agentId);

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
                //TODO: implement a navigation tactic that allows opening doors
                // along the way to the destination.
                .withTactic(UUTacticLib.smartNavigateToTAC(destination)) 
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
        test_Goal(agent, state, G) ;
        return new Pair<>(agent,G) ;
    }

    public void test_Goal(TestAgent agent, UUSeAgentState state, GoalStructure G) throws InterruptedException {
        agent.setGoal(G) ;
        int turn= 0 ;
        while(G.getStatus().inProgress()) {
            //console(">> [" + turn + "] " + showWOMAgent(state.wom));
            agent.update();
            //Thread.sleep(50);
            turn++ ;
            if (turn >= 1400) break ;
        }
        //closeIfCloseable(state.env().getController());f
        TestUtils.closeConnectionToSE(state);
    }

    @Test
    public void test_navigate_through_doors() throws InterruptedException {
        // This is a position that is hidden behind three doors that should be opened along the way.
        console("*** start test...") ;
        Vec3 dest = new Vec3(43,1.25f,7.5f) ;
        var agent_and_goal = deployAgent(dest);
        TestAgent agent = agent_and_goal.fst ;
        agent.setTestDataCollector(new TestDataCollector()) ;
        GoalStructure G = agent_and_goal.snd;
        G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
        //assertTrue(agent.getTestDataCollector().getNumberOfPassVerdictsSeen() == 2) ;
    }
}
