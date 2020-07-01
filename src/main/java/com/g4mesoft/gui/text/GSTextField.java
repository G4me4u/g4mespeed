package com.g4mesoft.gui.text;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.g4mesoft.access.GSIBufferBuilderAccess;
import com.g4mesoft.gui.GSClipRect;
import com.g4mesoft.gui.GSPanel;
import com.g4mesoft.util.GSMathUtils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;

public class GSTextField extends GSPanel implements GSITextCaretListener, GSITextModelListener {

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
	private static final int TAB_CONTROL_CHARACTER       = 0x09;
	private static final int NEW_LINE_CONTROL_CHARACTER  = 0x0A;
	private static final int CONTROL_Z_CONTROL_CHARACTER = 0x1A;
	private static final int ESCAPE_CONTROL_CHARACTER    = 0x1B;
	private static final int DELETE_CONTROL_CHARACTER    = 0x7F;
	
	private GSITextModel textModel;
	private final List<GSIModelChangeListener> modelChangeListeners;

	private int backgroundColor;
	
	private int editableTextColor;
	private int uneditableTextColor;
	private GSTextAlignment textAlignment;
	
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
		
		modelChangeListeners = new ArrayList<GSIModelChangeListener>();
		
		backgroundColor = DEFAULT_BACKGROUND_COLOR;
		
		editableTextColor = DEFAULT_EDITABLE_TEXT_COLOR;
		uneditableTextColor = DEFAULT_UNEDITABLE_TEXT_COLOR;
		textAlignment = GSTextAlignment.LEFT;
		
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
	}
	
	public void initPreferredBounds(MinecraftClient client, int x, int y) {
		int textWidth = client.textRenderer.getStringWidth(getText());
		int prefWidth = textWidth + (borderWidth + horizontalMargin) * 2;
		
		initPreferredBounds(client, x, y, prefWidth);
	}

	public void initPreferredBounds(MinecraftClient client, int x, int y, int width) {
		int textHeight = client.textRenderer.fontHeight;
		int prefHeight = textHeight + (borderWidth + verticalMargin + VERTICAL_PADDING) * 2;
		
		super.initBounds(client, x, y, width, prefHeight);
	}

	@Override
	public void init() {
		super.init();

		clippedModelInvalid = true;
	}
	
	@Override
	public boolean isEditingText() {
		return isElementFocused() && editable;
	}
	
	private float expandClippedModelLeft(float availableWidth) {
		while (availableWidth > 0.0f && clippedModelStart > 0) {
			clippedModelStart--;
			
			char c = textModel.getChar(clippedModelStart);
			availableWidth -= font.getCharWidth(c);
		}
		
		return availableWidth;
	}

	private float expandClippedModelRight(float availableWidth) {
		while (availableWidth > 0.0f && clippedModelEnd < textModel.getLength()) {
			char c = textModel.getChar(clippedModelEnd);
			availableWidth -= font.getCharWidth(c);
			
			clippedModelEnd++;
		}
		
		return availableWidth;
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
	public void tick() {
		if (isEditable() && isElementFocused())
			caret.update();
	}

	@Override
	protected void renderTranslated(int mouseX, int mouseY, float partialTicks) {
		if (clippedModelInvalid)
			reconstructClippedModel();
		
		drawBorderAndBackground(mouseX, mouseY, partialTicks);

		int selectStart = getCaretSelectionStart();
		int selectEnd = getCaretSelectionEnd();
		
		boolean hasSelection = caret.hasCaretSelection();
		int textColor = isEditable() ? editableTextColor : uneditableTextColor;
		
		int x0 = borderWidth + horizontalMargin;
		int y0 = borderWidth + verticalMargin;
		int x1 = width - x0;
		int y1 = height - y0;
		
		BufferBuilder builder = Tessellator.getInstance().getBuffer();
		((GSIBufferBuilderAccess)builder).pushClip(new GSClipRect(x0, y0, x1, y1));
		
		// Only draw text if it is not all selected.
		if (!hasSelection || selectStart > clippedModelStart || selectEnd < clippedModelEnd) {
			if (hasSelection) {
				drawVisibleTextSegment(clippedModelStart, selectStart, textColor);
				drawVisibleTextSegment(selectEnd, clippedModelEnd, textColor);
			} else {
				drawVisibleTextSegment(clippedModelStart, clippedModelEnd, textColor);
			}
		}
		
		if (hasSelection && selectEnd > clippedModelStart && selectStart < clippedModelEnd)
			drawCaretSelection(selectStart, selectEnd);

		((GSIBufferBuilderAccess)builder).popClip();

		if (isEditable() && isElementFocused())
			caret.render(mouseX, mouseY, partialTicks);
	}
	
	protected void drawBorderAndBackground(int mouseX, int mouseY, float partialTicks) {
		if (borderWidth != 0) {
			fill(0, 0, borderWidth, height - borderWidth, borderColor);
			fill(width - borderWidth, borderWidth, width, height, borderColor);
			fill(borderWidth, 0, width, borderWidth, borderColor);
			fill(0, height - borderWidth, width - borderWidth, height, borderColor);
		}
		
		if (((backgroundColor >>> 24) & 0xFF) != 0x00)
			fill(borderWidth, borderWidth, width - borderWidth, height - borderWidth, backgroundColor);
	}
	
	protected void drawVisibleTextSegment(int modelStart, int modelEnd, int textColor) {
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

		String text = clippedText;
		if (clipLength != clippedText.length()) {
			text = clippedText.substring(clipOffset, clipOffset + clipLength);
			
			if (clipOffset != 0)
				x += font.getStringWidth(clippedText.substring(0, clipOffset));
		}
		
		font.drawWithShadow(text, x, (height - font.fontHeight) / 2, textColor);
	}
	
	protected void drawCaretSelection(int selectStart, int selectEnd) {
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
			DrawableHelper.fill(x0, borderWidth, x1, height - borderWidth, selectionBackgroundColor);
			
			drawVisibleTextSegment(selectStart, selectEnd, selectionTextColor);
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

		int offset = location - clippedModelStart;
		bounds.x += font.getStringWidth(clippedText.substring(0, offset));
		
		char c;
		if (offset == clippedText.length()) {
			c = clippedText.charAt(offset - 1);
			bounds.width = 0;
		} else {
			c = clippedText.charAt(offset);
			bounds.width = (int)Math.ceil(font.getCharWidth(c));
		}
		
		return bounds;
	}

	public int viewToModel(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height)
			return -1;

		x -= borderWidth + horizontalMargin;
		
		int baseDist = x - clippedViewOffset;
		int minimumDist = Math.abs(baseDist);

		int index = 0;
		while (index < clippedText.length()) {
			String text = clippedText.substring(0, index + 1);
			int width = (int)Math.ceil(font.getStringWidth(text));
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
			
			int clipWidth = (int)font.getStringWidth(clippedText);
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
	public boolean onMouseClickedGS(double mouseX, double mouseY, int button) {
		if (caret.onMouseClicked(mouseX, mouseY, button))
			return true;
		
		return super.onMouseClickedGS(mouseX, mouseY, button);
	}
	
	@Override
	public boolean onMouseReleasedGS(double mouseX, double mouseY, int button) {
		if (caret.onMouseReleased(mouseX, mouseY, button))
			return true;

		return super.onMouseReleasedGS(mouseX, mouseY, button);
	}
	
	@Override
	public boolean onMouseDraggedGS(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (caret.onMouseDragged(mouseX, mouseY, button, dragX, dragY))
			return true;
		
		return super.onMouseDraggedGS(mouseX, mouseY, button, dragX, dragY);
	}
	
	@Override
	public boolean onKeyPressedGS(int key, int scancode, int mods) {
		if (caret.onKeyPressed(key, scancode, mods))
			return true;
		
		if (Screen.isCopy(key)) {
			copyToClipboard();
			return true;
		}
		
		if (Screen.isCut(key)) {
			cutToClipboard();
			return true;
		}
		
		if (Screen.isPaste(key)) {
			pasteFromClipboard();
			return true;
		}
		
		checkAndDispatchControlCharacter(key, mods);
		
		return super.onKeyPressedGS(key, scancode, mods);
	}
	
	private void checkAndDispatchControlCharacter(int key, int mods) {
		switch (key) {
		case GLFW.GLFW_KEY_BACKSPACE:
			handleTypedCodePoint(BACKSPACE_CONTROL_CHARACTER);
			break;
		case GLFW.GLFW_KEY_TAB:
			handleTypedCodePoint(TAB_CONTROL_CHARACTER);
			break;
		case GLFW.GLFW_KEY_ENTER:
			handleTypedCodePoint(NEW_LINE_CONTROL_CHARACTER);
			break;
		case GLFW.GLFW_KEY_Z:
			if ((mods & GLFW.GLFW_MOD_CONTROL) != 0)
				handleTypedCodePoint(CONTROL_Z_CONTROL_CHARACTER);
			break;
		case GLFW.GLFW_KEY_ESCAPE:
			handleTypedCodePoint(ESCAPE_CONTROL_CHARACTER);
			break;
		case GLFW.GLFW_KEY_DELETE:
			handleTypedCodePoint(DELETE_CONTROL_CHARACTER);
			break;
		}
	}
	
	protected void copyToClipboard() {
		if (caret.hasCaretSelection()) {
			int cs = getCaretSelectionStart();
			int ce = getCaretSelectionEnd();
			
			if (cs >= 0 && ce <= textModel.getLength()) {
				String selectedText = textModel.getText(cs, ce - cs);
				client.keyboard.setClipboard(selectedText);
			}
		}
	}
	
	protected void cutToClipboard() {
		copyToClipboard();
		removeCaretSelection();
	}
	
	protected void pasteFromClipboard() {
		String clipboard = client.keyboard.getClipboard();
		if (clipboard != null && !clipboard.isEmpty()) {
			removeCaretSelection();
			
			int cl = getCaretLocation();
			if (cl >= 0 && cl <= textModel.getLength())
				textModel.insertText(cl, clipboard);
		}
	}

	@Override
	public boolean onKeyReleasedGS(int key, int scancode, int mods) {
		if (caret.onKeyReleased(key, scancode, mods))
			return true;
		
		return super.onKeyReleasedGS(key, scancode, mods);
	}
	
	@Override
	public boolean onCharTypedGS(char c, int mods) {
		if (isEditable() && isElementFocused())
			handleTypedCodePoint((int)c);
		
		return super.onCharTypedGS(c, mods);
	}
	
	protected void handleTypedCodePoint(int codePoint) {
		if (Character.isBmpCodePoint(codePoint)) {
			char c = (char)codePoint;
			
			if (isTypeableCharacter(c)) {
				removeCaretSelection();
				
				if (!caret.hasCaretSelection() || !isControlCharacter(c))
					insertTypedChar(getCaretLocation(), c);
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
				moveCaretPointX(textModel.getText(cs, ce - cs), -1);
				
				textModel.removeText(cs, ce - cs);
			}
		}
	}
	
	private void insertTypedChar(int offset, char c) {
		if (isControlCharacter(c)) {
			switch (c) {
			case BACKSPACE_CONTROL_CHARACTER:
				if (offset > 0) {
					moveCaretPointX(textModel.getText(offset - 1, 1), -1);
			
					textModel.removeText(offset - 1, 1);
				}
				break;
			case DELETE_CONTROL_CHARACTER:
				if (offset < textModel.getLength())
					textModel.removeText(offset, 1);
				break;
			}
		} else {
			textModel.insertChar(offset, c);
			
			moveCaretPointX(textModel.getText(offset, 1), 1);
		}
	}

	private void moveCaretPointX(String removedText, int sign) {
		oldCaretPointX += sign * font.getStringWidth(removedText);
	}
	
	private boolean isControlCharacter(char c) {
		return (c < PRINTABLE_CHARACTERS_START || c == DELETE_CONTROL_CHARACTER);
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

	public GSTextAlignment getTextAlignment() {
		return textAlignment;
	}

	public void setTextAlignment(GSTextAlignment textAlignment) {
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
}
