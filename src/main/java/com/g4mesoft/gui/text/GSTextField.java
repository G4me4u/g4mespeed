package com.g4mesoft.gui.text;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.g4mesoft.gui.GSECursorType;
import com.g4mesoft.gui.GSElementContext;
import com.g4mesoft.gui.GSPanel;
import com.g4mesoft.gui.event.GSEvent;
import com.g4mesoft.gui.event.GSIKeyListener;
import com.g4mesoft.gui.event.GSKeyEvent;
import com.g4mesoft.gui.renderer.GSIRenderer2D;
import com.g4mesoft.util.GSMathUtils;

import net.minecraft.client.gui.screen.Screen;

public class GSTextField extends GSPanel implements GSITextCaretListener, GSITextModelListener, 
                                                    GSIKeyListener {

	private static final int DEFAULT_BACKGROUND_COLOR = 0xFF000000;

	private static final int DEFAULT_EDITABLE_TEXT_COLOR        = 0xFFE0E0E0;
	private static final int DEFAULT_UNEDITABLE_TEXT_COLOR      = 0xFF707070;
	private static final int DEFAULT_SELECTION_TEXT_COLOR       = 0xFFFFFFFF;
	private static final int DEFAULT_SELECTION_BACKGROUND_COLOR = 0xFF0E6CDC;
	
	private static final int DEFAULT_BORDER_WIDTH = 1;
	private static final int DEFAULT_BORDER_COLOR = 0xFFA0A0A0;

	private static final int DEFAULT_VERTICAL_MARGIN   = 2;
	private static final int DEFAULT_HORIZONTAL_MARGIN = 2;

	private static final int VERTICAL_PADDING = 2;
	
	private static final int PRINTABLE_CHARACTERS_START  = 0x20;

	private static final int BACKSPACE_CONTROL_CHARACTER = 0x08;
	private static final int DELETE_CONTROL_CHARACTER    = 0x7F;
	
	private GSITextModel textModel;
	private final List<GSIModelChangeListener> modelChangeListeners;

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
	
	private String clippedText;
	private int clippedModelStart;
	private int clippedModelEnd;
	private int clippedViewOffset;
	private boolean clippedModelInvalid;
	
	private int oldCaretPointX;
	
	public GSTextField() {
		this(null);
	}
	
	public GSTextField(String text) {
		if (text != null) {
			textModel = new GSSingleLineTextModel(text.length());
			textModel.insertText(0, text);
		} else {
			textModel = new GSSingleLineTextModel();
		}
		
		modelChangeListeners = new ArrayList<>();
		
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
		
		textModel.addTextModelListener(this);
		
		caret.install(this);
		caret.addTextCaretListener(this);
		
		addKeyEventListener(this);
	}
	
	public void setPreferredBounds(int x, int y) {
		GSIRenderer2D renderer = GSElementContext.getRenderer();
		
		int textWidth = (int)Math.ceil(renderer.getStringWidth(getText()));
		int prefWidth = textWidth + (borderWidth + horizontalMargin) * 2;
		
		setPreferredBounds(x, y, prefWidth);
	}

	public void setPreferredBounds(int x, int y, int width) {
		GSIRenderer2D renderer = GSElementContext.getRenderer();

		int textHeight = renderer.getFontHeight();
		int prefHeight = textHeight + (borderWidth + verticalMargin + VERTICAL_PADDING) * 2;
		
		super.setBounds(x, y, width, prefHeight);
	}

	@Override
	public void onBoundsChanged() {
		super.onBoundsChanged();

		reconstructClippedModel();
	}
	
	@Override
	public boolean isEditingText() {
		return isFocused() && editable;
	}
	
	private float expandClippedModelLeft(float availableWidth) {
		float expandedWidth = 0;
		int expansionIndex = clippedModelStart;

		GSIRenderer2D renderer = GSElementContext.getRenderer();
		
		while (availableWidth > expandedWidth && clippedModelStart > 0) {
			clippedModelStart--;
			
			String text = textModel.getText(clippedModelStart, expansionIndex - clippedModelStart);
			expandedWidth = renderer.getStringWidth(text);
		}
		
		return availableWidth - expandedWidth;
	}

	private float expandClippedModelRight(float availableWidth) {
		float expandedWidth = 0;
		int expansionIndex = clippedModelEnd;
		
		GSIRenderer2D renderer = GSElementContext.getRenderer();
		
		while (availableWidth > expandedWidth && clippedModelEnd < textModel.getLength()) {
			clippedModelEnd++;

			String text = textModel.getText(expansionIndex, clippedModelEnd - expansionIndex);
			expandedWidth = renderer.getStringWidth(text);
		}
		
		return availableWidth - expandedWidth;
	}
	
	private void reconstructClippedModel() {
		int caretLocation = GSMathUtils.clamp(getCaretLocation(), 0, textModel.getLength());

		int margin = borderWidth + horizontalMargin;
		int width = this.width - margin * 2;
		
		int caretX;
		if (caretLocation <= clippedModelStart) {
			caretX = 0;
		} else if (caretLocation >= clippedModelEnd) {
			caretX = width;
		} else {
			caretX = GSMathUtils.clamp(oldCaretPointX - margin, 0, width);
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
		
		int count = clippedModelEnd - clippedModelStart;
		clippedText = (count != 0) ? textModel.getText(clippedModelStart, count) : "";
	
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
	public void update() {
		if (isEditingText())
			caret.update();
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
		
		int x0 = borderWidth + horizontalMargin;
		int y0 = borderWidth + verticalMargin;
		int x1 = width - x0;
		int y1 = height - y0;
		
		renderer.pushClip(x0, y0, x1, y1);
		
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
		
		if (((backgroundColor >>> 24) & 0xFF) != 0x00)
			renderer.fillRect(borderWidth, borderWidth, width - bw2, height - bw2, backgroundColor);
	}
	
	protected void drawVisibleTextSegment(GSIRenderer2D renderer, int modelStart, int modelEnd, int textColor) {
		int clipOffset = modelStart - clippedModelStart;
		if (clipOffset < 0)
			clipOffset = 0;
		
		int clipLength = modelEnd - clippedModelStart - clipOffset;
		if (clipLength > clippedText.length() - clipOffset)
			clipLength = clippedText.length() - clipOffset;

		// No need to render an empty string.
		if (clipOffset >= clippedText.length() || clipLength <= 0)
			return;

		int x = borderWidth + horizontalMargin + clippedViewOffset;
		int y = (height - renderer.getFontHeight() + 1) / 2;
		
		String text = clippedText;
		if (clipLength != clippedText.length()) {
			text = clippedText.substring(clipOffset, clipOffset + clipLength);
			
			if (clipOffset != 0)
				x += renderer.getStringWidth(clippedText.substring(0, clipOffset));
		}
		
		renderer.drawString(text, x, y, textColor, true);
	}
	
	protected void drawCaretSelection(GSIRenderer2D renderer, int selectStart, int selectEnd) {
		int x0 = borderWidth;
		if (selectStart >= clippedModelStart) {
			Rectangle sBounds = modelToView(selectStart);
			if (sBounds.x > x0)
				x0 = sBounds.x;
		}
		
		int x1 = width - borderWidth;
		if (selectEnd <= clippedModelEnd) {
			Rectangle eBounds = modelToView(selectEnd);
			if (eBounds.x < x1)
				x1 = eBounds.x;
		}
		
		if (x0 < x1) {
			renderer.fillRect(x0, borderWidth, x1 - x0, height - borderWidth * 2, selectionBackgroundColor);
			
			drawVisibleTextSegment(renderer, selectStart, selectEnd, selectionTextColor);
		}
	}
	
	public Rectangle modelToView(int location) {
		// Make sure we're within view
		if (location < clippedModelStart || location > clippedModelEnd)
			return null;

		Rectangle bounds = new Rectangle();
		bounds.x = clippedViewOffset + borderWidth + horizontalMargin;
		bounds.y = borderWidth + verticalMargin;

		bounds.height = height - (borderWidth + verticalMargin) * 2;
		
		if (clippedText.isEmpty()) {
			bounds.width = 0;
			return bounds;
		}

		GSIRenderer2D renderer = GSElementContext.getRenderer();
		
		int offset = location - clippedModelStart;
		bounds.x += renderer.getStringWidth(clippedText.substring(0, offset));
		
		if (offset == clippedText.length()) {
			bounds.width = 0;
		} else {
			String cs = clippedText.substring(offset, offset + 1);
			bounds.width = (int)Math.ceil(renderer.getStringWidth(cs));
		}
		
		return bounds;
	}
	
	@Override
	public GSECursorType getCursor() {
		return GSECursorType.IBEAM;
	}

	public int viewToModel(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height)
			return -1;

		x -= borderWidth + horizontalMargin;
		
		int baseDist = x - clippedViewOffset;
		int minimumDist = Math.abs(baseDist);

		GSIRenderer2D renderer = GSElementContext.getRenderer();
		
		int index = 0;
		while (index < clippedText.length()) {
			String text = clippedText.substring(0, index + 1);
			int width = (int)Math.ceil(renderer.getStringWidth(text));
			int dist = Math.abs(baseDist - width);
		
			if (dist > minimumDist)
				break;
			
			minimumDist = dist;
			index++;
		}
		
		return index + clippedModelStart;
	}
	
	@Override
	public void caretLocationChanged(int dot, int mark) {
		int caretLocation = getCaretLocation();
		
		if (!clippedModelInvalid) {
			Rectangle caretBounds = modelToView(caretLocation);
			if (caretBounds != null) {
				oldCaretPointX = caretBounds.x;
			} else {
				oldCaretPointX = borderWidth + horizontalMargin;
				
				if (caretLocation > (clippedModelStart + clippedModelEnd) / 2)
					oldCaretPointX = width - oldCaretPointX;
			}
		}

		if (!isLocationInView(caretLocation))
			clippedModelInvalid = true;
	}
	
	private boolean isLocationInView(int caretLocation) {
		if (caretLocation > clippedModelStart && caretLocation < clippedModelEnd)
			return true;
		
		if (caretLocation == clippedModelStart) {
			return (clippedViewOffset >= 0);
		} else if (caretLocation == clippedModelEnd) {
			int fieldWidth = width - (borderWidth + horizontalMargin) * 2;
			
			GSIRenderer2D renderer = GSElementContext.getRenderer();
			int clipWidth = (int)Math.ceil(renderer.getStringWidth(clippedText));
			return (clipWidth + clippedViewOffset <= fieldWidth);
		}
		
		return false;
	}
	
	@Override
	public void textInserted(GSITextModel model, int offset, int count) {
		clippedModelInvalid = true;
	}

	@Override
	public void textRemoved(GSITextModel model, int offset, int count) {
		clippedModelInvalid = true;
	}
	
	@Override
	public void keyPressed(GSKeyEvent event) {
		if (!event.isRepeating() && Screen.isCopy(event.getKeyCode())) {
			copyToClipboard();
			event.consume();
		} else if (isEditingText()) {
			if (!event.isRepeating() && Screen.isCut(event.getKeyCode())) {
				cutToClipboard();
				event.consume();
			} else if (Screen.isPaste(event.getKeyCode())) {
				pasteFromClipboard();
				event.consume();
			}
		}
	}
	
	protected void copyToClipboard() {
		if (caret.hasCaretSelection()) {
			int cs = getCaretSelectionStart();
			int ce = getCaretSelectionEnd();
			
			if (cs >= 0 && ce <= textModel.getLength()) {
				String selectedText = textModel.getText(cs, ce - cs);
				GSElementContext.setClipboardString(selectedText);
			}
		}
	}
	
	protected void cutToClipboard() {
		copyToClipboard();
		removeCaretSelection();
	}
	
	protected void pasteFromClipboard() {
		String clipboard = GSElementContext.getClipboardString();
		if (clipboard != null && !clipboard.isEmpty()) {
			removeCaretSelection();
			
			int cl = getCaretLocation();
			if (cl >= 0 && cl <= textModel.getLength())
				textModel.insertText(cl, clipboard);
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
				removeCaretSelection();
				
				if (!caret.hasCaretSelection() || !isControlCharacter(c))
					insertTypedChar(getCaretLocation(), c, modifiers);
			}
		}
	}
	
	private boolean isTypeableCharacter(char c) {
		if (!isControlCharacter(c))
			return true;
		
		return c == BACKSPACE_CONTROL_CHARACTER ||
		       c == DELETE_CONTROL_CHARACTER;
	}

	private void removeCaretSelection() {
		if (caret.hasCaretSelection()) {
			int cs = getCaretSelectionStart();
			int ce = getCaretSelectionEnd();
			
			if (cs >= 0 && ce <= textModel.getLength()) {
				if (cs != caret.getCaretDot())
					moveCaretPointX(textModel.getText(cs, ce - cs), -1);
				
				textModel.removeText(cs, ce - cs);
			}
		}
	}
	
	private void insertTypedChar(int offset, char c, int modifiers) {
		if (isControlCharacter(c)) {
			switch (c) {
			case BACKSPACE_CONTROL_CHARACTER:
				if (offset > 0) {
					if ((modifiers & GSEvent.MODIFIER_CONTROL) != 0) {
						removeTextRange(offset, getLocationAfterWord(offset, true));
					} else {
						removeTextRange(offset, offset - 1);
					}
				}
				break;
			case DELETE_CONTROL_CHARACTER:
				if (offset < textModel.getLength()) {
					if ((modifiers & GSEvent.MODIFIER_CONTROL) != 0) {
						removeTextRange(offset, getLocationAfterWord(offset, false));
					} else {
						removeTextRange(offset, offset + 1);
					}
				}
				break;
			}
		} else {
			textModel.insertChar(offset, c);
			
			moveCaretPointX(textModel.getText(offset, 1), 1);
		}
	}
	
	private void removeTextRange(int startOffset, int endOffset) {
		int count = Math.abs(endOffset - startOffset);
		int offset = Math.min(startOffset, endOffset);

		if (startOffset > endOffset) {
			// The caret is located after the removed text. We should
			// move the caret to the left to make it more intuitive.
			moveCaretPointX(textModel.getText(offset, count), -1);
		}
		
		textModel.removeText(offset, count);
	}

	private void moveCaretPointX(String removedText, int sign) {
		GSIRenderer2D renderer = GSElementContext.getRenderer();
		
		float tw = renderer.getStringWidth(removedText);
		oldCaretPointX += sign * (int)Math.ceil(tw);
	}
	
	private boolean isControlCharacter(char c) {
		return (c < PRINTABLE_CHARACTERS_START || c == DELETE_CONTROL_CHARACTER);
	}
	
	int getLocationAfterWord(int startLocation, boolean backward) {
		int nextLocation = startLocation;
		
		if (backward) {
			GSEWordCharacterType prevType = GSEWordCharacterType.OTHER;

			for ( ; nextLocation > 0; nextLocation--) {
				GSEWordCharacterType type = getWordCharacterTypeAt(nextLocation - 1);
				if (type != prevType && prevType != GSEWordCharacterType.OTHER)
					break;
				
				prevType = type;
			}
		} else {
			GSEWordCharacterType prevType = getWordCharacterTypeAt(startLocation);

			for ( ; nextLocation < textModel.getLength(); nextLocation++) {
				GSEWordCharacterType type = getWordCharacterTypeAt(nextLocation);
				if (type != prevType && type != GSEWordCharacterType.OTHER)
					break;
				
				prevType = type;
			}
		}
		
		return nextLocation;
	}
	
	private GSEWordCharacterType getWordCharacterTypeAt(int location) {
		if (location >= 0 && location < textModel.getLength()) {
			char c = textModel.getChar(location);
			
			switch (Character.getType(c)) {
			case Character.UPPERCASE_LETTER:
			case Character.LOWERCASE_LETTER:
			case Character.TITLECASE_LETTER:
			case Character.MODIFIER_LETTER:
			case Character.OTHER_LETTER:
			case Character.DECIMAL_DIGIT_NUMBER:
			case Character.OTHER_NUMBER:
				return GSEWordCharacterType.LETTER_OR_DIGIT;
	
			case Character.LETTER_NUMBER:
			case Character.DASH_PUNCTUATION:
			case Character.START_PUNCTUATION:
			case Character.END_PUNCTUATION:
			case Character.CONNECTOR_PUNCTUATION:
			case Character.OTHER_PUNCTUATION:
			case Character.MATH_SYMBOL:
			case Character.CURRENCY_SYMBOL:
			case Character.MODIFIER_SYMBOL:
			case Character.OTHER_SYMBOL:
			case Character.INITIAL_QUOTE_PUNCTUATION:
			case Character.FINAL_QUOTE_PUNCTUATION:
				return GSEWordCharacterType.SYMBOL;
			
			case Character.UNASSIGNED:
			case Character.NON_SPACING_MARK:
			case Character.ENCLOSING_MARK:
			case Character.COMBINING_SPACING_MARK:
			case Character.SPACE_SEPARATOR:
			case Character.LINE_SEPARATOR:
			case Character.PARAGRAPH_SEPARATOR:
			case Character.CONTROL:
			case Character.FORMAT:
			case Character.PRIVATE_USE:
			case Character.SURROGATE:
			default:
				return GSEWordCharacterType.OTHER;
			}
		}

		return null;
	}
	
	public void addModelChangeListener(GSIModelChangeListener changeListener) {
		modelChangeListeners.add(changeListener);
	}

	public void removeModelChangeListener(GSIModelChangeListener changeListener) {
		modelChangeListeners.remove(changeListener);
	}
	
	public void setTextModel(GSITextModel textModel) {
		if (textModel == null)
			throw new IllegalArgumentException("Text model can not be null!");
		
		this.textModel.removeTextModelListener(this);
		this.textModel = textModel;
		textModel.addTextModelListener(this);
		
		invokeModelChangeEvent();
	}
	
	private void invokeModelChangeEvent() {
		modelChangeListeners.forEach(GSIModelChangeListener::modelChanged);
	}
	
	public GSITextModel getTextModel() {
		return textModel;
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
		return textModel.getText(0, textModel.getLength());
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
	
	private enum GSEWordCharacterType {
		
		LETTER_OR_DIGIT, SYMBOL, OTHER;
		
	}
}
