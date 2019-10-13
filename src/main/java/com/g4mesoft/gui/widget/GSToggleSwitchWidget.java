package com.g4mesoft.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.util.Identifier;

public class GSToggleSwitchWidget extends AbstractPressableButtonWidget {

	private static final Identifier TEXTURE = new Identifier("g4mespeed/textures/switch.png");

	public static final int TOGGLE_SWITCH_WIDTH = 30;
	public static final int TOGGLE_SWITCH_HEIGHT = 16;
	
	private boolean enabled;
	private GSSwitchListener listener;
	
	public GSToggleSwitchWidget(int x, int y, boolean enabled, GSSwitchListener listener) {
		super(x, y, TOGGLE_SWITCH_WIDTH, TOGGLE_SWITCH_HEIGHT, "");
		
		this.enabled = enabled;
		this.listener = listener;
	}
	
	@Override
	public void onPress() {
		enabled = !enabled;
		
		listener.onStateChanged(enabled);
	}

	public void setEnabledSilent(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}

	@Override
	public void renderButton(int mouseX, int mouseY, float partialTicks) {
		MinecraftClient.getInstance().getTextureManager().bindTexture(TEXTURE);

		GlStateManager.enableDepthTest();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, this.alpha);
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		
		float tx = (isMouseOver(mouseX, mouseY) || isFocused()) ? 30.0f : 0.0f;
		float ty = enabled ? 16.0f : 0.0f;
		blit(x, y, tx, ty, 30, 16, 60, 32);
	}
	
	public static interface GSSwitchListener {
		
		public void onStateChanged(boolean state);
		
	}
}
