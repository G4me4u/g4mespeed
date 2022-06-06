package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.VertexFormatElement;

@Mixin(VertexFormatElement.class)
public interface GSIVertexFormatElementAccess {

	@Accessor("componentCount")
	public int getComponentCount();
	
}
