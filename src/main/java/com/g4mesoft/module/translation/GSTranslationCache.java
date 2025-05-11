package com.g4mesoft.module.translation;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.g4mesoft.util.GSDecodeBuffer;
import com.g4mesoft.util.GSEncodeBuffer;

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
	
	public static GSTranslationCache read(GSDecodeBuffer buf) throws IOException {
		int cacheVersion = buf.readInt();
		int n = buf.readInt();
		
		Map<String, String> translations = new HashMap<String, String>(n);
		while (n-- > 0) {
			String key = buf.readString();
			String value = buf.readString();
			translations.put(key, value);
		}
		
		return new GSTranslationCache(cacheVersion, translations);
	}
	
	public static void write(GSEncodeBuffer buf, GSTranslationCache cache) throws IOException {
		buf.writeInt(cache.getCacheVersion());
		
		Map<String, String> translations = cache.getTranslationMap();
		buf.writeInt(translations.size());
		for (Map.Entry<String, String> entry : translations.entrySet()) {
			buf.writeString(entry.getKey());
			buf.writeString(entry.getValue());
		}
	}
}
