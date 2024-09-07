package com.g4mesoft.mixin.client;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.client.GSClientController;
import com.g4mesoft.module.tps.GSTpsModule;
import com.g4mesoft.module.translation.GSTranslationModule;
import com.g4mesoft.ui.util.GSMathUtil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;

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
	
	@Unique
	private static final DecimalFormat LOW_PRECISION_TPS_FORMAT = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ENGLISH));

	@Shadow private int scaledWidth;
	@Shadow private int scaledHeight;

	@Shadow @Final private MinecraftClient client;
	
	@Shadow public abstract TextRenderer getFontRenderer();

	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE,
			target =
				"Lnet/minecraft/client/gui/hud/BossBarHud;render(" +
					"Lnet/minecraft/client/util/math/MatrixStack;" +
				")V"
		)
	)
	private void onRenderBeforeBossBar(MatrixStack matrixStack, float partialTicks, CallbackInfo ci) {
		if (GSClientController.getInstance().getTpsModule().cTpsLabel.get() == GSTpsModule.TPS_LABEL_TOP_CENTER) {
			matrixStack.push();
			matrixStack.translate(0.0, client.textRenderer.fontHeight + 5, 0.0);
		}
	}

	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			shift = Shift.AFTER,
			target =
				"Lnet/minecraft/client/gui/hud/BossBarHud;render(" +
					"Lnet/minecraft/client/util/math/MatrixStack;" +
				")V"
		)
	)
	private void onRenderAfterBossBar(MatrixStack matrixStack, float partialTicks, CallbackInfo ci) {
		if (GSClientController.getInstance().getTpsModule().cTpsLabel.get() == GSTpsModule.TPS_LABEL_TOP_CENTER)
			matrixStack.pop();
	}
	
	@Inject(
		method = "render",
		at = @At(
			value = "INVOKE",
			shift = Shift.BEFORE, 
			target =
				"Lnet/minecraft/client/gui/hud/SubtitlesHud;render(" +
					"Lnet/minecraft/client/util/math/MatrixStack;" +
				")V"
		)
	)
	private void onRenderBeforeSubtitles(MatrixStack matrixStack, float partialTicks, CallbackInfo ci) {
		GSClientController controller = GSClientController.getInstance();
		GSTpsModule tpsModule = controller.getTpsModule();
		
		int labelLocation = tpsModule.cTpsLabel.get();
		if (!client.options.debugEnabled && labelLocation != GSTpsModule.TPS_LABEL_DISABLED) {
			TextRenderer font = getFontRenderer();
			GSTranslationModule translationModule = controller.getTranslationModule();
			
			float averageTps = tpsModule.getServerTps();
			float targetTps = tpsModule.getTps();
			
			String current = LOW_PRECISION_TPS_FORMAT.format(averageTps);
			String target = GSTpsModule.TPS_FORMAT.format(targetTps);
			
			String targetText = translationModule.getFormattedTranslation("play.info.tpsLabelTarget", target);
			
			int lx;
			int ly = TPS_LABEL_MAGIN;
			int lw = font.getWidth(current + " " + targetText);
			int lh = font.fontHeight;

			switch (labelLocation) {
			case GSTpsModule.TPS_LABEL_TOP_CENTER:
				lx = (scaledWidth - lw) / 2;
				break;
			case GSTpsModule.TPS_LABEL_TOP_RIGHT:
				lx = scaledWidth - lw - TPS_LABEL_MAGIN + 1;
				break;
			case GSTpsModule.TPS_LABEL_TOP_LEFT:
			default:
				lx = TPS_LABEL_MAGIN;
				break;
			}
			
			fill(matrixStack, lx - 1, ly - 1, lx + lw, ly + lh, LABEL_BACKGROUND_COLOR);
			
			float tx = font.draw(matrixStack, current, lx, ly, getTpsLabelColor(averageTps, targetTps));
			font.draw(matrixStack, targetText, tx + font.getWidth(" "), ly, LABEL_TARGET_COLOR);
		}
	}
	
	@Unique
	private static int getTpsLabelColor(float averageTps, float targetTps) {
		float frac = Math.max(averageTps / targetTps, 0.0f);
		
		float rs = Math.max(0.0f, (frac - RED_START  ) / (RED_END   - RED_START  ));
		float gs = Math.max(0.0f, (frac - GREEN_START) / (GREEN_END - GREEN_START));
		float bs = Math.max(0.0f, (frac - BLUE_START ) / (BLUE_END  - BLUE_START ));

		int r = GSMathUtil.clamp((int)(rs * LABEL_COLOR_DIFF) + LABEL_COLOR_MIN, 0, 0xFF);
		int g = GSMathUtil.clamp((int)(gs * LABEL_COLOR_DIFF) + LABEL_COLOR_MIN, 0, 0xFF);
		int b = GSMathUtil.clamp((int)(bs * LABEL_COLOR_DIFF) + LABEL_COLOR_MIN, 0, 0xFF);
		
		return (0xFF << 24) | (r << 16) | (g << 8) | b;
	}
}
