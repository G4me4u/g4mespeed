package com.g4mesoft.gui;

import java.awt.Rectangle;

import org.lwjgl.glfw.GLFW;

import com.g4mesoft.access.GSIBufferBuilderAccess;
import com.g4mesoft.util.GSMathUtils;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;

public abstract class GSScrollablePanel extends GSPanel {

	private static final double SCROLL_AMOUNT = 10.0;
	
	private static final int SCROLL_BAR_WIDTH    = 5;
	private static final int SCROLL_BAR_MARGIN_X = 5;
	private static final int SCROLL_BAR_MARGIN_Y = 10;
	
	private static final int SCROLL_HIGHLIGHT  = 0xFFC0C0C0;
	private static final int SCROLL_SHADOW     = 0xFF808080;
	private static final int SCROLL_BACKGROUND = 0xFF000000;
	
	protected double scrollOffset;
	protected boolean scrollDragActive;
	
	protected abstract int getScrollableHeight();
	
	public void setScrollOffset(double scroll) {
		int maxScrollOffset = Math.max(getScrollableHeight() - height, 0);
		this.scrollOffset = GSMathUtils.clamp(scroll, 0.0, maxScrollOffset);
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		GSIBufferBuilderAccess bufferAccess = (GSIBufferBuilderAccess)buffer;
		GSClipRect oldClipRect = bufferAccess.getClip();

		int scrollableHeight = getScrollableHeight();
		if (scrollableHeight > height)
			bufferAccess.setClip(x, y, x + width, y + height);
		
		super.render(mouseX, mouseY, partialTicks);
		bufferAccess.setClip(oldClipRect);
		
		if (scrollableHeight > height) {
			// Note that we're not rendering in a translated
			// mode, so we'll have to translate ourselves.
			int sx = x + width - SCROLL_BAR_WIDTH - SCROLL_BAR_MARGIN_X;
			int sy = y + SCROLL_BAR_MARGIN_Y;
			fill(sx, sy, sx + SCROLL_BAR_WIDTH, sy + height - SCROLL_BAR_MARGIN_Y * 2, SCROLL_BACKGROUND);
			
			Rectangle r = getDraggableScrollArea();
			r.translate(x, y);
			
			fill(r.x, r.y, r.x + r.width    , r.y + r.height    , SCROLL_SHADOW);
			fill(r.x, r.y, r.x + r.width - 1, r.y + r.height - 1, SCROLL_HIGHLIGHT);
		}
	}
	
	private Rectangle getDraggableScrollArea() {
		int scrollableHeight = getScrollableHeight();
		Rectangle rect = new Rectangle();
		if (scrollableHeight > height) {
			int h = height - SCROLL_BAR_MARGIN_Y * 2;
			rect.x = width - SCROLL_BAR_WIDTH - SCROLL_BAR_MARGIN_X;
			rect.width = SCROLL_BAR_WIDTH;
			rect.height = Math.max(h * height / scrollableHeight, SCROLL_BAR_WIDTH);
			rect.y = SCROLL_BAR_MARGIN_Y + (h - rect.height) * getScrollOffset() / (scrollableHeight - height);
		}
		return rect;
	}
	
	@Override
	public void init() {
		super.init();
		
		// Update the scroll offset to make sure
		// it is a valid scroll.
		setScrollOffset(scrollOffset);
	}
	
	@Override
	protected int getTranslationY() {
		return super.getTranslationY() - getScrollOffset();
	}
	
	@Override
	protected boolean mouseScrolledTranslated(double mouseX, double mouseY, double scroll) {
		if (isMouseInView(mouseX, mouseY - getScrollOffset())) {
			setScrollOffset(scrollOffset - (int)(scroll * SCROLL_AMOUNT));
			return true;
		}
		
		return super.mouseScrolledTranslated(mouseX, mouseY, scroll);
	}
	
	private boolean isMouseInView(double mouseX, double mouseY) {
		return mouseX > 0.0 && mouseY > 0.0 && mouseX < width && mouseY < height;
	}
	
	@Override
	protected boolean mouseClickedTranslated(double mouseX, double mouseY, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_1 && getDraggableScrollArea().contains(mouseX, mouseY - getScrollOffset())) {
			scrollDragActive = true;
			return true;
		}
		
		return super.mouseClickedTranslated(mouseX, mouseY, button);
	}

	@Override
	protected boolean mouseReleasedTranslated(double mouseX, double mouseY, int button) {
		scrollDragActive = false;
		return super.mouseReleasedTranslated(mouseX, mouseY, button);
	}
	
	@Override
	protected boolean mouseDraggedTranslated(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (scrollDragActive) {
			Rectangle r = getDraggableScrollArea();
			int h = height - SCROLL_BAR_MARGIN_Y * 2;
			if (r.height < h) {
				setScrollOffset(scrollOffset + dragY * (getScrollableHeight() - height) / (h - r.height));
				return true;
			}
		}
		
		return super.mouseDraggedTranslated(mouseX, mouseY, button, dragX, dragY);
	}
	
	public int getScrollOffset() {
		return (int)scrollOffset;
	}
}
