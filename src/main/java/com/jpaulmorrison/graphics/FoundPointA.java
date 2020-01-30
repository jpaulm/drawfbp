package com.jpaulmorrison.graphics;

// can only be ending point for arrow

import java.awt.Point;

public class FoundPointA extends Point{


		private static final long serialVersionUID = 1L;	
		
		Arrow arrow;
		int segNo;
		//x and y are declared in Point
		
		FoundPointA(int x, int y, Arrow arr, int segno) {
			this.x = x; this.y = y; 
			arrow = arr;
			segNo = segno;
		}
	}

 
