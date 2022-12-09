package com.g4mesoft.renderer;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.render.VertexFormat;

public abstract interface GSIRenderer {

	public static final int LINES          = GL11.GL_LINES;
	public static final int LINE_STRIP     = GL11.GL_LINE_STRIP;
	public static final int TRIANGLES      = GL11.GL_TRIANGLES;
	public static final int TRIANGLE_STRIP = GL11.GL_TRIANGLE_STRIP;
	public static final int QUADS          = GL11.GL_QUADS;
	public static final int QUAD_STRIP     = GL11.GL_QUAD_STRIP;
	
	public void build(int shape, VertexFormat format);

	public GSIRenderer vert(float x, float y, float z);
	
	default public GSIRenderer color(int color) {
		float a = ((color >>> 24) & 0xFF) / 255.0f;
		float r = ((color >>> 16) & 0xFF) / 255.0f;
		float g = ((color >>>  8) & 0xFF) / 255.0f;
		float b = ((color       ) & 0xFF) / 255.0f;
		
		return color(r, g, b, a);
	}

	default public GSIRenderer color(float r, float g, float b) {
		return color(r, g, b, 1.0f);
	}

	public GSIRenderer color(float r, float g, float b, float a);

	public GSIRenderer tex(float u, float v);

	public GSIRenderer next();
	
	public void finish();

}
