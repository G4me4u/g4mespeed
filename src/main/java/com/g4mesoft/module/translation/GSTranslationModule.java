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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.g4mesoft.G4mespeedMod;
import com.g4mesoft.GSExtensionUID;
import com.g4mesoft.GSIExtension;
import com.g4mesoft.GSIExtensionListener;
import com.g4mesoft.access.GSINetworkHandlerAccess;
import com.g4mesoft.core.GSIModule;
import com.g4mesoft.core.GSIModuleManager;
import com.g4mesoft.core.GSVersion;
import com.g4mesoft.packet.GSIPacket;

import net.minecraft.server.network.ServerPlayerEntity;

public class GSTranslationModule implements GSIModule, GSIExtensionListener {

	public static final GSVersion TRANSLATION_INTRODUCTION_VERSION = new GSVersion(1, 0, 0);
	public static final GSVersion TRANSLATION_EXTENSION_VERSION = new GSVersion(1, 1, 0);
	
	private static final String CACHED_TRANSLATION_FILENAME = "en.lang";
	public static final int INVALID_TRANSLATION_VERSION = -1;
	
	private static final long MAX_CACHE_LIFE_HOURS = 3 * 24;
	
	private final Map<String, String> translations;
	private final Map<GSExtensionUID, GSTranslationCacheList> cacheLists;
	
	private GSIModuleManager manager;
	
	private long cacheSaveTime;
	private long translationsChangeTimestamp;
	
	public GSTranslationModule() {
		translations = new ConcurrentHashMap<String, String>();
		cacheLists = new HashMap<GSExtensionUID, GSTranslationCacheList>();
	
		translationsChangeTimestamp = -1L;
	}

	@Override
	public void init(GSIModuleManager manager) {
		this.manager = manager;
		
		manager.runOnClient((managerClient) -> {
			try (FileInputStream is = new FileInputStream(getCachedFile(manager))) {
				loadCachedTranslations(is);
			} catch (FileNotFoundException | SecurityException e) {
			} catch (IOException e) {
				// Silently handle exceptions.
				// e.printStackTrace();
			}
		});

		G4mespeedMod.getExtensions().forEach(this::addExtensionTranslations);
		G4mespeedMod.addExtensionListener(this);
	}
	
	@Override
	public void onClose() {
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
		
		G4mespeedMod.removeExtensionListener(this);
	}
	
	@Override
	public void extensionAdded(GSIExtension extension) {
		addExtensionTranslations(extension);
	}
	
	private void addExtensionTranslations(GSIExtension extension) {
		URL url = GSTranslationModule.class.getResource(extension.getTranslationPath());
		if (url != null) {
			try (InputStream is = url.openStream()) {
				loadTranslations(is, extension.getUniqueId(), false);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onJoinG4mespeedServer(GSVersion serverVersion) {
		if (serverVersion.isGreaterThanOrEqualTo(TRANSLATION_INTRODUCTION_VERSION)) {
			if (serverVersion.isGreaterThanOrEqualTo(TRANSLATION_EXTENSION_VERSION)) {
				GSIPacket packet = new GSTranslationVersionsPacket(cacheLists);
				manager.runOnClient(m -> m.sendPacket(packet, TRANSLATION_EXTENSION_VERSION));
			} else {
				GSTranslationCacheList cacheList = cacheLists.get(G4mespeedMod.CORE_EXTENSION_UID);
				if (cacheList != null) {
					@SuppressWarnings("deprecation")
					GSIPacket packet = new GSOutdatedTranslationVersionPacket(cacheList.getVersion());
					manager.runOnClient(m -> m.sendPacket(packet, TRANSLATION_INTRODUCTION_VERSION));
				}
			}
		}
	}
	
	void onOutdatedTranslationVersionReceived(ServerPlayerEntity player, int translationVersion) {
		sendMissingTranslations(player, G4mespeedMod.CORE_EXTENSION_UID, translationVersion);
	}
	
	void onTranslationVersionsReceived(ServerPlayerEntity player, Map<GSExtensionUID, Integer> uidToVersion) {
		for (GSExtensionUID uid : cacheLists.keySet())
			sendMissingTranslations(player, uid, uidToVersion.getOrDefault(uid, INVALID_TRANSLATION_VERSION));
	}
	
	private void sendMissingTranslations(ServerPlayerEntity player, GSExtensionUID uid, int translationVersion) {
		// Make sure the player hasn't already requested
			// a translation mapping in the current session.
			if (((GSINetworkHandlerAccess)player.networkHandler).getTranslationVersion(uid) != INVALID_TRANSLATION_VERSION)
				return;
			
			GSTranslationCacheList cacheList = cacheLists.get(uid);
			if (cacheList != null && translationVersion < cacheList.getVersion()) {
				manager.runOnServer((managerServer) -> {
					Queue<GSTranslationCache> cachesToSend = new PriorityQueue<GSTranslationCache>((e1, e2) -> {
						return Integer.compare(e1.getCacheVersion(), e2.getCacheVersion());
					});
					
					for (GSTranslationCache cache : cacheList.getCaches().values()) {
						if (cache.getCacheVersion() > translationVersion)
							cachesToSend.add(cache);
					}
					
					for (GSTranslationCache cache : cachesToSend)
						managerServer.sendPacket(new GSTranslationCachePacket(uid, cache), player);
				});

				((GSINetworkHandlerAccess)player.networkHandler).setTranslationVersion(uid, cacheList.getVersion());
			}
	}
	
	private void loadCachedTranslations(InputStream is) throws IOException {
		cacheSaveTime = System.currentTimeMillis();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			String line = reader.readLine();
			// Skip '# ' characters at the beginning
			long cacheTime = -1;
			if (line != null && line.length() > 2) {
				try {
					cacheTime = Long.parseLong(line.substring(2));
				} catch (NumberFormatException e) {
				}
			}
			
			if (cacheTime < 0)
				throw new IOException("Invalid cache date: " + line);

			long cacheLifeDuration = System.currentTimeMillis() - cacheTime;
			if (cacheLifeDuration < 0L)
				throw new IOException("Cache lifetime duration is negative! System time changed?");
			
			if (TimeUnit.HOURS.convert(cacheLifeDuration, TimeUnit.MILLISECONDS) > MAX_CACHE_LIFE_HOURS)
				throw new IOException("Cache is too old. Discard it.");
			
			loadTranslations(reader, G4mespeedMod.CORE_EXTENSION_UID, true);
			cacheSaveTime = cacheTime;
		}
	}
	
	private void loadTranslations(InputStream is, GSExtensionUID extensionUid, boolean cachedTranslations) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			loadTranslations(reader, extensionUid, cachedTranslations);
		}
	}

	private void loadTranslations(BufferedReader reader, GSExtensionUID extensionUid, boolean cachedTranslations) throws IOException {
		Map<String, String> translations = new HashMap<String, String>();

		int currentVersion = INVALID_TRANSLATION_VERSION;
		
		String line;
		while ((line = reader.readLine()) != null) {
			if (!line.isEmpty()) {
				switch (line.charAt(0)) {
				case '#':
					break;
				case '/':
					if (cachedTranslations) {
						extensionUid = GSExtensionUID.parseUID(line.substring(1));
						
						if (extensionUid == null)
							throw new IOException("Unable to read extension uid! (" + line + ")");
					}
				case ':':
					if (!translations.isEmpty()) {
						addTranslationCache(extensionUid, new GSTranslationCache(currentVersion, translations));
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
			addTranslationCache(extensionUid, new GSTranslationCache(currentVersion, translations));
	}
	
	private void saveTranslations(OutputStream os) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os))){
			writer.write("# ");
			writer.write(Long.toString(cacheSaveTime));
			writer.newLine();

			writer.write("# Cache saved at ");
			writer.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			writer.newLine();
			
			for (Map.Entry<GSExtensionUID, GSTranslationCacheList> entry : cacheLists.entrySet()) {
				writer.write('/');
				writer.write(GSExtensionUID.toString(entry.getKey()));
				writer.newLine();
				
				for (GSTranslationCache cache : entry.getValue().getCaches().values()) {
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
	}

	private File getCachedFile(GSIModuleManager manager) {
		return new File(manager.getCacheFile(), CACHED_TRANSLATION_FILENAME);
	}
	
	void addTranslationCache(GSExtensionUID uid, GSTranslationCache cache) {
		cache.getAllTranslations(translations);
		translationsChangeTimestamp = System.currentTimeMillis();

		GSTranslationCacheList cacheList = cacheLists.get(uid);
		if (cacheList == null) {
			cacheList = new GSTranslationCacheList();
			cacheLists.put(uid, cacheList);
		}
		
		cacheList.addTranslationCache(cache);
	}
	
	public long getTranslationTimestamp() {
		return translationsChangeTimestamp;
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
