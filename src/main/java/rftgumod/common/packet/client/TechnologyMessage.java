package rftgumod.common.packet.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import rftgumod.RFTGU;
import rftgumod.api.event.RFTGUClientSyncEvent;
import rftgumod.api.technology.ITechnology;
import rftgumod.common.compat.jei.CompatJEI;
import rftgumod.common.packet.MessageHandler;
import rftgumod.common.technology.CapabilityTechnology;
import rftgumod.common.technology.Technology;
import rftgumod.common.technology.TechnologyManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class TechnologyMessage implements IMessage {

    private Collection<String> tech;
    private boolean force;
    private ITechnology[] toasts;

    public TechnologyMessage() {
    }

    public TechnologyMessage(EntityPlayer player, boolean force, ITechnology... toasts) {
        CapabilityTechnology.ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
        if (cap != null) {
            this.tech = cap.getResearched();
            this.force = force;
            this.toasts = toasts;
        } else
            throw new IllegalArgumentException();
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        force = buffer.readBoolean();

        this.tech = new HashSet<>();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++)
            tech.add(ByteBufUtils.readUTF8String(buffer));

        toasts = new ITechnology[buffer.readInt()];
        for (int i = 0; i < toasts.length; i++)
            toasts[i] = TechnologyManager.INSTANCE
                    .getTechnology(new ResourceLocation(ByteBufUtils.readUTF8String(buffer)));
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeBoolean(force);

        if (tech != null) {
            buffer.writeInt(tech.size());
            for (String s : tech)
                ByteBufUtils.writeUTF8String(buffer, s);
        } else
            buffer.writeInt(0);

        buffer.writeInt(toasts.length);
        for (ITechnology toast : toasts)
            ByteBufUtils.writeUTF8String(buffer, toast.getRegistryName().toString());
    }

    public static class TechnologyMessageHandler extends MessageHandler<TechnologyMessage> {

        @Override
        public IMessage handleMessage(EntityPlayer player, TechnologyMessage message) {
            if (player == null)
                return null;

            CapabilityTechnology.ITechnology cap = player.getCapability(CapabilityTechnology.TECH_CAP, null);
            if (cap != null) {
                if (!message.force && cap.getResearched().size() == message.tech.size())
                    return null;

                // Defensive copy to prevent concurrent modification exception
                Collection<String> researched = new ArrayList<>(cap.getResearched());
                for (String name : researched)
                    if (!message.tech.contains(name)) {
                        cap.removeResearched(name);

                        String[] split = name.split("#");
                        if (split.length == 2) {
                            Technology tech = TechnologyManager.INSTANCE.getTechnology(new ResourceLocation(split[0]));
                            if (tech != null) {
                                TechnologyManager.INSTANCE.getProgress(player, tech).revokeCriterion(split[1]);
                            }
                        }
                    }

                for (String name : message.tech)
                    if (!cap.isResearched(name)) {
                        cap.setResearched(name);

                        String[] split = name.split("#");
                        if (split.length == 2) {
                            Technology tech = TechnologyManager.INSTANCE.getTechnology(new ResourceLocation(split[0]));
                            if (tech != null) {
                                TechnologyManager.INSTANCE.getProgress(player, tech).grantCriterion(split[1]);
                            }
                        }
                    }

                for (ITechnology toast : message.toasts)
                    RFTGU.PROXY.displayToastTechnology(toast);

                if(RFTGU.JEI_LOADED)
                    CompatJEI.refreshHiddenItems(false);
                MinecraftForge.EVENT_BUS.post(new RFTGUClientSyncEvent.Post());
            }

            return null;
        }

    }

}
