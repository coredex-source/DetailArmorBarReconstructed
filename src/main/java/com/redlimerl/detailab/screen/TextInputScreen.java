package com.redlimerl.detailab.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class TextInputScreen extends Screen {
    private final Screen parent;
    private final Text fieldLabel;
    private final String initialValue;
    private final Consumer<Integer> onValueChanged;
    private TextFieldWidget textField;

    public TextInputScreen(Screen parent, Text fieldLabel, String initialValue, Consumer<Integer> onValueChanged) {
        super(fieldLabel);
        this.parent = parent;
        this.fieldLabel = fieldLabel;
        this.initialValue = initialValue;
        this.onValueChanged = onValueChanged;
    }

    @Override
    protected void init() {
        super.init();

        // Create text field
        this.textField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, this.height / 2 - 10, 200, 20, Text.empty());
        this.textField.setText(initialValue);
        this.textField.setMaxLength(8); // Allow for larger numbers like -9999999
        
        // Set text predicate to only allow numbers and minus sign
        this.textField.setTextPredicate(text -> {
            if (text.isEmpty()) return true;
            try {
                Integer.parseInt(text);
                return true;
            } catch (NumberFormatException e) {
                // Allow just a minus sign at the beginning
                return text.equals("-");
            }
        });
        
        this.addSelectableChild(this.textField);
        this.setInitialFocus(this.textField);

        // Done button
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
            String text = this.textField.getText();
            if (text.isEmpty() || text.equals("-")) {
                // Empty or just minus, treat as 0
                this.onValueChanged.accept(0);
                this.close();
            } else {
                try {
                    int value = Integer.parseInt(text);
                    this.onValueChanged.accept(value);
                    this.close();
                } catch (NumberFormatException e) {
                    // This shouldn't happen with our text predicate, but just in case
                    this.textField.setText(initialValue);
                }
            }
        }).dimensions(this.width / 2 - 102, this.height / 2 + 20, 100, 20).build());

        // Cancel button
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> {
            this.close();
        }).dimensions(this.width / 2 + 2, this.height / 2 + 20, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        // Draw label
        context.drawCenteredTextWithShadow(this.textRenderer, this.fieldLabel, this.width / 2, this.height / 2 - 35, 16777215);
        
        // Draw text field
        this.textField.render(context, mouseX, mouseY, delta);
        
        // Draw instructions
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Enter a number (positive or negative)"), this.width / 2, this.height / 2 + 50, 11184810);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC key
            this.close();
            return true;
        }
        if (keyCode == 257 || keyCode == 335) { // ENTER or NUMPAD_ENTER
            String text = this.textField.getText();
            if (text.isEmpty() || text.equals("-")) {
                // Empty or just minus, treat as 0
                this.onValueChanged.accept(0);
                this.close();
                return true;
            } else {
                try {
                    int value = Integer.parseInt(text);
                    this.onValueChanged.accept(value);
                    this.close();
                    return true;
                } catch (NumberFormatException e) {
                    // This shouldn't happen with our text predicate, but just in case
                    this.textField.setText(initialValue);
                    return true;
                }
            }
        }
        return this.textField.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return this.textField.charTyped(chr, modifiers) || super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}