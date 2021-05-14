package com.g4mesoft.renderer;

public interface GSITextureRegion {

	public GSTexture getTexture();
	
	public GSITextureRegion getRegion(int rx, int ry, int rw, int rh);
	
	public int getRegionWidth();
	
	public int getRegionHeight();
	
	public float getU0();

	public float getV0();

	public float getU1();
	
	public float getV1();
	
}
