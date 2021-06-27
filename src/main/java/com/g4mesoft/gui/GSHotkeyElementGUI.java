package com.g4mesoft.gui;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.g4mesoft.hotkey.GSKeyBinding;
import com.g4mesoft.hotkey.GSKeyCode;
import com.g4mesoft.panel.GSPanel;
import com.g4mesoft.panel.GSParentPanel;
import com.g4mesoft.panel.event.GSIKeyListener;
import com.g4mesoft.panel.event.GSIMouseListener;
import com.g4mesoft.panel.event.GSKeyEvent;
import com.g4mesoft.panel.event.GSMouseEvent;
import com.g4mesoft.panel.legend.GSButtonPanel;
import com.g4mesoft.renderer.GSIRenderer2D;

import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.Key;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class GSHotkeyElementGUI extends GSParentPanel implements GSIMouseListener, GSIKeyListener {

	private static final int HOTKEY_HEIGHT = 20;
	private static final int HOTKEY_PADDING = 2;
	private static final int MAX_NAME_WIDTH = 128;
	
	private static final int MODIFY_BUTTON_WIDTH = 160;
	private static final int RESET_BUTTON_WIDTH = 48;
	
	private static final int FONT_COLOR = 0xFFFFFFFF;
	private static final int HOVERED_BACKGROUND = 0x80000000;//0x66EDEDFF;
	
	private static final Text RESET_TEXT = new TranslatableText("gui.hotkey.reset");
	private static final Text CANCEL_TEXT = new TranslatableText("gui.hotkey.cancel");
	
	private final GSHotkeyGUI hotkeyGui;
	private final GSKeyBinding keyBinding;
	
	private final Text nameText;

	private final GSButtonPanel modifyButton;
	private final GSButtonPanel resetButton;

	private boolean modifyingKeyCode;
	private final Set<InputUtil.Key> pressedKeys;
	private final Set<InputUtil.Key> activeKeys;
	
	public GSHotkeyElementGUI(GSHotkeyGUI hotkeyGui, GSKeyBinding keyBinding) {
		this.hotkeyGui = hotkeyGui;
		this.keyBinding = keyBinding;
		
		nameText = new TranslatableText("hotkey." + keyBinding.getCategory() + "." + keyBinding.getName());
	
		resetButton = new GSButtonPanel(RESET_TEXT, () -> {
			if (modifyingKeyCode) {
				stopModifying();
			} else if (hotkeyGui.getChangingElement() == null) {
				resetKeyCode();
			}
		});
		modifyButton = new GSButtonPanel("", () -> startModifying());
		
		add(resetButton);
		add(modifyButton);

		modifyingKeyCode = false;
		pressedKeys = new LinkedHashSet<>();
		activeKeys = new HashSet<>();
	}

	@Override
	public void layout() {
		int by = HOTKEY_PADDING;
		int rbx = width - RESET_BUTTON_WIDTH - HOTKEY_PADDING;
		int mbx = rbx - MODIFY_BUTTON_WIDTH - HOTKEY_PADDING;

		resetButton.setBounds(rbx, by, RESET_BUTTON_WIDTH, HOTKEY_HEIGHT);
		modifyButton.setBounds(mbx, by, MODIFY_BUTTON_WIDTH, HOTKEY_HEIGHT);

		updateButtons();
	}
	
	@Override
	public void onAdded(GSPanel parent) {
		super.onAdded(parent);

		parent.addMouseEventListener(this);
		parent.addKeyEventListener(this);
	}

	@Override
	public void onRemoved(GSPanel parent) {
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
			resetButton.setText(CANCEL_TEXT);
		} else {
			resetButton.setText(RESET_TEXT);
		}
	}
	
	private void updateModifyButton() {
		Text keyName = keyBinding.getLocalizedName();
		
		if (modifyingKeyCode) {
			keyName = keyName.shallowCopy().formatted(Formatting.YELLOW);

			modifyButton.setText(new LiteralText("> ").append(keyName).append(" <"));
		} else {
			modifyButton.setText(keyName);
		}
	}
	
	@Override
	public void render(GSIRenderer2D renderer) {
		if (renderer.isMouseInside(0, 0, width, height))
			renderer.fillRect(0, 0, width, height, HOVERED_BACKGROUND);

		super.render(renderer);

		int ty = (height - renderer.getTextHeight() + 1) / 2;
		renderer.drawText(nameText, HOTKEY_PADDING, ty, FONT_COLOR);
	}

	private void setKeyCode(GSKeyCode keyCode) {
		keyBinding.setKeyCode(keyCode);
		updateButtons();
	}

	public void resetKeyCode() {
		setKeyCode(keyBinding.getDefaultKeyCode());
	}

	public void unbindKeyCode() {
		setKeyCode(GSKeyCode.UNKNOWN_KEY);
	}
	
	private void startModifying() {
		if (hotkeyGui.getChangingElement() == null) {
			modifyingKeyCode = true;
			hotkeyGui.setChangingElement(this);
			updateButtons();
		}

		modifyButton.setPassingEvents(modifyingKeyCode);
	}
	
	private void stopModifying() {
		modifyingKeyCode = false;
		pressedKeys.clear();
		activeKeys.clear();

		modifyButton.setPassingEvents(false);

		if (hotkeyGui.getChangingElement() == this) {
			hotkeyGui.setChangingElement(null);
			updateButtons();
		}
	}

	@Override
	public void mousePressed(GSMouseEvent event) {
		if (modifyingKeyCode) {
			onKeyPressed(InputUtil.Type.MOUSE.createFromCode(event.getButton()));
			event.consume();
		}
	}
	
	@Override
	public void mouseReleased(GSMouseEvent event) {
		if (modifyingKeyCode) {
			onKeyReleased(InputUtil.Type.MOUSE.createFromCode(event.getButton()));
			event.consume();
		}
	}
	
	@Override
	public void keyPressed(GSKeyEvent event) {
		if (modifyingKeyCode) {
			if (event.getKeyCode() == GSKeyEvent.KEY_ESCAPE) {
				unbindKeyCode();
				stopModifying();
			} else {
				onKeyPressed(InputUtil.fromKeyCode(event.getKeyCode(), event.getScanCode()));
			}
			event.consume();
		}
	}

	@Override
	public void keyReleased(GSKeyEvent event) {
		if (modifyingKeyCode) {
			onKeyReleased(InputUtil.fromKeyCode(event.getKeyCode(), event.getScanCode()));
			event.consume();
		}
	}
	
	private void onKeyPressed(Key key) {
		pressedKeys.add(key);
		activeKeys.add(key);
	}

	private void onKeyReleased(Key key) {
		if (activeKeys.remove(key) && activeKeys.isEmpty() && !pressedKeys.isEmpty()) {
			setKeyCode(GSKeyCode.fromKeys(pressedKeys.toArray(new InputUtil.Key[0])));
			stopModifying();
		}
	}
	
	public int getPreferredHeight() {
		return HOTKEY_HEIGHT + HOTKEY_PADDING * 2;
	}

	public int getPreferredWidth() {
		return MAX_NAME_WIDTH + MODIFY_BUTTON_WIDTH + RESET_BUTTON_WIDTH + HOTKEY_PADDING * 4;
	}
}
