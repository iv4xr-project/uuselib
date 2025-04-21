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
import java.util.LinkedList;
import java.util.List;

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

public class Test_ConstructionOptimization {

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
    
    public void test_construction(String structureName, ConstructionOptimizer optimizer) throws InterruptedException {
    	// Builds a simple box house out of armor blocks.
    	
    	Blueprint blueprint = Blueprint.loadFromFile("assets/blueprints/" + structureName + ".cons");
    
    	Vec3 dest = new Vec3(21.25f, -5f, 60);
    	
        var agent_and_goal = deployAgent(blueprint, dest, optimizer);
        TestAgent agent = agent_and_goal.fst ;
        agent.setTestDataCollector(new TestDataCollector()) ;
        GoalStructure G = agent_and_goal.snd;
        //G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
        console("*** test succesful!") ;
        //assertTrue(agent.getTestDataCollector().getNumberOfPassVerdictsSeen() == 2) ;
    }
    
    @TestFactory
    Collection<DynamicTest> testFactory_optimization() {
    	var structures = Arrays.asList(
    			"pillars",
    			"3x2x3",
    			"flatDisk",
    			"flatCross",
    			"mushroom",
    			"simpleHouse"
    			);
    	var optimizers = Arrays.asList(
    			null,
    			ConstructionOptimizer.DFS(1),
    			ConstructionOptimizer.DFS(2),
    			ConstructionOptimizer.DFS(3),
    			ConstructionOptimizer.BFS(1),
    			ConstructionOptimizer.BFS(2),
    			ConstructionOptimizer.CustomOptimizer(1),
    			ConstructionOptimizer.CustomOptimizer(2)
    			);
    	
    	List<DynamicTest> tests = new LinkedList<>();
    	
    	for (String structure : structures) {
    		for (ConstructionOptimizer optimizer : optimizers) {
    			String optimizerName = optimizer != null ? optimizer.getName() : "Default";
    			String testName = structure + " (" + optimizerName + ")";
    			tests.add(DynamicTest.dynamicTest(
    					testName, 
    					() -> test_construction(structure, optimizer)));
    		}
    	}
    	
    	return tests;
    }
    
}