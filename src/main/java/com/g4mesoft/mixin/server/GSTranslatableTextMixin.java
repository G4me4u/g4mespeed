package com.g4mesoft.mixin.server;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.g4mesoft.core.GSController;
import com.g4mesoft.core.server.GSServerController;
import com.g4mesoft.module.translation.GSTranslationModule;

import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.text.TranslationException;
import net.minecraft.util.Language;

@Mixin(TranslatableText.class)
public abstract class GSTranslatableTextMixin {

	@Shadow private Language languageCache;
	@Shadow @Final private String key;
	@Shadow @Final protected List<Text> translations;
	
	private long lastTranslationTimestamp = -1L;
	
	@Shadow protected abstract void setTranslation(String translation);
	
	@Inject(method = "updateTranslations", at = @At("HEAD"), cancellable = true)
	public void onUpdateTranslations(CallbackInfo ci) {
		GSController threadController = GSController.getInstanceOnThread();
		
		// If we don't know which controller we are
		// dealing with, fallback to server controller
		if (threadController == null)
			threadController = GSServerController.getInstance();
	
		GSTranslationModule translationModule = threadController.getTranslationModule();
		if (translationModule.hasTranslation(this.key)) {
			Language language = Language.getInstance();
			
			long timestamp = translationModule.getTranslationTimestamp();
			if (lastTranslationTimestamp == timestamp && !this.translations.isEmpty() && this.languageCache == language) {
				ci.cancel();
				return;
			}
			
			lastTranslationTimestamp = timestamp;
			this.translations.clear();
			
			try {
				this.setTranslation(translationModule.getTranslation(key));
				this.languageCache = language;
				ci.cancel();
			} catch (TranslationException e) {
				this.translations.clear();
				
				// Make sure we fallback to default
				this.languageCache = null;
				lastTranslationTimestamp = -1L;
			}
		}
	}
}
