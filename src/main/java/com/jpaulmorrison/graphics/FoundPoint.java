package com.jpaulmorrison.graphics;

import java.awt.Point;

import com.jpaulmorrison.graphics.DrawFBP.Side;

public class FoundPoint extends Point {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;	
	Side side;
	Block block;
	//x and y are declared in Point
	
	FoundPoint(int x, int y, Side s, Block blk) {
		this.x = x; this.y = y; 
		side = s; block = blk;
	}
}
