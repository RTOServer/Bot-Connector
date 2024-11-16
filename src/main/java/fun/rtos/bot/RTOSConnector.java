package fun.rtos.bot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fun.rtos.bot.http.HttpRequests;
import fun.rtos.bot.http.RTOSHttpServer;
import fun.rtos.bot.util.Config;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public class RTOSConnector {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Logger LOGGER = LoggerFactory.getLogger(RTOSConnector.class);
    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("rtos-connector.json");
    public static final Config CONFIG = new Config(CONFIG_PATH);
    public static MinecraftServer server = null;
    public static RTOSHttpServer httpServer = null;

    public static void onServerRun(MinecraftServer server) {
        RTOSConnector.server = server;
        RTOSConnector.CONFIG.loadOrCreate();
        try {
            RTOSConnector.httpServer = new RTOSHttpServer(RTOSConnector.CONFIG.port);
            RTOSConnector.httpServer.loadClass(HttpRequests.class);
            RTOSConnector.httpServer.start();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static void onServerStop(MinecraftServer server) {
        RTOSConnector.server = server;
        RTOSConnector.httpServer.stop();
        RTOSConnector.CONFIG.save();
    }
}
