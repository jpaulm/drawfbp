package com.jpaulmorrison.graphics;
import java.util.*;

public class SubnetPort {
  int x;
  int y;
  String name; 
  
  Block eb;  // cross referenced External Port Block (in subnet diagram)
  
  DrawFBP.Side side;
  boolean substreamSensitive = false;
  SubnetPort(){	 
  }
  SubnetPort(int x, int y, DrawFBP.Side s, ExtPortBlock eb){
	  this.x = x;
	  this.y = y;
	  this.eb = eb;
	  side = s;
  }
  void buildBlockFromXML(HashMap<String, String> item){
	  String t = item.get("y").trim();
		y = Integer.parseInt(t);
		name = item.get("name");
			if (name != null)
		name = name.trim();
		t = item.get("side").trim();
		if (t.equals("L"))
			side = DrawFBP.Side.LEFT;
		else
			side = DrawFBP.Side.RIGHT;
		String ss = item.get("substreamsensitive");
		if (ss != null)
			substreamSensitive = true;
			
  }
}
