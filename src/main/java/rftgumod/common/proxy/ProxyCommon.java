package rftgumod.common.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import rftgumod.api.technology.ITechnology;
import rftgumod.client.gui.GuiHandler;
import rftgumod.common.technology.CapabilityTechnology;
import rftgumod.common.technology.Technology;

public class ProxyCommon {

    public void displayToastTechnology(ITechnology technology) {
    }

    public void clearToasts() {
    }

    public void openResearchBook(EntityPlayer player) {
    }

    public IGuiHandler getGuiHandler() {
        return new GuiHandler();
    }

    public EntityPlayer getPlayerEntity(MessageContext ctx) {
        return ctx.getServerHandler().player;
    }

    public void autoResearch(Technology tech) {
        FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers().forEach(player -> {
            CapabilityTechnology.ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
            cap.setResearched(tech.getRegistryName().toString());
        });
    }

    public void init(FMLInitializationEvent event) {
    }

    public void postInit(FMLPostInitializationEvent event) {
    }
}