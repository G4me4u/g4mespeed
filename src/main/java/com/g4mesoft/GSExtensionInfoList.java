package com.g4mesoft;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.g4mesoft.core.GSCoreExtension;
import com.g4mesoft.core.GSVersion;

public class GSExtensionInfoList {

	private static final String UNKNOWN_NAME = "Unknown";
	
	private Map<GSExtensionUID, GSExtensionInfo> extensionInfo;
	
	public GSExtensionInfoList() {
		extensionInfo = new LinkedHashMap<>();
		
		clearInfo();
	}
	
	public GSExtensionInfo getInfo(GSExtensionUID extensionUid) {
		GSExtensionInfo info = extensionInfo.get(extensionUid);
		
		if (info == null) {
			// Make a default extension info. Note that the name may
			// be invalid in the case where the version is invalid.
			return new GSExtensionInfo(UNKNOWN_NAME, extensionUid, GSVersion.INVALID);
		}
		
		return info;
	}
	
	public Collection<GSExtensionInfo> getAllInfo() {
		return Collections.unmodifiableCollection(extensionInfo.values());
	}
	
	public void addInfo(GSExtensionInfo info) {
		extensionInfo.put(info.getUniqueId(), info);
	}
	
	public boolean isExtensionInstalled(GSExtensionUID extensionUid) {
		return isExtensionInstalled(extensionUid, GSVersion.MINIMUM_VERSION);
	}

	public boolean isExtensionInstalled(GSExtensionUID extensionUid, GSVersion minimumVersion) {
		return getInfo(extensionUid).getVersion().isGreaterThanOrEqualTo(minimumVersion);
	}
	
	public void clearInfo() {
		extensionInfo.clear();
		
		addInfo(GSCoreExtension.INVALID_VERSION_INFO);
	}
	
	public void addAllInfo(GSExtensionInfo[] infoArray) {
		for (GSExtensionInfo info : infoArray)
			addInfo(info);
	}

	public void addAllExtensionInfo(List<GSExtensionInfo> infoList) {
		infoList.forEach(this::addInfo);
	}
}
