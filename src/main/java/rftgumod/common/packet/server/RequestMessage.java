package rftgumod.common.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import rftgumod.common.packet.MessageHandler;
import rftgumod.common.packet.client.TechnologyMessage;

public class RequestMessage implements IMessage {
    public RequestMessage() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class RequestMessageHandler extends MessageHandler<RequestMessage> {
        @Override
        public IMessage handleMessage(EntityPlayer player, RequestMessage message) {
            if (player != null) {
                return new TechnologyMessage(player, false);
            }
            return null;
        }
    }
}
