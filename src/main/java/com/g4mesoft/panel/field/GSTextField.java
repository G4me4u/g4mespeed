package com.g4mesoft.panel.field;

import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.panel.GSDimension;
import com.g4mesoft.panel.GSECursorType;
import com.g4mesoft.panel.GSETextAlignment;
import com.g4mesoft.panel.GSIActionListener;
import com.g4mesoft.panel.GSIChangeListener;
import com.g4mesoft.panel.GSIModelListener;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSPanelContext;
import com.g4mesoft.panel.GSPanelUtil;
import com.g4mesoft.panel.GSRectangle;
import com.g4mesoft.panel.dropdown.GSDropdown;
import com.g4mesoft.panel.dropdown.GSDropdownAction;
import com.g4mesoft.panel.event.GSEvent;
import com.g4mesoft.panel.event.GSFocusEvent;
import com.g4mesoft.panel.event.GSIFocusEventListener;
import com.g4mesoft.panel.event.GSIKeyListener;
import com.g4mesoft.panel.event.GSKeyEvent;
import com.g4mesoft.renderer.GSIRenderer2D;
import com.g4mesoft.util.GSColorUtil;
import com.g4mesoft.util.GSMathUtil;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class GSTextField extends GSPanel implements GSITextCaretListener, GSITextModelListener, 
                                                    GSIKeyListener, GSIFocusEventListener {

	private static final int DEFAULT_BACKGROUND_COLOR = 0xFF202020;

	private static final int DEFAULT_EDITABLE_TEXT_COLOR        = 0xFFE0E0E0;
	private static final int DEFAULT_UNEDITABLE_TEXT_COLOR      = 0xFF707070;
	private static final int DEFAULT_SELECTION_TEXT_COLOR       = 0xFFFFFFFF;
	private static final int DEFAULT_SELECTION_BACKGROUND_COLOR = 0xFF0E6CDC;
	
	private static final int DEFAULT_BORDER_WIDTH = 1;
	private static final int DEFAULT_BORDER_COLOR = 0xFF171717;

	private static final int DEFAULT_VERTICAL_MARGIN   = 2;
	private static final int DEFAULT_HORIZONTAL_MARGIN = 2;

	private static final int VERTICAL_PADDING = 2;
	
	private static final int PRINTABLE_CHARACTERS_START  = 0x20;

	private static final int BACKSPACE_CONTROL_CHARACTER = 0x08;
	private static final int DELETE_CONTROL_CHARACTER    = 0x7F;
	
	private static final Text CUT_TEXT = new TranslatableText("panel.textfield.cut");
	private static final Text COPY_TEXT = new TranslatableText("panel.textfield.copy");
	private static final Text PASTE_TEXT = new TranslatableText("panel.textfield.paste");
	private static final Text SELECT_ALL_TEXT = new TranslatableText("panel.textfield.selectall");
	
	private GSITextModel textModel;
	private final List<GSIModelListener> modelListeners;
	private final List<GSIChangeListener> changeListeners;
	private final List<GSIActionListener> actionListeners;

	private int backgroundColor;
	
	private int editableTextColor;
	private int uneditableTextColor;
	private GSETextAlignment textAlignment;
	
	private boolean editable;
	private GSITextCaret caret;
	
	private int selectionTextColor;
	private int selectionBackgroundColor;

	private int borderWidth;
	private int borderColor;

	private int verticalMargin;
	private int horizontalMargin;
	
	private int clippedModelStart;
	private int clippedModelEnd;
	private int clippedViewOffset;
	private boolean clippedModelInvalid;
	
	private int oldCaretPointX;
	
	private boolean focusLostOnConfirm;
	
	public GSTextField() {
		this(null);
	}
	
	public GSTextField(String text) {
		if (text != null && !text.isEmpty()) {
			textModel = new GSSingleLineTextModel(text.length());
			textModel.insertText(0, text);
		} else {
			textModel = new GSSingleLineTextModel();
		}
		
		modelListeners = new ArrayList<>();
		changeListeners = new ArrayList<>();
		actionListeners = new ArrayList<>();
		
		backgroundColor = DEFAULT_BACKGROUND_COLOR;
		
		editableTextColor = DEFAULT_EDITABLE_TEXT_COLOR;
		uneditableTextColor = DEFAULT_UNEDITABLE_TEXT_COLOR;
		textAlignment = GSETextAlignment.LEFT;
		
		editable = true;
		caret = new GSBasicTextCaret();
		
		selectionTextColor = DEFAULT_SELECTION_TEXT_COLOR;
		selectionBackgroundColor = DEFAULT_SELECTION_BACKGROUND_COLOR;
	
		borderWidth = DEFAULT_BORDER_WIDTH;
		borderColor = DEFAULT_BORDER_COLOR;
		
		verticalMargin = DEFAULT_VERTICAL_MARGIN;
		horizontalMargin = DEFAULT_HORIZONTAL_MARGIN;
		
		focusLostOnConfirm = false;
		
		textModel.addTextModelListener(this);
		
		caret.install(this);
		caret.addTextCaretListener(this);
		
		addKeyEventListener(this);
		addFocusEventListener(this);
	}
	
	@Override
	public void onResized(int oldWidth, int oldHeight) {
		reconstructClippedModel();
	}
	
	@Override
	public boolean isEditingText() {
		return isFocused() && editable;
	}
	
	private float expandClippedModelLeft(float availableWidth) {
		float expandedWidth = 0;
		int expansionIndex = clippedModelStart;

		GSIRenderer2D renderer = GSPanelContext.getRenderer();
		
		while (availableWidth > expandedWidth && clippedModelStart > 0) {
			clippedModelStart--;
			
			int len = expansionIndex - clippedModelStart;
			CharSequence text = textModel.getCharSequence(clippedModelStart, len);
			expandedWidth = renderer.getTextWidthNoStyle(text);
		}
		
		return availableWidth - expandedWidth;
	}

	private float expandClippedModelRight(float availableWidth) {
		float expandedWidth = 0;
		int expansionIndex = clippedModelEnd;
		
		GSIRenderer2D renderer = GSPanelContext.getRenderer();
		
		while (availableWidth > expandedWidth && clippedModelEnd < textModel.getLength()) {
			clippedModelEnd++;

			int len = clippedModelEnd - expansionIndex;
			CharSequence text = textModel.getCharSequence(expansionIndex, len);
			expandedWidth = renderer.getTextWidthNoStyle(text);
		}
		
		return availableWidth - expandedWidth;
	}
	
	private void reconstructClippedModel() {
		int caretLocation = GSMathUtil.clamp(getCaretLocation(), 0, textModel.getLength());

		int margin = borderWidth + horizontalMargin;
		int width = this.width - margin * 2;
		
		int caretX;
		if (caretLocation <= clippedModelStart) {
			caretX = 0;
		} else if (caretLocation >= clippedModelEnd) {
			caretX = width;
		} else {
			caretX = GSMathUtil.clamp(oldCaretPointX - margin, 0, width);
		}
		
		clippedModelStart = caretLocation;
		float clippedViewOffset = expandClippedModelLeft(caretX);
		
		if (clippedViewOffset > 0) {
			caretX -= clippedViewOffset;
			clippedViewOffset = 0;
		}
		
		clippedModelEnd = caretLocation;
		float availableWidth = expandClippedModelRight(width - caretX);
		
		if (availableWidth > 0.0f && clippedModelStart > 0) {
			availableWidth += clippedViewOffset;
			clippedViewOffset = expandClippedModelLeft(availableWidth);

			availableWidth = 0;
		}
		
		// Fix alignments
		availableWidth += clippedViewOffset;
		if (availableWidth > 0) {
			switch (textAlignment) {
			case RIGHT:
				clippedViewOffset = availableWidth;
				break;
			case CENTER:
				clippedViewOffset = availableWidth / 2;
				break;
			case LEFT:
			default:
				clippedViewOffset = 0;
			}
		}
		
		this.clippedViewOffset = (int)clippedViewOffset;
		
		clippedModelInvalid = false;
	}
	
	private int getCaretLocation() {
		return caret.getCaretLocation();
	}
	
	private int getCaretSelectionStart() {
		return Math.min(caret.getCaretDot(), caret.getCaretMark());
	}

	private int getCaretSelectionEnd() {
		return Math.max(caret.getCaretDot(), caret.getCaretMark());
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		if (clippedModelInvalid)
			reconstructClippedModel();
		
		drawBorderAndBackground(renderer);

		int selectStart = getCaretSelectionStart();
		int selectEnd = getCaretSelectionEnd();
		
		boolean hasSelection = caret.hasCaretSelection();
		int textColor = isEditable() ? editableTextColor : uneditableTextColor;
		
		// Calculate clip bounds
		int x0 = borderWidth + horizontalMargin;
		int y0 = borderWidth + verticalMargin;
		int x1 = width - x0;
		int y1 = height - y0;
		
		renderer.pushClip(x0, y0, x1 - x0, y1 - y0);
		
		// Only draw selection if it is visible
		if (hasSelection && selectEnd > clippedModelStart && selectStart < clippedModelEnd) {
			if (selectStart > clippedModelStart)
				drawVisibleTextSegment(renderer, clippedModelStart, selectStart, textColor);
			if (selectEnd < clippedModelEnd)
				drawVisibleTextSegment(renderer, selectEnd, clippedModelEnd, textColor);

			drawCaretSelection(renderer, selectStart, selectEnd);
		} else {
			drawVisibleTextSegment(renderer, clippedModelStart, clippedModelEnd, textColor);
		}

		renderer.popClip();

		if (isEditingText())
			caret.render(renderer);
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
	
	protected void drawVisibleTextSegment(GSIRenderer2D renderer, int modelStart, int modelEnd, int textColor) {
		// Ensure that the desired text is within view
		if (clippedModelStart > modelStart)
			modelStart = clippedModelStart;
		if (clippedModelEnd < modelEnd)
			modelEnd = clippedModelEnd;

		// No need to render an empty string.
		if (modelStart >= modelEnd)
			return;

		int x = borderWidth + horizontalMargin + clippedViewOffset;
		int y = (height - renderer.getTextAscent()) / 2;
		
		CharSequence text = textModel.getCharSequence(modelStart, modelEnd - modelStart);
		if (modelStart != clippedModelStart) {
			// Offset the text by the not-rendered start of the clipped model.
			int clippedCount = modelStart - clippedModelStart;
			CharSequence clippedText = textModel.getCharSequence(clippedModelStart, clippedCount);
			x += renderer.getTextWidthNoStyle(clippedText);
		}
		
		renderer.drawTextNoStyle(text, x, y, textColor, true);
	}
	
	protected void drawCaretSelection(GSIRenderer2D renderer, int selectStart, int selectEnd) {
		int x0 = borderWidth;
		if (selectStart >= clippedModelStart) {
			GSRectangle sBounds = modelToView(selectStart);
			if (sBounds.x > x0)
				x0 = sBounds.x;
		}
		
		int x1 = width - borderWidth;
		if (selectEnd <= clippedModelEnd) {
			GSRectangle eBounds = modelToView(selectEnd);
			if (eBounds.x < x1)
				x1 = eBounds.x;
		}
		
		if (x0 < x1) {
			int y0 = borderWidth + verticalMargin;
			int y1 = height - y0;
			
			renderer.fillRect(x0, y0, x1 - x0, y1 - y0, selectionBackgroundColor);
			
			drawVisibleTextSegment(renderer, selectStart, selectEnd, selectionTextColor);
		}
	}
	
	public GSRectangle modelToView(int location) {
		// Make sure we're within view
		if (clippedModelInvalid || location < clippedModelStart || location > clippedModelEnd)
			return null;

		GSRectangle bounds = new GSRectangle();
		bounds.x = clippedViewOffset + borderWidth + horizontalMargin;
		bounds.y = borderWidth + verticalMargin;

		bounds.height = height - (borderWidth + verticalMargin) * 2;
		
		if (clippedModelStart == clippedModelEnd) {
			bounds.width = 0;
			return bounds;
		}

		GSIRenderer2D renderer = GSPanelContext.getRenderer();
		
		int leadingLength = location - clippedModelStart;
		CharSequence leadingText = textModel.getCharSequence(clippedModelStart, leadingLength);
		bounds.x += renderer.getTextWidthNoStyle(leadingText);
		
		if (location == clippedModelEnd) {
			bounds.width = 0;
		} else {
			CharSequence cs = textModel.getCharSequence(location, 1);
			bounds.width = (int)Math.ceil(renderer.getTextWidthNoStyle(cs));
		}
		
		return bounds;
	}
	
	@Override
	public GSECursorType getCursor() {
		return GSECursorType.IBEAM;
	}
	
	@Override
	public void populateRightClickMenu(GSDropdown dropdown, int x, int y) {
		GSDropdownAction cutAction, copyAction, pasteAction;
		dropdown.addItem(cutAction = new GSDropdownAction(CUT_TEXT, this::cutToClipboard));
		dropdown.addItem(copyAction = new GSDropdownAction(COPY_TEXT, this::copyToClipboard));
		dropdown.addItem(pasteAction = new GSDropdownAction(PASTE_TEXT, this::pasteFromClipboard));
		dropdown.separate();
		dropdown.addItem(new GSDropdownAction(SELECT_ALL_TEXT, this::selectAll));

		cutAction.setEnabled(caret.hasCaretSelection() && isEditable());
		copyAction.setEnabled(caret.hasCaretSelection());
		pasteAction.setEnabled(GSPanelContext.hasClipboardString() && isEditable());
	}
	
	public void selectAll() {
		caret.setCaretMark(0);
		caret.setCaretDot(textModel.getLength());
	}
	
	public void unselect() {
		caret.setCaretLocation(isFocused() ? textModel.getLength() : 0);
	}
	
	@Override
	protected GSDimension calculatePreferredSize() {
		GSIRenderer2D renderer = GSPanelContext.getRenderer();
		
		// Base bounds of text
		int w = (int)Math.ceil(renderer.getTextWidthNoStyle(getText()));
		int h = renderer.getTextHeight();
		
		// Add borders, margin, and padding
		w += (borderWidth + horizontalMargin) * 2;
		h += (borderWidth + verticalMargin + VERTICAL_PADDING) * 2;
	
		return new GSDimension(w, h);
	}
	
	public int viewToModel(int x, int y) {
		if (clippedModelInvalid || x < 0 || x >= width || y < 0 || y >= height)
			return -1;

		x -= borderWidth + horizontalMargin;
		
		int baseDist = x - clippedViewOffset;
		int minimumDist = Math.abs(baseDist);

		GSIRenderer2D renderer = GSPanelContext.getRenderer();
		
		int i = 0;
		while (i < clippedModelEnd - clippedModelStart) {
			CharSequence sequence = textModel.getCharSequence(clippedModelStart, i + 1);
			int width = (int)Math.ceil(renderer.getTextWidthNoStyle(sequence));
			int dist = Math.abs(baseDist - width);
		
			if (dist > minimumDist)
				break;
			
			minimumDist = dist;
			i++;
		}
		
		return clippedModelStart + i;
	}
	
	@Override
	public void caretLocationChanged(int dot, int mark) {
		int caretLocation = getCaretLocation();
		
		if (!clippedModelInvalid) {
			GSRectangle caretBounds = modelToView(caretLocation);
			if (caretBounds != null) {
				oldCaretPointX = caretBounds.x;
			} else {
				oldCaretPointX = borderWidth + horizontalMargin;
				
				if (caretLocation > (clippedModelStart + clippedModelEnd) / 2)
					oldCaretPointX = width - oldCaretPointX;
			}

			if (!isLocationInView(caretLocation))
				clippedModelInvalid = true;
		}
	}
	
	private boolean isLocationInView(int caretLocation) {
		if (caretLocation > clippedModelStart && caretLocation < clippedModelEnd)
			return true;
		
		if (caretLocation == clippedModelStart) {
			return (clippedViewOffset >= 0);
		} else if (caretLocation == clippedModelEnd) {
			int fieldWidth = width - (borderWidth + horizontalMargin) * 2;
			
			GSIRenderer2D renderer = GSPanelContext.getRenderer();
			int clipLength = clippedModelEnd - clippedModelStart;
			CharSequence clippedText = textModel.getCharSequence(clippedModelStart, clipLength);
			int clipWidth = (int)Math.ceil(renderer.getTextWidthNoStyle(clippedText));
			return (clipWidth + clippedViewOffset <= fieldWidth);
		}
		
		return false;
	}
	
	@Override
	public void textInserted(GSITextModel model, int offset, int count) {
		clippedModelInvalid = true;
		dispatchValueChanged();
	}

	@Override
	public void textRemoved(GSITextModel model, int offset, int count) {
		clippedModelInvalid = true;
		dispatchValueChanged();
	}
	
	@Override
	public void keyPressed(GSKeyEvent event) {
		if (Screen.isCopy(event.getKeyCode())) {
			if (!event.isRepeating())
				copyToClipboard();
			event.consume();
		} else if (Screen.isCut(event.getKeyCode())) {
			if (isEditingText() && !event.isRepeating())
				cutToClipboard();
			event.consume();
		} else if (Screen.isPaste(event.getKeyCode())) {
			if (isEditingText())
				pasteFromClipboard();
			event.consume();
		} else {
			switch (event.getKeyCode()) {
			case GSKeyEvent.KEY_BACKSPACE:
				handleTypedCodePoint(BACKSPACE_CONTROL_CHARACTER, event.getModifiers());
				break;
			case GSKeyEvent.KEY_DELETE:
				handleTypedCodePoint(DELETE_CONTROL_CHARACTER, event.getModifiers());
				break;
			case GSKeyEvent.KEY_ENTER:
			case GSKeyEvent.KEY_KP_ENTER:
				if (!isFocusLostOnConfirm()) {
					if (isEditable())
						dispatchActionPerformed();
					// Do not consume event.
					break;
				} else {
					// pass-through
				}
			case GSKeyEvent.KEY_ESCAPE:
				// Note: event is invoked at focusLost
				//dispatchActionPerformed();
				GSPanel parent = getParent();
				if (parent != null) {
					parent.requestFocus();
				} else {
					unfocus();
				}
				event.consume();
				break;
			}
		}
	}
	
	protected void copyToClipboard() {
		if (caret.hasCaretSelection()) {
			int cs = getCaretSelectionStart();
			int ce = getCaretSelectionEnd();
			
			if (cs >= 0 && ce <= textModel.getLength()) {
				String selectedText = textModel.getText(cs, ce - cs);
				GSPanelContext.setClipboardString(selectedText);
			}
		}
	}
	
	protected void cutToClipboard() {
		copyToClipboard();
		removeSelectionText();
	}
	
	protected void pasteFromClipboard() {
		String clipboard = GSPanelContext.getClipboardString();
		if (clipboard != null && !clipboard.isEmpty()) {
			removeSelectionText();
			
			int cl = getCaretLocation();
			if (cl >= 0 && cl <= textModel.getLength())
				textModel.insertText(cl, clipboard);
		}
	}
	
	private void removeSelectionText() {
		if (caret.hasCaretSelection()) {
			int cs = getCaretSelectionStart();
			int ce = getCaretSelectionEnd();
			
			if (cs >= 0 && ce <= textModel.getLength()) {
				if (cs != caret.getCaretDot())
					moveCaretPointX(textModel.getCharSequence(cs, ce - cs), -1);
				
				textModel.removeText(cs, ce - cs);
			}
		}
	}

	@Override
	public void keyTyped(GSKeyEvent event) {
		if (isEditingText()) {
			handleTypedCodePoint(event.getCodePoint(), event.getModifiers());
			event.consume();
		}
	}
	
	protected void handleTypedCodePoint(int codePoint, int modifiers) {
		if (Character.isBmpCodePoint(codePoint)) {
			char c = (char)codePoint;
			
			if (isTypeableCharacter(c)) {
				boolean wasTextSelected = caret.hasCaretSelection();
				if (wasTextSelected) {
					// Replace selection by the codePoint.
					removeSelectionText();
				}

				if (isControlCharacter(c)) {
					// Ensure that we do not handle control characters
					// when we have replaced a selection.
					if (!wasTextSelected)
						handleTypedControlChar(getCaretLocation(), c, modifiers);
				} else {
					insertTypedChar(getCaretLocation(), c, modifiers);
				}
			}
		}
	}
	
	protected boolean isTypeableCharacter(char c) {
		if (!isControlCharacter(c))
			return true;
		
		return c == BACKSPACE_CONTROL_CHARACTER ||
		       c == DELETE_CONTROL_CHARACTER;
	}
	
	protected void handleTypedControlChar(int offset, char c, int modifiers) {
		switch (c) {
		case BACKSPACE_CONTROL_CHARACTER:
			if (offset > 0) {
				if ((modifiers & GSEvent.MODIFIER_CONTROL) != 0) {
					CharSequence s = textModel.asCharSequence();
					removeTextRange(offset, GSPanelUtil.getIndexAfterWord(s, offset, true));
				} else {
					removeTextRange(offset, offset - 1);
				}
			}
			break;
		case DELETE_CONTROL_CHARACTER:
			if (offset < textModel.getLength()) {
				if ((modifiers & GSEvent.MODIFIER_CONTROL) != 0) {
					CharSequence s = textModel.asCharSequence();
					removeTextRange(offset, GSPanelUtil.getIndexAfterWord(s, offset, false));
				} else {
					removeTextRange(offset, offset + 1);
				}
			}
			break;
		}
	}

	protected void insertTypedChar(int offset, char c, int modifiers) {
		int previousLength = textModel.getLength();
		textModel.insertChar(offset, c);
		
		// Ensure that the character was inserted.
		if (previousLength + 1 == textModel.getLength())
			moveCaretPointX(textModel.getCharSequence(offset, 1), 1);
	}
	
	protected final void removeTextRange(int startOffset, int endOffset) {
		int count = Math.abs(endOffset - startOffset);
		int offset = Math.min(startOffset, endOffset);

		if (startOffset > endOffset) {
			// The caret is located after the removed text. We should
			// move the caret to the left to make it more intuitive.
			moveCaretPointX(textModel.getCharSequence(offset, count), -1);
		}
		
		textModel.removeText(offset, count);
	}

	protected final void moveCaretPointX(CharSequence removedText, int sign) {
		GSIRenderer2D renderer = GSPanelContext.getRenderer();
		
		float tw = renderer.getTextWidthNoStyle(removedText);
		oldCaretPointX += sign * (int)Math.ceil(tw);
	}
	
	private boolean isControlCharacter(char c) {
		return (c < PRINTABLE_CHARACTERS_START || c == DELETE_CONTROL_CHARACTER);
	}

	@Override
	public void focusGained(GSFocusEvent event) {
		if (!caret.hasCaretSelection())
			caret.setCaretLocation(textModel.getLength());
	}
	
	@Override
	public void focusLost(GSFocusEvent event) {
		if (!hasPopupVisible()) {
			if (caret.hasCaretSelection())
				caret.setCaretLocation(0);
			if (isEditable())
				dispatchActionPerformed();
		}
	}
	
	public void addModelListener(GSIModelListener listener) {
		modelListeners.add(listener);
	}

	public void removeModelListener(GSIModelListener listener) {
		modelListeners.remove(listener);
	}
	
	public void setTextModel(GSITextModel textModel) {
		if (textModel == null)
			throw new IllegalArgumentException("Text model can not be null!");
		
		this.textModel.removeTextModelListener(this);
		this.textModel = textModel;
		textModel.addTextModelListener(this);
		
		invokeModelChangedEvent();
	}
	
	private void invokeModelChangedEvent() {
		modelListeners.forEach(GSIModelListener::modelChanged);
	}
	
	public GSITextModel getTextModel() {
		return textModel;
	}
	
	public void addChangeListener(GSIChangeListener listener) {
		changeListeners.add(listener);
	}

	public void removeChangeListener(GSIChangeListener listener) {
		changeListeners.remove(listener);
	}

	private void dispatchValueChanged() {
		changeListeners.forEach(GSIChangeListener::valueChanged);
	}

	public void addActionListener(GSIActionListener listener) {
		actionListeners.add(listener);
	}
	
	public void removeActionListener(GSIActionListener listener) {
		actionListeners.remove(listener);
	}
	
	private void dispatchActionPerformed() {
		actionListeners.forEach(GSIActionListener::actionPerformed);
	}
	
	public void setText(String text) {
		if (textModel.getLength() != 0)
			textModel.removeText(0, textModel.getLength());
		if (text != null && !text.isEmpty())
			textModel.insertText(0, text);
	}
	
	public void appendText(String text) {
		if (text == null || text.isEmpty())
			return;

		textModel.appendText(text);
	}
	
	public String getText() {
		return textModel.getText();
	}

	public int getBackgroundColor() {
		return backgroundColor;
	}
	
	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	
	public int getEditableTextColor() {
		return editableTextColor;
	}
	
	public void setEditableTextColor(int editableTextColor) {
		this.editableTextColor = editableTextColor;
	}

	public int getUneditableTextColor() {
		return uneditableTextColor;
	}
	
	public void setUneditableTextColor(int uneditableTextColor) {
		this.uneditableTextColor = uneditableTextColor;
	}

	public GSETextAlignment getTextAlignment() {
		return textAlignment;
	}

	public void setTextAlignment(GSETextAlignment textAlignment) {
		if (textAlignment == null)
			throw new IllegalArgumentException("textAlignment is null!");
		
		this.textAlignment = textAlignment;
	}
	
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	
	public boolean isEditable() {
		return editable;
	}
	
	public void setCaret(GSITextCaret caret) {
		if (caret == null)
			throw new IllegalArgumentException("caret is null!");
		
		this.caret.removeTextCaretListener(this);
		this.caret.uninstall(this);

		this.caret = caret;

		caret.install(this);
		caret.addTextCaretListener(this);
	}
	
	public GSITextCaret getCaret() {
		return caret;
	}
	
	public void setSelectionTextColor(int selectionTextColor) {
		this.selectionTextColor = selectionTextColor;
	}

	public int getSelectionTextColor() {
		return selectionTextColor;
	}
	
	public void setSelectionBackgroundColor(int selectionBackgroundColor) {
		this.selectionBackgroundColor = selectionBackgroundColor;
	}

	public int getSelectionBackgroundColor() {
		return selectionBackgroundColor;
	}
	
	public int getBorderWidth() {
		return borderWidth;
	}

	public void setBorderWidth(int borderWidth) {
		if (borderWidth < 0)
			throw new IllegalArgumentException("borderWidth must be non-negative!");
		
		this.borderWidth = borderWidth;
	}

	public int getBorderColor() {
		return borderColor;
	}
	
	public void setBorderColor(int borderColor) {
		this.borderColor = borderColor;
	}
	
	public int getVerticalMargin() {
		return verticalMargin;
	}

	public void setVerticalMargin(int verticalMargin) {
		if (verticalMargin < 0)
			throw new IllegalArgumentException("verticalMargin must be non-negative!");
		
		this.verticalMargin = verticalMargin;
	}

	public int getHorizontalMargin() {
		return horizontalMargin;
	}
	
	public void setHorizontalMargin(int horizontalMargin) {
		if (horizontalMargin < 0)
			throw new IllegalArgumentException("horizontalMargin must be non-negative!");
		
		this.horizontalMargin = horizontalMargin;
	}
	
	public boolean isFocusLostOnConfirm() {
		return focusLostOnConfirm;
	}

	public void setFocusLostOnConfirm(boolean flag) {
		focusLostOnConfirm = flag;
	}
}
