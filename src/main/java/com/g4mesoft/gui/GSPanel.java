package com.g4mesoft.gui;

import com.g4mesoft.access.GSIBufferBuilderAccess;
import com.g4mesoft.access.GSIMouseAccess;
import com.g4mesoft.core.GSCoreOverride;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;

public abstract class GSPanel extends DrawableHelper implements GSElement, GSIDrawableHelper, GSIViewport {

	private boolean selected;
	
	protected MinecraftClient client;
	protected TextRenderer font;
	
	private boolean added;
	private boolean focused;
	
	public int x;
	public int y;
	public int width;
	public int height;
	
	protected GSPanel() {
		setSelected(true);
	}
	
	public void initBounds(MinecraftClient client, int x, int y, int width, int height) {
		if (this.client == null || this.x != x || this.y != y || this.width != width || this.height != height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		
			this.client = client;
			this.font = client.textRenderer;
			
			init();
		}
	}

	public void init() {
	}

	protected void onAdded() {
		added = true;
	}

	protected void onRemoved() {
		added = false;
		focused = false;
		
		// Used when initializing to check if the panel
		// was removed from its parent.
		client = null;
	}
	
	public void tick() {
	}
	
	protected int getTranslationX() {
		return x;
	}

	protected int getTranslationY() {
		return y;
	}
	
	@Deprecated
	public void render(int mouseX, int mouseY, float partialTicks) {
		int tx = getTranslationX();
		int ty = getTranslationY();

		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		float oldOffsetX = ((GSIBufferBuilderAccess)buffer).getOffsetX();
		float oldOffsetY = ((GSIBufferBuilderAccess)buffer).getOffsetY();
		float oldOffsetZ = ((GSIBufferBuilderAccess)buffer).getOffsetZ();
		
		float oldClipOffsetX = ((GSIBufferBuilderAccess)buffer).getClipXOffset();
		float oldClipOffsetY = ((GSIBufferBuilderAccess)buffer).getClipYOffset();
		
		((GSIBufferBuilderAccess)buffer).setOffset(oldOffsetX + tx, oldOffsetY + ty, oldOffsetZ);
		((GSIBufferBuilderAccess)buffer).setClipOffset(oldClipOffsetX + tx, oldClipOffsetY + ty);
		
		renderTranslated(mouseX - tx, mouseY - ty, partialTicks);

		((GSIBufferBuilderAccess)buffer).setClipOffset(oldClipOffsetX, oldClipOffsetY);
		((GSIBufferBuilderAccess)buffer).setOffset(oldOffsetX, oldOffsetY, oldOffsetZ);
	}
	
	protected void renderTranslated(int mouseX, int mouseY, float partialTicks) {
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}
	
	private double translateMouseX(double mouseX) {
		return mouseX - getTranslationX();
	}

	private double translateMouseY(double mouseY) {
		return mouseY - getTranslationY();
	}
	
	@Override
	@Deprecated
	@GSCoreOverride
	public final void mouseMoved(double mouseX, double mouseY) {
		if (selected)
			onMouseMovedGS(translateMouseX(mouseX), translateMouseY(mouseY));
	}

	@Override
	@Deprecated
	@GSCoreOverride
	public final boolean mouseClicked(double mouseX, double mouseY, int button) {
		return selected && onMouseClickedGS(translateMouseX(mouseX), translateMouseY(mouseY), button);
	}

	@Override
	@Deprecated
	@GSCoreOverride
	public final boolean mouseReleased(double mouseX, double mouseY, int button) {
		return selected && onMouseReleasedGS(translateMouseX(mouseX), translateMouseY(mouseY), button);
	}

	@Override
	@Deprecated
	@GSCoreOverride
	public final boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return selected && onMouseDraggedGS(translateMouseX(mouseX), translateMouseY(mouseY), button, dragX, dragY);
	}
	
	@Override
	@Deprecated
	@GSCoreOverride
	public final boolean keyPressed(int key, int scancode, int mods) {
		if (preKeyPressed(key, scancode, mods))
			return true;
		return selected && onKeyPressedGS(key, scancode, mods);
	}

	@Override
	@Deprecated
	@GSCoreOverride
	public final boolean keyReleased(int key, int scancode, int mods) {
		if (preKeyReleased(key, scancode, mods))
			return true;
		return selected && onKeyReleasedGS(key, scancode, mods);
	}

	@Override
	@Deprecated
	@GSCoreOverride
	public final boolean charTyped(char c, int mods) {
		return selected && onCharTypedGS(c, mods);
	}
	
	@Override
	@Deprecated
	@GSCoreOverride
	public final boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
		if (!selected)
			return false;
		
		double scrollX = ((GSIMouseAccess)MinecraftClient.getInstance().mouse).getScrollX();
		return onMouseScrolledGS(translateMouseX(mouseX), translateMouseY(mouseY), scrollX, scrollY);
	}
	
	@Override
	@GSCoreOverride
	public final boolean isMouseOver(double mouseX, double mouseY) {
		if (!selected)
			return false;
		
		return mouseX >= x && mouseX < x + width && 
		       mouseY >= y && mouseY < y + height;
	}
	
	@Override
	public boolean isAdded() {
		return added;
	}
	
	@Override
	public boolean isElementFocused() {
		return focused;
	}
	
	@Override
	public void setElementFocused(boolean focused) {
		this.focused = focused;
	}
	
	boolean preKeyPressed(int keyCode, int scanCode, int modifiers) {
		return false;
	}
	
	boolean preKeyReleased(int keyCode, int scanCode, int modifiers) {
		return false;
	}
	
	@Override
	public boolean isEditingText() {
		return false;
	}
	
	@Override
	public TextRenderer getFont() {
		return font;
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return height;
	}
}
