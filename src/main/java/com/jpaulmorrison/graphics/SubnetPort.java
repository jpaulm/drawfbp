package com.jpaulmorrison.graphics;
import java.util.*;

public class SubnetPort {
  //int x;
  int y;
  String name; 
  DrawFBP.Side side;
  boolean substreamSensitive = false;
  SubnetPort(){	 
  }
  SubnetPort(int y, DrawFBP.Side s){
	  //this.x = x;
	  this.y = y;
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
		//String ss = item.get("substreamsensitive");
		//if (ss != null)
		//	substreamSensitive = true;
			
  }
}
