package net.pl3x.commandbinds.gui;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.pl3x.commandbinds.CommandBinds;
import net.pl3x.commandbinds.configuration.Config;
import net.pl3x.keyboard.Key;
import net.pl3x.keyboard.Keyboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandKey extends Key implements Tickable {
    private static final AtomicInteger NEXT_ID = new AtomicInteger(0);

    public final List<GuiEventListener> children = new ArrayList<>();

    private String command;
    private EditBox editbox;
    private String lastEditboxValue;
    private CustomButton removeBtn;
    private int keycode;

    public CommandKey(int keycode, @NotNull String command) {
        this(keycode, false, false, false, false, command);
    }

    public CommandKey(int keycode, boolean alt, boolean ctrl, boolean meta, boolean shift, @NotNull String command) {
        super(
                CommandBinds.CATEGORY_TITLE,
                String.format(CommandBinds.KEY_NAME, NEXT_ID.getAndIncrement()),
                keycode, alt, ctrl, meta, shift
        );
        this.keycode = keycode;
        this.command = command;
    }

    public int getKeyCode() {
        return this.keycode;
    }

    @Override
    public void setKey(@Nullable InputConstants.Key key) {
        this.keycode = key == null ? -1 : key.getValue();
        super.setKey(key);
    }

    @Override
    public @NotNull Action getAction() {
        return client -> {
            if (client.player != null) {
                client.player.connection.sendCommand(getCommand());
            }
        };
    }

    public @NotNull String getCommand() {
        return this.command;
    }

    public void setCommand(@NotNull String command) {
        Preconditions.checkNotNull(command, "Command cannot be null");
        this.command = command;
    }

    public void execute(@NotNull Minecraft client) {
        super.execute(client);
    }

    public void tickable$init() {
        this.removeBtn = new CustomButton(0, 0, 16, 16, 16, 0, 32, 16, Component.empty(), btn -> {
            Config.COMMAND_KEYBINDS.removeIf(key -> key.getName().equals(getName()));
            Config.save();
            Keyboard.unbindKey(this);
            CommandBinds.refreshKeyBindScreen();
        });
        this.removeBtn.setTooltip(Tooltip.create(Component.translatable("commandbinds.button.remove")));

        this.editbox = new EditBox(Minecraft.getInstance().font, 0, 0, 0, 0, Component.empty());
        this.editbox.setMaxLength(1024);
        this.editbox.setValue(this.lastEditboxValue);
        this.editbox.setResponder(value -> {
            if (Objects.equals(value, this.lastEditboxValue)) {
                return;
            }
            this.lastEditboxValue = value;
            setCommand(value.startsWith("/") ? value.substring(1) : value);
            Config.save();
        });

        this.children.clear();
        this.children.add(this.removeBtn);
        this.children.add(this.editbox);
    }

    public @NotNull List<GuiEventListener> children() {
        return this.children;
    }

    public void tickable$tick() {
        if (this.editbox != null) {
            this.editbox.tick();
        }
    }

    public void render(@NotNull GuiGraphics gfx, int top, int left, int width, int height, int mouseX, int mouseY, float delta) {
        if (this.editbox != null) {
            this.editbox.setX(left + 90 - width);
            this.editbox.setY((top + height / 2) - 6);
            this.editbox.setWidth(width + 3);
            this.editbox.height = height;
            this.editbox.setValue(String.format("/%s", getCommand()));
            this.editbox.render(gfx, mouseX, mouseY, delta);
        }

        this.removeBtn.setX(left + 67 - width);
        this.removeBtn.setY(top + 3);
        this.removeBtn.render(gfx, mouseX, mouseY, delta);
    }
}
