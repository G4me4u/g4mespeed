package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.client.render.block.entity.PistonBlockEntityRenderer;

@Mixin(PistonBlockEntityRenderer.class)
public class GSPistonBlockEntityRendererMixin {

	@ModifyConstant(
		method = "render",
		constant = @Constant(
			floatValue = 4.0f
		)
	)
	private float fixShortArm(float shortArmCutoff) {
		return 0.5f;
	}
	
	@Inject(method = "getRenderDistance", cancellable = true, at = @At("HEAD"))
	private void onGetRenderDistance(CallbackInfoReturnable<Integer> cir) {
		GSTpsModule tpsModule = GSClientController.getInstance().getTpsModule();
		int chunkDist = tpsModule.cPistonRenderDistance.get();
		if (chunkDist == GSTpsModule.AUTOMATIC_PISTON_RENDER_DISTANCE) {
			if (tpsModule.sParanoidMode.get()) {
				// When using paranoid mode there is no limit to where
				// the piston block entities might occur. So we just
				// render all of the ones within maximum view distance.
				chunkDist = tpsModule.cPistonRenderDistance.getMax();
			} else {
				chunkDist = tpsModule.sBlockEventDistance.get();
			}
		}
		
		cir.setReturnValue(chunkDist * 16);
		cir.cancel();
	}
}
