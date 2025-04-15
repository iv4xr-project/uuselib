package nl.uu.cs.uuspaceagent.construction;

import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.AplibEDSL;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.utils.Pair;
import nl.uu.cs.uuspaceagent.SEBlockFunctions;
import nl.uu.cs.uuspaceagent.TestUtils;
import nl.uu.cs.uuspaceagent.UUGoalLib;
import nl.uu.cs.uuspaceagent.UUSeAgentState;
import nl.uu.cs.uuspaceagent.UUTacticLib;
import spaceEngineers.model.DefinitionId;

import org.junit.jupiter.api.Test;
import static nl.uu.cs.aplib.AplibEDSL.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import static nl.uu.cs.uuspaceagent.PrintInfos.showWOMAgent;
import static nl.uu.cs.uuspaceagent.TestUtils.console;
import static nl.uu.cs.uuspaceagent.TestUtils.loadSE;

public class Test_ItemTransfers {

    /**
     * Auto-navigate to a given point and then place a block there.
     */
    public Pair<TestAgent,GoalStructure> deployAgent(GoalStructure transfer) throws InterruptedException {
        console("*** start test...") ;
        var agentAndState = loadSE("blockPlacementTests") ;
        TestAgent agent = agentAndState.fst ;
        UUSeAgentState state = agentAndState.snd ;
        Thread.sleep(1000);
        state.updateState(state.agentId);

        console(showWOMAgent(state.worldmodel));

        // Container block to access
        var block = SEBlockFunctions.findClosestBlock(state.worldmodel, (WorldEntity we) -> {
        	return we.getProperty("blockType").toString().contains("Container");
        });

        GoalStructure G = SEQ( 
        		DEPLOY(UUGoalLib.accessedBlockInventory(block)),
        		transfer
        		);
        
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
        return new Pair<>(agent,G) ;
    }


    @Test
    public void test_transfer1() throws InterruptedException {
    	
    	GoalStructure transferItem = goal("transfer items from container")
        		.toSolve((Integer transferredCount) -> {
        			return transferredCount > 0;
        		})
        		.withTactic(FIRSTof(UUTacticLib.withdrawItemToPlayer(new DefinitionId("AmmoMagazine", "NATO_5p56x45mm")).lift(), ABORT()))
        		.lift();
    	
        var agent_and_goal = deployAgent(transferItem);
        TestAgent agent = agent_and_goal.fst ;
        agent.setTestDataCollector(new TestDataCollector()) ;
        GoalStructure G = agent_and_goal.snd;
        G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
        console("*** test succesful!") ;
        //assertTrue(agent.getTestDataCollector().getNumberOfPassVerdictsSeen() == 2) ;
    }
    
    @Test
    public void test_transfer2() throws InterruptedException {
    	
    	GoalStructure transferItem = goal("transfer items to container")
        		.toSolve((Integer transferredCount) -> {
        			return transferredCount > 0;
        		})
        		.withTactic(FIRSTof(UUTacticLib.depositItemToContainer(new DefinitionId("AmmoMagazine", "NATO_5p56x45mm")).lift(), ABORT()))
        		.lift();
    	
        var agent_and_goal = deployAgent(transferItem);
        TestAgent agent = agent_and_goal.fst ;
        agent.setTestDataCollector(new TestDataCollector()) ;
        GoalStructure G = agent_and_goal.snd;
        G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
        console("*** test succesful!") ;
        //assertTrue(agent.getTestDataCollector().getNumberOfPassVerdictsSeen() == 2) ;
    }
    
}
