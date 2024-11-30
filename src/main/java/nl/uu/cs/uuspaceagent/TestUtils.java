package nl.uu.cs.uuspaceagent;

import environments.SeEnvironment;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import nl.uu.cs.aplib.utils.Pair;
import spaceEngineers.controller.*;
import spaceEngineers.model.ToolbarLocation;

import java.io.IOException;
import java.nio.file.Paths;

public class TestUtils {

    public static void console(String str) {
        System.out.println(str);
    }

    /**
     * For creating an SE-env, loading a gameworld into SE, then creating a test-agent bound to
     * the gameworld through the SE-env. The world is assume to be located in the worlds-folder
     * specified by SeEnvironment.Companion.getDEFAULT_SCENARIO_DIR().
     */
    public static Pair<TestAgent, UUSeAgentState> loadSE(String worldName) {
        return loadSE(worldName,null) ;
    }
    /**
     * For creating an SE-env, loading a gameworld into SE, then creating a test-agent bound to
     * the gameworld through the SE-env.
     * The parameter worldsFolder specifies a path to where worlds are saved.
     */
    public static Pair<TestAgent, UUSeAgentState> loadSE(String worldName, String worldsFolder) {
        var agentId = "se0" ; // ""agentId" ;
        var blockType = "LargeHeavyBlockArmorBlock" ;
        var context = new SpaceEngineersTestContext() ;
        context.getBlockTypeToToolbarLocation().put(blockType, new ToolbarLocation(1, 0))  ;

        var controllerWrapper = new ContextControllerWrapper(
                //JsonRpcCharacterController.Companion.localhost(agentId),
                //JsonRpcSpaceEngineersBuilder.Companion.localhost(agentId),
                // WP: it should be this:
                //   JvmSpaceEngineersBuilder.Companion.default().localhost(agentId)
                // but rejected ... i think this is Intellij issue
                new SpaceEngineersJavaProxyBuilder().localhost(agentId),
                context
        ) ;

        if (worldsFolder == null) {
        	// assume this default world-dir
        	//worldsFolder = SeEnvironment.Companion.getDEFAULT_SCENARIO_DIR() ;	
        	worldsFolder = "assets/se-worlds/" ;
        }
        console("** Loading the world " + worldName) ;
        var theEnv = new SeEnvironment( worldName,
                controllerWrapper,
                //SeEnvironment.Companion.getDEFAULT_SCENARIO_DIR()
                worldsFolder
        ) ;
        theEnv.loadWorld() ;

        var myAgentState = new UUSeAgentState(agentId) ;

        console("** Creating a test-agent");
        var testAgent = new TestAgent(agentId, "some role name, else nothing")
                .attachState(myAgentState)
                .attachEnvironment(theEnv) ;

        return new Pair<>(testAgent,myAgentState) ;
    }

    public static void closeConnectionToSE(UUSeAgentState state){
        try {
            state.env().getController().close();
        }
        catch (Exception e) {
            // swallow...
        }
    }
}
