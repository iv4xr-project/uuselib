package nl.uu.cs.uuspaceagent.construction;

import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.utils.Pair;
import nl.uu.cs.uuspaceagent.TestUtils;
import nl.uu.cs.uuspaceagent.UUGoalLib;
import nl.uu.cs.uuspaceagent.UUSeAgentState;
import nl.uu.cs.uuspaceagent.UUTacticLib;

import org.junit.jupiter.api.Test;
import static nl.uu.cs.aplib.AplibEDSL.* ;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import static nl.uu.cs.uuspaceagent.PrintInfos.showWOMAgent;
import static nl.uu.cs.uuspaceagent.TestUtils.console;
import static nl.uu.cs.uuspaceagent.TestUtils.loadSE;

public class Test_REPEATwith {

    /**
     * Auto-navigate to a given point, opening closed doors along the way.
     */
    public Pair<TestAgent,GoalStructure> deployAgent(GoalStructure G) throws InterruptedException {
        console("*** start test...") ;
        var agentAndState = loadSE("islanddswithdoors") ;
        TestAgent agent = agentAndState.fst ;
        UUSeAgentState state = agentAndState.snd ;
        Thread.sleep(1000);
        state.updateState(state.agentId);

        console(showWOMAgent(state.worldmodel));

        //float dth = 1.3f * Grid2DNav.SQUARE_SIZE ;
        //final float distance_to_sq_threshold = dth*dth ;

        agent.setGoal(G) ;

        int turn= 0 ;
        while(G.getStatus().inProgress()) {
            console(">> [" + turn + "] " + showWOMAgent(state.worldmodel));
            agent.update();
            Thread.sleep(50);
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
    public void test_repeatwith() throws InterruptedException {
        // This is a position that is hidden behind three doors that should be opened along the way.
    	Vec3 dest = new Vec3(43f, 1.25f, 6);
    	Vec3 dest2 = new Vec3(15f, 1.25f, 25);
    	
    	List<GoalStructure> goals = Arrays.asList(new GoalStructure[] {
    		DEPLOY(UUGoalLib.closeTo(dest)),
    		DEPLOY(UUGoalLib.closeTo(dest2))
    	});
    	
    	GoalStructure G = UUGoalLib.REPEATwith(goals.iterator());
    	
        var agent_and_goal = deployAgent(G);
        TestAgent agent = agent_and_goal.fst ;
        agent.setTestDataCollector(new TestDataCollector()) ;
        G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
        console("*** test succesful!") ;
        //assertTrue(agent.getTestDataCollector().getNumberOfPassVerdictsSeen() == 2) ;
    }
}
