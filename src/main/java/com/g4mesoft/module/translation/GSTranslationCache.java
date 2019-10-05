package com.g4mesoft.module.translation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GSTranslationCache {

	private final int cacheVersion;
	private final Map<String, String> translations;
	
	public GSTranslationCache(int cacheVersion, Map<String, String> translations) {
		this.cacheVersion = cacheVersion;
		this.translations = new HashMap<String, String>(translations);
	}
	
	public void getAllTranslations(Map<String, String> dest) {
		dest.putAll(translations);
	}

	public int getCacheVersion() {
		return cacheVersion;
	}

	public GSTranslationCache merge(GSTranslationCache cache) {
		Map<String, String> result = new HashMap<String, String>(translations);
		result.putAll(cache.translations);
		return new GSTranslationCache(Math.max(cacheVersion, cache.cacheVersion), result);
	}

	public Map<String, String> getTranslationMap() {
		return Collections.unmodifiableMap(translations);
	}
}
