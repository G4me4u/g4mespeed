package com.g4mesoft.renderer;

public class GSTextureRegion implements GSITextureRegion {

	private final GSTexture texture;

	private final int rx;
	private final int ry;
	private final int rw;
	private final int rh;

	private final float u0;
	private final float v0;
	private final float u1;
	private final float v1;
	
	public GSTextureRegion(GSTexture texture, int rx, int ry, int rw, int rh) {
		this.texture = texture;
		
		this.rx = rx;
		this.ry = ry;
		this.rw = rw;
		this.rh = rh;

		u0 = (float)rx / texture.getWidth();
		v0 = (float)ry / texture.getHeight();
		u1 = (float)(rx + rw) / texture.getWidth();
		v1 = (float)(ry + rh) / texture.getHeight();
	}

	@Override
	public GSTexture getTexture() {
		return texture;
	}

	@Override
	public GSITextureRegion getRegion(int rx, int ry, int rw, int rh) {
		return new GSTextureRegion(texture, this.rx + rx, this.ry + ry, rw, rh);
	}

	@Override
	public int getRegionWidth() {
		return rw;
	}

	@Override
	public int getRegionHeight() {
		return rh;
	}

	@Override
	public float getU0() {
		return u0;
	}

	@Override
	public float getV0() {
		return v0;
	}

	@Override
	public float getU1() {
		return u1;
	}

	@Override
	public float getV1() {
		return v1;
	}
}
