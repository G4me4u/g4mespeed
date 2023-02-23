package com.g4mesoft.panel.field;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.panel.GSColorPicker;
import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSECursorType;
import com.g4mesoft.panel.GSEPopupPlacement;
import com.g4mesoft.panel.GSIActionListener;
import com.g4mesoft.panel.GSIcon;
import com.g4mesoft.panel.GSParentPanel;
import com.g4mesoft.panel.GSPopup;
import com.g4mesoft.panel.button.GSButton;
import com.g4mesoft.panel.event.GSFocusEvent;
import com.g4mesoft.panel.event.GSIFocusEventListener;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.util.GSColorUtil;

public class GSColorPickerField extends GSParentPanel {

	private static final int BUTTON_MARGIN = 1;
	
	private final GSTextField textField;
	private final GSColorPickerFieldTextModel textModel;
	
	private final GSButton colorButton;
	private final GSColorPicker picker;
	
	private final List<GSIActionListener> listeners;
	
	private int color;

	private int backgroundColor;
	private int borderWidth;
	private int borderColor;
	
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
		
		textField.addChangeListener(this::onFieldChanged);
		colorButton.addActionListener(this::onButtonPressed);
		picker.addActionListener(this::onPickerChanged);
		
		listeners = new ArrayList<>();
		
		add(textField);
		add(colorButton);
	
		// Use text field styling as default values
		backgroundColor = textField.getBackgroundColor();
		borderWidth = textField.getBorderWidth();
		borderColor = textField.getBorderColor();
		
		// Style text field and button to no background or border.
		textField.setBorderWidth(0);
		textField.setBackgroundColor(0x00000000);
		colorButton.setBorderWidth(0);
		
		// Set color of button and field.
		setColor(initialColor);
		setEditable(true);
	}
	
	@Override
	public void layout() {
		int h = height - 2 * borderWidth;
		int w = width - 2 * borderWidth;
		
		int bs = h - 2 * BUTTON_MARGIN;
		
		colorButton.setBounds(borderWidth + BUTTON_MARGIN, borderWidth + BUTTON_MARGIN, bs, bs);
		textField.setBounds(borderWidth + bs + 2 * BUTTON_MARGIN, borderWidth, w - bs - 2 * BUTTON_MARGIN, h);
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		drawBorderAndBackground(renderer);
		
		super.render(renderer);
	}
	
	protected void drawBorderAndBackground(GSIRenderer2D renderer) {
		int bw2 = borderWidth * 2;
		
		if (borderWidth != 0) {
			// Top, Bottom, Left, Right
			renderer.fillRect(0, 0, width - borderWidth, borderWidth, borderColor);
			renderer.fillRect(borderWidth, height - borderWidth, width - borderWidth, borderWidth, borderColor);
			renderer.fillRect(0, borderWidth, borderWidth, height - borderWidth, borderColor);
			renderer.fillRect(width - borderWidth, 0, borderWidth, height - borderWidth, borderColor);
		}
		
		if (GSColorUtil.unpackA(backgroundColor) != 0x00)
			renderer.fillRect(borderWidth, borderWidth, width - bw2, height - bw2, backgroundColor);
	}
	
	@Override
	protected GSDimension calculateMinimumSize() {
		GSDimension fs = textField.getProperty(MINIMUM_SIZE);
		
		int h = fs.getHeight() + 2 * borderWidth;
		int w = fs.getWidth() + fs.getHeight() + 2 * borderWidth;
		
		return new GSDimension(w, h);
	}

	@Override
	protected GSDimension calculatePreferredSize() {
		GSDimension fs = textField.getProperty(PREFERRED_SIZE);
		GSDimension ps = picker.getProperty(PREFERRED_SIZE);
		
		int h = fs.getHeight() + 2 * borderWidth;
		int w = fs.getWidth() + fs.getHeight() + 2 * borderWidth;
		
		return new GSDimension(Math.max(w, ps.getWidth()), h);
	}
	
	private void onFieldChanged() {
		color = GSColorUtil.str2rgba(textField.getText());
		setButtonColor(color);

		dispatchActionEvent();
	}

	private void onButtonPressed() {
		if (isEditable()) {
			picker.setColor(color);
			
			GSPopup popup = new GSPopup(picker, true);
			popup.setHiddenOnFocusLost(true);
			popup.show(this, 0, height, GSEPopupPlacement.RELATIVE);
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
	
	public int getBackgroundColor() {
		return backgroundColor;
	}
	
	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	
	public int getBorderWidth() {
		return borderWidth;
	}
	
	public void setBorderWidth(int borderWidth) {
		this.borderWidth = borderWidth;
	}
	
	public int getBorderColor() {
		return borderColor;
	}
	
	public void setBorderColor(int borderColor) {
		this.borderColor = borderColor;
	}
	
	public boolean isEditable() {
		return editable;
	}
	
	public void setEditable(boolean editable) {
		this.editable = editable;
		
		textField.setEditable(editable);
	}
}
