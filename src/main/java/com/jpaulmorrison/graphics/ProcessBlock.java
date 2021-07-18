package com.jpaulmorrison.graphics;

public class ProcessBlock extends Block {
	
	ProcessBlock (Diagram diag){
		super(diag);
		width = BLOCKWIDTH;
		height = BLOCKHEIGHT;
		typeCode = Block.Types.PROCESS_BLOCK;
		//buildSides();
		buildSideRects();
	}
	
}