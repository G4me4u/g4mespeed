package com.g4mesoft.panel.text;

import com.g4mesoft.renderer.GSIRenderer2D;

public interface GSITextCaret {

	public void install(GSTextField textField);

	public void uninstall(GSTextField textField);

	public void addTextCaretListener(GSITextCaretListener listener);

	public void removeTextCaretListener(GSITextCaretListener listener);
	
	public void update();

	public void render(GSIRenderer2D renderer);

	public int getCaretLocation();

	public void setCaretLocation(int location);
	
	public int getCaretDot();

	public void setCaretDot(int dot);
	
	public int getCaretMark();

	public void setCaretMark(int mark);
	
	public boolean hasCaretSelection();
	
	public int getBlinkRate();

	public void setBlinkRate(int blinkRate);
	
}
