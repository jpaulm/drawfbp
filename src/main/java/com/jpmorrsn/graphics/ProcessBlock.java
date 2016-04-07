package com.jpmorrsn.graphics;

public class ProcessBlock extends Block {
	
	ProcessBlock (Diagram ctlr){
		super(ctlr);
		width = BLOCKWIDTH;
		height = BLOCKHEIGHT;
		type = Block.Types.PROCESS_BLOCK;
		//calcEdges();
	}
	
}