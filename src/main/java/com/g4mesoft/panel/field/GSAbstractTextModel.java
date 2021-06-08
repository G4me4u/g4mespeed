package com.g4mesoft.panel.field;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple abstract text model that allows new text models to have simple
 * registration of model listeners without having to worry about handling them.
 * The registration is handled using an array-list with all the registered 
 * listeners. To dispatch events to the registered listeners, the sub-class of
 * this text model should invoke {@link #dispatchTextInsertedEvent(int, int)}
 * and {@link #dispatchTextRemovedEvent(int, int)} with the desired parameters.
 * <br><br>
 * <b>Note: </b> the {@link #appendText(String)} method has also been overridden
 * by the following code snippet:
 * <pre>
 * public void appendText(String text) {
 *     insertText(getLength(), text);
 * }
 * </pre>
 * If the above code for appendText is not desired, then it can be overriden by
 * any sub-class of this abstract model.
 * 
 * @author Christian
 */
public abstract class GSAbstractTextModel implements GSITextModel {

	private final List<GSITextModelListener> modelListeners;
	
	public GSAbstractTextModel() {
		modelListeners = new ArrayList<>();
	}
	
	@Override
	public void addTextModelListener(GSITextModelListener textModelListener) {
		if (modelListeners.contains(textModelListener))
			return;
		modelListeners.add(textModelListener);
	}

	@Override
	public void removeTextModelListener(GSITextModelListener textModelListener) {
		modelListeners.remove(textModelListener);
	}
	
	@Override
	public void appendText(String text) {
		insertText(getLength(), text);
	}

	/**
	 * Dispatches a text inserted event to all the registered model-listeners in
	 * this model.
	 * 
	 * @param offset - the offset at which the text was inserted
	 * @param count - the amount of characters that were inserted at the given
	 *                offset.
	 */
	protected void dispatchTextInsertedEvent(int offset, int count) {
		for (GSITextModelListener listener : modelListeners)
			listener.textInserted(this, offset, count);
	}

	/**
	 * Dispatches a text removed event to all the registered model-listeners in
	 * this model.
	 * 
	 * @param offset - the offset at which the text was removed
	 * @param count - the amount of characters that have been removed at the
	 *                given offset.
	 */
	protected void dispatchTextRemovedEvent(int offset, int count) {
		for (GSITextModelListener listener : modelListeners)
			listener.textRemoved(this, offset, count);
	}
}
