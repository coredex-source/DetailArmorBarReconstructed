package com.redlimerl.detailab.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.KeyEvent;

import java.util.function.Consumer;

public class TextInputScreen extends Screen {
    private final Screen parent;
    private final Component fieldLabel;
    private final String initialValue;
    private final Consumer<Integer> onValueChanged;
    private EditBox textField;

    public TextInputScreen(Screen parent, Component fieldLabel, String initialValue, Consumer<Integer> onValueChanged) {
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
        this.textField = new EditBox(this.font, this.width / 2 - 100, this.height / 2 - 10, 200, 20, Component.empty());
        this.textField.setValue(initialValue);
        this.textField.setMaxLength(8); // Allow for larger numbers like -9999999
        
        this.addWidget(this.textField);
        this.setInitialFocus(this.textField);

        // Done button
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            this.submitValue();
        }).bounds(this.width / 2 - 102, this.height / 2 + 20, 100, 20).build());

        // Cancel button
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
            this.onClose();
        }).bounds(this.width / 2 + 2, this.height / 2 + 20, 100, 20).build());
    }

    @Override
    //? if >=26.4 {
    /*public void extractRenderState(GuiGraphicsExtractor context) {
        super.extractRenderState(context);
    *///?} else {
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
    //?}

        // Draw label
        context.centeredText(this.font, this.fieldLabel, this.width / 2, this.height / 2 - 35, 16777215);
        
        // Draw text field
        //? if >=26.4 {
        /*this.textField.extractRenderState(context);
        *///?} else {
        this.textField.extractRenderState(context, mouseX, mouseY, delta);
        //?}
        
        // Draw instructions
        context.centeredText(this.font, Component.literal("Enter a number (positive or negative)"), this.width / 2, this.height / 2 + 50, 11184810);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isConfirmation()) {
            this.submitValue();
            return true;
        }
        return super.keyPressed(event);
    }

    private void submitValue() {
        String text = this.textField.getValue();
        if (text.isEmpty() || text.equals("-")) {
            // Empty or just minus, treat as 0
            this.onValueChanged.accept(0);
            this.onClose();
        } else {
            try {
                int value = Integer.parseInt(text);
                this.onValueChanged.accept(value);
                this.onClose();
            } catch (NumberFormatException e) {
                // This shouldn't happen with our text predicate, but just in case
                this.textField.setValue(initialValue);
            }
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            //? if minecraft_26_1 {
            /*this.minecraft.setScreen(this.parent);
            *///?} else {
            this.minecraft.gui.setScreen(this.parent);
            //?}
        }
    }
}
