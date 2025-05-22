package com.g4mesoft.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;

import net.minecraft.unmapped.C_7775554;
import paulscode.sound.SoundBuffer;
import paulscode.sound.Source;

@Mixin(C_7775554.class)
public class GSSourceOpenALMixin extends Source {

	public GSSourceOpenALMixin(Source old, SoundBuffer soundBuffer) {
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
				"Lnet/minecraft/unmapped/C_7775554;m_8107823(" +
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
