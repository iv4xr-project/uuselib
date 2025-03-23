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
            
            
            if (true)
            	break;
            
            Thread.sleep(500);
            turn++ ;
            if (turn >= 1400) break ;
        }
	}

}
