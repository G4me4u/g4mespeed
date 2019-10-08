package com.g4mesoft.module.translation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import com.g4mesoft.access.GSINetworkHandlerAccess;
import com.g4mesoft.core.GSIModule;
import com.g4mesoft.core.GSIModuleManager;

import net.minecraft.server.network.ServerPlayerEntity;

public class GSTranslationModule implements GSIModule {

	private static final String TRANSLATION_FILENAME = "en.lang";

	public static final int INVALID_TRANSLATION_VERSION = -1;
	
	private final Map<String, String> translations;
	private final Map<Integer, GSTranslationCache> caches;
	
	private GSIModuleManager manager;
	
	private int cachedTranslationVersion;
	
	public GSTranslationModule() {
		translations = new HashMap<String, String>();
		caches = new HashMap<Integer, GSTranslationCache>();
	
		cachedTranslationVersion = INVALID_TRANSLATION_VERSION;
	}

	@Override
	public void init(GSIModuleManager manager) {
		this.manager = manager;
		
		manager.runOnClient((managerClient) -> {
			try (FileInputStream is = new FileInputStream(getCachedFile(manager))) {
				loadTranslations(is);
			} catch (FileNotFoundException | SecurityException e) {
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		URL url = GSTranslationModule.class.getResource("/assets/g4mespeed/lang/" + TRANSLATION_FILENAME);
		if (url != null) {
			try (InputStream is = url.openStream()) {
				loadTranslations(is);
			} catch (IOException e) {
			}
		}
	}
	
	@Override
	public void onClientClose() {
		manager.runOnClient((managerClient) -> {
			try {
				File file = getCachedFile(manager);
				if (!file.exists()) {
					File parentFile = file.getParentFile();
					if (parentFile != null && !parentFile.isDirectory())
						parentFile.mkdirs();
					
					file.createNewFile();
				}
				
				try (FileOutputStream os = new FileOutputStream(file)) {
					saveTranslations(os);
				}
			} catch (IOException e) {
			}
		});
	}

	@Override
	public void onJoinG4mespeedServer(int serverVersion) {
		manager.runOnClient(m -> m.sendPacket(new GSTranslationVersionPacket(cachedTranslationVersion)));
	}
	
	public void onTranslationVersionReceived(ServerPlayerEntity player, int translationVersion) {
		// Make sure the player hasn't already requested
		// a translation mapping in the current session.
		if (((GSINetworkHandlerAccess)player.networkHandler).getTranslationVersion() != INVALID_TRANSLATION_VERSION)
			return;
		
		((GSINetworkHandlerAccess)player.networkHandler).setTranslationVersion(translationVersion);
		
		if (translationVersion < cachedTranslationVersion) {
			manager.runOnServer((managerServer) -> {
				Queue<GSTranslationCache> cachesToSend = new PriorityQueue<GSTranslationCache>((e1, e2) -> {
					return Integer.compare(e1.getCacheVersion(), e2.getCacheVersion());
				});
				
				for (GSTranslationCache cache : caches.values()) {
					if (cache.getCacheVersion() > translationVersion)
						cachesToSend.add(cache);
				}
				
				for (GSTranslationCache cache : cachesToSend)
					managerServer.sendPacket(new GSTranslationCachePacket(cache), player);
			});
		}
	}
	
	private void loadTranslations(InputStream is) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			Map<String, String> translations = new HashMap<String, String>();
			
			int currentVersion = INVALID_TRANSLATION_VERSION;
			
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.isEmpty()) {
					switch (line.charAt(0)) {
					case '#':
						break;
					case ':':
						if (!translations.isEmpty()) {
							addTranslationCache(new GSTranslationCache(currentVersion, translations));
							translations.clear();
						}
						
						try {
							currentVersion = Integer.parseInt(line.substring(1));
						} catch (NumberFormatException e) {
							throw new IOException("Unable to read translation version! (" + line + ")");
						}
						break;
					default:
						if (currentVersion != INVALID_TRANSLATION_VERSION) {
							String[] entry = line.split("=");
							if (entry.length == 2)
								translations.put(entry[0], entry[1]);
						}
						break;
					}
				}
			}
			
			if (!translations.isEmpty())
				addTranslationCache(new GSTranslationCache(currentVersion, translations));
		}
	}
	
	private void saveTranslations(OutputStream os) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os))){
			writer.write("# Cache saved at ");
			writer.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			writer.newLine();
			
			writer.write("# Cache version ");
			writer.write(Integer.toString(cachedTranslationVersion));
			writer.newLine();
			
			for (GSTranslationCache cache : caches.values()) {
				writer.write(':');
				writer.write(Integer.toString(cache.getCacheVersion()));
				writer.newLine();
				
				for (Map.Entry<String, String> translation : cache.getTranslationMap().entrySet()) {
					writer.write(translation.getKey());
					writer.write('=');
					writer.write(translation.getValue());
					writer.newLine();
				}
			}
		}
	}

	private File getCachedFile(GSIModuleManager manager) {
		return new File(manager.getCacheFile(), TRANSLATION_FILENAME);
	}
	
	public void addTranslationCache(GSTranslationCache cache) {
		if (cache.getCacheVersion() > cachedTranslationVersion)
			cachedTranslationVersion = cache.getCacheVersion();
		                                    
		cache.getAllTranslations(translations);
		
		GSTranslationCache currentCache = caches.get(cache.getCacheVersion());
		if (currentCache != null)
			cache = currentCache.merge(cache);
		
		caches.put(cache.getCacheVersion(), cache);
	}
	
	public String getTranslation(String key) {
		String value = translations.get(key);
		return (value == null) ? key : value;
	}

	public String getFormattedTranslation(String key, Object... args) {
		String value = translations.get(key);
		return (value == null) ? key : String.format(value, args);
	}

	public boolean hasTranslation(String key) {
		return translations.containsKey(key);
	}
}
