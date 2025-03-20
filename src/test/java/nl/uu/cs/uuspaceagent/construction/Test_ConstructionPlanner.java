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

import org.junit.jupiter.api.Test;

import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import spaceEngineers.model.Block;
import spaceEngineers.model.CharacterObservation;
import spaceEngineers.model.DefinitionId;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.utils.Pair;
import nl.uu.cs.uuspaceagent.Blueprint;
import nl.uu.cs.uuspaceagent.ConstructionPlanner;
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
    public Pair<TestAgent,GoalStructure> deployAgent(Blueprint blueprint, Vec3 location) throws InterruptedException {
        console("*** start test...") ;
        var agentAndState = loadSE("ConstructionPlatform") ;
        TestAgent agent = agentAndState.fst ;
        UUSeAgentState state = agentAndState.snd ;
        Thread.sleep(1000);
        state.updateState(state.agentId);

        console(showWOMAgent(state.worldmodel));


        //Find grid
        WorldEntity grid = null;
        for (var e : state.worldmodel.elements.keySet())
        {
        	if (state.worldmodel.elements.get(e).type == "grid")
        	{
        		grid = state.worldmodel.elements.get(e);
        	}
        }  
        
        ConstructionPlanner planner = new ConstructionPlanner(blueprint, location, grid, state);

        GoalStructure buildStructure = UUGoalLib.REPEATwith(planner);
        
        GoalStructure G = SEQ(buildStructure);
        
        agent.setGoal(G) ;

        int turn= 0 ;
        while(G.getStatus().inProgress()) {
            console(">> [" + turn + "] " + showWOMAgent(state.worldmodel));
            agent.update();
            Thread.sleep(50);
            turn++ ;
            //if (turn >= 1400) break ;
        }

        TestUtils.closeConnectionToSE(state);
        return new Pair<>(agent,G) ;
    }
    
    @Test
    public void test_construction1() throws InterruptedException {
    	
    	
    	//TODO fix bug where pathfinding to the playerDestination of the second block gets stuck for unknown reasons.
    	Blueprint blueprint = Blueprint.loadFromFile("assets/blueprints/simpleHouse.cons");
    
    	Vec3 dest = new Vec3(21.25f, -5f, 60);
        
        var agent_and_goal = deployAgent(blueprint, dest);
        TestAgent agent = agent_and_goal.fst ;
        agent.setTestDataCollector(new TestDataCollector()) ;
        GoalStructure G = agent_and_goal.snd;
        G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
        console("*** test succesful!") ;
        //assertTrue(agent.getTestDataCollector().getNumberOfPassVerdictsSeen() == 2) ;
    }
    
}