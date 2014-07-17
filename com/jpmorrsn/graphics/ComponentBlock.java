package com.jpmorrsn.graphics;

public class ComponentBlock extends Block {
	
	ComponentBlock (Diagram ctlr){
		super(ctlr);
		width = BLOCKWIDTH;
		height = BLOCKHEIGHT;
		type = Block.Types.COMPONENT_BLOCK;
		//calcEdges();
	}
	
}