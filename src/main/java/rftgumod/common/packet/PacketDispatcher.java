package rftgumod.common.packet;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import rftgumod.common.packet.client.HintMessage;
import rftgumod.common.packet.client.HintMessage.HintMessageHandler;
import rftgumod.common.packet.client.TechnologyInfoMessage;
import rftgumod.common.packet.client.TechnologyInfoMessage.TechnologyInfoMessageHandler;
import rftgumod.common.packet.client.TechnologyMessage;
import rftgumod.common.packet.client.TechnologyMessage.TechnologyMessageHandler;
import rftgumod.common.packet.server.CopyTechMessage;
import rftgumod.common.packet.server.CopyTechMessage.CopyTechMessageHandler;
import rftgumod.common.packet.server.RequestMessage;
import rftgumod.common.packet.server.RequestMessage.RequestMessageHandler;

public class PacketDispatcher {

    private static byte packetId = 0;

    public static SimpleNetworkWrapper dispatcher;

    public static void registerPackets() {
        dispatcher = NetworkRegistry.INSTANCE.newSimpleChannel("rftgu");

        PacketDispatcher.registerMessage(RequestMessageHandler.class, RequestMessage.class, Side.SERVER);
        PacketDispatcher.registerMessage(CopyTechMessageHandler.class, CopyTechMessage.class, Side.SERVER);

        PacketDispatcher.registerMessage(TechnologyMessageHandler.class, TechnologyMessage.class, Side.CLIENT);
        PacketDispatcher.registerMessage(TechnologyInfoMessageHandler.class, TechnologyInfoMessage.class, Side.CLIENT);
        PacketDispatcher.registerMessage(HintMessageHandler.class, HintMessage.class, Side.CLIENT);
    }

    @SuppressWarnings({"unchecked"})
    private static void registerMessage(Class handlerClass, Class messageClass, Side side) {
        PacketDispatcher.dispatcher.registerMessage(handlerClass, messageClass, packetId++, side);
    }

    public static void sendTo(IMessage message, EntityPlayerMP player) {
        PacketDispatcher.dispatcher.sendTo(message, player);
    }

    public static void sendToAll(IMessage message) {
        PacketDispatcher.dispatcher.sendToAll(message);
    }

    public static void sendToServer(IMessage message) {
        PacketDispatcher.dispatcher.sendToServer(message);
    }

}
