package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;

import paulscode.sound.SoundBuffer;
import paulscode.sound.Source;
import paulscode.sound.libraries.SourceLWJGLOpenAL;

@Mixin(value = SourceLWJGLOpenAL.class, remap = false)
public class GSSourceLWJGLOpenALMixin extends Source {

	public GSSourceLWJGLOpenALMixin(Source old, SoundBuffer soundBuffer) {
		super(old, soundBuffer);
	}

	@Unique
	private GSTpsModule gs_tpsModule;
	
	@Inject(
		method = "<init>",
		at = @At("RETURN")
	)
	private void onInit(CallbackInfo ci) {
		gs_tpsModule = GSClientController.getInstance().getTpsModule();
	}
	
	@Inject(
		method = "setPitch",
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE,
			target =
				"Lpaulscode/sound/libraries/SourceLWJGLOpenAL;checkPitch(" +
				")V"
		)
	)
	public void onSetPitch(float newPitch, CallbackInfo ci) {
		// Note: pitch is generally clamped between 0.5 and 2.0, but when
		//       using OpenAL this restriction is not required.
		if (gs_tpsModule.cShiftPitch.get()) {
			// Scale pitch by relative tps difference to the default.
			this.pitch = newPitch * gs_tpsModule.getTps() / GSTpsModule.DEFAULT_TPS;
		}
	}
}