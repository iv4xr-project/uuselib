package nl.uu.cs.uuspaceagent;

import static nl.uu.cs.uuspaceagent.PrintInfos.showWOMAgent;
import static nl.uu.cs.uuspaceagent.TestUtils.console;
import static nl.uu.cs.uuspaceagent.TestUtils.loadSE;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                     
            WorldEntity grid = null;
            for (var e : state.worldmodel.elements.keySet())
            {
            	console("---");
            	console("id: " + e);
            	if (state.worldmodel.elements.get(e).type == "grid")
            	{
            		grid = state.worldmodel.elements.get(e);
            		console(state.worldmodel.elements.get(e).position.toString());
            	}
            }          
//            if (true)
//            	break;

            
            
            var val = state.worldmodel.elements.get(state.agentId);
            if (val != null)
            {
            	var targetId = val.getStringProperty ("targetBlock");
            	if (targetId != null)
            	{
                    WorldEntity e = grid.elements.get(targetId);
                    console(e.toString());
                    console(e.getClass().toString());
                    
            	}
            	
                
            }
            	
//            CharacterObservation cobs = state.env().getController().getObserver().observe();
//			console(state.navgrid.gridProjectedLocation(state.worldmodel.position).toString());
			//UUTacticLib.fixRoll(state);
                  
            
            
            Thread.sleep(500);
            turn++ ;
            if (turn >= 1400) break ;
        }
	}

}
