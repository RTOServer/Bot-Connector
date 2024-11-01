package net.cjsah.rtos.bot.mixin;

import net.cjsah.rtos.bot.http.RTOSHttpServer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Unique
    private RTOSHttpServer httpServer;

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMeasuringTimeNano()J", ordinal = 0))
    private void start(CallbackInfo ci) throws IOException {
        this.httpServer = new RTOSHttpServer(25585, (MinecraftServer) (Object) this);
    }

    @Inject(method = "stop", at = @At("HEAD"))
    public void stop(boolean waitForShutdown, CallbackInfo ci) {
        this.httpServer.stop();
    }
}
