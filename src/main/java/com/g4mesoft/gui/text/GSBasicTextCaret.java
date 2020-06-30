package com.g4mesoft.gui.text;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.g4mesoft.access.GSIMouseAccess;
import com.g4mesoft.util.GSMathUtils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;

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
 * elsewhere. To see the full functionality of the {@code mark} and how it is
 * activated by the user see {@link #setSelectionModifierKey(KeyInput)}.
 * 
 * @author Christian
 */
public class GSBasicTextCaret implements GSITextCaret, GSITextModelListener, GSIModelChangeListener {

	private static final int DEFAULT_BLINK_RATE = 500;
	private static final int DEFAULT_CARET_WIDTH = 2;
	private static final int DEFAULT_CARET_INSETS = 0;
	
	private static final int DEFAULT_CARET_COLOR = 0xFFFFFFFF;
	
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
	 * @param selectionModifier - whether the selection modifier is held.
	 */
	protected void navigateStep(boolean backward, boolean selectionModifier) {
		if (!selectionModifier && hasCaretSelection()) {
			if (backward) {
				setCaretLocation(Math.min(dot, mark));
			} else {
				setCaretLocation(Math.max(dot, mark));
			}
		} else {
			navigateToLocation(backward ? (dot - 1) : (dot + 1), selectionModifier);
		}
	}
	
	/**
	 * Navigates the cursor to the given location. If the selectionModifier is
	 * currently active, this function will only set the dot location.
	 * 
	 * @param location - the new location of the caret, or the dot, if the
	 *                   selectionModifier is not active.
	 * @param selectionModifier - whether the selection modifier is held.
	 */
	protected void navigateToLocation(int location, boolean selectionModifier) {
		if (selectionModifier) {
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
	 */
	protected void navigateToPoint(int navX, int navY, boolean selectionModifier) {
		int x0 = textField.getBorderWidth();
		int x1 = textField.getWidth() - textField.getBorderWidth();
		
		int indexOffset = 0;
		if (selectionModifier) {
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
				navigateToLocation(navigationIndex + indexOffset, selectionModifier);
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
			int x0 = textField.getBorderWidth();
			int x1 = textField.getWidth() - textField.getBorderWidth();
			
			int x = GSMathUtils.clamp(bounds.x, x0, x1 - caretWidth);
			int y = bounds.y + caretInsets;

			int height = bounds.height - caretInsets * 2;
			if (height > 0)
				DrawableHelper.fill(x, y, caretWidth, height, caretColor);
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
			GSITextModel model = textField.getTextModel();
			if (location > model.getLength())
				return model.getLength();
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

	public boolean onMouseClicked(double mouseX, double mouseY, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			int mods = ((GSIMouseAccess)MinecraftClient.getInstance().mouse).getButtonMods();
			navigateToPoint((int)mouseX, (int)mouseY, (mods & GLFW.GLFW_MOD_SHIFT) != 0);
			return true;
		}
		
		return false;
	}
	
	public boolean onMouseReleased(double mouseX, double mouseY, int button) {
		return false;
	}
	
	public boolean onMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			int y = GSMathUtils.clamp((int)mouseY, 0, textField.getHeight() - 1);
			navigateToPoint((int)mouseX, y, true);
			return true;
		}
		
		return false;
	}
	
	public boolean onKeyPressed(int key, int scancode, int mods) {
		return handleKeyEvent(key, mods, (mods & GLFW.GLFW_MOD_SHIFT) != 0);
	}

	public boolean onKeyReleased(int key, int scancode, int mods) {
		return false;
	}
	
	private boolean handleKeyEvent(int key, int mods, boolean selectionModifier) {
		switch (key) {
		case GLFW.GLFW_KEY_RIGHT:
			navigateStep(false, selectionModifier);
			return true;
		case GLFW.GLFW_KEY_LEFT:
			navigateStep(true, selectionModifier);
			return true;
		case GLFW.GLFW_KEY_HOME:
			navigateToLocation(0, selectionModifier);
			return true;
		case GLFW.GLFW_KEY_END:
			navigateToLocation(textModel.getLength(), selectionModifier);
			return true;
		case GLFW.GLFW_KEY_A:
			if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) {
				setSelection(0, textModel.getLength());
				return true;
			}
		default:
			break;
		}
		
		return false;
	}
}
