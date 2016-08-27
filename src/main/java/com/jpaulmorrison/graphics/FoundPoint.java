package com.jpaulmorrison.graphics;

import com.jpaulmorrison.graphics.DrawFBP.Side;

public class FoundPoint {
	int xa, ya;
	Side side;
	Block b;
	
	FoundPoint(int x, int y, Side s, Block block) {
		xa = x;ya = y; side = s; b = block;
	}
}
