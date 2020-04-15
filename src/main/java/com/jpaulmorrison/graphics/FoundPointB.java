package com.jpaulmorrison.graphics;


import java.awt.Point;

import com.jpaulmorrison.graphics.DrawFBP.Side;

// can be starting or ending point for arrow

public class FoundPointB extends Point {
	 
	private static final long serialVersionUID = 1L;	
	Block block;	
	Side side;	 
	
	//x and y are declared in Point
	
	FoundPointB(int x, int y, Side s, Block blk) {
		this.x = x; this.y = y; 
		this.side = s; this.block = blk;		
	}
	
	

}
