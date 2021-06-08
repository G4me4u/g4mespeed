package com.g4mesoft.panel.field;

public interface GSITextModelListener {

	public void textInserted(GSITextModel model, int offset, int count);
	
	public void textRemoved(GSITextModel model, int offset, int count);
	
}
