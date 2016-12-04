package com.jpaulmorrison.graphics;

import java.awt.Point;

import com.jpaulmorrison.graphics.DrawFBP.Side;

public class FoundPoint extends Point {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;	
	Side side;
	Block b;
	
	FoundPoint(int x, int y, Side s, Block block) {
		this.x = x; this.y = y; side = s; b = block;
	}
}
