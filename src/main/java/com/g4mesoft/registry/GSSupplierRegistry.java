package com.g4mesoft.registry;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GSSupplierRegistry<K, V> {

	private final Map<K, Supplier<? extends V>> idToSupplier;
	private final Map<Class<? extends V>, K> elementToId;
	
	public GSSupplierRegistry() {
		idToSupplier = new HashMap<>();
		elementToId = new IdentityHashMap<>();
	}
	
	public <T extends V> void register(K id, Class<T> elementClazz, Supplier<T> supplier) {
		if (idToSupplier.containsKey(id))
			throw new GSDuplicateRegisterException("Identifier is already registered: " + id);
		if (elementToId.containsKey(elementClazz))
			throw new GSDuplicateRegisterException("Element class is already registered: " + elementClazz);
		
		idToSupplier.put(id, supplier);
		elementToId.put(elementClazz, id);
	}

	public boolean containsIdentifier(K id) {
		return idToSupplier.containsKey(id);
	}

	public <E extends V> boolean containsElement(E element) {
		@SuppressWarnings("unchecked")
		Class<E> elementClazz = (Class<E>)element.getClass();
		return containsElement(elementClazz);
	}

	public <E extends V> boolean containsElement(Class<E> elementClazz) {
		return elementToId.containsKey(elementClazz);
	}
	
	public <E extends V> K getIdentifier(E element) {
		@SuppressWarnings("unchecked")
		Class<E> elementClazz = (Class<E>)element.getClass();
		return getIdentifier(elementClazz);
	}

	public <E extends V> K getIdentifier(Class<E> elementClazz) {
		return elementToId.get(elementClazz);
	}

	public Supplier<? extends V> getSupplier(K id) {
		return idToSupplier.get(id);
	}
	
	public V createNewElement(K id) {
		Supplier<? extends V> provider = getSupplier(id);
		return provider == null ? null : provider.get();
	}
	
	public int getSize() {
		return idToSupplier.size();
	}
}
