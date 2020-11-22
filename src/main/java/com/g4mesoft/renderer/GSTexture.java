package com.g4mesoft.renderer;

import net.minecraft.util.Identifier;

public class GSTexture implements GSITextureRegion {

	private final Identifier identifier;
	private final int width;
	private final int height;
	
	public GSTexture(Identifier identifier, int width, int height) {
		this.identifier = identifier;
		this.width = width;
		this.height = height;
	}
	
	public GSITextureRegion getRegion(int rx, int ry, int rw, int rh) {
		return new GSTextureRegion(this, rx, ry, rw, rh);
	}
	
	public Identifier getIdentifier() {
		return identifier;
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public GSTexture getTexture() {
		return this;
	}
	
	@Override
	public int getRegionWidth() {
		return getWidth();
	}
	
	@Override
	public int getRegionHeight() {
		return getHeight();
	}
	
	@Override
	public float getU0() {
		return 0.0f;
	}

	@Override
	public float getV0() {
		return 0.0f;
	}

	@Override
	public float getU1() {
		return 1.0f;
	}

	@Override
	public float getV1() {
		return 1.0f;
	}
}
