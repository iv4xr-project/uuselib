package nl.uu.cs.uuspaceagent;

import static nl.uu.cs.uuspaceagent.PrintInfos.showWOMAgent;
import static nl.uu.cs.uuspaceagent.TestUtils.console;
import static nl.uu.cs.uuspaceagent.TestUtils.loadSE;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import eu.iv4xr.framework.mainConcepts.TestAgent;
import spaceEngineers.model.CharacterObservation;

class Coba_Debug {

	@Test
	void test() throws InterruptedException {
		console("*** start coba...") ;
		var agentAndState = loadSE("islanddswithdoors") ;
        TestAgent agent = agentAndState.fst ;
        UUSeAgentState state = agentAndState.snd ;
        Thread.sleep(1000);
        state.updateState(state.agentId);
        
        
        int turn= 0 ;
        while(true) {
            console(">> [" + turn + "] " + showWOMAgent(state.worldmodel));
            agent.update();
            state.updateState(state.agentId);
                     
//            for (var e : state.worldmodel.elements.keySet())
//            {
//            	console("---");
//            	console("id: " + e);
//            	if (state.worldmodel.elements.get(e).type == "grid")
//            		console(state.worldmodel.elements.get(e).position.toString());//.entrySet().toString());
//            }
            
//            if (true)
//            	break;
            
//            var val = state.worldmodel.elements.get(state.agentId);
//            if (val != null)
//            	console(val.getStringProperty ("targetBlock"));
//            
            CharacterObservation cobs = state.env().getController().getObserver().observe();
			
            console("Y: " + cobs.getHeadLocalYAngle() + ", X: " + cobs.getHeadLocalXAngle());
            
	        if(cobs.getTargetBlock() != null) {
	        	console(cobs.getTargetBlock().toString());
	        
	        }
//            
            
            
            Thread.sleep(500);
            turn++ ;
            if (turn >= 1400) break ;
        }
	}

}
