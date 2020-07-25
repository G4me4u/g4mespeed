package com.g4mesoft;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.g4mesoft.core.GSCoreExtension;
import com.g4mesoft.core.GSVersion;

public class GSExtensionInfoList {

	private static final String UNKNOWN_NAME = "Unknown";
	
	private Map<GSExtensionUID, GSExtensionInfo> extensionInfo;
	
	public GSExtensionInfoList() {
		extensionInfo = new HashMap<GSExtensionUID, GSExtensionInfo>();
		
		clearExtensionInfo();
	}
	
	public GSExtensionInfo getExtensionInfo(GSExtensionUID extensionUid) {
		GSExtensionInfo info = extensionInfo.get(extensionUid);
		if (info != null)
			return info;
		return new GSExtensionInfo(UNKNOWN_NAME, extensionUid, GSVersion.INVALID);
	}
	
	public boolean isExtensionInstalled(GSExtensionUID extensionUid) {
		return isExtensionInstalled(extensionUid, GSVersion.MINIMUM_VERSION);
	}

	public boolean isExtensionInstalled(GSExtensionUID extensionUid, GSVersion minimumVersion) {
		return getExtensionInfo(extensionUid).getVersion().isGreaterThanOrEqualTo(minimumVersion);
	}
	
	public void clearExtensionInfo() {
		extensionInfo.clear();
		
		addExtensionInfo(GSCoreExtension.INVALID_VERSION_INFO);
	}
	
	public void addAllExtensionInfo(GSExtensionInfo[] infoArray) {
		for (GSExtensionInfo info : infoArray)
			addExtensionInfo(info);
	}

	public void addAllExtensionInfo(List<GSExtensionInfo> infoList) {
		infoList.forEach(this::addExtensionInfo);
	}

	public void addExtensionInfo(GSExtensionInfo info) {
		extensionInfo.put(info.getUniqueId(), info);
	}
	
	public Collection<GSExtensionInfo> getAllExtensionInfo() {
		return Collections.unmodifiableCollection(extensionInfo.values());
	}
}
