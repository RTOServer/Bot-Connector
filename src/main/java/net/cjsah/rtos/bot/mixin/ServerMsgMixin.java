package net.cjsah.rtos.bot.mixin;

import net.cjsah.rtos.bot.http.RTOSHttpServer;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerMsgMixin {

    @Redirect(method = "handleDecoratedMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V"))
    private void onMessage(PlayerManager instance, SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params) {
        RTOSHttpServer.msg(sender.getNameForScoreboard(), message.getSignedContent());
        instance.broadcast(message, sender, params);
    }
}
