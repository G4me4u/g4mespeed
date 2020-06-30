package com.g4mesoft.gui.text;

public interface GSITextCaret {

	public void install(GSTextField textField);

	public void uninstall(GSTextField textField);

	public void addTextCaretListener(GSITextCaretListener listener);

	public void removeTextCaretListener(GSITextCaretListener listener);
	
	public void update();

	public void render(int mouseX, int mouseY, float dt);

	public void setCaretLocation(int location);
	
	public void setCaretDot(int dot);
	
	public void setCaretMark(int mark);
	
	public int getCaretLocation();
	
	public int getCaretDot();

	public int getCaretMark();
	
	public boolean hasCaretSelection();
	
	public void setBlinkRate(int blinkRate);
	
	public int getBlinkRate();
	
}
