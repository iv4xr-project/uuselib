package nl.uu.cs.uuspaceagent.construction;

import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
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

public class Test_PlaceBlockAt {

    /**
     * Auto-navigate to a given point and then place a block there.
     */
    public Pair<TestAgent,GoalStructure> deployAgent(Vec3 destination) throws InterruptedException {
        console("*** start test...") ;
        var agentAndState = loadSE("blockPlacementTests") ;
        TestAgent agent = agentAndState.fst ;
        UUSeAgentState state = agentAndState.snd ;
        Thread.sleep(1000);
        state.updateState(state.agentId);

        console(showWOMAgent(state.worldmodel));

        var sqAgent = state.navgrid.gridProjectedLocation(state.worldmodel.position) ;
        var sqDestination = state.navgrid.gridProjectedLocation(destination) ;
        var centerOfSqDestination = state.navgrid.getSquareCenterLocation(sqDestination) ;

        var itemId = DefinitionId.Companion.cubeBlock("LargeHeavyBlockArmorBlock");

        GoalStructure placeBlock = DEPLOY(UUGoalLib.placedBlockAt(destination, itemId));
        
        // Goal to check that placement was correct
        GoalStructure checkCorrectness = goal("check succesful placement")
        		.toSolve((Boolean e) -> {
        			return e;
        		})
        		.withTactic(
        				SEQ(
        						UUTacticLib.equip(new DefinitionId(DefinitionId.PHYSICAL_GUN, "AngleGrinder4Item")),
        						action("checkTargetBlock").on((UUSeAgentState state2) -> {
        							var targetBlock = state2.targetBlock();
        							var req1 = targetBlock.getProperty("blockType").toString().equals(itemId.getType());
        							var req2 = SEBlockFunctions.pointInsideArea(
        									Vec3.sub(targetBlock.position, Vec3.one()), 
        									Vec3.add(targetBlock.position, Vec3.one()),
        									destination);      			
        							
        							console(req1 + " and " + req2);
        							return Boolean.valueOf(req1 && req2);
        						}).do2((UUSeAgentState na) -> (Boolean queryResult) -> { 
        							return queryResult; 
    							})
        						.lift(),
        						ABORT()
						)
				).lift();
        
        
        GoalStructure G = SEQ(placeBlock, UUGoalLib.faceToward("face towards new block", destination), checkCorrectness);
        
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
    public void test_placeBlockAt1() throws InterruptedException {
        //TODO: finish writing this test
        Vec3 dest = new Vec3(8.75f, -3.75f, 40);
        var agent_and_goal = deployAgent(dest);
        TestAgent agent = agent_and_goal.fst ;
        agent.setTestDataCollector(new TestDataCollector()) ;
        GoalStructure G = agent_and_goal.snd;
        G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
        console("*** test succesful!") ;
        //assertTrue(agent.getTestDataCollector().getNumberOfPassVerdictsSeen() == 2) ;
    }
    
    @Test
    public void test_placeBlockAt2() throws InterruptedException {
        //TODO: finish writing this test
        Vec3 dest = new Vec3(13.75f, -1.25f, 42.5f);
        var agent_and_goal = deployAgent(dest);
        TestAgent agent = agent_and_goal.fst ;
        agent.setTestDataCollector(new TestDataCollector()) ;
        GoalStructure G = agent_and_goal.snd;
        G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
        console("*** test succesful!") ;
        //assertTrue(agent.getTestDataCollector().getNumberOfPassVerdictsSeen() == 2) ;
    }
    
    
    @Test
    public void test_placeBlockAt3() throws InterruptedException {
        //TODO: finish writing this test
        Vec3 dest = new Vec3(18.75f, 1.25f, 42.5f);
        var agent_and_goal = deployAgent(dest);
        TestAgent agent = agent_and_goal.fst ;
        agent.setTestDataCollector(new TestDataCollector()) ;
        GoalStructure G = agent_and_goal.snd;
        G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
        console("*** test succesful!") ;
        //assertTrue(agent.getTestDataCollector().getNumberOfPassVerdictsSeen() == 2) ;
    }
    
    @Test
    public void test_placeBlockAt4() throws InterruptedException {
        //TODO: finish writing this test
        Vec3 dest = new Vec3(8.75f, -1.25f, 40f);
        var agent_and_goal = deployAgent(dest);
        TestAgent agent = agent_and_goal.fst ;
        agent.setTestDataCollector(new TestDataCollector()) ;
        GoalStructure G = agent_and_goal.snd;
        G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
        console("*** test succesful!") ;
        //assertTrue(agent.getTestDataCollector().getNumberOfPassVerdictsSeen() == 2) ;
    }
    
    @Test
    public void test_placeBlockAt5() throws InterruptedException {
        //TODO: finish writing this test
        Vec3 dest = new Vec3(8.75f, 1.25f, 42.5f);
        var agent_and_goal = deployAgent(dest);
        TestAgent agent = agent_and_goal.fst ;
        agent.setTestDataCollector(new TestDataCollector()) ;
        GoalStructure G = agent_and_goal.snd;
        G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
        console("*** test succesful!") ;
        //assertTrue(agent.getTestDataCollector().getNumberOfPassVerdictsSeen() == 2) ;
    }
}
