package rftgumod.util;

import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import rftgumod.common.packet.PacketDispatcher;
import rftgumod.common.packet.client.TechnologyMessage;
import rftgumod.common.technology.Technology;
import rftgumod.common.technology.TechnologyManager;

public final class ListenerTechnology<T extends ICriterionInstance> extends ICriterionTrigger.Listener<T> {

    private final Technology technology;
    private final String name;

    public ListenerTechnology(T instance, Technology technology, String name) {
        super(instance, null, null);
        this.technology = technology;
        this.name = name;
    }

    @Override
    public void grantCriterion(PlayerAdvancements playerAdvancements) {
        EntityPlayerMP player = playerAdvancements.player;

        if (TechnologyManager.INSTANCE.contains(technology))
            technology.grantCriterion(player, name);
        PacketDispatcher.sendTo(new TechnologyMessage(player, true), player);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof ListenerTechnology && (this.getCriterionInstance().equals(((ListenerTechnology) obj).getCriterionInstance()) && this.technology == ((ListenerTechnology) obj).technology);
    }

    @Override
    public int hashCode() {
        int i = getCriterionInstance().hashCode();
        i = 31 * i + technology.hashCode();
        i = 31 * i + name.hashCode();
        return i;
    }
}
