package com.g4mesoft.gui.renderer;

import java.util.List;

import net.minecraft.client.util.math.MatrixStack;

public interface GSIRenderer2D {

	public static final String DEFAULT_ELLIPSIS = "...";
	public static final float COLOR_DARKEN_FACTOR = 0.7f;
	
	public void beginRendering(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);

	public void endRendering();

	public int getMouseX();
	
	public int getMouseY();

	public float getPartialTicks();
	
	default public boolean isMouseInside(int x, int y, int w, int h) {
		int mx = getMouseX();
		int my = getMouseY();
		
		if (mx < x || mx >= x + w)
			return false;
		if (my < y || my >= y + h)
			return false;
		return true;
	}

	public void fillRect(int x, int y, int width, int height, int color);

	public void fillRectGradient(int i, int j, int width, int height, int tlColor, int brColor);

	public void drawRect(int x, int y, int width, int height, int color);

	public void drawTexture(GSTexture texture, int x, int y, int width, int height, int sx, int sy);
	
	public void drawTexture(GSITextureRegion texture, int x, int y);

	public void drawVLine(int x, int y0, int y1, int color);
	
	public void drawHLine(int x0, int x1, int y, int color);

	public void drawDottedVLine(int x, int y0, int y1, int length, int spacing, int color);

	public void drawDottedHLine(int x0, int x1, int y, int length, int spacing, int color);
	
	public int getFontHeight();
	
	public int getLineHeight();
	
	public float getStringWidth(String str);
	
	default public String trimString(String str, int availableWidth) {
		return trimString(str, availableWidth, DEFAULT_ELLIPSIS);
	}

	public String trimString(String str, int availableWidth, String ellipsis);

	public List<String> splitToLines(String str, int availableWidth);

	default public void drawCenteredString(String str, int xc, int y, int color) {
		drawCenteredString(str, xc, y, color, true);
	}
	
	default public void drawCenteredString(String str, int xc, int y, int color, boolean shadowed) {
		drawString(str, xc - (int)Math.ceil(getStringWidth(str)) / 2, y, color, shadowed);
	}
	
	default public void drawString(String str, int x, int y, int color) {
		drawString(str, x, y, color, true);
	}

	public void drawString(String str, int x, int y, int color, boolean shadowed);
	
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
	
	public void pushClip(int x, int y, int width, int height);
	
	public void pushClip(GSClipRect clipRect);

	public GSClipRect popClip();
	
	public void pushTransform();
	
	public void popTransform();

	public void translate(int x, int y);

}
