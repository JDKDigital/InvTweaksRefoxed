package cy.jdkdigital.invtweaks.gui;

import cy.jdkdigital.invtweaks.InvTweaksMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;


public class InvTweaksButton extends ExtendedButton {
    protected static final ResourceLocation button = new ResourceLocation(InvTweaksMod.MODID, "textures/gui/button_sprites.png");
    private final int tx;
    private final int ty;

    public InvTweaksButton(int x, int y, int tx, int ty, OnPress handler) {
        super(x, y, 14, 16, Component.empty(), handler);
        this.tx = tx;
        this.ty = ty;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        isHovered = this.active
                        && this.visible
                        && mouseX >= this.getX()
                        && mouseY >= this.getY()
                        && mouseX < this.getX() + this.width
                        && mouseY < this.getY() + this.height;
        graphics.blit(button, getX(), getY(), tx, ty + (isHovered ? 16 : 0), 14, 16);
    }
}
