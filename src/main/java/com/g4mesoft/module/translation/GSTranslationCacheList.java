package com.g4mesoft.module.translation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GSTranslationCacheList {

	private final Map<Integer, GSTranslationCache> caches;
	private int cachedTranslationVersion;
	
	public GSTranslationCacheList() {
		this.caches = new HashMap<Integer, GSTranslationCache>();
		cachedTranslationVersion = GSTranslationModule.INVALID_TRANSLATION_VERSION;
	}
	
	public void addTranslationCache(GSTranslationCache cache) {
		if (cache.getCacheVersion() > cachedTranslationVersion)
			cachedTranslationVersion = cache.getCacheVersion();
		
		GSTranslationCache currentCache = caches.get(cache.getCacheVersion());
		if (currentCache != null)
			cache = currentCache.merge(cache);
		
		caches.put(cache.getCacheVersion(), cache);
	}
	
	public int getVersion() {
		return cachedTranslationVersion;
	}
	
	public Map<Integer, GSTranslationCache> getCaches() {
		return Collections.unmodifiableMap(caches);
	}
}
