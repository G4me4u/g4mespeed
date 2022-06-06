package com.g4mesoft.mixin.server;

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

import net.minecraft.text.StringVisitable;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.text.TranslationException;
import net.minecraft.util.Language;

@Mixin(TranslatableTextContent.class)
public abstract class GSTranslatableTextMixin {

	@Shadow @Final private String key;
	@Shadow private Language languageCache;
	@Shadow private List<StringVisitable> translations;
	
	@Shadow protected abstract void forEachPart(String translation, Consumer<StringVisitable> partsConsumer);

	@Unique
	private long gs_lastTranslationTimestamp = -1L;
	
	@Inject(method = "updateTranslations", at = @At("HEAD"), cancellable = true)
	private void onUpdateTranslations(CallbackInfo ci) {
		GSController threadController = GSController.getInstanceOnThread();
		
		// If we don't know which controller we are
		// dealing with, fallback to server controller
		if (threadController == null)
			threadController = GSServerController.getInstance();
	
		GSTranslationModule translationModule = threadController.getTranslationModule();
		if (translationModule.hasTranslation(this.key)) {
			Language language = Language.getInstance();
			
			long timestamp = translationModule.getTranslationTimestamp();
			if (gs_lastTranslationTimestamp == timestamp && !this.translations.isEmpty() && this.languageCache == language) {
				ci.cancel();
				return;
			}
			
			gs_lastTranslationTimestamp = timestamp;
			this.languageCache = language;
			
			try {
				ImmutableList.Builder<StringVisitable> builder = ImmutableList.builder();
				this.forEachPart(translationModule.getTranslation(key), builder::add);
				this.translations = builder.build();
				ci.cancel();
			} catch (TranslationException e) {
				this.translations = ImmutableList.of();
				
				// Make sure we fallback to default
				this.languageCache = null;
				gs_lastTranslationTimestamp = -1L;
			}
		}
	}
}
