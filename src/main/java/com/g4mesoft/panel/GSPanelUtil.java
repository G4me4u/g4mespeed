package com.g4mesoft.panel;

import com.g4mesoft.renderer.GSIRenderer2D;

import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public final class GSPanelUtil {

	private GSPanelUtil() {
	}
	
	public static void drawLabel(GSIRenderer2D renderer, GSIcon icon, int spacing, Text text, int textColor,
	                             boolean shadowed, GSEIconAlignment iconAlignment, GSETextAlignment textAlignment,
	                             int x, int y, int width, int height) {
		
		// Remaining width for text
		int rw = width;
		if (icon != null) {
			rw -= icon.getWidth();
			// Handle extra margin between text and icon
			if (text != null)
				rw -= spacing;
		}
		
		// Trim text and get text width
		OrderedText trimmedText = null;
		int tw = 0;
		if (text != null) {
			trimmedText = renderer.trimString(text, rw);
			tw = (int)Math.ceil(renderer.getTextWidth(trimmedText));
		}
		
		// Handle text alignment
		int cx = x;
		switch (textAlignment) {
		case CENTER:
			cx += (rw - tw) / 2;
			break;
		case RIGHT:
			cx += rw - tw;
			break;
		case LEFT:
		default:
			break;
		}
		
		if (text != null) {
			int tx = cx;
			int ty = y + (height - renderer.getTextAscent()) / 2;
		
			// Handle left icon alignment (move text to the right)
			if (icon != null && iconAlignment != GSEIconAlignment.RIGHT)
				tx += icon.getWidth() + spacing;
			renderer.drawText(trimmedText, tx, ty, textColor, shadowed);
		}

		if (icon != null) {
			int ix = cx;
			int iy = y + (height - icon.getHeight()) / 2;
			
			// Handle right icon alignment (move icon to the right)
			if (text != null && iconAlignment == GSEIconAlignment.RIGHT)
				ix += tw + spacing;
			
			icon.render(renderer, new GSRectangle(ix, iy, icon.getSize()));
		}
	}
	
	public static GSDimension labelPreferredSize(GSIcon icon, Text text, int spacing) {
		GSIRenderer2D renderer = GSPanelContext.getRenderer();
		
		int w = 0, h = 0;
		if (text != null) {
			w = (int)Math.ceil(renderer.getTextWidth(text));
			h = renderer.getLineHeight();
			
			if (icon != null) {
				w += icon.getWidth() + spacing;
				h = Math.max(h, icon.getHeight());
			}
		} else if (icon != null) {
			w = icon.getWidth();
			h = icon.getHeight();
		}
		
		return new GSDimension(w, h);
	}
	
	public static GSLocation getViewLocation(GSPanel panel) {
		int x = 0, y = 0;
		while (panel != null) {
			x += panel.getViewOffsetX();
			y += panel.getViewOffsetY();
			panel = panel.getParent();
		}
		
		return new GSLocation(x, y);
	}
}
