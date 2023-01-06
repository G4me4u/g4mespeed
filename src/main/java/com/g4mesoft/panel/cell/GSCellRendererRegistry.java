package com.g4mesoft.panel.cell;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.text.Text;

public class GSCellRendererRegistry {

	private final Map<Class<?>, GSICellRenderer<?>> cellRendererByClass;
	/* Cache for derived cell renderers (inherited from classes and interfaces) */
	private final Map<Class<?>, GSICellRenderer<?>> cellRendererCache;
	
	public GSCellRendererRegistry() {
		cellRendererByClass = new IdentityHashMap<>();
		cellRendererCache = new IdentityHashMap<>();

		initDefaultCellRenderers();
	}
	
	private void initDefaultCellRenderers() {
		setCellRenderer(String.class, GSStringCellRenderer.INSTANCE);
		setCellRenderer(Text.class, GSTextCellRenderer.INSTANCE);
		setCellRenderer(Date.class, GSDateCellRenderer.INSTANCE);
		setCellRenderer(Instant.class, GSInstantCellRenderer.INSTANCE);
		setCellRenderer(LocalDate.class, GSLocalDateCellRenderer.INSTANCE);
		setCellRenderer(LocalTime.class, GSLocalTimeCellRenderer.INSTANCE);
		setCellRenderer(LocalDateTime.class, GSLocalDateTimeCellRenderer.INSTANCE);
		setCellRenderer(ZonedDateTime.class, GSZonedDateTimeCellRenderer.INSTANCE);
	}
	
	public <T> void setCellRenderer(Class<? extends T> valueClazz, GSICellRenderer<T> cellRenderer) {
		if (cellRenderer == null) {
			cellRendererByClass.remove(valueClazz);
		} else {
			cellRendererByClass.put(valueClazz, cellRenderer);
		}
		// Clear the cell renderer cache
		cellRendererCache.clear();
	}
	
	public <T> GSICellRenderer<T> getCellRenderer(T value) {
		return value == null ? GSEmptyCellRenderer.getInstance() : getCellRendererImpl(value.getClass());
	}
	
	@SuppressWarnings("unchecked")
	private <T> GSICellRenderer<T> getCellRendererImpl(Class<?> clazz) {
		GSICellRenderer<?> cellRenderer = cellRendererCache.get(clazz);
		if (cellRenderer == null) {
			// Search for closest cell renderer, where:
			//    1. A directly assigned cell renderer takes precedence
			//    2. Then direct interfaces, by DFS order, take precedence.
			//    3. Finally, if the direct interfaces do not derive a cell
			//       renderer, we recursively return the cell renderer of
			//       the super class.
			Deque<Class<?>> s = new ArrayDeque<>();
			// 1) The root of the DFS is clazz
			s.push(clazz);
			// Search through direct interfaces and their inherited
			// interfaces using DFS.
			// Note: no need to check for visited nodes, since inheritance
			//       is always a DAG. We *might* visit the same node many
			//       times, however this expensive operation happens once.
			Class<?> c;
			while ((c = s.poll()) != null) {
				cellRenderer = cellRendererByClass.get(c);
				if (cellRenderer != null)
					break;
				// 2) add direct interfaces to the stack
				for (Class<?> ic : c.getInterfaces())
					s.push(ic);
			}
			if (cellRenderer == null) {
				// 3) Recursively search the super-class.
				c = clazz.getSuperclass();
				if (c != null) {
					cellRenderer = getCellRendererImpl(c);
				} else {
					// clazz is primitive type or Object, and has no
					// assigned cell renderer.
					cellRenderer = GSEmptyCellRenderer.getInstance();
				}
			}
			//assert cellRenderer != null
			// Add the cell renderer to the cache
			cellRendererCache.put(clazz, cellRenderer);
		}
		return (GSICellRenderer<T>)cellRenderer;
	}
}
