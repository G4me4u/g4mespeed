package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.g4mesoft.settings.GSIKeyBinding;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;

@Mixin(KeyBinding.class)
@Environment(EnvType.CLIENT)
public class GSKeyBindingMixin implements GSIKeyBinding {

	@Shadow private InputUtil.KeyCode keyCode;
	
	@Override
	public int getKeyCode() {
		return keyCode.getKeyCode();
	}
}
