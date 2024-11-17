package fun.rtos.bot.mixin;

import fun.rtos.bot.RTOSConnector;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;getNanos()J", ordinal = 0))
    private void start(CallbackInfo ci) {
        RTOSConnector.onServerRun((MinecraftServer) (Object) this);
    }

    @Inject(method = "halt", at = @At("HEAD"))
    public void stop(boolean waitForShutdown, CallbackInfo ci) {
        RTOSConnector.onServerStop((MinecraftServer) (Object) this);
    }
}
