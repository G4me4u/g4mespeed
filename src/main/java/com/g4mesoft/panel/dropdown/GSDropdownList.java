package com.g4mesoft.panel.dropdown;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSIModelListener;
import com.g4mesoft.panel.GSIcon;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSPanelContext;
import com.g4mesoft.panel.GSParentPanel;
import com.g4mesoft.panel.button.GSButton;

public class GSDropdownList<T> extends GSParentPanel {

	private static final GSIcon DOWN_ARROW_ICON = GSPanelContext.getIcon(30, 62, 10, 10);
	private static final GSIcon HOVERED_DOWN_ARROW_ICON = GSPanelContext.getIcon(40, 62, 10, 10);
	private static final GSIcon DISABLED_DOWN_ARROW_ICON = GSPanelContext.getIcon(50, 62, 10, 10);

	private static final int MINIMUM_FIELD_WIDTH = 50;
	
	private GSIDropdownListModel<T> model;
	private final List<GSIModelListener> modelListeners;
	
	private GSIDropdownListField<T> field;
	private GSPanel fieldPanel;
	private final GSButton button;
	
	public GSDropdownList(T[] items) {
		this(new GSBasicDropdownListModel<T>(items));
	}

	public GSDropdownList(GSIDropdownListModel<T> model) {
		this.model = model;
		
		modelListeners = new ArrayList<>();
	
		button = new GSButton(DOWN_ARROW_ICON);
		button.setHoveredIcon(HOVERED_DOWN_ARROW_ICON);
		button.setDisabledIcon(DISABLED_DOWN_ARROW_ICON);
		
		add(button);

		setField(new GSBasicDropdownListField<>());
	}
	
	@Override
	public void layout() {
		int buttonWidth = Math.min(width, button.getProperty(PREFERRED_WIDTH));
		button.setBounds(width - buttonWidth, 0, buttonWidth, height);
		fieldPanel.setBounds(0, 0, width - buttonWidth, height);
	}
	
	@Override
	protected GSDimension calculatePreferredInnerSize() {
		GSDimension buttonSize = button.getProperty(PREFERRED_SIZE);
		int w = buttonSize.getWidth();
		int h = buttonSize.getHeight();
		
		if (fieldPanel != null) {
			GSDimension fieldSize = fieldPanel.getProperty(PREFERRED_SIZE);
			w += Math.max(MINIMUM_FIELD_WIDTH, fieldSize.getWidth());
			if (fieldSize.getHeight() > h)
				h = fieldSize.getHeight();
		} else {
			w += MINIMUM_FIELD_WIDTH;
		}

		return new GSDimension(w, h);
	}
	
	public void addModelListener(GSIModelListener listener) {
		modelListeners.add(listener);
	}

	public void removeModelListener(GSIModelListener listener) {
		modelListeners.remove(listener);
	}

	private void invokeModelChangedEvent() {
		modelListeners.forEach(GSIModelListener::modelChanged);
	}
	
	public GSIDropdownListModel<T> getModel() {
		return model;
	}
	
	public void setModel(GSIDropdownListModel<T> model) {
		if (model == null)
			throw new IllegalArgumentException("Model can not be null!");
		
		//this.model.removeListener(this);
		this.model = model;
		//model.addListener(this);
		
		invokeModelChangedEvent();
	}
	
	public GSIDropdownListField<T> getField() {
		return field;
	}

	public void setField(GSIDropdownListField<T> field) {
		if (field == null)
			throw new IllegalArgumentException("field is null");
		
		if (fieldPanel != null)
			remove(fieldPanel);
		
		this.field = field;

		field.setHighlightedItem(getSelectedItem());

		// Add field panel (fail if null)
		fieldPanel = field.getFieldPanel();
		if (fieldPanel != null)
			add(fieldPanel);
	
		invalidate();
	}
	
	private T getSelectedItem() {
		return null;
	}
}
