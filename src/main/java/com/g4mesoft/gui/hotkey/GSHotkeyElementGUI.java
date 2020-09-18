package com.g4mesoft.gui.hotkey;

import com.g4mesoft.gui.GSIElement;
import com.g4mesoft.gui.GSParentPanel;
import com.g4mesoft.gui.action.GSButtonPanel;
import com.g4mesoft.gui.event.GSIKeyListener;
import com.g4mesoft.gui.event.GSIMouseListener;
import com.g4mesoft.gui.event.GSKeyEvent;
import com.g4mesoft.gui.event.GSMouseEvent;
import com.g4mesoft.gui.renderer.GSIRenderer2D;
import com.g4mesoft.hotkey.GSKeyBinding;

import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Key;
import net.minecraft.util.Formatting;

public class GSHotkeyElementGUI extends GSParentPanel implements GSIMouseListener, GSIKeyListener {

	private static final int HOTKEY_HEIGHT = 20;
	private static final int HOTKEY_PADDING = 2;
	private static final int MAX_NAME_WIDTH = 128;
	
	private static final int MODIFY_BUTTON_WIDTH = 96;
	private static final int RESET_BUTTON_WIDTH = 48;
	
	private static final int FONT_COLOR = 0xFFFFFFFF;
	private static final int HOVERED_BACKGROUND = 0x80000000;//0x66EDEDFF;
	
	private static final String RESET_TEXT = "gui.hotkey.reset";
	private static final String CANCEL_TEXT = "gui.hotkey.cancel";
	
	private final GSHotkeyGUI hotkeyGui;
	private final GSKeyBinding keyBinding;
	
	private final String nameTranslationKey;
	private String localizedKeyCodeName;

	private boolean modifyingKeyCode;
	private final GSButtonPanel modifyButton;
	private final GSButtonPanel resetButton;
	
	public GSHotkeyElementGUI(GSHotkeyGUI hotkeyGui, GSKeyBinding keyBinding) {
		this.hotkeyGui = hotkeyGui;
		this.keyBinding = keyBinding;
		
		nameTranslationKey = "hotkey." + keyBinding.getCategory() + "." + keyBinding.getName();
	
		resetButton = new GSButtonPanel(RESET_TEXT, () -> {
			if (modifyingKeyCode) {
				setModifying(false);
			} else if (hotkeyGui.getChangingElement() == null) {
				resetKeyCode();
			}
		});
		
		modifyButton = new GSButtonPanel("", () -> setModifying(true));
		
		add(resetButton);
		add(modifyButton);
	}

	@Override
	public void onBoundsChanged() {
		super.onBoundsChanged();

		localizedKeyCodeName = keyBinding.getLocalizedName();
		
		int by = HOTKEY_PADDING;
		int rbx = width - RESET_BUTTON_WIDTH - HOTKEY_PADDING;
		int mbx = rbx - MODIFY_BUTTON_WIDTH - HOTKEY_PADDING;

		resetButton.setBounds(rbx, by, RESET_BUTTON_WIDTH, HOTKEY_HEIGHT);
		modifyButton.setBounds(mbx, by, MODIFY_BUTTON_WIDTH, HOTKEY_HEIGHT);

		updateButtons();
	}
	
	@Override
	public void onAdded(GSIElement parent) {
		super.onAdded(parent);

		parent.addMouseEventListener(this);
		parent.addKeyEventListener(this);
	}

	@Override
	public void onRemoved(GSIElement parent) {
		super.onAdded(parent);
		
		parent.removeMouseEventListener(this);
		parent.removeKeyEventListener(this);
	}
	
	private void updateButtons() {
		updateResetButton();
		updateModifyButton();
	}
	
	private void updateResetButton() {
		resetButton.setEnabled(modifyingKeyCode || !keyBinding.getKeyCode().equals(keyBinding.getDefaultKeyCode()));
		
		if (modifyingKeyCode) {
			resetButton.setTranslationKey(CANCEL_TEXT);
		} else {
			resetButton.setTranslationKey(RESET_TEXT);
		}
	}
	
	private void updateModifyButton() {
		if (modifyingKeyCode) {
			modifyButton.setLiteralText("> " + Formatting.YELLOW + localizedKeyCodeName + Formatting.RESET + " <");
		} else {
			modifyButton.setLiteralText(localizedKeyCodeName);
		}
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		if (renderer.isMouseInside(0, 0, width, height))
			renderer.fillRect(0, 0, width, height, HOVERED_BACKGROUND);

		super.render(renderer);

		int ty = (height - renderer.getFontHeight()) / 2;
		renderer.drawString(i18nTranslate(nameTranslationKey), HOTKEY_PADDING, ty, FONT_COLOR);
	}

	private void setKeyCode(Key keyCode) {
		keyBinding.setKeyCode(keyCode);
		localizedKeyCodeName = keyBinding.getLocalizedName();
		updateButtons();
	}

	public void resetKeyCode() {
		setKeyCode(keyBinding.getDefaultKeyCode());
	}

	public void unbindKeyCode() {
		setKeyCode(InputUtil.UNKNOWN_KEY);
	}
	
	private void setModifying(boolean modifying) {
		GSHotkeyElementGUI changingElement = hotkeyGui.getChangingElement();
		
		if (modifying && changingElement == null) {
			modifyingKeyCode = true;
			hotkeyGui.setChangingElement(this);
			updateButtons();
		} else if (!modifying && changingElement == this) {
			modifyingKeyCode = false;
			hotkeyGui.setChangingElement(null);
			updateButtons();
		}

		modifyButton.setPassingEvents(modifyingKeyCode);
	}

	@Override
	public void mousePressed(GSMouseEvent event) {
		if (modifyingKeyCode) {
			setKeyCode(InputUtil.Type.MOUSE.createFromCode(event.getButton()));
			setModifying(false);
			event.consume();
		}
	}
	
	@Override
	public void keyPressed(GSKeyEvent event) {
		if (modifyingKeyCode) {
			if (event.getKeyCode() == GSKeyEvent.KEY_ESCAPE) {
				unbindKeyCode();
			} else {
				setKeyCode(InputUtil.fromKeyCode(event.getKeyCode(), event.getScanCode()));
			}
			
			setModifying(false);
			event.consume();
		}
	}
	
	public int getPreferredHeight() {
		return HOTKEY_HEIGHT + HOTKEY_PADDING * 2;
	}

	public int getPreferredWidth() {
		return MAX_NAME_WIDTH + MODIFY_BUTTON_WIDTH + RESET_BUTTON_WIDTH + HOTKEY_PADDING * 4;
	}
}
