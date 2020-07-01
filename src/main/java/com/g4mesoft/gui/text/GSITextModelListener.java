package com.g4mesoft.gui.text;

public interface GSITextModelListener {

	public void textInserted(GSITextModel model, int offset, int count);
	
	public void textRemoved(GSITextModel model, int offset, int count);
	
}
