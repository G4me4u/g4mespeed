package com.g4mesoft.gui.scroll;

import com.g4mesoft.access.GSIBufferBuilderAccess;
import com.g4mesoft.gui.GSParentPanel;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;

public abstract class GSScrollableParentPanel extends GSParentPanel implements GSIScrollableViewport {

	private static final int SCROLL_BAR_MARGIN_X = 5;
	private static final int SCROLL_BAR_MARGIN_Y = 10;
	
	private final GSScrollBar scrollBar;
	
	public GSScrollableParentPanel() {
		scrollBar = new GSScrollBar(this, null);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		
		boolean visibleScrollBar = (getScrollableHeight() > height);
		if (visibleScrollBar)
			((GSIBufferBuilderAccess)buffer).pushClip(x, y, x + width, y + height);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		
		if (visibleScrollBar) {
			// We must remember to pop the clip
			((GSIBufferBuilderAccess)buffer).popClip();

			matrixStack.push();
			matrixStack.translate(x, y, 0.0);
			scrollBar.render(matrixStack, mouseX - x, mouseY - y, partialTicks);
			matrixStack.pop();
		}
	}
	
	@Override
	public void init() {
		super.init();
		
		scrollBar.initVerticalRight(client, width - SCROLL_BAR_MARGIN_X, SCROLL_BAR_MARGIN_Y, height - SCROLL_BAR_MARGIN_Y * 2);
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
	@SuppressWarnings("deprecation")
	public boolean onMouseScrolledGS(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scrollBar.mouseScrolled(mouseX, mouseY - getScrollOffset(), scrollY))
			return true;
		return super.onMouseScrolledGS(mouseX, mouseY, scrollX, scrollY);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public boolean onMouseClickedGS(double mouseX, double mouseY, int button) {
		if (scrollBar.mouseClicked(mouseX, mouseY - getScrollOffset(), button))
			return true;
		return super.onMouseClickedGS(mouseX, mouseY, button);
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean onMouseReleasedGS(double mouseX, double mouseY, int button) {
		if (scrollBar.mouseReleased(mouseX, mouseY - getScrollOffset(), button))
			return false;
		return super.onMouseReleasedGS(mouseX, mouseY, button);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public boolean onMouseDraggedGS(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (scrollBar.mouseDragged(mouseX, mouseY - getScrollOffset(), button, dragX, dragY))
			return true;
		return super.onMouseDraggedGS(mouseX, mouseY, button, dragX, dragY);
	}
}
