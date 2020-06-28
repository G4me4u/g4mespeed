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
import com.g4mesoft.util.GSMathUtils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;

@Mixin(InGameHud.class)
public abstract class GSInGameHudMixin extends DrawableHelper {

	private static final int TPS_LABEL_MAGIN = 5;

	private static final float RED_START   = 1.00f;
	private static final float RED_END     = 0.75f; // 100% -  75%
	private static final float GREEN_START = 0.50f;
	private static final float GREEN_END   = 0.75f; //  50% -  75%
	private static final float BLUE_START  = 1.00f;
	private static final float BLUE_END    = 1.50f; // 100% - 150%

	private static final int LABEL_COLOR_MIN = 0x40;
	private static final int LABEL_COLOR_MAX = 0xDA;
	private static final int LABEL_COLOR_DIFF = LABEL_COLOR_MAX - LABEL_COLOR_MIN;

	private static final int LABEL_BACKGROUND_COLOR = 0x60333333;
	private static final int LABEL_TARGET_COLOR     = 0xFFEEEEEE;
	
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
			float targetTps = tpsModule.getTps();
			
			String current = LOW_PRECISION_TPS_FORMAT.format(averageTps);
			String target = GSTpsModule.TPS_FORMAT.format(targetTps);
			
			String targetText = translationModule.getFormattedTranslation("play.info.tpsLabelTarget", target);
			
			int lx0 = TPS_LABEL_MAGIN;
			int ly0 = TPS_LABEL_MAGIN;
			int lx1 = lx0 + font.getStringWidth(current + " " + targetText);
			int ly1 = ly0 + font.fontHeight;
			
			fill(lx0 - 1, ly0 - 1, lx1, ly1, LABEL_BACKGROUND_COLOR);
			
			float tx = font.draw(current, lx0, ly0, getTpsLabelColor(averageTps, targetTps));
			font.draw(targetText, tx + font.getCharWidth(' '), ly0, LABEL_TARGET_COLOR);
		}
	}
	
	private static int getTpsLabelColor(float averageTps, float targetTps) {
		float frac = Math.max(averageTps / targetTps, 0.0f);
		
		float rs = Math.max(0.0f, (frac - RED_START  ) / (RED_END   - RED_START  ));
		float gs = Math.max(0.0f, (frac - GREEN_START) / (GREEN_END - GREEN_START));
		float bs = Math.max(0.0f, (frac - BLUE_START ) / (BLUE_END  - BLUE_START ));

		int r = GSMathUtils.clamp((int)(rs * LABEL_COLOR_DIFF) + LABEL_COLOR_MIN, 0, 0xFF);
		int g = GSMathUtils.clamp((int)(gs * LABEL_COLOR_DIFF) + LABEL_COLOR_MIN, 0, 0xFF);
		int b = GSMathUtils.clamp((int)(bs * LABEL_COLOR_DIFF) + LABEL_COLOR_MIN, 0, 0xFF);
		
		return (0xFF << 24) | (r << 16) | (g << 8) | b;
	}
}
