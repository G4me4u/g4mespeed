package com.g4mesoft.renderer;

public interface GSIRenderable3D {

	public void render(GSIRenderer3D renderer3d);
	
	public GSERenderPhase getRenderPhase();
	
}
