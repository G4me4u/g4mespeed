package com.g4mesoft.panel;

import java.util.Map;

public interface GSILayoutProperty<T> {

	public T get(Map<GSILayoutProperty<?>, Object> map, GSPanel panel);

	public T remove(Map<GSILayoutProperty<?>, Object> map, GSPanel panel);
	
	public void put(Map<GSILayoutProperty<?>, Object> map, T value, GSPanel panel);
	
	public T computeDefaultValue(GSPanel panel);
	
	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);
	
}
