package rftgumod.client.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Mouse;
import rftgumod.RFTGU;
import rftgumod.api.technology.ITechnology;
import rftgumod.api.technology.unlock.IUnlock;
import rftgumod.client.gui.tab.BetterTabType;
import rftgumod.client.proxy.ProxyClient;
import rftgumod.common.Content;
import rftgumod.common.config.RFTGUConfig;
import rftgumod.common.packet.PacketDispatcher;
import rftgumod.common.packet.server.CopyTechMessage;
import rftgumod.common.technology.Chapter;
import rftgumod.common.technology.Technology;
import rftgumod.common.technology.TechnologyManager;
import rftgumod.util.StackUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SideOnly(Side.CLIENT)
public class GuiResearchBook extends GuiScreen {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation ACHIEVEMENT_BACKGROUND = new ResourceLocation(RFTGU.MODID,
            "textures/gui/achievement/achievement_background.png");
    private static final ResourceLocation WINDOW = new ResourceLocation(RFTGU.MODID,
            "textures/gui/achievement/window.png");
    private static final ResourceLocation TABS = new ResourceLocation(RFTGU.MODID,
            "textures/gui/achievement/tabs.png");
    private static final ResourceLocation STAINED_CLAY = new ResourceLocation(
            "textures/blocks/hardened_clay_stained_cyan.png");
    private static final ResourceLocation RECIPE_BOOK = new ResourceLocation("textures/gui/recipe_book.png");
    public static Map<ResourceLocation, Float> zoom;
    public static Map<ResourceLocation, Double> xScrollO;
    public static Map<ResourceLocation, Double> yScrollO;
    private static boolean showResearchTree;
    private static Technology root;
    private static Technology selected;
    private static Chapter selectedChapter;
    private static int scroll = 1;
    private final EntityPlayer player;
    private final int num = 4;
    private static BetterTabType type;


    private int x_min;
    private int y_min;
    private int x_max;
    private int y_max;
    private final int imageWidth;
    private final int imageHeight;
    private double xScrollP;
    private double yScrollP;
    private double xScrollTarget;
    private double yScrollTarget;
    private int scrolling;
    private double xLastScroll;
    private double yLastScroll;
    private int pages;


    private static final int WIDTH = 252, HEIGHT = 152, CORNER_SIZE = 30;
    private static final int SIDE = 30, TOP = 40, BOTTOM = 30, PADDING = 9;


    public GuiResearchBook(EntityPlayer player) {
        this.player = player;

        imageWidth = 256;
        imageHeight = 202;

        if (selectedChapter == null || !TechnologyManager.INSTANCE.contains(selectedChapter)) {
            for (Chapter chapter : TechnologyManager.INSTANCE.getChapters()) {
                for (Technology root : TechnologyManager.INSTANCE.getRoots().stream().filter(r -> r.isChapterEqual(chapter)).collect(Collectors.toList())) {
                    if (root.canResearchIgnoreResearched(player)) {
                        selectedChapter = chapter;
                        break;
                    }
                }
            }
        }

        if (root == null || !TechnologyManager.INSTANCE.contains(root) || !root.canResearchIgnoreResearched(player)) {
            for (Technology technology : TechnologyManager.INSTANCE.getRoots().stream().filter(r -> r.isChapterEqual(selectedChapter)).collect(Collectors.toList())) {
                if (technology.canResearchIgnoreResearched(player)) {
                    root = technology;
                    break;
                }
            }
        }

    }

    @Override
    public void initGui() {
        if (selected == null || !TechnologyManager.INSTANCE.contains(selected) || !selected.isResearched(player)) {
            selected = null;
            showResearchTree = true;
        }


        xScrollP = xScrollTarget = xScrollO.get(root.getRegistryName());
        yScrollP = yScrollTarget = yScrollO.get(root.getRegistryName());

        buttonList.clear();
        if (showResearchTree) {
            Set<Technology> tree = new HashSet<>();
            for  (Technology technology : TechnologyManager.INSTANCE.getRoots().stream().filter(r -> r.isChapterEqual(selectedChapter)).collect(Collectors.toList())) {
                technology.getChildren(tree, true);
            }

            x_min = (int) root.getDisplayInfo().getX();
            y_min = (int) root.getDisplayInfo().getY();
            x_max = (int) root.getDisplayInfo().getX();
            y_max = (int) root.getDisplayInfo().getY();

            for (Technology technology : tree) {
                if (technology.getDisplayInfo().getX() < x_min)
                    x_min = (int) technology.getDisplayInfo().getX();
                else if (technology.getDisplayInfo().getX() > x_max)
                    x_max = (int) technology.getDisplayInfo().getX();
                if (technology.getDisplayInfo().getY() < y_min)
                    y_min = (int) technology.getDisplayInfo().getY();
                else if (technology.getDisplayInfo().getY() > y_max)
                    y_max = (int) technology.getDisplayInfo().getY();
            }

            x_min = x_min * 24 - 112;
            y_min = y_min * 24 - 112;
            x_max = x_max * 24 - 77;
            y_max = y_max * 24 - 77;

            GuiButton page = new GuiButton(2, (width - imageWidth) / 2 + 24, height - 51, 125, 20,
                    selectedChapter.getDisplayInfo().getTitle().getFormattedText());
            if (TechnologyManager.INSTANCE.getRoots().stream().filter(t -> t.canResearchIgnoreResearched(player))
                    .count() < 2)
                page.enabled = false;

            buttonList.add(new GuiButton(1, width / 2 + 24, height - 51, 80, 20, I18n.format("gui.done")));
            buttonList.add(page);

            scroll = 1;
        } else {
            buttonList.add(new GuiButton(1, width / 2 + 24, height - 51, 80, 20, I18n.format("gui.done")));
            if (RFTGUConfig.allowResearchCopy && selected.canCopy()) {
                GuiButton copy = new GuiButton(2, (width - imageWidth) / 2 + 24, height - 51, 125, 20,
                        I18n.format("gui.copy"));
                copy.enabled = false;
                for (int i = 0; i < player.inventory.getSizeInventory(); i++)
                    if (!player.inventory.getStackInSlot(i).isEmpty()
                            && player.inventory.getStackInSlot(i).getItem() == Content.i_parchmentEmpty) {
                        copy.enabled = true;
                        break;
                    }
                buttonList.add(copy);
            }

            pages = (int) Math.max(
                    Math.ceil(((double) selected.getUnlock().stream().filter(IUnlock::isDisplayed).count()) / num), 1);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            if (showResearchTree) {
                mc.displayGuiScreen(null);
                mc.setIngameFocus();
            } else {
                showResearchTree = true;
                initGui();
            }
        } else if (button.id == 2) {
            if (showResearchTree) {
                Technology first = null;
                boolean next = false;
                for (Chapter chapter : TechnologyManager.INSTANCE.getChapters()){
                    for (Technology technology : TechnologyManager.INSTANCE.getRoots().stream().filter(r -> r.isChapterEqual(chapter)).collect(Collectors.toList())) {
                        if (technology.canResearchIgnoreResearched(player)) {
                            if (next){
                                next = false;
                                selectedChapter = chapter;
                                root = technology;
                                break;
                            }
                            if (first == null)
                                first = technology;
                        }
                        if (technology == root)
                            next = true;
                    }
                    if (next) {
                        root = first;
                        selectedChapter = first.getChapter();
                    }
                }
                initGui();
            } else {
                PacketDispatcher.sendToServer(new CopyTechMessage(selected));
            }
        }
    }

    @Override
    protected void keyTyped(char key, int id) throws IOException {
        if (mc.gameSettings.keyBindInventory.isActiveAndMatches(id) || ProxyClient.key.isActiveAndMatches(id)) {
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        } else {
            super.keyTyped(key, id);
        }
    }

    @Override
    public void drawScreen(int x, int y, float z) {
        if (showResearchTree) {
            if (Mouse.isButtonDown(0)) {

                if ((scrolling == 0 || scrolling == 1) && x >= SIDE + PADDING && x < width - SIDE - PADDING && y >= TOP + PADDING && y < height - BOTTOM - PADDING) {
                    if (scrolling == 0) {
                        scrolling = 1;
                    } else {
                        xScrollP -= (float) (x - xLastScroll) * zoom.get(root.getRegistryName());
                        yScrollP -= (float) (y - yLastScroll) * zoom.get(root.getRegistryName());

                        xScrollTarget = xScrollP;
                        yScrollTarget = yScrollP;

                        xScrollO.put(root.getRegistryName(), xScrollP);
                        yScrollO.put(root.getRegistryName(), yScrollP);
                    }
                    xLastScroll = x;
                    yLastScroll = y;
                }
            } else {
                scrolling = 0;
            }

            int dWheel = Mouse.getDWheel();
            float rootZoom = zoom.get(root.getRegistryName());
            zoom.put(root.getRegistryName(),
                    MathHelper.clamp(dWheel < 0 ? rootZoom + 0.25F : dWheel > 0 ? rootZoom - 0.25F : rootZoom, 1.0F, 2.0F));

            if (zoom.get(root.getRegistryName()) != rootZoom) {
                float zoomScreenWidth = rootZoom * (width - SIDE - SIDE);
                float zoomScreenHeight = rootZoom * (height - TOP - BOTTOM);
                float actualScreenWidth = zoom.get(root.getRegistryName()) * (width - SIDE - SIDE);
                float actualScreenHeight = zoom.get(root.getRegistryName()) * (height - TOP - BOTTOM);

                xScrollP -= (actualScreenWidth - zoomScreenWidth) * 0.5F;
                yScrollP -= (actualScreenHeight - zoomScreenHeight) * 0.5F;

                xScrollTarget = xScrollP;
                yScrollTarget = yScrollP;

                xScrollO.put(root.getRegistryName(), xScrollP);
                yScrollO.put(root.getRegistryName(), yScrollP);
            }

            if (xScrollTarget < x_min)
                xScrollTarget = x_min;
            if (yScrollTarget < y_min)
                yScrollTarget = y_min;
            if (xScrollTarget >= x_max)
                xScrollTarget = x_max - 1;
            if (yScrollTarget >= y_max)
                yScrollTarget = y_max - 1;
        }

        drawDefaultBackground();
        drawResearchScreen(x, y, z);

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        drawTitle();

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
    }

    @Override
    public void mouseClicked(int x, int y, int b) throws IOException {
        if (b == 0 && selected != null && selected.isResearched(player)) {
            showResearchTree = false;
            initGui();
        }
        int index = 0;
        for (Chapter chapter : TechnologyManager.INSTANCE.getChapters()){
            if (chapter.hasCanResearchRoots(player)) {
                if (type.isMouseOver(SIDE, TOP, width - SIDE - SIDE, height - SIDE - SIDE, index ,x ,y)){
                    selectedChapter = chapter;
                    showResearchTree = true;
                    initGui();
                }
                index++;
            }
        }
        super.mouseClicked(x, y, b);
    }

    @Override
    public void updateScreen() {
        xScrollO.put(root.getRegistryName(), xScrollP);
        yScrollO.put(root.getRegistryName(), yScrollP);
        double xDistance = xScrollTarget - xScrollP;
        double yDistance = yScrollTarget - yScrollP;
        if (xDistance * xDistance + yDistance * yDistance < 4D) {
            xScrollP += xDistance;
            yScrollP += yDistance;
        } else {
            xScrollP += xDistance * 0.85D;
            yScrollP += yDistance * 0.85D;
        }
    }

    private void drawTitle() {
        fontRenderer.drawString(I18n.format("item.research_book.name"), SIDE + 8, TOP + 6, 0x404040);
    }

    private void drawResearchScreen(int x, int y, float z) {

        int left = SIDE;
        int top = TOP;

        int right = width - SIDE;
        int bottom = height - SIDE;

        int i1 = SIDE + 8;
        int j1 = TOP + 17;

        int boxLeft = left + PADDING;
        int boxTop = top + 2*PADDING;
        int boxRight = right - PADDING;
        int boxBottom = bottom - PADDING;
        int boxWidth = boxRight - boxLeft;
        int boxHeight = boxBottom - boxTop;

        GlStateManager.depthFunc(518);
        GlStateManager.pushMatrix();
        GlStateManager.translate(boxLeft , boxTop, -200F);
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableColorMaterial();

        int chunkHeight = boxHeight / 16 + 1;
        int chunkWidth = boxWidth / 16 + 1;
        for (int l3 = 0; l3 < chunkHeight; l3++) {
            for (int i4 = 0; i4 < chunkWidth; i4++) {
                if (selectedChapter.getDisplayInfo().getBackground() == null)
                    mc.getTextureManager().bindTexture(STAINED_CLAY);
                else
                    mc.getTextureManager().bindTexture(selectedChapter.getDisplayInfo().getBackground());
                if (l3 < chunkHeight - 1 && i4 < chunkWidth - 1)
                    drawModalRectWithCustomSizedTexture(i4 * 16, l3 * 16, 0, 0, 16, 16, 16, 16);
                if (l3 == chunkHeight - 1 && i4 < chunkWidth - 1)
                    drawModalRectWithCustomSizedTexture(i4 * 16, l3 * 16, 0, 0, 16, boxHeight % 16, 16, 16);
                if (l3 < chunkHeight - 1 && i4 == chunkWidth - 1)
                    drawModalRectWithCustomSizedTexture(i4 * 16, l3 * 16, 0, 0, boxWidth % 16, 16, 16, 16);
                if (l3 == chunkHeight - 1 && i4 == chunkWidth - 1)
                    drawModalRectWithCustomSizedTexture(i4 * 16, l3 * 16, 0, 0, boxWidth % 16, boxHeight % 16, 16, 16);
            }
        }
        mc.getTextureManager().bindTexture(ACHIEVEMENT_BACKGROUND);

        GlStateManager.enableDepth();
        GlStateManager.depthFunc(515);
        if (showResearchTree) {
            GlStateManager.scale(1.0F / zoom.get(root.getRegistryName()), 1.0F / zoom.get(root.getRegistryName()),
                    1.0F);

            int interpolatedXScroll = MathHelper.floor(
                    xScrollO.get(root.getRegistryName()) + (xScrollP - xScrollO.get(root.getRegistryName())) * z);
            int interpolatedYScroll = MathHelper.floor(
                    yScrollO.get(root.getRegistryName()) + (yScrollP - yScrollO.get(root.getRegistryName())) * z);

            if (interpolatedXScroll < x_min)
                interpolatedXScroll = x_min;
            if (interpolatedYScroll < y_min)
                interpolatedYScroll = y_min;
            if (interpolatedXScroll >= x_max)
                interpolatedXScroll = x_max - 1;
            if (interpolatedYScroll >= y_max)
                interpolatedYScroll = y_max - 1;

            Set<Technology> tech = new HashSet<>();
            for  (Technology technology : TechnologyManager.INSTANCE.getRoots().stream().filter(r -> r.isChapterEqual(selectedChapter)).collect(Collectors.toList())) {
                technology.getChildren(tech, true);
            }


            if (tech != null) {
                try {
                    for (Technology technologyForLines : tech) {
                        if (!technologyForLines.canResearchIgnoreResearched(player))
                            continue;
                        if (!technologyForLines.isResearched(player) && !technologyForLines.isUnlocked(player))
                            continue;
                        if (technologyForLines.getDisplayInfo().isHidden() && !technologyForLines.isResearched(player))
                            continue;
                        if (technologyForLines.getParent() == null || !tech.contains(technologyForLines.getParent()))
                            continue;
                        int xStart = (int) ((technologyForLines.getDisplayInfo().getX() * 24 - interpolatedXScroll) + 11);
                        int yStart = (int) ((technologyForLines.getDisplayInfo().getY() * 24 - interpolatedYScroll) + 11);
                        int xStop = (int) ((technologyForLines.getParent().getDisplayInfo().getX() * 24 - interpolatedXScroll) + 11);
                        int yStop = (int) ((technologyForLines.getParent().getDisplayInfo().getY() * 24 - interpolatedYScroll) + 11);

                        boolean flag = technologyForLines.isResearched(player);

                        int colorLine;
                        if (flag)
                            colorLine = 0xffa0a0a0;
                        else
                            colorLine = 0xff00ff00;

                        drawHorizontalLine(xStart, xStop, yStart, colorLine);
                        drawVerticalLine(xStop, yStart, yStop, colorLine);

                        if (xStart > xStop)
                            drawTexturedModalRect(xStart - 11 - 7, yStart - 5, 114, 234, 7, 11);
                        else if (xStart < xStop)
                            drawTexturedModalRect(xStart + 11, yStart - 5, 107, 234, 7, 11);
                        else if (yStart > yStop)
                            drawTexturedModalRect(xStart - 5, yStart - 11 - 7, 96, 234, 11, 7);
                        else if (yStart < yStop)
                            drawTexturedModalRect(xStart - 5, yStart + 11, 96, 241, 11, 7);
                    }

                    selected = null;

                    float f3 = (x - i1) * zoom.get(root.getRegistryName());
                    float f4 = (y - j1) * zoom.get(root.getRegistryName());

                    RenderHelper.enableGUIStandardItemLighting();
                    GlStateManager.disableLighting();
                    GlStateManager.enableRescaleNormal();
                    GlStateManager.enableColorMaterial();

                    for (Technology technologyForFrame : tech) {
                        if (!technologyForFrame.canResearchIgnoreResearched(player))
                            continue;
                        if (!technologyForFrame.isResearched(player) && !technologyForFrame.isUnlocked(player))
                            continue;
                        if (technologyForFrame.getDisplayInfo().isHidden() && !technologyForFrame.isResearched(player))
                            continue;
                        int xPoint = (int) (technologyForFrame.getDisplayInfo().getX() * 24 - interpolatedXScroll);
                        int yPoint = (int) (technologyForFrame.getDisplayInfo().getY() * 24 - interpolatedYScroll);
                        if (xPoint < -24 || yPoint < -24 || xPoint > (float)(boxRight - boxLeft) * zoom.get(root.getRegistryName())
                                || yPoint > (float)(boxBottom - boxTop - 8) * zoom.get(root.getRegistryName()))
                            continue;

                        if (technologyForFrame.isResearched(player))
                            GlStateManager.color(0.75F, 0.75F, 0.75F, 1.0F);
                        else
                            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                        mc.getTextureManager().bindTexture(ACHIEVEMENT_BACKGROUND);
                        GlStateManager.enableBlend();
                        if (technologyForFrame.hasCustomUnlock())
                            drawTexturedModalRect(xPoint - 2, yPoint - 2, 26, 202, 26, 26);
                        else
                            drawTexturedModalRect(xPoint - 2, yPoint - 2, 0, 202, 26, 26);
                        GlStateManager.disableBlend();

                        GlStateManager.disableLighting();
                        GlStateManager.enableCull();
                        itemRender.renderItemAndEffectIntoGUI(technologyForFrame.getDisplayInfo().getIcon(), xPoint + 3, yPoint + 3);
                        GlStateManager.blendFunc(net.minecraft.client.renderer.GlStateManager.SourceFactor.SRC_ALPHA,
                                net.minecraft.client.renderer.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                        GlStateManager.disableLighting();

                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        if (f3 >= xPoint && f3 <= xPoint + 22 && f4 >= yPoint && f4 <= yPoint + 22
                                && technologyForFrame.canResearchIgnoreResearched(player))
                            selected = technologyForFrame;
                    }
                } catch (ConcurrentModificationException e) {
                    LOGGER.debug("Prevented ConcurrentModificationException while rendering GuiResearchBook");
                }
            }
        } else {
            int wheel = Mouse.getDWheel();
            if (wheel < 0)
                scroll = Math.min(scroll + 1, pages);
            if (wheel > 0)
                scroll = Math.max(scroll - 1, 1);

            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableColorMaterial();
            List<IUnlock> displayUnlockList = selected.getUnlock().stream().filter(IUnlock::isDisplayed)
                    .collect(Collectors.toList());
            for (int pos = 0; pos < num; pos++) {
                int n = pos + (num * (scroll - 1));
                if (n >= displayUnlockList.size())
                    break;

                ItemStack[] unlockItems = displayUnlockList.get(n).getIcon().getMatchingStacks();

                NonNullList[] sub = new NonNullList[unlockItems.length];
                int length = 0;

                for (int q = 0; q < unlockItems.length; q++) {
                    if (unlockItems[q].getItemDamage() == OreDictionary.WILDCARD_VALUE) {
                        sub[q] = NonNullList.create();

                        // noinspection unchecked
                        unlockItems[q].getItem().getSubItems(unlockItems[q].getItem().getCreativeTab(), sub[q]);
                    } else
                        sub[q] = NonNullList.from(null, unlockItems[q]);

                    length += sub[q].size();
                }

                unlockItems = new ItemStack[length];
                int pp = 0;
                for (NonNullList nonNullList : sub)
                    // noinspection unchecked
                    for (ItemStack stack : (NonNullList<ItemStack>) nonNullList)
                        unlockItems[pp++] = stack;

                long tick = mc.world.getTotalWorldTime() / 30;
                int index = (int) (tick % unlockItems.length);

                ItemStack item = unlockItems[index];

                mc.getTextureManager().bindTexture(ACHIEVEMENT_BACKGROUND);
                GlStateManager.enableBlend();
                drawTexturedModalRect(9 , 9 + (pos * 28), 0, 202, 26, 26);
                GlStateManager.disableBlend();

                GlStateManager.disableLighting();
                GlStateManager.enableCull();
                itemRender.renderItemAndEffectIntoGUI(item, 9 + 5, 9 + 5 + (pos * 28));
                GlStateManager.blendFunc(net.minecraft.client.renderer.GlStateManager.SourceFactor.SRC_ALPHA,
                        net.minecraft.client.renderer.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                GlStateManager.disableLighting();

                fontRenderer.drawStringWithShadow(item.getDisplayName(), 9 + 29, 9 + 8 + (pos * 28), 0xFFFFFF);

                if (x >= i1 + 9 && x < i1 + 35 && y >= j1 + 9 + (pos * 28) && y < j1 + 35 + (pos * 28)) {
                    int r = 0;
                    for (IRecipe recipe : ForgeRegistries.RECIPES) {
                        if (StackUtils.INSTANCE.isStackOf(item, recipe.getRecipeOutput())) {
                            mc.getTextureManager().bindTexture(RECIPE_BOOK);

                            int xp = 34 + (r * 25);
                            int yp = 10 + (pos * 28);
                            drawTexturedModalRect(xp, yp, 152, 78, 24, 24);

                            int width = 3;
                            int height = 3;

                            if (recipe instanceof IShapedRecipe) {
                                IShapedRecipe shaped = (IShapedRecipe) recipe;
                                width = shaped.getRecipeWidth();
                                height = shaped.getRecipeHeight();
                            }

                            Iterator<Ingredient> iterator = recipe.getIngredients().iterator();

                            outer: for (int i = 0; i < height; ++i) {
                                int kk = 3 + i * 7;

                                for (int j = 0; j < width; ++j) {
                                    if (iterator.hasNext()) {
                                        ItemStack[] stack = (iterator.next()).getMatchingStacks();

                                        if (stack.length != 0) {
                                            int l1 = 3 + j * 7;
                                            GlStateManager.pushMatrix();
                                            int i2 = (int) ((float) (xp + l1) / 0.42F - 3.0F);
                                            int j2 = (int) ((float) (yp + kk) / 0.42F - 3.0F);
                                            GlStateManager.scale(0.42F, 0.42F, 1.0F);
                                            GlStateManager.enableLighting();
                                            mc.getRenderItem().renderItemAndEffectIntoGUI(
                                                    stack[(int) (tick % stack.length)], i2, j2);
                                            GlStateManager.disableLighting();
                                            GlStateManager.popMatrix();
                                        }
                                    } else
                                        break outer;
                                }
                            }
                            r++;
                        }
                    }
                }
            }
        }

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(WINDOW);
        // drawTexturedModalRect(k, l, 0, 0, imageWidth, imageHeight);

        this.drawTexturedModalRect(left, top, 0, 0, CORNER_SIZE, CORNER_SIZE);
        // Top side
        renderRepeating(this, left + CORNER_SIZE, top, width - CORNER_SIZE - 2*SIDE - CORNER_SIZE, CORNER_SIZE, CORNER_SIZE, 0, WIDTH - CORNER_SIZE - CORNER_SIZE, CORNER_SIZE);
        // Top right corner
        this.drawTexturedModalRect(right - CORNER_SIZE, top, WIDTH - CORNER_SIZE, 0, CORNER_SIZE, CORNER_SIZE);
        // Left side
        renderRepeating(this, left, top + CORNER_SIZE, CORNER_SIZE, bottom - top - 2 * CORNER_SIZE, 0, CORNER_SIZE, CORNER_SIZE, HEIGHT - CORNER_SIZE - CORNER_SIZE);
        // Right side
        renderRepeating(this, right - CORNER_SIZE, top + CORNER_SIZE, CORNER_SIZE, bottom - top - 2 * CORNER_SIZE, WIDTH - CORNER_SIZE, CORNER_SIZE, CORNER_SIZE, HEIGHT - CORNER_SIZE - CORNER_SIZE);
        // Bottom left corner
        this.drawTexturedModalRect(left, bottom - CORNER_SIZE, 0, HEIGHT - CORNER_SIZE, CORNER_SIZE, CORNER_SIZE);
        // Bottom side
        renderRepeating(this, left + CORNER_SIZE, bottom - CORNER_SIZE, width - CORNER_SIZE - 2*SIDE - CORNER_SIZE, CORNER_SIZE, CORNER_SIZE, HEIGHT - CORNER_SIZE, WIDTH - CORNER_SIZE - CORNER_SIZE, CORNER_SIZE);
        // Bottom right corner
        this.drawTexturedModalRect(right - CORNER_SIZE, bottom - CORNER_SIZE, WIDTH - CORNER_SIZE, HEIGHT - CORNER_SIZE, CORNER_SIZE, CORNER_SIZE);
        int count = 0;
        for (Chapter chapter : TechnologyManager.INSTANCE.getChapters()) {
            if (chapter.hasCanResearchRoots(player)){
                count++;
            }
        }
        type = BetterTabType.getTabType(right - left, bottom - top, count );
        if (count > 1 ) {
            mc.getTextureManager().bindTexture(TABS);
            int index = 0;
            for (Chapter chapter : TechnologyManager.INSTANCE.getChapters()){
                if (chapter.hasCanResearchRoots(player)) {
                    type.draw(this, left, top, right - left, bottom - top, chapter == selectedChapter , index);
                    index++;
                }
            }
            GlStateManager.enableRescaleNormal();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderHelper.enableGUIStandardItemLighting();
            int iconIndex = 0;
            for (Chapter chapter : TechnologyManager.INSTANCE.getChapters()) {
                if (chapter.hasCanResearchRoots(player)) {
                    type.drawIcon(left, top, right - left, bottom - top, iconIndex, mc.getRenderItem(), chapter.getDisplayInfo().getIcon() );
                    iconIndex++;
                }
            }
        }

        zLevel = 0.0F;
        GlStateManager.depthFunc(515);
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();

        super.drawScreen(x, y, z);
        if (selected != null) {
            if (showResearchTree) {
                String s = selected.getDisplayInfo().getTitle().getFormattedText();
                String s1 = selected.getDisplayInfo().getDescription().getFormattedText();
                String s2 = null;
                String s3 = null;

                if (selected.hasSubtilte()) {
                    s2 = selected.getSubtilte().getFormattedText();
                }
                if (selected.hasChapter()) {
                    s3 = selected.getChapter().getDisplayInfo().getDescription().getFormattedText();
                }
                int children = 0;
                for (ITechnology child : selected.getChildren())
                    if (child.isRoot())
                        children++;

                int i7 = x + 12;
                int k7 = y - 4;

                int j8 = Math.max(fontRenderer.getStringWidth(s), 120);
                int i9 = fontRenderer.getWordWrappedHeight(s1, j8);
                if (selected.isResearched(player) || children > 0)
                    i9 += 12;

                if (selected.isResearched(player))
                    fontRenderer.drawStringWithShadow(I18n.format("technology.researched"), i7, k7 +36,
                            0xff9090ff);
                else if (children > 0)
                    fontRenderer.drawStringWithShadow(I18n.format(children == 1 ? "technology.tab" : "technology.tabs"),
                            i7, k7 + 36, 0xffff5555);
                fontRenderer.drawStringWithShadow(s, i7, k7, -1);
                fontRenderer.drawStringWithShadow(s2, i7, k7 + 12, 0xffa0a0a0);
                fontRenderer.drawStringWithShadow(s3, i7, k7 + 24, -1);

            } else {
                String s1 = selected.getDisplayInfo().getTitle().getFormattedText();
                int x1 = (width - fontRenderer.getStringWidth(s1)) / 2;
                fontRenderer.drawStringWithShadow(s1, x1, TOP + 6, 0xffffff);

                String s2 = selected.getDisplayInfo().getDescription().getFormattedText();
                int x2 = (width / 2 + width - SIDE - SIDE) / 2;
                int y2 = TOP + 35;

                for (String s : fontRenderer.listFormattedStringToWidth(s2, 180)) {
                    fontRenderer.drawStringWithShadow(s, x2 - (fontRenderer.getStringWidth(s) / 2), y2, 0xffffff);
                    y2 += fontRenderer.FONT_HEIGHT;
                }

                String s3 = scroll + "/" + pages;
                int x3 = width - SIDE - PADDING - fontRenderer.getStringWidth(s3);
                int y3 = height;
                fontRenderer.drawStringWithShadow(s3, x3 - 2  , y3 - 45, 0xffa0a0a0);
            }
        }

        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public static void renderRepeating(Gui screen, int x, int y, int width, int height, int textureX, int textureY, int textureWidth, int textureHeight) {
        for (int i = 0; i < width; i += textureWidth) {
            int drawX = x + i;
            int drawWidth = Math.min(textureWidth, width - i);

            for (int l = 0; l < height; l += textureHeight) {
                int drawY = y + l;
                int drawHeight = Math.min(textureHeight, height - l);
                screen.drawTexturedModalRect(drawX, drawY, textureX, textureY, drawWidth, drawHeight);
            }
        }
    }
}
