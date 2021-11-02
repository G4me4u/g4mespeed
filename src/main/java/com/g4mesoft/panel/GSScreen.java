package com.g4mesoft.panel;

import com.g4mesoft.access.client.GSIKeyboardAccess;
import com.g4mesoft.access.client.GSIMouseAccess;
import com.g4mesoft.core.GSCoreOverride;
import com.g4mesoft.renderer.GSBasicRenderer2D;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;

final class GSScreen extends Screen {

	private final GSRootPanel rootPanel;

	private boolean visible;
	
	GSScreen() {
		super(NarratorManager.EMPTY);
	
		rootPanel = new GSRootPanel();
		
		visible = false;
	}

	@Override
	@GSCoreOverride
	protected void init() {
		super.init();
	
		client.keyboard.setRepeatEvents(true);
		rootPanel.setBounds(0, 0, width, height);
		
		setVisibleImpl(true);
	}
	
	@Override
	@GSCoreOverride
	public void removed() {
		super.removed();

		client.keyboard.setRepeatEvents(false);

		setVisibleImpl(false);
	}
	
	private void setVisibleImpl(boolean visible) {
		if (visible != this.visible) {
			this.visible = visible;
			rootPanel.setVisible(visible);
		}
	}
	
	@Override
	@GSCoreOverride
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		// Validate panels before rendering
		GSPanelContext.validateAll();
		
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		
		GSIRenderer2D renderer = GSPanelContext.getRenderer();
		
		((GSBasicRenderer2D)renderer).begin(Tessellator.getInstance().getBuffer(),
				matrixStack, mouseX, mouseY, width, height);
		
		rootPanel.preRender(renderer);
		rootPanel.render(renderer);
		rootPanel.postRender(renderer);
		
		((GSBasicRenderer2D)renderer).end();
		
		RenderSystem.disableBlend();
		RenderSystem.enableTexture();
	}

	@Override
	@GSCoreOverride
	public void mouseMoved(double mouseX, double mouseY) {
		GSPanelContext.getEventDispatcher().mouseMoved((float)mouseX, (float)mouseY);
	}

	@Override
	@GSCoreOverride
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int modifiers = ((GSIMouseAccess)client.mouse).getPreviousEventModifiers();
		GSPanelContext.getEventDispatcher().mousePressed(button, (float)mouseX, (float)mouseY, modifiers);
		return true;
	}

	@Override
	@GSCoreOverride
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		int modifiers = ((GSIMouseAccess)client.mouse).getPreviousEventModifiers();
		GSPanelContext.getEventDispatcher().mouseReleased(button, (float)mouseX, (float)mouseY, modifiers);
		return true;
	}

	@Override
	@GSCoreOverride
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		GSPanelContext.getEventDispatcher().mouseDragged(button, (float)mouseX, (float)mouseY, (float)deltaX, (float)deltaY);
		return true;
	}

	@Override
	@GSCoreOverride
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
		float scrollX = (float)((GSIMouseAccess)client.mouse).getPreviousEventScrollX();
		GSPanelContext.getEventDispatcher().mouseScroll((float)mouseX, (float)mouseY, scrollX, (float)scrollY);
		return true;
	}

	@Override
	@GSCoreOverride
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (((GSIKeyboardAccess)client.keyboard).isPreviousEventRepeating()) {
			GSPanelContext.getEventDispatcher().keyRepeated(keyCode, scanCode, modifiers);
		} else {
			GSPanelContext.getEventDispatcher().keyPressed(keyCode, scanCode, modifiers);
		}
		return true;
	}

	@Override
	@GSCoreOverride
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		GSPanelContext.getEventDispatcher().keyReleased(keyCode, scanCode, modifiers);
		return true;
	}

	@Override
	@GSCoreOverride
	public boolean charTyped(char chr, int keyCode) {
		GSPanelContext.getEventDispatcher().keyTyped((int)chr);
		return true;
	}

	public GSRootPanel getRootPanel() {
		return rootPanel;
	}
}
