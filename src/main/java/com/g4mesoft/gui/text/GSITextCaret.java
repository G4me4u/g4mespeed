package com.g4mesoft.gui.text;

public interface GSITextCaret {

	public void install(GSTextField textField);

	public void uninstall(GSTextField textField);

	public void addTextCaretListener(GSITextCaretListener listener);

	public void removeTextCaretListener(GSITextCaretListener listener);
	
	public void update();

	public void render(int mouseX, int mouseY, float dt);

	public int getCaretLocation();

	public void setCaretLocation(int location);
	
	public int getCaretDot();

	public void setCaretDot(int dot);
	
	public int getCaretMark();

	public void setCaretMark(int mark);
	
	public boolean hasCaretSelection();
	
	public int getBlinkRate();

	public void setBlinkRate(int blinkRate);

	public boolean onMouseClicked(double mouseX, double mouseY, int button, int mods);
	
	public boolean onMouseReleased(double mouseX, double mouseY, int button, int mods);
	
	public boolean onMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY);
	
	public boolean onKeyPressed(int key, int scancode, int mods);

	public boolean onKeyReleased(int key, int scancode, int mods);
	
}
