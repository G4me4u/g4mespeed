package com.g4mesoft.gui.hotkey;

import org.lwjgl.glfw.GLFW;

import com.g4mesoft.gui.GSParentPanel;
import com.g4mesoft.hotkey.GSKeyBinding;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.InputUtil.KeyCode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class GSHotkeyElementGUI extends GSParentPanel {

	private static final int HOTKEY_HEIGHT = 20;
	private static final int HOTKEY_PADDING = 2;
	private static final int MAX_NAME_WIDTH = 128;
	
	private static final int MODIFY_BUTTON_WIDTH = 96;
	private static final int RESET_BUTTON_WIDTH = 48;
	
	private static final int FONT_COLOR = 0xFFFFFFFF;
	private static final int HOVERED_BACKGROUND = 0x80000000;//0x66EDEDFF;
	
	private static final String RESET_TEXT = "gui.hotkey.button.reset";
	private static final String CANCEL_TEXT = "gui.hotkey.button.cancel";
	
	private final GSHotkeyGUI hotkeyGui;
	private final GSKeyBinding keyBinding;
	
	private final String keyName;

	private boolean modifyingKeyCode;
	private ButtonWidget modifyButton;
	private ButtonWidget resetButton;
	
	public GSHotkeyElementGUI(GSHotkeyGUI hotkeyGui, GSKeyBinding keyBinding) {
		this.hotkeyGui = hotkeyGui;
		this.keyBinding = keyBinding;
		
		this.keyName = "hotkey." + keyBinding.getCategory() + "." + keyBinding.getName();
	}

	@Override
	public void init() {
		super.init();

		int by = HOTKEY_PADDING;
		int rbx = width - RESET_BUTTON_WIDTH - HOTKEY_PADDING;
		int mbx = rbx - MODIFY_BUTTON_WIDTH - HOTKEY_PADDING;

		resetButton = new ButtonWidget(rbx, by, RESET_BUTTON_WIDTH, HOTKEY_HEIGHT, LiteralText.EMPTY, b -> {
			if (modifyingKeyCode) {
				setModifying(false);
			} else if (hotkeyGui.getChangingElement() == null) {
				setKeyCode(keyBinding.getDefaultKeyCode());
			}
		});

		modifyButton = new ButtonWidget(mbx, by, MODIFY_BUTTON_WIDTH, HOTKEY_HEIGHT, LiteralText.EMPTY, b -> {
			setModifying(true);
		});

		addWidget(resetButton);
		addWidget(modifyButton);
		updateButtons();
	}
	
	private void updateButtons() {
		updateResetButton();
		updateModifyButton();
	}
	
	private void updateResetButton() {
		if (resetButton == null)
			return;
		
		resetButton.active = modifyingKeyCode || !keyBinding.getKeyCode().equals(keyBinding.getDefaultKeyCode());
		
		if (modifyingKeyCode) {
			resetButton.setMessage(new TranslatableText(CANCEL_TEXT));
		} else {
			resetButton.setMessage(new TranslatableText(RESET_TEXT));
		}
	}
	
	private void updateModifyButton() {
		if (modifyButton == null)
			return;
		
		Text name = keyBinding.getLocalizedName();
		if (modifyingKeyCode) {
			modifyButton.setMessage(new LiteralText("> ").append(name.shallowCopy().formatted(Formatting.YELLOW)).append(" <"));
		} else {
			modifyButton.setMessage(name);
		}
	}
	
	@Override
	protected void renderTranslated(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (mouseX >= 0 && mouseY >= 0 && mouseX < width && mouseY < height)
			fill(matrixStack, 0, 0, width, height, HOVERED_BACKGROUND);

		super.renderTranslated(matrixStack, mouseX, mouseY, partialTicks);

		String name = getTranslationModule().getTranslation(keyName);
		drawStringWithShadow(matrixStack, textRenderer, name, HOTKEY_PADDING, (height - textRenderer.fontHeight) / 2, FONT_COLOR);
	}

	private void setKeyCode(KeyCode keyCode) {
		keyBinding.setKeyCode(keyCode);
		updateButtons();
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
	}
	
	@Override
	public boolean onKeyPressedGS(int key, int scancode, int mods) {
		if (modifyingKeyCode && !resetButton.isFocused()) {
			if (key == GLFW.GLFW_KEY_ESCAPE) {
				setKeyCode(InputUtil.UNKNOWN_KEYCODE);
			} else {
				setKeyCode(InputUtil.getKeyCode(key, scancode));
			}
			
			setModifying(false);
			return true;
		}
		
		return super.onKeyPressedGS(key, scancode, mods);
	}

	@Override
	public boolean onMouseClickedGS(double mouseX, double mouseY, int button) {
		if (modifyingKeyCode && !resetButton.isHovered()) {
			setKeyCode(InputUtil.Type.MOUSE.createFromCode(button));
			setModifying(false);
			return true;
		}
		
		return super.onMouseClickedGS(mouseX, mouseY, button);
	}
	
	public int getPreferredHeight() {
		return HOTKEY_HEIGHT + HOTKEY_PADDING * 2;
	}

	public int getPreferredWidth() {
		return MAX_NAME_WIDTH + MODIFY_BUTTON_WIDTH + RESET_BUTTON_WIDTH + HOTKEY_PADDING * 4;
	}
}
