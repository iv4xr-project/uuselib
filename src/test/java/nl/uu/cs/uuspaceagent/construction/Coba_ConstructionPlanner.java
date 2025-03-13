package nl.uu.cs.uuspaceagent.construction;

import static nl.uu.cs.uuspaceagent.PrintInfos.showWOMAgent;
import static nl.uu.cs.uuspaceagent.TestUtils.console;
import static nl.uu.cs.uuspaceagent.TestUtils.loadSE;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import spaceEngineers.model.Block;
import spaceEngineers.model.CharacterObservation;
import spaceEngineers.model.DefinitionId;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.uuspaceagent.Blueprint;
import nl.uu.cs.uuspaceagent.ConstructionPlanner;
import nl.uu.cs.uuspaceagent.DPos3;
import nl.uu.cs.uuspaceagent.UUSeAgentState;

class Coba_ConstructionPlanner {

	@Test
	void test() throws InterruptedException {
		console("*** start coba...") ;
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
        
        var blueprint = Blueprint.loadFromFile("assets/se-worlds/ConstructionPlatform/newConstruction.cons");
        
        //TODO Test random ConstructionPlanner stuff      
        var origin = new Vec3(21.25f, -5f, 60);
		ConstructionPlanner planner = new ConstructionPlanner(blueprint, origin, grid, state);
        
        var G = planner.getConstructionGoal(new DPos3(4,0,8));
        agent.setGoal(G);
        
        int turn= 0 ;
        while(G.getStatus().inProgress()) {
            console(">> [" + turn + "] " + showWOMAgent(state.worldmodel));
            agent.update();
            state.updateState(state.agentId);
                     
            
            Thread.sleep(500);
            turn++ ;
            if (turn >= 1400) break ;
        }
        G.printGoalStructureStatus();
        assertTrue(G.getStatus().success());
        console("*** test succesful!") ;
	}

}
