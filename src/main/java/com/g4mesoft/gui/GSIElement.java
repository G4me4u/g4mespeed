package com.g4mesoft.gui;

import java.util.List;

import com.g4mesoft.gui.event.GSIFocusEventListener;
import com.g4mesoft.gui.event.GSIKeyListener;
import com.g4mesoft.gui.event.GSIMouseListener;
import com.g4mesoft.gui.event.GSKeyEvent;
import com.g4mesoft.gui.event.GSMouseEvent;
import com.g4mesoft.gui.renderer.GSIRenderer2D;

public interface GSIElement extends GSIViewport {

	public void setBounds(int x, int y, int width, int height);
	
	public void onAdded(GSIElement parent);

	public void onRemoved(GSIElement parent);

	public void update();
	
	public void preRender(GSIRenderer2D renderer);
	
	public void render(GSIRenderer2D renderer);
	
	public void postRender(GSIRenderer2D renderer);
	
	public boolean isAdded();
	
	public GSIElement getParent();

	public GSIElement getChildAt(int x, int y);
	
	public boolean isInBounds(int x, int y);
	
	public int getX();
	
	public int getY();
	
	@Override
	public int getWidth();

	@Override
	public int getHeight();
	
	public void addMouseEventListener(GSIMouseListener eventListener);

	public void removeMouseEventListener(GSIMouseListener eventListener);

	public List<GSIMouseListener> getMouseEventListeners();
	
	public void addKeyEventListener(GSIKeyListener eventListener);
	
	public void removeKeyEventListener(GSIKeyListener eventListener);

	public List<GSIKeyListener> getKeyEventListeners();

	public void addFocusEventListener(GSIFocusEventListener eventListener);
	
	public void removeFocusEventListener(GSIFocusEventListener eventListener);

	public List<GSIFocusEventListener> getFocusEventListeners();

	public int getEventOffsetX();
	
	public int getEventOffsetY();

	public boolean isPassingEvents();
	
	public void setPassingEvents(boolean passingEvents);
	
	public void dispatchMouseEvent(GSMouseEvent event, GSIElement source);

	public void dispatchKeyEvent(GSKeyEvent event, GSIElement source);
	
	public boolean isFocused();

	public void setFocused(boolean focused);
	
	public void requestFocus();

	public void unfocus();
	
	public boolean isEditingText();
	
	public GSECursorType getCursor();

	public void setCursor(GSECursorType cursor);

}
