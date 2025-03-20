package nl.uu.cs.uuspaceagent;

import static nl.uu.cs.uuspaceagent.PrintInfos.showWOMAgent;
import static nl.uu.cs.uuspaceagent.TestUtils.console;
import static nl.uu.cs.uuspaceagent.TestUtils.loadSE;
import static org.junit.jupiter.api.Assertions.assertTrue;
import nl.uu.cs.uuspaceagent.SEBlockFunctions;

import org.junit.jupiter.api.Test;

import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import spaceEngineers.model.Block;
import spaceEngineers.model.CharacterObservation;
import eu.iv4xr.framework.spatial.Vec3;

class Coba_Debug {

	@Test
	void test() throws InterruptedException {
		console("*** start coba...") ;
		var agentAndState = loadSE("ConstructionPlatform") ;
        TestAgent agent = agentAndState.fst ;
        UUSeAgentState state = agentAndState.snd ;
        Thread.sleep(1000);
        state.updateState(state.agentId);
        
        
        int turn= 0 ;
        while(true) {
            console(">> [" + turn + "] " + showWOMAgent(state.worldmodel));
            agent.update();
            state.updateState(state.agentId);
                     
//            var p = state.navgrid.gridProjectedLocation(state.worldmodel.position);
//        	
//            int distance = 2;
//            
//            var x = new DPos3(p.x,p.y-distance,p.z);
//
//            var obstacle = state.navgrid.knownObstacles.get(x) ;
//			console("obstacle: "+ obstacle);
            
            WorldEntity block = SEBlockFunctions.findClosestBlockPosition(
            		state.worldmodel, Vec3.sub(state.worldmodel.position, new Vec3(0,2.5f,0)), 1.25f);
            
            if (block != null)
            	console(block.toString());
            
            for (DPos3 p : state.navgrid.knownObstacles.keySet()) {
            	if (p.y < 0) console(p.toString()); 
            }
            
            if (true)
            	break;
            
            Thread.sleep(500);
            turn++ ;
            if (turn >= 1400) break ;
        }
	}

}
