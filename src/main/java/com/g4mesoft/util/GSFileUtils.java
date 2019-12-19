package com.g4mesoft.util;

import java.io.File;
import java.io.IOException;

public class GSFileUtils {

	public static void ensureDirectoryExists(File dir) throws IOException {
		if (dir != null && !dir.exists())
			dir.mkdirs();
	}
	
	public static void ensureFileExists(File file) throws IOException {
		if (file != null && !file.isFile()) {
			ensureDirectoryExists(file.getParentFile());
			file.createNewFile();
		}
	}
}
