package com.g4mesoft.renderer;

import java.util.List;

import net.minecraft.text.Text;

public interface GSIRenderer2D extends GSIRenderer {

	public static final String DEFAULT_ELLIPSIS = "...";
	
	public int getMouseX();

	public int getMouseY();

	default public boolean isMouseInside(int x, int y, int width, int height) {
		if (getMouseX() < x || getMouseX() >= x + width)
			return false;
		if (getMouseY() < y || getMouseY() >= y + height)
			return false;
		return true;
	}
	
	public void pushMatrix();
	
	public void popMatrix();
	
	public void translate(int x, int y);
	
	public void pushClip(int x, int y, int width, int height);
	
	public void pushClip(GSClipRect clipRect);

	public GSClipRect popClip();

	default public void fillRect(int x, int y, int width, int height, int color) {
		float a = ((color >>> 24) & 0xFF) / 255.0f;
		float r = ((color >>> 16) & 0xFF) / 255.0f;
		float g = ((color >>>  8) & 0xFF) / 255.0f;
		float b = ((color       ) & 0xFF) / 255.0f;
		
		fillRect(x, y, width, height, r, g, b, a);
	}

	default public void fillRect(int x, int y, int width, int height, float r, float g, float b, float a) {
		fillRectGradient(x, y, width, height, r, g, b, a, r, g, b, a);
	}
		
	default public void fillRectGradient(int x, int y, int width, int height, int topColor, int botColor) {
		float a0 = ((topColor >>> 24) & 0xFF) / 255.0f;
		float r0 = ((topColor >>> 16) & 0xFF) / 255.0f;
		float g0 = ((topColor >>>  8) & 0xFF) / 255.0f;
		float b0 = ((topColor       ) & 0xFF) / 255.0f;

		float a1 = ((botColor >>> 24) & 0xFF) / 255.0f;
		float r1 = ((botColor >>> 16) & 0xFF) / 255.0f;
		float g1 = ((botColor >>>  8) & 0xFF) / 255.0f;
		float b1 = ((botColor       ) & 0xFF) / 255.0f;
		
		fillRectGradient(x, y, width, height, r0, g0, b0, a0, r1, g1, b1, a1);
	}

	public void fillRectGradient(int x, int y, int width, int height,
	                             float r0, float g0, float b0, float a0,
	                             float r1, float g1, float b1, float a1);

	public void drawRect(int x, int y, int width, int height, int color);

	public void drawTexture(GSTexture texture, int x, int y, int width, int height, int sx, int sy);
	
	public void drawTexture(GSITextureRegion texture, int x, int y);

	public void drawVLine(int x, int y0, int y1, int color);
	
	public void drawHLine(int x0, int x1, int y, int color);

	public void drawDottedVLine(int x, int y0, int y1, int length, int spacing, int color);

	public void drawDottedHLine(int x0, int x1, int y, int length, int spacing, int color);
	
	public int getTextHeight();
	
	public int getLineHeight();
	
	public float getTextWidth(String text);

	public float getTextWidth(Text text);
	
	default public void drawCenteredText(String text, int xc, int y, int color) {
		drawCenteredText(text, xc, y, color, true);
	}
	
	default public void drawCenteredText(String text, int xc, int y, int color, boolean shadowed) {
		drawText(text, xc - (int)Math.ceil(getTextWidth(text)) / 2, y, color, shadowed);
	}
	
	default public void drawText(String text, int x, int y, int color) {
		drawText(text, x, y, color, true);
	}

	public void drawText(String text, int x, int y, int color, boolean shadowed);
	
	default public void drawCenteredText(Text text, int xc, int y, int color) {
		drawCenteredText(text, xc, y, color, true);
	}
	
	default public void drawCenteredText(Text text, int xc, int y, int color, boolean shadowed) {
		drawText(text, xc - (int)Math.ceil(getTextWidth(text)) / 2, y, color, shadowed);
	}
	
	default public void drawText(Text text, int x, int y, int color) {
		drawText(text, x, y, color, true);
	}

	public void drawText(Text text, int x, int y, int color, boolean shadowed);
	
	default public String trimString(String str, int availableWidth) {
		return trimString(str, availableWidth, DEFAULT_ELLIPSIS);
	}

	public String trimString(String str, int availableWidth, String ellipsis);

	public List<String> splitToLines(String str, int availableWidth);
	
	default public void vert(float x, float y) {
		vert(x, y, 0.0f);
	}
}
