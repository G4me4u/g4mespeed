package com.g4mesoft.gui;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.module.translation.GSTranslationModule;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;

public interface GSIDrawableHelper {

	default public String trimText(String text, int availableWidth) {
		return trimText(getFont(), text, availableWidth);
	}
	
	default public String trimText(TextRenderer font, String text, int availableWidth) {
		int len = text.length();
		if (len <= 0)
			return text;

		// Text fits inside bounds.
		if (font.getStringWidth(text) <= availableWidth)
			return text;

		availableWidth -= font.getStringWidth(GSGUIConstants.TRIMMED_TEXT_ELLIPSIS);

		// No space for any other
		// characters.
		if (availableWidth < 0)
			return GSGUIConstants.TRIMMED_TEXT_ELLIPSIS;

		for (int i = 0; i < len; i++) {
			char c = text.charAt(i);
			// Should probably use getStringWidth
			// and substring instead, but for
			// optimization we use getCharWidth.
			availableWidth -= font.getCharWidth(c);

			if (availableWidth < 0)
				return text.substring(0, i) + GSGUIConstants.TRIMMED_TEXT_ELLIPSIS;
		}

		// This should never happen.
		return text;
	}

	default public List<String> splitToLines(String text, int availableWidth) {
		return splitToLines(getFont(), text, availableWidth);
	}
	
	default public List<String> splitToLines(TextRenderer font, String text, int availableWidth) {
		List<String> result = new ArrayList<String>();
		
		int len = text.length();
		if (len <= 0)
			return result;
		
		int lineWidth = 0;
		int lineBegin = 0;
		
		int lastSpaceIndex = -1;
		
		String formattingNextLine = "";
		String formattingThisLine = formattingNextLine;
		
		for (int i = 0; i < len; i++) {
			char c = text.charAt(i);
			if (c == GSGUIConstants.FORMATTING_CHAR) {
				i++;
				
				if (i < len) {
					c = text.charAt(i);
					if (c == 'r') {
						formattingNextLine = "";
					} else {
						formattingNextLine += Character.toString(GSGUIConstants.FORMATTING_CHAR) + c;
					}
				}
			} else {
				lineWidth += font.getCharWidth(c);
				
				if (c == ' ')
					lastSpaceIndex = i;
				
				if (lineWidth > availableWidth) {
					if (lastSpaceIndex != -1) {
						result.add(formattingThisLine + text.substring(lineBegin, lastSpaceIndex));
						formattingThisLine = formattingNextLine;
						
						i = lastSpaceIndex;
						lineBegin = lastSpaceIndex + 1;
						
						lastSpaceIndex = -1;
					} else {
						result.add(text.substring(lineBegin, i));
						lineBegin = i;
					}
					
					lineWidth = 0;
				}
			}
		}

		if (lineBegin != len)
			result.add(formattingThisLine + text.substring(lineBegin));
		
		return result;
	}
	
	default public int darkenColor(int color) {
		int a = (color >> 24) & 0xFF;
		int r = (color >> 16) & 0xFF;
		int g = (color >>  8) & 0xFF;
		int b = (color >>  0) & 0xFF;
	
		r = (int)(r * GSGUIConstants.COLOR_DARKEN_FACTOR);
		g = (int)(g * GSGUIConstants.COLOR_DARKEN_FACTOR);
		b = (int)(b * GSGUIConstants.COLOR_DARKEN_FACTOR);

		return (a << 24) | (r << 16) | (g << 8) | b;
	}
	
	default public int brightenColor(int color) {
		int a = (color >> 24) & 0xFF;
		int r = (color >> 16) & 0xFF;
		int g = (color >>  8) & 0xFF;
		int b = (color >>  0) & 0xFF;
	
		int i = (int)(1.0f / (1.0f - GSGUIConstants.COLOR_DARKEN_FACTOR));
		if (r == 0 && g == 0 && b == 0) {
			r = g = b = i;
		} else {
			if (r > 0 && r < i) r = i;
			if (g > 0 && g < i) g = i;
			if (b > 0 && b < i) b = i;
			
			r = Math.min((int)(r / GSGUIConstants.COLOR_DARKEN_FACTOR), 0xFF);
			g = Math.min((int)(g / GSGUIConstants.COLOR_DARKEN_FACTOR), 0xFF);
			b = Math.min((int)(b / GSGUIConstants.COLOR_DARKEN_FACTOR), 0xFF);
		}

		return (a << 24) | (r << 16) | (g << 8) | b;
	}
	
	default public void drawVerticalDottedLine(int x, int y0, int y1, int length, int spacing, int color) {
		int n = (y1 - y0) / (length + spacing);
		
		for (int yl = 0; yl <= n; yl++) {
			int yl0 = y0 + yl * (length + spacing);
			int yl1 = Math.min(yl0 + length, y1);
			DrawableHelper.fill(x, yl0, x + 1, yl1, color);
		}
	}

	default public void drawHorizontalDottedLine(int x0, int x1, int y, int length, int spacing, int color) {
		int n = (x1 - x0) / (length + spacing);
		
		for (int xl = 0; xl <= n; xl++) {
			int xl0 = x0 + xl * (length + spacing);
			int xl1 = Math.min(xl0 + length, x1);
			DrawableHelper.fill(xl0, y, xl1, y + 1, color);
		}
	}
	
	public TextRenderer getFont();
	
	default public GSTranslationModule getTranslationModule() {
		return GSControllerClient.getInstance().getTranslationModule();
	}
	
	static final class GSGUIConstants {
		
		private static final String TRIMMED_TEXT_ELLIPSIS = "...";
		private static final char FORMATTING_CHAR = '\u00A7';
		
		private static final float COLOR_DARKEN_FACTOR = 0.7f;
		
		private GSGUIConstants() {
		}
	}
}
