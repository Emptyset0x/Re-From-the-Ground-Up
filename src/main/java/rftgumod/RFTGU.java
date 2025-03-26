package rftgumod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import rftgumod.api.technology.puzzle.ResearchConnect;
import rftgumod.api.technology.puzzle.ResearchMatch;
import rftgumod.api.util.predicate.ItemFluid;
import rftgumod.api.util.predicate.ItemLambda;
import rftgumod.api.util.predicate.ItemMod;
import rftgumod.common.Content;
import rftgumod.common.command.CommandTechnology;
import rftgumod.common.compat.gamestages.CompatGameStages;
import rftgumod.common.compat.gamestages.UnlockGameStage;
import rftgumod.common.event.EventHandler;
import rftgumod.common.packet.PacketDispatcher;
import rftgumod.common.proxy.ProxyCommon;
import rftgumod.common.technology.CapabilityTechnology;
import rftgumod.common.technology.Technology;
import rftgumod.common.technology.TechnologyManager;
import rftgumod.common.tileentity.TileEntityIdeaTable;
import rftgumod.common.tileentity.TileEntityResearchTable;
import rftgumod.util.StackUtils;
import rftgumod.common.technology.CapabilityTechnology.DefaultImpl;
import java.io.File;

@Mod(modid = RFTGU.MODID, name = "Re-From the Ground Up!")
public class RFTGU {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Technology.Builder.class, new Technology.Deserializer())
            .registerTypeAdapter(AdvancementRewards.class, new AdvancementRewards.Deserializer())
            .registerTypeHierarchyAdapter(ITextComponent.class, new ITextComponent.Serializer())
            .registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
            .registerTypeAdapterFactory(new EnumTypeAdapterFactory()).create();

    public static final String MODID = "rftgumod";

    public static File configFolder;

    @Mod.Instance(value = RFTGU.MODID)
    public static RFTGU INSTANCE;

    @SidedProxy(clientSide = "rftgumod.client.proxy.ProxyClient", serverSide = "rftgumod.common.proxy.ProxyCommon")
    public static ProxyCommon PROXY;
    public static final boolean GAME_STAGES_LOADED = Loader.isModLoaded("gamestages");
    public static final boolean JEI_LOADED = Loader.isModLoaded("jei");

    private void registerItem(Item item, String name) {
        item.setRegistryName(name);
        ForgeRegistries.ITEMS.register(item);
    }

    private void registerBlock(Block block, ItemBlock item, String name) {
        block.setRegistryName(name);
        ForgeRegistries.BLOCKS.register(block);

        registerItem(item, name);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        TileEntity.register(MODID + ":" + Content.n_ideaTable, TileEntityIdeaTable.class);
        TileEntity.register(MODID + ":" + Content.n_researchTable, TileEntityResearchTable.class);

        registerBlock(Content.b_ideaTable, Content.i_ideaTable, Content.n_ideaTable);
        registerBlock(Content.b_researchTable, Content.i_researchTable, Content.n_researchTable);

        registerItem(Content.i_parchmentEmpty, Content.n_parchmentEmpty);
        registerItem(Content.i_parchmentIdea, Content.n_parchmentIdea);
        registerItem(Content.i_parchmentResearch, Content.n_parchmentResearch);
        registerItem(Content.i_researchBook, Content.n_researchBook);
        registerItem(Content.i_magnifyingGlass, Content.n_magnifyingGlass);

        CriteriaTriggers.register(Content.c_technologyUnlocked);
        CriteriaTriggers.register(Content.c_technologyResearched);
        CriteriaTriggers.register(Content.c_itemLocked);
        CriteriaTriggers.register(Content.c_inspect);

        StackUtils.INSTANCE.registerItemPredicate(new ResourceLocation(MODID, "fluid"), new ItemFluid.Factory());
        StackUtils.INSTANCE.registerItemPredicate(new ResourceLocation(MODID, "enchantment"),
                new ItemLambda.Factory(i -> EnchantmentHelper.getEnchantments(i).size() > 0));
        StackUtils.INSTANCE.registerItemPredicate(new ResourceLocation(MODID, "mod"), new ItemMod.Factory());

        TechnologyManager.INSTANCE.registerPuzzle(new ResourceLocation(MODID, "match"), new ResearchMatch.Factory());
        TechnologyManager.INSTANCE.registerPuzzle(new ResourceLocation(MODID, "connect"),
                new ResearchConnect.Factory());

        CapabilityManager.INSTANCE.register(CapabilityTechnology.ITechnology.class, new CapabilityTechnology.Storage(), DefaultImpl::new);

        MinecraftForge.EVENT_BUS.register(new CapabilityTechnology());
        MinecraftForge.EVENT_BUS.register(new EventHandler());

        LootTableList.register(new ResourceLocation(MODID, "inject/blacksmith"));
        LootTableList.register(new ResourceLocation(MODID, "inject/pyramid"));
        LootTableList.register(new ResourceLocation(MODID, "inject/library"));

        PacketDispatcher.registerPackets();

        configFolder = new File(event.getModConfigurationDirectory(), MODID);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, PROXY.getGuiHandler());
        PROXY.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
//        if (Loader.isModLoaded("immersiveengineering")) {
//            MinecraftForge.EVENT_BUS.register(CompatIE.class);
//            TechnologyManager.INSTANCE.registerUnlock(new ResourceLocation("immersiveengineering", "multiblock"),
//                    new UnlockMultiblockFactory());
//        }
        if (GAME_STAGES_LOADED) {
            MinecraftForge.EVENT_BUS.register(CompatGameStages.class);
            TechnologyManager.INSTANCE.registerUnlock(new ResourceLocation("gamestages", "stage"),
                    new UnlockGameStage.Factory());
        }
        PROXY.postInit(event);
    }

    @Mod.EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        TechnologyManager.INSTANCE
                .reload(event.getServer().getActiveAnvilConverter().getFile(event.getServer().getFolderName(), "data"));
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandTechnology());
    }

}