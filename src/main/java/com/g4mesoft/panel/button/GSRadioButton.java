package com.g4mesoft.panel.button;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.panel.GSECursorType;
import com.g4mesoft.panel.GSIActionListener;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.event.GSIMouseListener;
import com.g4mesoft.panel.event.GSMouseEvent;
import com.g4mesoft.renderer.GSIRenderer2D;

public class GSRadioButton extends GSPanel implements GSIMouseListener {

	private static final int BORDER_COLOR         = 0xFF9E9E9E;
	private static final int FILL_COLOR           = BORDER_COLOR;
	private static final int HOVERED_BORDER_COLOR = 0xFFDDDDDD;
	private static final int HOVERED_FILL_COLOR   = HOVERED_BORDER_COLOR;
	
	private final List<GSIActionListener> actionListeners;

	private boolean selected;
	
	public GSRadioButton() {
		actionListeners = new ArrayList<>();
		
		selected = false;
		
		addMouseEventListener(this);
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		super.render(renderer);

		int size = Math.min(width, height);
		int x = (width  - size) / 2;
		int y = (height - size) / 2;
		
		boolean hovered = renderer.isMouseInside(0, 0, width, height);
		renderer.drawRect(x, y, size, size, hovered ? HOVERED_BORDER_COLOR : BORDER_COLOR);
		if (isSelected() && size > 4)
			renderer.fillRect(x + 2, y + 2, size - 4, size - 4, hovered ? HOVERED_FILL_COLOR : FILL_COLOR);
	}
	
	@Override
	public void mouseReleased(GSMouseEvent event) {
		if (!selected && event.getButton() == GSMouseEvent.BUTTON_LEFT) {
			int x = event.getX();
			int y = event.getY();

			// Ensure the mouse pointer is in bounds
			if (x >= 0 && y >= 0 && x < width && y < height) {
				setSelected(true);
				dispatchActionPerformed();
				event.consume();
			}
		}
	}
	
	@Override
	public GSECursorType getCursor() {
		return GSECursorType.HAND;
	}
	
	public void addActionListener(GSIActionListener actionListener) {
		actionListeners.add(actionListener);
	}

	public void removeActionListener(GSIActionListener actionListener) {
		actionListeners.remove(actionListener);
	}
	
	private void dispatchActionPerformed() {
		actionListeners.forEach(GSIActionListener::actionPerformed);
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
