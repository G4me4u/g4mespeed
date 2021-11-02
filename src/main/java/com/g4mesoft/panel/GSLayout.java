package com.g4mesoft.panel;

import java.util.HashMap;

public class GSLayout {

	private final GSPanel panel;
	
	private final HashMap<GSILayoutProperty<?>, Object> properties;

	public GSLayout(GSPanel panel) {
		this.panel = panel;

		properties = new HashMap<>();
	}
	
	public <T> T get(GSILayoutProperty<T> property) {
		if (property == null)
			throw new IllegalArgumentException("property is null");
		return property.get(properties, panel);
	}
	
	public <T> T remove(GSILayoutProperty<T> property) {
		if (property == null)
			throw new IllegalArgumentException("property is null");
		return property.remove(properties, panel);
	}
	
	public <T> GSLayout set(GSILayoutProperty<T> property, T value) {
		if (property == null)
			throw new IllegalArgumentException("property is null");
		if (value == null)
			throw new IllegalArgumentException("value is null");
		
		property.put(properties, value, panel);
		
		GSPanel parent = panel.getParent();
		if (parent != null)
			parent.invalidate();
		
		return this;
	}
	
	public GSPanel getPanel() {
		return panel;
	}
}
