package com.g4mesoft.setting;

import com.g4mesoft.util.GSDecodeBuffer;
import com.g4mesoft.util.GSEncodeBuffer;

public interface GSISettingDecoder<T extends GSSetting<?>> {

	public T decodeSetting(String name, GSDecodeBuffer buf);

	public void encodeSetting(GSEncodeBuffer buf, T setting);

	public String getTypeString();
	
	public Class<T> getSettingClass();
	
}
