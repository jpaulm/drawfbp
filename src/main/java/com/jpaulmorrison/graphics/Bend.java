package com.jpaulmorrison.graphics;

import java.util.HashMap;

public class Bend {    	
	int x, y; 
	boolean marked;
	
	Bend() {}
	
	Bend(int x, int y) {
		this.x = x; 
		this.y = y;
		}
	
	void buildBend (HashMap<String, String> item) {
  		 String s;
  		 
  		 s = item.get("x").trim();
  		 x = Integer.parseInt(s);
  		 s = item.get("y").trim();
  		 y = Integer.parseInt(s);
  	}
}