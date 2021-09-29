package com.g4mesoft.panel.field;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.panel.GSColorPicker;
import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSECursorType;
import com.g4mesoft.panel.GSIActionListener;
import com.g4mesoft.panel.GSIcon;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSParentPanel;
import com.g4mesoft.panel.GSPopup;
import com.g4mesoft.panel.button.GSButton;
import com.g4mesoft.panel.event.GSFocusEvent;
import com.g4mesoft.panel.event.GSIFocusEventListener;
import com.g4mesoft.util.GSColorUtil;

public class GSColorPickerField extends GSParentPanel {

	private static final int BUTTON_MARGIN = 1;
	
	private final GSTextField textField;
	private final GSColorPickerFieldTextModel textModel;
	
	private final GSButton colorButton;
	private final GSColorPicker picker;
	
	private final List<GSIActionListener> listeners;
	
	private int color;

	private boolean editable;
	
	public GSColorPickerField(int initialColor) {
		textField = new GSTextField();
		textModel = new GSColorPickerFieldTextModel(false);
		textField.setTextModel(textModel);
		
		colorButton = new GSButton((GSIcon)null);
		colorButton.setCursor(GSECursorType.HAND);
		
		picker = new GSColorPicker(0x00000000);
		
		textField.addFocusEventListener(new GSIFocusEventListener() {
			@Override
			public void focusLost(GSFocusEvent event) {
				if (!textModel.isValidRGBColor())
					setFieldColor(color);
			}
		});
		
		picker.addFocusEventListener(new GSIFocusEventListener() {
			@Override
			public void focusLost(GSFocusEvent event) {
				GSPanel parent = picker.getParent();
				if (parent instanceof GSPopup)
					((GSPopup)parent).hide();
			}
		});
		
		textField.addChangeListener(this::onFieldChanged);
		colorButton.addActionListener(this::onButtonPressed);
		picker.addActionListener(this::onPickerChanged);
		
		listeners = new ArrayList<>();
		
		add(textField);
		add(colorButton);
	
		// Use text field styling as default values
		setBackgroundColor(textField.getBackgroundColor());
		setDisabledBackgroundColor(textField.getDisabledBackgroundColor());
		setBorder(textField.getBorder());
		setDisabledBorder(textField.getDisabledBorder());
		
		// Style text field and button to no background or border.
		textField.setBackgroundColor(UNSPECIFIED_COLOR);
		textField.setDisabledBackgroundColor(UNSPECIFIED_COLOR);
		textField.setBorder(null);
		textField.setDisabledBorder(null);
		
		// Set color of button and field.
		setColor(initialColor);
		setEditable(true);
	}
	
	@Override
	public void layout() {
		int bs = innerHeight - 2 * BUTTON_MARGIN;
		colorButton.setOuterBounds(BUTTON_MARGIN, BUTTON_MARGIN, bs, bs);
		textField.setOuterBounds(bs + 2 * BUTTON_MARGIN, 0, innerWidth - bs - 2 * BUTTON_MARGIN, innerHeight);
	}
	
	@Override
	protected GSDimension calculateMinimumInnerSize() {
		GSDimension fs = textField.getProperty(MINIMUM_SIZE);
		
		int h = fs.getHeight();
		int w = fs.getWidth() + h;
		
		return new GSDimension(w, h);
	}

	@Override
	protected GSDimension calculatePreferredInnerSize() {
		GSDimension fs = textField.getProperty(PREFERRED_SIZE);
		GSDimension ps = picker.getProperty(PREFERRED_SIZE);
		
		int h = fs.getHeight();
		int w = Math.max(fs.getWidth() + h, ps.getWidth());
		
		return new GSDimension(w, h);
	}
	
	private void onFieldChanged() {
		color = GSColorUtil.str2rgba(textField.getText());
		setButtonColor(color);

		dispatchActionEvent();
	}

	private void onButtonPressed() {
		if (isEditable()) {
			picker.setColor(color);
			
			GSPopup popup = new GSPopup(picker);
			popup.show(this, 0, innerHeight, true);
		}
	}
	
	private void onPickerChanged() {
		setColor(picker.getColor());
		
		dispatchActionEvent();
	}

	public void addActionListener(GSIActionListener listener) {
		listeners.add(listener);
	}

	public void removeActionListener(GSIActionListener listener) {
		listeners.remove(listener);
	}
	
	private void dispatchActionEvent() {
		listeners.forEach(GSIActionListener::actionPerformed);
	}
	
	public int getColor() {
		return color;
	}
	
	public void setColor(int color) {
		this.color = color;
		
		setFieldColor(color);
		setButtonColor(color);
	}
	
	private void setFieldColor(int color) {
		String str = GSColorUtil.rgb2str(color);
		if (!str.equals(textField.getText()))
			textField.setText(str);
	}
	
	private void setButtonColor(int color) {
		colorButton.setBackgroundColor(color);
		colorButton.setHoveredBackgroundColor(color);
		colorButton.setDisabledBackgroundColor(color);
	}
	
	public boolean isEditable() {
		return editable;
	}
	
	public void setEditable(boolean editable) {
		this.editable = editable;
		
		textField.setEditable(editable);
	}
}
