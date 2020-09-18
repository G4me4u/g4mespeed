package com.g4mesoft.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GSElementRegistry<E> {

	private final Map<Class<? extends E>, Integer> elementToId;
	private final Map<Integer, Class<? extends E>> idToElement;
	private final Map<Integer, Supplier<? extends E>> idToSupplier;
	
	public GSElementRegistry() {
		elementToId = new HashMap<>();
		idToElement = new HashMap<>();
		idToSupplier = new HashMap<>();
	}
	
	public <T extends E> void register(int id, Class<T> elementClazz, Supplier<T> provider) {
		if (idToElement.containsKey(id))
			throw new IllegalStateException("ID is already registered: " + id);
		if (elementToId.containsKey(elementClazz))
			throw new IllegalStateException("Element class already registered: " + elementClazz);
		
		idToElement.put(id, elementClazz);
		elementToId.put(elementClazz, id);
		idToSupplier.put(id, provider);
	}
	
	public Class<? extends E> getElementFromId(int id) {
		return idToElement.get(id);
	}

	public boolean containsElement(Class<? extends E> elementClazz) {
		return elementToId.containsKey(elementClazz);
	}
	
	public int getIdFromElement(Class<? extends E> elementClazz) {
		Integer elementId = elementToId.get(elementClazz);
		return (elementId == null) ? 0 : elementId.intValue();
	}
	
	public E createNewElement(int id) {
		Supplier<? extends E> provider = idToSupplier.get(id);
		return (provider == null) ? null : provider.get();
	}
}
