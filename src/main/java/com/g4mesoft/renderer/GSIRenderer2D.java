package com.g4mesoft.renderer;

import java.util.List;

import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public interface GSIRenderer2D extends GSIRenderer {

	public static final String DEFAULT_ELLIPSIS = "...";
	public static final Text DEFAULT_ELLIPSIS_TEXT = new LiteralText(DEFAULT_ELLIPSIS);
	
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
	
	public void translateDepth(float z);
	
	public void pushClip(int x, int y, int width, int height);
	
	public void pushClip(GSClipRect clipRect);

	public GSClipRect popClip();
	
	public void pushOpacity(float opacity);

	public float popOpacity();

	default public void fillRect(int x, int y, int width, int height, int color) {
		float a = ((color >>> 24) & 0xFF) / 255.0f;
		float r = ((color >>> 16) & 0xFF) / 255.0f;
		float g = ((color >>>  8) & 0xFF) / 255.0f;
		float b = ((color       ) & 0xFF) / 255.0f;
		
		fillRect(x, y, width, height, r, g, b, a);
	}

	default public void fillRect(int x, int y, int width, int height, float r, float g, float b, float a) {
		fillGradient(x, y, width, height, r, g, b, a, r, g, b, a, r, g, b, a, r, g, b, a, false);
	}
	
	default public void fillHGradient(int x, int y, int width, int height, int leftColor, int rightColor) {
		float al = ((leftColor >>> 24) & 0xFF) / 255.0f;
		float rl = ((leftColor >>> 16) & 0xFF) / 255.0f;
		float gl = ((leftColor >>>  8) & 0xFF) / 255.0f;
		float bl = ((leftColor       ) & 0xFF) / 255.0f;
		
		float ar = ((rightColor >>> 24) & 0xFF) / 255.0f;
		float rr = ((rightColor >>> 16) & 0xFF) / 255.0f;
		float gr = ((rightColor >>>  8) & 0xFF) / 255.0f;
		float br = ((rightColor       ) & 0xFF) / 255.0f;
		
		fillHGradient(x, y, width, height, rl, gl, bl, al, rr, gr, br, ar);
	}
	
	default public void fillHGradient(int x, int y, int width, int height,
	                                  float rl, float gl, float bl, float al,
	                                  float rr, float gr, float br, float ar) {
		
		fillGradient(x, y, width, height, rl, gl, bl, al,
		                                  rr, gr, br, ar,
		                                  rl, gl, bl, al,
		                                  rr, gr, br, ar,
		                                  false);
	}

	default public void fillVGradient(int x, int y, int width, int height, int topColor, int botColor) {
		float at = ((topColor >>> 24) & 0xFF) / 255.0f;
		float rt = ((topColor >>> 16) & 0xFF) / 255.0f;
		float gt = ((topColor >>>  8) & 0xFF) / 255.0f;
		float bt = ((topColor       ) & 0xFF) / 255.0f;

		float ab = ((botColor >>> 24) & 0xFF) / 255.0f;
		float rb = ((botColor >>> 16) & 0xFF) / 255.0f;
		float gb = ((botColor >>>  8) & 0xFF) / 255.0f;
		float bb = ((botColor       ) & 0xFF) / 255.0f;
		
		fillVGradient(x, y, width, height, rt, gt, bt, at, rb, gb, bb, ab);
	}

	default public void fillVGradient(int x, int y, int width, int height,
	                                  float rt, float gt, float bt, float at,
	                                  float rb, float gb, float bb, float ab) {
		
		fillGradient(x, y, width, height, rt, gt, bt, at, 
		                                  rt, gt, bt, at,
		                                  rb, gb, bb, ab,
		                                  rb, gb, bb, ab,
		                                  false);
	}
	
	default void fillGradient(int x, int y, int width, int height,
	                          int tlColor, int trColor, int blColor, int brColor,
	                          boolean mirror) {
		
		float atl = ((tlColor >>> 24) & 0xFF) / 255.0f;
		float rtl = ((tlColor >>> 16) & 0xFF) / 255.0f;
		float gtl = ((tlColor >>>  8) & 0xFF) / 255.0f;
		float btl = ((tlColor       ) & 0xFF) / 255.0f;

		float atr = ((trColor >>> 24) & 0xFF) / 255.0f;
		float rtr = ((trColor >>> 16) & 0xFF) / 255.0f;
		float gtr = ((trColor >>>  8) & 0xFF) / 255.0f;
		float btr = ((trColor       ) & 0xFF) / 255.0f;
		
		float abl = ((blColor >>> 24) & 0xFF) / 255.0f;
		float rbl = ((blColor >>> 16) & 0xFF) / 255.0f;
		float gbl = ((blColor >>>  8) & 0xFF) / 255.0f;
		float bbl = ((blColor       ) & 0xFF) / 255.0f;

		float abr = ((brColor >>> 24) & 0xFF) / 255.0f;
		float rbr = ((brColor >>> 16) & 0xFF) / 255.0f;
		float gbr = ((brColor >>>  8) & 0xFF) / 255.0f;
		float bbr = ((brColor       ) & 0xFF) / 255.0f;
		
		fillGradient(x, y, width, height, rtl, gtl, btl, atl, 
		                                  rtr, gtr, btr, atr,
		                                  rbl, gbl, bbl, abl,
		                                  rbr, gbr, bbr, abr,
		                                  mirror);
	}

	public void fillGradient(int x, int y, int width, int height,
	                         float rtl, float gtl, float btl, float atl,
	                         float rtr, float gtr, float btr, float atr,
	                         float rbl, float gbl, float bbl, float abl,
	                         float rbr, float gbr, float bbr, float abr,
	                         boolean mirror);

	public void drawRect(int x, int y, int width, int height, int color);

	public void drawTexture(GSITextureRegion texture, int x, int y, int width, int height, int sx, int sy);

	public void drawTexture(GSITextureRegion texture, int x, int y, int width, int height, int sx, int sy, float r, float g, float b);
	
	public void drawTexture(GSITextureRegion texture, int x, int y);

	public void drawTexture(GSITextureRegion texture, int x, int y, float r, float g, float b);

	public void drawVLine(int x, int y0, int y1, int color);
	
	public void drawHLine(int x0, int x1, int y, int color);

	public void drawDottedVLine(int x, int y0, int y1, int length, int spacing, int color);

	public void drawDottedHLine(int x0, int x1, int y, int length, int spacing, int color);
	
	public int getTextAscent();
	
	public int getTextDescent();

	public int getTextHeight();
	
	public int getLineHeight();
	
	public float getTextWidth(String text);

	public float getTextWidthNoStyle(CharSequence text);

	default public float getTextWidth(Text text) {
		return getTextWidth(text.asOrderedText());
	}
	
	public float getTextWidth(OrderedText text);
	
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
	
	default public void drawCenteredTextNoStyle(CharSequence text, int xc, int y, int color) {
		drawCenteredTextNoStyle(text, xc, y, color, true);
	}
	
	default public void drawCenteredTextNoStyle(CharSequence text, int xc, int y, int color, boolean shadowed) {
		drawTextNoStyle(text, xc - (int)Math.ceil(getTextWidthNoStyle(text)) / 2, y, color, shadowed);
	}
	
	default public void drawTextNoStyle(CharSequence text, int x, int y, int color) {
		drawTextNoStyle(text, x, y, color, true);
	}
	
	public void drawTextNoStyle(CharSequence text, int x, int y, int color, boolean shadowed);
	
	default public void drawCenteredText(Text text, int xc, int y, int color) {
		drawCenteredText(text.asOrderedText(), xc, y, color);
	}
	
	default public void drawCenteredText(Text text, int xc, int y, int color, boolean shadowed) {
		drawCenteredText(text.asOrderedText(), xc, y, color, shadowed);
	}
	
	default public void drawText(Text text, int x, int y, int color) {
		drawText(text.asOrderedText(), x, y, color);
	}

	default public void drawText(Text text, int x, int y, int color, boolean shadowed) {
		drawText(text.asOrderedText(), x, y, color, shadowed);
	}
	
	default public void drawCenteredText(OrderedText text, int xc, int y, int color) {
		drawCenteredText(text, xc, y, color, true);
	}
	
	default public void drawCenteredText(OrderedText text, int xc, int y, int color, boolean shadowed) {
		drawText(text, xc - (int)Math.ceil(getTextWidth(text)) / 2, y, color, shadowed);
	}
	
	default public void drawText(OrderedText text, int x, int y, int color) {
		drawText(text, x, y, color, true);
	}

	public void drawText(OrderedText text, int x, int y, int color, boolean shadowed);
	
	default public String trimString(String text, int availableWidth) {
		return trimString(text, availableWidth, DEFAULT_ELLIPSIS);
	}

	public String trimString(String text, int availableWidth, String ellipsis);

	public List<String> splitToLines(String text, int availableWidth);

	default public OrderedText trimString(Text text, int availableWidth) {
		return trimString(text, availableWidth, DEFAULT_ELLIPSIS_TEXT);
	}
	
	public OrderedText trimString(Text text, int availableWidth, Text ellipsis);
	
	public List<OrderedText> splitToLines(Text text, int availableWidth);

	default public void vert(float x, float y) {
		vert(x, y, 0.0f);
	}
}
