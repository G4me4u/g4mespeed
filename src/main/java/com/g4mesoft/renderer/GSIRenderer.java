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
	
	public static final float COLOR_DARKEN_FACTOR = 0.7f;
	
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
	
	default public int darkenColor(int color) {
		int a = (color >> 24) & 0xFF;
		int r = (color >> 16) & 0xFF;
		int g = (color >>  8) & 0xFF;
		int b = (color >>  0) & 0xFF;
	
		r = (int)(r * COLOR_DARKEN_FACTOR);
		g = (int)(g * COLOR_DARKEN_FACTOR);
		b = (int)(b * COLOR_DARKEN_FACTOR);

		return (a << 24) | (r << 16) | (g << 8) | b;
	}
	
	default public int brightenColor(int color) {
		int a = (color >> 24) & 0xFF;
		int r = (color >> 16) & 0xFF;
		int g = (color >>  8) & 0xFF;
		int b = (color >>  0) & 0xFF;
	
		int i = (int)(1.0f / (1.0f - COLOR_DARKEN_FACTOR));
		if (r == 0 && g == 0 && b == 0) {
			r = g = b = i;
		} else {
			if (r > 0 && r < i) r = i;
			if (g > 0 && g < i) g = i;
			if (b > 0 && b < i) b = i;
			
			r = Math.min((int)(r / COLOR_DARKEN_FACTOR), 0xFF);
			g = Math.min((int)(g / COLOR_DARKEN_FACTOR), 0xFF);
			b = Math.min((int)(b / COLOR_DARKEN_FACTOR), 0xFF);
		}

		return (a << 24) | (r << 16) | (g << 8) | b;
	}
}
