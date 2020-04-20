package com.g4mesoft.gui;

import com.g4mesoft.access.GSIBufferBuilderAccess;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;

public abstract class GSScrollablePanel extends GSPanel implements GSIScrollableViewport {

	private static final int SCROLL_BAR_MARGIN_X = 5;
	private static final int SCROLL_BAR_MARGIN_Y = 10;
	
	private final GSScrollBar scrollBar;
	
	public GSScrollablePanel() {
		scrollBar = new GSScrollBar(true, this, null);
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
			double oldOffsetX = bufferAccess.getOffsetX();
			double oldOffsetY = bufferAccess.getOffsetY();
			double oldOffsetZ = bufferAccess.getOffsetZ();
			buffer.setOffset(oldOffsetX + x, oldOffsetY + y, oldOffsetZ);
			scrollBar.render(mouseX - x, mouseY - y, partialTicks);
			buffer.setOffset(oldOffsetX, oldOffsetY, oldOffsetZ);
		}
	}
	
	@Override
	public void init() {
		super.init();
		
		scrollBar.init(client, SCROLL_BAR_MARGIN_X, SCROLL_BAR_MARGIN_Y);
	}
	
	protected abstract int getScrollableHeight();
	
	@Override
	public int getContentHeight() {
		return getScrollableHeight();
	}
	
	@Override
	public int getContentWidth() {
		return width;
	}
	
	@Override
	protected int getTranslationY() {
		return super.getTranslationY() - getScrollOffset();
	}
	
	protected int getScrollOffset() {
		return (int)scrollBar.getScrollOffset();
	}

	@Override
	protected boolean mouseScrolledTranslated(double mouseX, double mouseY, double scroll) {
		if (scrollBar.mouseScrolled(mouseX, mouseY - getScrollOffset(), scroll))
			return true;
		return super.mouseScrolledTranslated(mouseX, mouseY, scroll);
	}
	
	@Override
	protected boolean mouseClickedTranslated(double mouseX, double mouseY, int button) {
		if (scrollBar.mouseClicked(mouseX, mouseY - getScrollOffset(), button))
			return true;
		return super.mouseClickedTranslated(mouseX, mouseY, button);
	}

	@Override
	protected boolean mouseReleasedTranslated(double mouseX, double mouseY, int button) {
		if (scrollBar.mouseReleased(mouseX, mouseY - getScrollOffset(), button))
			return false;
		return super.mouseReleasedTranslated(mouseX, mouseY, button);
	}
	
	@Override
	protected boolean mouseDraggedTranslated(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (scrollBar.mouseDragged(mouseX, mouseY - getScrollOffset(), button, dragX, dragY))
			return true;
		return super.mouseDraggedTranslated(mouseX, mouseY, button, dragX, dragY);
	}
}
