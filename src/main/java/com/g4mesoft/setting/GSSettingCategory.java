package com.g4mesoft.setting;

public class GSSettingCategory {

	private final String name;
	
	public GSSettingCategory(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof GSSettingCategory))
			return false;
		return ((GSSettingCategory)other).name.equals(name);
	}
}
