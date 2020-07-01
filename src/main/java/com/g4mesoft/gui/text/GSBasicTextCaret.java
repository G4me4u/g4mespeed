package com.g4mesoft.gui.text;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.g4mesoft.access.GSIMouseAccess;
import com.g4mesoft.util.GSMathUtils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;

/**
 * A basic text caret used by the user to navigate the text area on which this
 * caret is installed. This text caret will handle almost all navigational
 * properties of a modern text caret. This includes navigating by arrow keys,
 * selection modifiers and much more. To see all the functionality of the user
 * selection and navigational tools, see the setter-methods for these inputs.
 * <br><br>
 * The caret itself is defined by two numbers; the dot and the mark. To query
 * these, use the methods {@link #getCaretDot()} and {@link #getCaretMark()} 
 * respectively. The caret dot represents the location in the document at which
 * the cursor itself is located, usually represented by a vertical line. This
 * vertical line will always be located just before the index of the character
 * that it points to. For example will {@code dot = 0} represent the position
 * before the first character in the view. If the user decides to create a
 * selection, this will be represented by the {@code mark}. The selection itself
 * is <i>not</i> painted in this text caret and should therefore be handled 
 * elsewhere.
 * 
 * @author Christian
 */
public class GSBasicTextCaret implements GSITextCaret, GSITextModelListener, GSIModelChangeListener {

	private static final int DEFAULT_BLINK_RATE   = 500;
	private static final int DEFAULT_CARET_WIDTH  = 1;
	private static final int DEFAULT_CARET_INSETS = 0;
	
	private static final int DEFAULT_CARET_COLOR = 0xFFFFFFFF;
	
	private static final int NO_MODIFIERS             = 0x00;
	private static final int SELECTION_MODIFIER       = 0x01;
	private static final int WORD_NAVIGATION_MODIFIER = 0x02;
	
	private GSTextField textField;
	private GSITextModel textModel;

	private final List<GSITextCaretListener> caretListeners;
	
	private int dot;
	private int mark;
	
	private int caretInsets;
	private int caretWidth;
	
	private long lastFrame;
	private int blinkRate;
	private int blinkTimer;
	
	private int caretColor;
	
	public GSBasicTextCaret() {
		caretListeners = new ArrayList<GSITextCaretListener>();
		
		dot = mark = 0;
		
		caretWidth = DEFAULT_CARET_WIDTH;
		caretInsets = DEFAULT_CARET_INSETS;
		
		blinkRate = DEFAULT_BLINK_RATE;
		lastFrame = -1L;
		
		caretColor = DEFAULT_CARET_COLOR;
	}
	
	@Override
	public void install(GSTextField textField) {
		if (this.textField != null)
			throw new IllegalStateException("Caret already bound!");
	
		this.textField = textField;
		
		installTextModel(textField.getTextModel());

		this.textField.addModelChangeListener(this);
	}
	
	private void installTextModel(GSITextModel textModel) {
		this.textModel = textModel;
		
		textModel.addTextModelListener(this);
		
		dot = textModel.getLength();
		mark = dot;
	}

	@Override
	public void uninstall(GSTextField textField) {
		if (this.textField == null)
			throw new IllegalStateException("Caret not bound!");
	
		this.textField.removeModelChangeListener(this);
		
		this.textField = null;
		
		uninstallTextModel(textField.getTextModel());
	}
	
	private void uninstallTextModel(GSITextModel textModel) {
		if (this.textModel != null) {
			this.textModel.removeTextModelListener(this);
			this.textModel = null;
		}
	}
	
	@Override
	public void modelChanged() {
		uninstallTextModel(textModel);
		installTextModel(textField.getTextModel());
	}
	
	@Override
	public void addTextCaretListener(GSITextCaretListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener is null!");
		
		caretListeners.add(listener);
	}

	@Override
	public void removeTextCaretListener(GSITextCaretListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener is null!");

		caretListeners.remove(listener);
	}
	
	private void dispatchCaretLocationChangedEvent() {
		for (GSITextCaretListener caretListener : caretListeners)
			caretListener.caretLocationChanged(dot, mark);
	}

	/**
	 * Navigates the model either forward or backward depending on the given
	 * {@code backward} parameter. If the parameter is true, then the navigation
	 * will be backward, otherwise it will be forward. If the caret currently
	 * has a selection, but the selectionModifier is not active, then the cursor
	 * will be set to the backward / forward location of the selection, again
	 * depending on the backward parameter.
	 * 
	 * @param backward - a parameter defining whether the navigation should be
	 *                   backward or forward.
	 * @param modifierFlags - the flags of the currently held modifiers.
	 */
	protected void navigateStep(boolean backward, int modifierFlags) {
		if (((modifierFlags & SELECTION_MODIFIER) == 0) && hasCaretSelection()) {
			if (backward) {
				setCaretLocation(Math.min(dot, mark));
			} else {
				setCaretLocation(Math.max(dot, mark));
			}
		} else if ((modifierFlags & WORD_NAVIGATION_MODIFIER) != 0) {
			navigateToNextWord(backward, modifierFlags);
		} else {
			navigateToLocation(backward ? (dot - 1) : (dot + 1), modifierFlags);
		}
	}
	
	protected void navigateToNextWord(boolean backward, int modifierFlags) {
		int nextDot;
		
		if (backward) {
			GSWordCharacterType prevType = GSWordCharacterType.OTHER;

			for (nextDot = dot; nextDot > 0; nextDot--) {
				GSWordCharacterType type = getWordCharacterTypeAt(nextDot - 1);
				if (type != prevType && prevType != GSWordCharacterType.OTHER)
					break;
				
				prevType = type;
			}
		} else {
			GSWordCharacterType prevType = getWordCharacterTypeAt(dot);

			for (nextDot = dot; nextDot < textModel.getLength(); nextDot++) {
				GSWordCharacterType type = getWordCharacterTypeAt(nextDot);
				if (type != prevType && type != GSWordCharacterType.OTHER)
					break;
				
				prevType = type;
			}
		}

		navigateToLocation(nextDot, modifierFlags);
	}
	
	private GSWordCharacterType getWordCharacterTypeAt(int location) {
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
				return GSWordCharacterType.LETTER_OR_DIGIT;
	
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
				return GSWordCharacterType.SYMBOL;
			
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
				return GSWordCharacterType.OTHER;
			}
		}

		return null;
	}
	
	/**
	 * Navigates the cursor to the given location. If the selectionModifier is
	 * currently active, this function will only set the dot location.
	 * 
	 * @param location - the new location of the caret, or the dot, if the
	 *                   selectionModifier is not active.
	 * @param modifierFlags - the flags of the currently held modifiers.
	 */
	protected void navigateToLocation(int location, int modifierFlags) {
		if ((modifierFlags & SELECTION_MODIFIER) != 0) {
			setCaretDot(location);
		} else {
			setCaretLocation(location);
		}
	}
	
	/**
	 * Navigates the caret to the specified point.
	 * 
	 * @param navX - the x-position of the navigation point
	 * @param navY - the y-position of the navigation point
	 * @param modifierFlags - the flags of the currently held modifiers.
	 */
	protected void navigateToPoint(int navX, int navY, int modifierFlags) {
		int x0 = textField.getBorderWidth();
		int x1 = textField.getWidth() - textField.getBorderWidth();
		
		int indexOffset = 0;
		if ((modifierFlags & SELECTION_MODIFIER) != 0) {
			if (navX < x0) {
				navX = x0;
				indexOffset = -1;
			} else if (navX > x1 - 1) {
				navX = x1 - 1;
				indexOffset = 1;
			}
		}
		
		if (navX >= x0 && navX < x1 && navY >= 0 && navY < textField.getHeight()) {
			int navigationIndex = textField.viewToModel(navX, navY);
			
			if (navigationIndex != -1)
				navigateToLocation(navigationIndex + indexOffset, modifierFlags);
		}
	}
	
	@Override
	public void update() {
	}
	
	@Override
	public void render(int mouseX, int mouseY, float dt) {
		long now = System.currentTimeMillis();
		if (lastFrame != -1L)
			blinkTimer += (int)Math.min(blinkRate, now - lastFrame);
		lastFrame = now;

		if (blinkTimer <= blinkRate) {
			paintCaret(mouseX, mouseY, dt);
		} else if (blinkTimer >= blinkRate << 1) {
			blinkTimer %= blinkRate << 1;
		}
	}
	
	/**
	 * Paints the graphical caret. The width and insets of the caret are specified
	 * by the methods {@link #setCaretWidth(int)} and {@link #setCaretInsets(int)}.
	 * 
	 * @param mouseX - the x-coordinate of the current mouse position
	 * @param mouseY - the y-coordinate of the current mouse position
	 * @param dt - the delta tick parameter for animation interpolation
	 */
	protected void paintCaret(int mouseX, int mouseY, float dt) {
		Rectangle bounds = textField.modelToView(dot);
		if (bounds != null) {
			int mnx = textField.getBorderWidth();
			int mxx = textField.getWidth() - textField.getBorderWidth() - caretWidth;
			
			int x0 = GSMathUtils.clamp(bounds.x, mnx, mxx);
			int y0 = bounds.y + caretInsets;
			int x1 = x0 + caretWidth;
			int y1 = y0 + bounds.height - caretInsets * 2;

			if (y0 < y1)
				DrawableHelper.fill(x0, y0, x1, y1, caretColor);
		}
	}
	
	/**
	 * Calculates a bounded location in the text area, meaning that if the given
	 * location is outside of the text model, then it will be clamped to ensure
	 * a valid caret location.
	 * 
	 * @param location - the location to be clamped within the text model bounds
	 * 
	 * @return A bounded location found by clamping the given {@code location}.
	 */
	private int getBoundedLocation(int location) {
		if (location <= 0) {
			return 0;
		} else {
			if (location > textModel.getLength())
				return textModel.getLength();
		}
		
		return location;
	}

	@Override
	public int getCaretLocation() {
		return dot;
	}
	
	@Override
	public void setCaretLocation(int location) {
		location = getBoundedLocation(location);
		
		if (location != dot || location != mark) {
			dot = mark = location;

			blinkTimer = 0;
			
			dispatchCaretLocationChangedEvent();
		}
	}
	
	@Override
	public int getCaretDot() {
		return dot;
	}
	
	@Override
	public void setCaretDot(int dot) {
		dot = getBoundedLocation(dot);
		
		if (dot != this.dot) {
			this.dot = dot;

			blinkTimer = 0;
			
			dispatchCaretLocationChangedEvent();
		}
	}

	@Override
	public int getCaretMark() {
		return mark;
	}
	
	@Override
	public void setCaretMark(int mark) {
		mark = getBoundedLocation(mark);
		
		if (mark != this.mark) {
			this.mark = mark;

			dispatchCaretLocationChangedEvent();
		}
	}

	/**
	 * Sets the selection of the caret to the specified dot and mark locations.
	 * 
	 * @param dot - the dot at which the selection ends
	 * @param mark - the mark at which the selection begins.
	 */
	private void setSelection(int dot, int mark) {
		dot = getBoundedLocation(dot);
		mark = getBoundedLocation(mark);
	
		if (dot != this.dot || mark != this.mark) {
			if (dot != this.dot)
				blinkTimer = 0;

			this.dot = dot;
			this.mark = mark;
			
			dispatchCaretLocationChangedEvent();
		}
	}
	
	@Override
	public boolean hasCaretSelection() {
		return (dot != mark);
	}

	@Override
	public void textInserted(GSITextModel model, int offset, int count) {
		int dot = this.dot;
		int mark = this.mark;
		
		if (offset <= dot)
			dot += count;
		if (offset <= mark)
			mark += count;
	
		setSelection(dot, mark);
	}

	@Override
	public void textRemoved(GSITextModel model, int offset, int count) {
		int dot = this.dot;
		int mark = this.mark;

		if (offset == dot)
			blinkTimer = 0;
		
		if (offset + count < dot) {
			dot -= count;
		} else if (offset < dot) {
			dot = offset;
		}

		if (offset + count < mark) {
			mark -= count;
		} else if (offset < mark) {
			mark = offset;
		}

		setSelection(dot, mark);
	}

	@Override
	public int getBlinkRate() {
		return blinkRate;
	}
	
	/**
	 * Sets the blink rate to the specified amount of milliseconds. A full cycle
	 * of the caret blinking will be double the amount given in this method. The
	 * default value of this parameter is {@code 500}ms.
	 * 
	 * @param blinkRate - the new blink rate of this caret in milliseconds.
	 * 
	 * @throws IllegalArgumentException if the blinkRate is non-positive.
	 */
	@Override
	public void setBlinkRate(int blinkRate) {
		if (blinkRate <= 0)
			throw new IllegalArgumentException("blinkRate <= 0");
		
		this.blinkRate = blinkRate;
		
		blinkTimer = 0;
	}

	/**
	 * @return The width of the graphical caret.
	 */
	public int getCaretWidth() {
		return caretWidth;
	}
	
	/**
	 * Sets the graphical width of the caret to the specified width. The default
	 * value of this parameter is {@code 2}.
	 * 
	 * @param width - the new width of the graphical caret.
	 * 
	 * @throws IllegalArgumentException if the given {@code width} is negative.
	 */
	public void setCaretWidth(int width) {
		if (width < 0)
			throw new IllegalArgumentException("Caret width is negative!");

		caretWidth = width;
	}
	
	/**
	 * @return The insets of the graphical caret.
	 */
	public int getCaretInsets() {
		return caretInsets;
	}
	
	/**
	 * Sets the caret insets to the specified amount. The insets define the
	 * amount of pixels on the top and bottom of the caret that wont be rendered
	 * when painting the caret. If one wishes to disable these insets, they
	 * should set this value to zero. If this value is too large, the caret will
	 * not be rendered. The default value of this parameter is {@code 0}.
	 * 
	 * @param insets - a non-negative integer defining the new caret insets
	 */
	public void setCaretInsets(int insets) {
		if (insets < 0)
			throw new IllegalArgumentException("Caret insets are negative!");
		
		caretInsets = insets;
	}
	
	public int getCaretColor() {
		return caretColor;
	}

	public void setCaretColor(int caretColor) {
		this.caretColor = caretColor;
	}

	@Override
	public boolean onMouseClicked(double mouseX, double mouseY, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			int mods = ((GSIMouseAccess)MinecraftClient.getInstance().mouse).getButtonMods();
			
			navigateToPoint((int)mouseX, (int)mouseY, getModifierFlags(mods));
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean onMouseReleased(double mouseX, double mouseY, int button) {
		return false;
	}
	
	@Override
	public boolean onMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			int mods = ((GSIMouseAccess)MinecraftClient.getInstance().mouse).getButtonMods();

			int y = GSMathUtils.clamp((int)mouseY, 0, textField.getHeight() - 1);
			navigateToPoint((int)mouseX, y, getModifierFlags(mods) | SELECTION_MODIFIER);
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean onKeyPressed(int key, int scancode, int mods) {
		return handleKeyEvent(key, mods, getModifierFlags(mods));
	}

	@Override
	public boolean onKeyReleased(int key, int scancode, int mods) {
		return false;
	}
	
	private int getModifierFlags(int mods) {
		int flags = NO_MODIFIERS;

		if ((mods & GLFW.GLFW_MOD_SHIFT) != 0)
			flags |= SELECTION_MODIFIER;
		
		if ((mods & GLFW.GLFW_MOD_CONTROL) != 0)
			flags |= WORD_NAVIGATION_MODIFIER;
		
		return flags;
	}
	
	private boolean handleKeyEvent(int key, int mods, int modifierFlags) {
		if (Screen.isSelectAll(key)) {
			setSelection(0, textModel.getLength());
			return true;
		} else {
			switch (key) {
			case GLFW.GLFW_KEY_RIGHT:
				navigateStep(false, modifierFlags);
				return true;
			case GLFW.GLFW_KEY_LEFT:
				navigateStep(true, modifierFlags);
				return true;
			case GLFW.GLFW_KEY_HOME:
				navigateToLocation(0, modifierFlags);
				return true;
			case GLFW.GLFW_KEY_END:
				navigateToLocation(textModel.getLength(), modifierFlags);
				return true;
			default:
				break;
			}
		}
		
		return false;
	}
	
	private enum GSWordCharacterType {
		
		LETTER_OR_DIGIT, SYMBOL, OTHER;
		
	}
}
