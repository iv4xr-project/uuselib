package nl.uu.cs.uuspaceagent.construction;

import static nl.uu.cs.aplib.AplibEDSL.ABORT;
import static nl.uu.cs.aplib.AplibEDSL.DEPLOY;
import static nl.uu.cs.aplib.AplibEDSL.SEQ;
import static nl.uu.cs.aplib.AplibEDSL.action;
import static nl.uu.cs.aplib.AplibEDSL.goal;
import static nl.uu.cs.uuspaceagent.PrintInfos.showWOMAgent;
import static nl.uu.cs.uuspaceagent.TestUtils.console;
import static nl.uu.cs.uuspaceagent.TestUtils.loadSE;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.json.*;

import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import spaceEngineers.model.Block;
import spaceEngineers.model.CharacterObservation;
import spaceEngineers.model.DefinitionId;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.utils.Pair;
import nl.uu.cs.uuspaceagent.DPos3;
import nl.uu.cs.uuspaceagent.SEBlockFunctions;
import nl.uu.cs.uuspaceagent.TestUtils;
import nl.uu.cs.uuspaceagent.UUGoalLib;
import nl.uu.cs.uuspaceagent.UUSeAgentState;
import nl.uu.cs.uuspaceagent.UUTacticLib;

public class Test_ConstructionPlanner {

    /**
     * Auto-navigate to a given point and then place a block there.
     */
    public Pair<TestAgent,GoalStructure> deployAgent(Blueprint blueprint, Vec3 location, ConstructionOptimizer optimizer) throws InterruptedException {
        console("*** start test...") ;
        var agentAndState = loadSE("ConstructionPlatform") ;
        TestAgent agent = agentAndState.fst ;
        UUSeAgentState state = agentAndState.snd ;
        Thread.sleep(1000);
        state.updateState(state.agentId);

        //Find grid
        WorldEntity grid = null;
        for (var e : state.worldmodel.elements.keySet())
        {
        	if (state.worldmodel.elements.get(e).type == "grid")
        	{
        		grid = state.worldmodel.elements.get(e);
        	}
        }  
        ConstructionPlanner planner = new ConstructionPlanner(blueprint, location, grid, state, optimizer);

        GoalStructure buildStructure = UUGoalLib.REPEATwith(planner);
        
        GoalStructure G = SEQ(buildStructure);
        
        agent.setGoal(G) ;

        int turn= 0 ;
        long start = System.currentTimeMillis();
        while(G.getStatus().inProgress()) {
            console(">> [" + turn + "] (" + 
            		Math.floor(planner.blueprint.getProgress()*100) + "%) " + 
            		showWOMAgent(state.worldmodel));
            agent.update();
            Thread.sleep(50);
            turn++ ;
            //if (turn >= 1400) break ;
        }
        long end = System.currentTimeMillis();
        float runtime = (end - start)/1000;
        System.out.println("Test took " + runtime + " seconds");

        JsonUtils.addRecord(blueprint.name, optimizer, runtime, turn);
        
        TestUtils.closeConnectionToSE(state);
        return new Pair<>(agent,G) ;
    }
    
    @Test
    public void test_construction1() throws InterruptedException {
    	// Builds a simple box house out of armor blocks.
    	
    	Blueprint blueprint = Blueprint.loadFromFile("assets/blueprints/simpleHouse.cons");
    
    	Vec3 dest = new Vec3(21.25f, -5f, 60);
        
    	var optimizer = ConstructionOptimizer.CustomOptimizer(2);
    	
        var agent_and_goal = deployAgent(blueprint, dest, optimizer);
        TestAgent agent = agent_and_goal.fst ;
        agent.setTestDataCollector(new TestDataCollector()) ;
        GoalStructure G = agent_and_goal.snd;
        //G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
        console("*** test succesful!") ;
        //assertTrue(agent.getTestDataCollector().getNumberOfPassVerdictsSeen() == 2) ;
    }
    
    @Test
    public void test_construction2() throws InterruptedException {
    	// Builds a chain of armor blocks that snakes in the air.
    	
    	Blueprint blueprint = Blueprint.loadFromFile("assets/blueprints/snake3D.cons");
    
    	Vec3 dest = new Vec3(21.25f, -5f, 60);
        
        var agent_and_goal = deployAgent(blueprint, dest, null);
        TestAgent agent = agent_and_goal.fst ;
        agent.setTestDataCollector(new TestDataCollector()) ;
        GoalStructure G = agent_and_goal.snd;
        G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
        console("*** test succesful!") ;
        //assertTrue(agent.getTestDataCollector().getNumberOfPassVerdictsSeen() == 2) ;
    }
    
    @Test
    public void test_construction3() throws InterruptedException {
    	// Builds a number of unique blocks such as doors and cargo containers.
    	
    	Blueprint blueprint = Blueprint.loadFromFile("assets/blueprints/blockTypes.cons");
    
    	Vec3 dest = new Vec3(21.25f, -5f, 60);
    	
    	ConstructionOptimizer optimizer = null;
        
        var agent_and_goal = deployAgent(blueprint, dest, optimizer);
        TestAgent agent = agent_and_goal.fst ;
        agent.setTestDataCollector(new TestDataCollector()) ;
        GoalStructure G = agent_and_goal.snd;
        G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
        console("*** test succesful!") ;
        //assertTrue(agent.getTestDataCollector().getNumberOfPassVerdictsSeen() == 2) ;
    }
    
    @Test
    public void test_construction4() throws InterruptedException {
    	// Builds a 4 pillars of 3 armor blocks high.
    	
    	Blueprint blueprint = Blueprint.loadFromFile("assets/blueprints/pillars.cons");
    
    	Vec3 dest = new Vec3(21.25f, -5f, 60);
        
    	ConstructionOptimizer optimizer = ConstructionOptimizer.DFS(3);
    	
        var agent_and_goal = deployAgent(blueprint, dest, optimizer);
        TestAgent agent = agent_and_goal.fst ;
        agent.setTestDataCollector(new TestDataCollector()) ;
        GoalStructure G = agent_and_goal.snd;
        G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
        console("*** test succesful!") ;
        //assertTrue(agent.getTestDataCollector().getNumberOfPassVerdictsSeen() == 2) ;
    }
    
    @Test
    public void test_construction5() throws InterruptedException {
    	// Try to force a priority block situation
    	
    	Blueprint blueprint = Blueprint.loadFromFile("assets/blueprints/3x2x3.cons");
    
    	Vec3 dest = new Vec3(21.25f, -5f, 60);
        
    	ConstructionOptimizer optimizer = null;
    	
        var agent_and_goal = deployAgent(blueprint, dest, optimizer);
        TestAgent agent = agent_and_goal.fst ;
        agent.setTestDataCollector(new TestDataCollector()) ;
        GoalStructure G = agent_and_goal.snd;
        G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
        console("*** test succesful!") ;
        //assertTrue(agent.getTestDataCollector().getNumberOfPassVerdictsSeen() == 2) ;
    }
    
    @Test
    public void test_construction6() throws InterruptedException {
    	// Try to force a priority block situation
    	
    	Blueprint blueprint = Blueprint.loadFromFile("assets/blueprints/flatStar.cons");
    
    	Vec3 dest = new Vec3(21.25f, -5f, 60);
        
    	ConstructionOptimizer optimizer = null;
    	
        var agent_and_goal = deployAgent(blueprint, dest, optimizer);
        TestAgent agent = agent_and_goal.fst ;
        agent.setTestDataCollector(new TestDataCollector()) ;
        GoalStructure G = agent_and_goal.snd;
        G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
        console("*** test succesful!") ;
        //assertTrue(agent.getTestDataCollector().getNumberOfPassVerdictsSeen() == 2) ;
    }
    
}