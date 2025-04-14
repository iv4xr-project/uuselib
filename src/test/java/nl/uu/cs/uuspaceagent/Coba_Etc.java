package nl.uu.cs.uuspaceagent;

import org.junit.jupiter.api.Test;

import eu.iv4xr.framework.spatial.Vec3;
import static nl.uu.cs.uuspaceagent.TestUtils.console;

/**
 * Just for trying out random things....
 */
public class Coba_Etc {

    @Test
    public void test1() {
        float x = 0 ;
        /*
        System.out.println("" + x + " --> " + NavGrid.myFloor(x)) ;
        x = -0.1f ;
        System.out.println("" + x + " --> " + NavGrid.myFloor(x)) ;
        x = -1.9f ;
        System.out.println("" + x + " --> " + NavGrid.myFloor(x)) ;
        x = -4.1f ;
        System.out.println("" + x + " --> " + NavGrid.myFloor(x)) ;
        x = -4.9f ;
        System.out.println("" + x + " --> " + NavGrid.myFloor(x)) ;
        x = 4.1f ;
        System.out.println("" + x + " --> " + NavGrid.myFloor(x)) ;
        x = 4.9f ;
        System.out.println("" + x + " --> " + NavGrid.myFloor(x)) ;
        */
        
        Vec3 v1 = new Vec3(0,0,0.4f); // magnitude irrelevant, direction is (0,0,1)
        Vec3 v2 = new Vec3(0.999351f, -0.02391127f , -0.026941478f); // ~ (1,0,0)
        Vec3 v3 = new Vec3(-2.1251287f, 0.027358294f, -9.460449E-4f); // ~ (-2,0,0)
        
        var r = Rotation.rotate(v1, v2, v3);
        console("result: " + r.toString()); // result of rotation is <0.0073323892,-0.27756357,-0.28793177> or ~(0, -0.3, -0.3)
        
        v1 = new Vec3(0,0,1); // magnitude irrelevant, direction is (0,0,1)
        v2 = new Vec3(1, 0 , 0); // ~ (1,0,0)
        v3 = new Vec3(-2, 0, 0); // ~ (-2,0,0)
        
        r = Rotation.rotate(v1, v2, v3);
        console("result rounded: " + r.toString()); // result of rotation is (0,0,-1)
        
        v1 = new Vec3(0,0,1);
        v2 = new Vec3(0, 0 , -1);
        v3 = new Vec3(0, 1, 0);
        
        r = Rotation.rotate(v1, v2, v3);
        console("result y case: " + r.toString()); // result of rotation is (0,-1,0)
    }
}
