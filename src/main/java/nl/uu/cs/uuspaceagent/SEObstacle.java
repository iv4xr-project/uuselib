package nl.uu.cs.uuspaceagent;

import eu.iv4xr.framework.spatial.Obstacle;


public class SEObstacle<T> extends Obstacle<T> {
	public Boolean isTraversable = false;
	
	public SEObstacle(T obstacle) {
		super(obstacle);
		// TODO Auto-generated constructor stub
	}
	
	public Boolean canTraverse(Boolean useDoors) {
		if (useDoors)
			return isTraversable;
		return !isBlocking;
		
	}
}
