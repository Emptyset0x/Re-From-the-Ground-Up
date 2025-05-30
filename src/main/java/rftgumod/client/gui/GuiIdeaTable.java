package rftgumod.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import rftgumod.RFTGU;
import rftgumod.common.Content;
import rftgumod.common.tileentity.TileEntityInventory;

public class GuiIdeaTable extends GuiContainer {

    private final ResourceLocation texture;
    private final InventoryPlayer player;

    public GuiIdeaTable(InventoryPlayer player, TileEntityInventory tileentity) {
        super(tileentity.createContainer(player, player.player));
        this.player = player;

        texture = new ResourceLocation(RFTGU.MODID + ":textures/gui/container/" + tileentity.getName() + ".png");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String s = Content.b_ideaTable.getLocalizedName();
        fontRenderer.drawString(s, xSize / 2 - fontRenderer.getStringWidth(s) / 2, 6, 4210752);
        fontRenderer.drawString(player.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float arg0, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

}