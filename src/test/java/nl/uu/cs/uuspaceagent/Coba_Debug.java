package nl.uu.cs.uuspaceagent;

import static nl.uu.cs.uuspaceagent.PrintInfos.showWOMAgent;
import static nl.uu.cs.uuspaceagent.TestUtils.console;
import static nl.uu.cs.uuspaceagent.TestUtils.loadSE;
import static org.junit.jupiter.api.Assertions.assertTrue;

import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.uuspaceagent.SEBlockFunctions;
import nl.uu.cs.aplib.mainConcepts.*;
import static nl.uu.cs.aplib.AplibEDSL.* ;

import org.junit.jupiter.api.Test;

import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import spaceEngineers.model.Block;
import spaceEngineers.model.CharacterObservation;
import spaceEngineers.model.DefinitionId;
import eu.iv4xr.framework.spatial.Vec3;

class Coba_Debug {

	@Test
	void test() throws InterruptedException {
		console("*** start coba...") ;
		var agentAndState = loadSE("ConstructionPlatformSurvival") ;
        TestAgent agent = agentAndState.fst ;
        UUSeAgentState state = agentAndState.snd ;
        Thread.sleep(1000);
        state.updateState(state.agentId);
        
        var block = SEBlockFunctions.findClosestBlock(state.worldmodel, (WorldEntity we) -> {
        	return we.getProperty("blockType").toString().contains("Container");
        });
        
        GoalStructure G = SEQ( 
        		DEPLOY(UUGoalLib.accessedBlockInventory(block)),
        		lift("deposit item", UUTacticLib.depositItemToContainer(new DefinitionId("AmmoMagazine", "NATO_5p56x45mm")))
        		);
        
        G = DEPLOY(UUGoalLib.rechargedPlayer());
        
        agent.setGoal(G);
        
        int turn= 0 ;
        while(G.getStatus().inProgress()) {
            console(">> [" + turn + "] " + showWOMAgent(state.worldmodel));
            agent.update();
            state.updateState(state.agentId);
            
            
//            if (true)
//            	break;
            
            Thread.sleep(50);
            turn++ ;
            if (turn >= 1400) break ;
        }
	}

}
