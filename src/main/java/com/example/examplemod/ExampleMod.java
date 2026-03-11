package com.example.examplemod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
@Mod(ExampleMod.MODID)
public class ExampleMod {
    public static final String MODID = "examplemod";
    public ExampleMod() {
    }
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        public static final KeyMapping OPEN_RADIAL_MENU = new KeyMapping(
                "key.examplemod.open_radial_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "key.categories.examplemod"
        );
        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(OPEN_RADIAL_MENU);
        }
    }
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) {
                return;
            }
            while (ClientModEvents.OPEN_RADIAL_MENU.consumeClick()) {
                if (!(minecraft.screen instanceof RadialCommandScreen)) {
                    minecraft.setScreen(new RadialCommandScreen());
                }
            }
        }
    }
    public static class RadialCommandScreen extends Screen {
        private static final MenuEntry[] ENTRIES = new MenuEntry[]{
                new MenuEntry("/rtp", "rtp"),
                new MenuEntry("/Space", "Space"),
                new MenuEntry("/naboo", "naboo"),
                new MenuEntry("/jeda", "jeda"),
                new MenuEntry("/kamino", "kamino"),
                new MenuEntry("/Mustafar", "Mustafar"),
                new MenuEntry("/alderaan", "alderaan"),
                new MenuEntry("/hoth", "hoth"),
                new MenuEntry("/endor", "endor"),
                new MenuEntry("/yavin4", "yavin4")
        };
        private static final int CENTER_RADIUS = 34;
        private static final int BUTTON_RADIUS = 26;
        private static final int MENU_RADIUS = 115;
        public RadialCommandScreen() {
            super(Component.literal("Command Wheel"));
        }
        @Override
        public boolean isPauseScreen() {
            return false;
        }
        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            this.renderBackground(guiGraphics);
            int centerX = this.width / 2;
            int centerY = this.height / 2;
            guiGraphics.fill(0, 0, this.width, this.height, 0x88000000);
            drawCircle(guiGraphics, centerX, centerY, CENTER_RADIUS + 6, 0xAA111111);
            drawCircle(guiGraphics, centerX, centerY, CENTER_RADIUS, 0xCC1E1E1E);
            guiGraphics.drawCenteredString(this.font, "K MENU", centerX, centerY - 9, 0xFFFFFF55);
            guiGraphics.drawCenteredString(this.font, "ESC - close", centerX, centerY + 5, 0xFFFFFFFF);
            for (int i = 0; i < ENTRIES.length; i++) {
                double angle = Math.toRadians((360.0 / ENTRIES.length) * i - 90.0);
                int x = centerX + (int) (Math.cos(angle) * MENU_RADIUS);
                int y = centerY + (int) (Math.sin(angle) * MENU_RADIUS);
                boolean hovered = isInsideCircle(mouseX, mouseY, x, y, BUTTON_RADIUS);
                int outerColor = hovered ? 0xFFFFC800 : 0xFF3A3A3A;
                int innerColor = hovered ? 0xFF4A3900 : 0xCC202020;
                int textColor = hovered ? 0xFFFFFFFF : 0xFFE0E0E0;
                drawCircle(guiGraphics, x, y, BUTTON_RADIUS + 3, outerColor);
                drawCircle(guiGraphics, x, y, BUTTON_RADIUS, innerColor);
                String label = ENTRIES[i].displayText;
                guiGraphics.drawCenteredString(this.font, label, x, y - 4, textColor);
            }
            super.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) {
                return super.mouseClicked(mouseX, mouseY, button);
            }
            int centerX = this.width / 2;
            int centerY = this.height / 2;
            for (int i = 0; i < ENTRIES.length; i++) {
                double angle = Math.toRadians((360.0 / ENTRIES.length) * i - 90.0);
                int x = centerX + (int) (Math.cos(angle) * MENU_RADIUS);
                int y = centerY + (int) (Math.sin(angle) * MENU_RADIUS);
                if (isInsideCircle(mouseX, mouseY, x, y, BUTTON_RADIUS)) {
                    executeCommand(ENTRIES[i].command);
                    return true;
                }
            }
            this.onClose();
            return true;
        }
        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_K) {
                this.onClose();
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        private void executeCommand(String commandWithoutSlash) {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if (player == null || minecraft.getConnection() == null) {
                this.onClose();
                return;
            }
            minecraft.getConnection().sendCommand(commandWithoutSlash);
            this.onClose();
        }
        private static boolean isInsideCircle(double mouseX, double mouseY, int centerX, int centerY, int radius) {
            double dx = mouseX - centerX;
            double dy = mouseY - centerY;
            return dx * dx + dy * dy <= radius * radius;
        }
        private static void drawCircle(GuiGraphics guiGraphics, int centerX, int centerY, int radius, int color) {
            for (int y = -radius; y <= radius; y++) {
                int halfWidth = (int) Math.sqrt(radius * radius - y * y);
                guiGraphics.fill(centerX - halfWidth, centerY + y, centerX + halfWidth + 1, centerY + y + 1, color);
            }
        }
        private static class MenuEntry {
            private final String displayText;
            private final String command;
            private MenuEntry(String displayText, String command) {
                this.displayText = displayText;
                this.command = command;
            }
        }
    }
}