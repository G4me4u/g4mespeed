package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.g4mesoft.access.GSIVertexFormatElementAccess;

import net.minecraft.client.render.VertexFormatElement;

@Mixin(VertexFormatElement.class)
public class GSVertexFormatElementMixin implements GSIVertexFormatElementAccess {

	@Shadow @Final private int count;
	
	@Override
	public int getCount() {
		return count;
	}
}
