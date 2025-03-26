package rftgumod.common.compat.gamestages;

import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.event.GameStageEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import rftgumod.common.technology.Technology;
import rftgumod.common.technology.TechnologyManager;

public class CompatGameStages {
    public static boolean hasGameStage(EntityPlayer player, String stage) {
        return GameStageHelper.hasStage(player, stage);
    }

    @SubscribeEvent
    public static void onGameStage(GameStageEvent.Added event) {
        if (!event.getEntityPlayer().world.isRemote)
            for (Technology tech : TechnologyManager.INSTANCE)
                if (event.getStageName().equals(tech.getGameStage()) && tech.isUnlockedIgnoreStage(event.getEntityPlayer()))
                    tech.unlock((EntityPlayerMP) event.getEntityPlayer());
    }

}
