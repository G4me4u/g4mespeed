package com.g4mesoft.mixin.client;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.client.GSControllerClient;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.module.translation.GSTranslationModule;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;

@Mixin(InGameHud.class)
public abstract class GSInGameHudMixin {

	private static final int TPS_LABEL_MAGIN = 5;
	private static final int TPS_LABEL_COLOR = 0xFFFFFFFF;
	
	private static final DecimalFormat LOW_PRECISION_TPS_FORMAT = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ENGLISH));
	
	@Shadow @Final private MinecraftClient client;
	
	@Shadow public abstract TextRenderer getFontRenderer();

	@Inject(method = "render", at = @At(value = "INVOKE", shift = Shift.BEFORE, 
			target = "Lnet/minecraft/client/gui/hud/SubtitlesHud;render()V"))
	public void render(float partialTicks, CallbackInfo ci) {
		GSControllerClient controller = GSControllerClient.getInstance();
		GSTpsModule tpsModule = controller.getTpsModule();
		
		if (!client.options.debugEnabled && tpsModule.cShowTpsLabel.getValue()) {
			TextRenderer font = getFontRenderer();
			GSTranslationModule translationModule = controller.getTranslationModule();
			
			float averageTps = tpsModule.getServerTps();
			
			String current = LOW_PRECISION_TPS_FORMAT.format(averageTps);
			String target = GSTpsModule.TPS_FORMAT.format(tpsModule.getTps());
			String label = translationModule.getFormattedTranslation("play.info.tpsLabel", current, target);
			font.draw(label, TPS_LABEL_MAGIN, TPS_LABEL_MAGIN, TPS_LABEL_COLOR);
		}
	}
}
