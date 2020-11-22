package com.g4mesoft.renderer;

public interface GSIRenderer3D extends GSIRenderer {

	public void pushMatrix();
	
	public void popMatrix();
	
	public void translate(float tx, float ty, float tz);

	public void rotate(float rx, float ry, float rz);

	public void scale(float sx, float sy, float sz);
	
	default public void fillCube(float x, float y, float z, int color) {
		fillCuboid(x, y, z, x + 1.0f, y + 1.0f, z + 1.0f, color);
	}

	default public void fillCuboid(float x0, float y0, float z0, float x1, float y1, float z1, int color) {
		float a = ((color >>> 24) & 0xFF) / 255.0f;
		float r = ((color >>> 16) & 0xFF) / 255.0f;
		float g = ((color >>>  8) & 0xFF) / 255.0f;
		float b = ((color       ) & 0xFF) / 255.0f;
		
		fillCuboid(x0, y0, z0, x1, y1, z1, r, g, b, a);
	}

	default public void fillCube(float x, float y, float z, float r, float g, float b) {
		fillCuboid(x, y, z, x + 1.0f, y + 1.0f, z + 1.0f, r, g, b);
	}

	default public void fillCuboid(float x0, float y0, float z0, float x1, float y1, float z1, float r, float g, float b) {
		fillCuboid(x0, y0, z0, x1, y1, z1, r, g, b, 1.0f);
	}

	default public void fillCube(float x, float y, float z, float r, float g, float b, float a) {
		fillCuboid(x, y, z, x + 1.0f, y + 1.0f, z + 1.0f, r, g, b, a);
	}

	public void fillCuboid(float x0, float y0, float z0, float x1, float y1, float z1, float r, float g, float b, float a);
	
	default public void drawCubeOutline(float x, float y, float z, int color) {
		drawCuboidOutline(x, y, z, x + 1.0f, y + 1.0f, z + 1.0f, color);
	}

	default public void drawCuboidOutline(float x0, float y0, float z0, float x1, float y1, float z1, int color) {
		float a = ((color >>> 24) & 0xFF) / 255.0f;
		float r = ((color >>> 16) & 0xFF) / 255.0f;
		float g = ((color >>>  8) & 0xFF) / 255.0f;
		float b = ((color       ) & 0xFF) / 255.0f;
		
		drawCuboidOutline(x0, y0, z0, x1, y1, z1, r, g, b, a);
	}

	default public void drawCubeOutline(float x, float y, float z, float r, float g, float b) {
		drawCuboidOutline(x, y, z, x + 1.0f, y + 1.0f, z + 1.0f, r, g, b);
	}

	default public void drawCuboidOutline(float x0, float y0, float z0, float x1, float y1, float z1, float r, float g, float b) {
		drawCuboidOutline(x0, y0, z0, x1, y1, z1, r, g, b, 1.0f);
	}

	default public void drawCubeOutline(float x, float y, float z, float r, float g, float b, float a) {
		drawCuboidOutline(x, y, z, x + 1.0f, y + 1.0f, z + 1.0f, r, g, b, a);
	}

	public void drawCuboidOutline(float x0, float y0, float z0, float x1, float y1, float z1, float r, float g, float b, float a);
	
}
