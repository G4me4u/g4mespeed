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
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.util.GSColorUtil;

public class GSColorPickerField extends GSParentPanel {

	private static final int BUTTON_MARGIN = 1;
	
	private static final int DEFAULT_BACKGROUND_COLOR = 0xFF202020;
	private static final int DEFAULT_BORDER_WIDTH     = 1;
	private static final int DEFAULT_BORDER_COLOR     = 0xFF171717;
	
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
		textField.setBorderWidth(0);
		textField.setBackgroundColor(0x00000000);
		
		textModel = new GSColorPickerFieldTextModel(false);
		textField.setTextModel(textModel);
		
		colorButton = new GSButton((GSIcon)null);
		colorButton.setBorderWidth(0);
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
		
		textField.addActionListener(this::onFieldChanged);
		colorButton.addActionListener(this::onButtonPressed);
		picker.addActionListener(this::onPickerChanged);
		
		listeners = new ArrayList<>();
		
		add(textField);
		add(colorButton);
	
		backgroundColor = DEFAULT_BACKGROUND_COLOR;
		borderWidth = DEFAULT_BORDER_WIDTH;
		borderColor = DEFAULT_BORDER_COLOR;
		
		// Set color of button and field.
		setColor(initialColor);
		setEditable(true);
	}
	
	@Override
	protected void onBoundsChanged() {
		super.onBoundsChanged();

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
		
		if (((backgroundColor >>> 24) & 0xFF) != 0x00)
			renderer.fillRect(borderWidth, borderWidth, width - bw2, height - bw2, backgroundColor);
	}
	
	@Override
	protected GSDimension calculateMinimumSize() {
		GSDimension fs = textField.getMinimumSize();
		
		int h = fs.getHeight() + 2 * borderWidth;
		int w = fs.getWidth() + fs.getHeight() + 2 * borderWidth;
		
		return new GSDimension(w, h);
	}

	@Override
	protected GSDimension calculatePreferredSize() {
		GSDimension fs = textField.getPreferredSize();
		GSDimension ps = picker.getPreferredSize();
		
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
			
			GSPopup popup = new GSPopup(picker);
			popup.show(this, 0, height, true);
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
