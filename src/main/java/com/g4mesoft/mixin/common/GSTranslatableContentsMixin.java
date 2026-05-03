package com.g4mesoft.mixin.common;

import java.util.List;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.GSController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.module.translation.GSTranslationModule;
import com.google.common.collect.ImmutableList;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.contents.TranslatableFormatException;

@Mixin(TranslatableContents.class)
public abstract class GSTranslatableContentsMixin {

	@Shadow @Final private String key;
	@Shadow private Language decomposedWith;
	@Shadow private List<FormattedText> decomposedParts;
	
	@Shadow protected abstract void decomposeTemplate(String translation, Consumer<FormattedText> partsConsumer);

	@Unique
	private long gs_lastTranslationTimestamp = -1L;
	
	@Inject(
		method = "decompose",
		cancellable = true,
		at = @At("HEAD")
	)
	private void onDecompose(CallbackInfo ci) {
		if (!GSController.hasInstances())
			return;

		GSController threadController = GSController.getInstanceOnThread();

		// If we don't know which controller we are
		// dealing with, fallback to server controller
		if (threadController == null)
			threadController = GSServerController.getInstance();

		GSTranslationModule translationModule = threadController.getTranslationModule();
		if (translationModule.hasTranslation(this.key)) {
			Language language = Language.getInstance();
			
			long timestamp = translationModule.getTranslationTimestamp();
			if (gs_lastTranslationTimestamp == timestamp && !this.decomposedParts.isEmpty() && this.decomposedWith == language) {
				ci.cancel();
				return;
			}
			
			gs_lastTranslationTimestamp = timestamp;
			this.decomposedWith = language;
			
			try {
				ImmutableList.Builder<FormattedText> builder = ImmutableList.builder();
				this.decomposeTemplate(translationModule.getTranslation(key), builder::add);
				this.decomposedParts = builder.build();
				ci.cancel();
			} catch (TranslatableFormatException e) {
				this.decomposedParts = ImmutableList.of();
				
				// Make sure we fallback to default
				this.decomposedWith = null;
				gs_lastTranslationTimestamp = -1L;
			}
		}
	}
}
