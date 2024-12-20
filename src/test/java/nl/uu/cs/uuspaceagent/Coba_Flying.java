package nl.uu.cs.uuspaceagent;

import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Vec3;
import org.junit.jupiter.api.Test;

import static nl.uu.cs.uuspaceagent.TestUtils.console;
import static nl.uu.cs.uuspaceagent.TestUtils.loadSE;

/**
 * For trying out flying. Primarily using second-tier actions provided by TacticLib
 * (so, not by directly invoking primitive SE methods).
 */
public class Coba_Flying {

   // @Test
    public void test1() throws InterruptedException {

        var state = loadSE("myworld-3").snd;

        state.updateState(state.agentId);

        System.out.println("** Trying to FLY");

        state.updateState(state.agentId);
        state.navgrid.enableFlying = true ;
        state.env().getController().getCharacter().turnOnJetpack() ;

        WorldEntity agentInfo = state.worldmodel.elements.get(state.agentId);
        System.out.println("** Agent's info: " + PrintInfos.showWorldEntity(agentInfo));

        int k = 0 ;
        while (k<100) {
            state.updateState(state.agentId);
            console("** k=" + k + ", agent: " + PrintInfos.showWOMAgent(state.worldmodel)) ;
            state.env().getController().getCharacter().moveAndRotate(
                    new spaceEngineers.model.Vec3F(0,0.3,0),
                    new spaceEngineers.model.Vec2F(0,0),
                    0,
                    1) ;
            k++ ;
        }
    }

    void moveTo(UUSeAgentState state, Vec3 destination) {
        state.updateState(state.agentId);
        for (int k = 0 ; k<20; k++) {
            UUTacticLib.moveToward(state, destination,10) ;
            state.updateState(state.agentId);
            float distance = Vec3.sub(destination,state.worldmodel.position).length() ;
            console(">>> dist to dest: " + distance);
            if(distance <= 0.5) {
                break ;
            }
        }
    }


    //@Test
    public void testFlyDifferentDirections() throws InterruptedException {

        var state = loadSE("myworld-3").snd;

        state.updateState(state.agentId);

        System.out.println("** Trying to FLY");

        state.updateState(state.agentId);
        state.navgrid.enableFlying = true ;
        state.env().getController().getCharacter().turnOnJetpack() ;
        state.updateState(state.agentId);

        WorldEntity agentInfo = state.worldmodel.elements.get(state.agentId);
        console("** Agent's info: " + PrintInfos.showWorldEntity(agentInfo));

        // agent se0 @<9.549925,-5.0025005,54.149185>, hdir:<-0.0064151124,1.6736684E-4,0.99997944>, vdir:<-3.9837923E-5,1.0,-1.6762585E-4>, health:1.0, jet:true

        //Vec3 destination = new Vec3(11.5f,-1.5f,60f) ;
        console("####  Moving up:");
        state.updateState(state.agentId);
        Vec3 destination = new Vec3(9.54f,3f,54.2f) ;
        moveTo(state, destination) ;


        console("####  Moving to z+:");
        state.updateState(state.agentId);
        destination = new Vec3(9.54f,3f,60f) ;
        moveTo(state, destination) ;

        console("####  Moving to z-:");
        state.updateState(state.agentId);
        destination = new Vec3(9.54f,3f,54.2f) ;
        moveTo(state, destination) ;

        console("####  Moving to x-:");
        state.updateState(state.agentId);
        destination = new Vec3(5f,3f,45f) ;
        moveTo(state, destination) ;

        console("####  Moving to x+:");
        state.updateState(state.agentId);
        destination = new Vec3(9.54f,3f,54.2f) ;
        moveTo(state, destination) ;

        console("####  Moving down:");
        state.updateState(state.agentId);
        destination = new Vec3(9.54f,-5f,54.2f) ;
        moveTo(state, destination) ;
    }

    @Test
    public void testFlyAndYrotate() throws InterruptedException {
        var state = loadSE("myworld-3").snd;

        state.updateState(state.agentId);

        System.out.println("** Trying to FLY");

        state.updateState(state.agentId);
        state.navgrid.enableFlying = true ;
        state.env().getController().getCharacter().turnOnJetpack() ;
        state.updateState(state.agentId);

        WorldEntity agentInfo = state.worldmodel.elements.get(state.agentId);
        console("** Agent's info: " + PrintInfos.showWorldEntity(agentInfo));
        console("** Agent @ " + state.worldmodel.position);

        //Thread.sleep(5000) ;


        // agent se0 @<9.549925,-5.0025005,54.149185>, hdir:<-0.0064151124,1.6736684E-4,0.99997944>, vdir:<-3.9837923E-5,1.0,-1.6762585E-4>, health:1.0, jet:true

        //Vec3 destination = new Vec3(11.5f,-1.5f,60f) ;
        console("####  Moving up:");
        state.updateState(state.agentId);
        Vec3 destination = new Vec3(9.54f,4f,54.2f) ;
        moveTo(state, destination) ;

        state.updateState(state.agentId);
         destination = new Vec3(9.54f,1f,65f) ;
        moveTo(state, destination) ;

        state.updateState(state.agentId);
        destination = new Vec3(9.54f,4f,54.2f) ;
        moveTo(state, destination) ;

        // rotate
        console("####  rotating around Y:");
        state.updateState(state.agentId);
        console("** Agent @ " + state.worldmodel.position);
        destination = new Vec3(9.54f,4f,50f) ;
        UUTacticLib.yTurnTowardACT(state, destination, 0.98f, 400) ;

        state.updateState(state.agentId);
        console("** Agent @ " + state.worldmodel.position);
    }
}
