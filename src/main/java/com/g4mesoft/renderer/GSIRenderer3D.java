package com.g4mesoft.renderer;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.render.VertexFormat;

public interface GSIRenderer3D {

	public static final int LINES          = GL11.GL_LINES;
	public static final int LINE_STRIP     = GL11.GL_LINE_STRIP;
	public static final int TRIANGLES      = GL11.GL_TRIANGLES;
	public static final int TRIANGLE_STRIP = GL11.GL_TRIANGLE_STRIP;
	public static final int QUADS          = GL11.GL_QUADS;
	public static final int QUAD_STRIP     = GL11.GL_QUAD_STRIP;
	
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
	
	public void build(int shape, VertexFormat format);

	public GSIRenderer3D vert(float x, float y, float z);
	
	default public GSIRenderer3D color(int color) {
		float a = ((color >>> 24) & 0xFF) / 255.0f;
		float r = ((color >>> 16) & 0xFF) / 255.0f;
		float g = ((color >>>  8) & 0xFF) / 255.0f;
		float b = ((color       ) & 0xFF) / 255.0f;
		
		return color(r, g, b, a);
	}

	default public GSIRenderer3D color(float r, float g, float b) {
		return color(r, g, b, 1.0f);
	}

	public GSIRenderer3D color(float r, float g, float b, float a);

	public GSIRenderer3D tex(float u, float v);

	public GSIRenderer3D next();
	
	public void finish();
	
}
