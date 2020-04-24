package com.g4mesoft.gui.widget;

import org.lwjgl.opengl.GL11;
import com.g4mesoft.core.GSCoreOverride;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.util.Identifier;

public class GSToggleSwitchWidget extends AbstractPressableButtonWidget {

	private static final Identifier TEXTURE = new Identifier("g4mespeed/textures/switch.png");

	public static final int TOGGLE_SWITCH_WIDTH = 30;
	public static final int TOGGLE_SWITCH_HEIGHT = 16;
	
	private boolean value;
	private GSSwitchListener listener;
	
	public GSToggleSwitchWidget(int x, int y, boolean enabled, GSSwitchListener listener) {
		super(x, y, TOGGLE_SWITCH_WIDTH, TOGGLE_SWITCH_HEIGHT, "");
		
		this.value = enabled;
		this.listener = listener;
	}
	
	@Override
	@GSCoreOverride
	public void onPress() {
		value = !value;
		
		listener.onStateChanged(value);
	}

	public void setValueSilent(boolean value) {
		this.value = value;
	}
	
	public boolean isEnabled() {
		return this.value;
	}

	@Override
	@GSCoreOverride
	public void renderButton(int mouseX, int mouseY, float partialTicks) {
		MinecraftClient.getInstance().getTextureManager().bindTexture(TEXTURE);

		GlStateManager.enableDepthTest();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, this.alpha);
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		float tx = active ? ((isMouseOver(mouseX, mouseY) || isFocused()) ? 30.0f : 0.0f) : 60.0f;
		float ty = value ? 16.0f : 0.0f;
		drawTexture(x, y, tx, ty, 30, 16, 90, 32);
	}
	
	public static interface GSSwitchListener {
		
		public void onStateChanged(boolean state);
		
	}
}
