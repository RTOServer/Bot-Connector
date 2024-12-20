package fun.rtos.bot.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import fun.rtos.bot.RTOSConnector;
import fun.rtos.bot.util.JsonUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HttpRequests {
    public static final ConcurrentLinkedQueue<PlayerMsg> QUEUE = new ConcurrentLinkedQueue<>();

    public static void msg(String sender, String msg) {
        HttpRequests.QUEUE.offer(new PlayerMsg(sender, msg));
    }

    @HttpRequest(value = "/command", method = "POST")
    public static void command(@NotNull HttpExchange exchange) throws IOException {
        CommandBody body = JsonUtil.deserialize(exchange.getRequestBody(), CommandBody.class);
        if (body == null) {
            RTOSHttpServer.sendError(exchange, "请传入命令");
            return;
        }
        RTOSConnector.server.getCommands().performPrefixedCommand(RTOSConnector.server.createCommandSourceStack(), body.command());

        RTOSHttpServer.sendPass(exchange, null);
    }

    @HttpRequest("/send")
    public static void send(@NotNull HttpExchange exchange) throws IOException {
        PlayerMsg body = JsonUtil.deserialize(exchange.getRequestBody(), PlayerMsg.class);
        if (body == null) {
            RTOSHttpServer.sendError(exchange, "请传入消息");
            return;
        }

        String msg = "<%s> %s".formatted(body.player(), body.msg());
        MutableComponent text = Component.literal(msg).withStyle(ChatFormatting.GRAY);
        RTOSConnector.server.getPlayerList().broadcastSystemMessage(text, false);
        RTOSConnector.server.sendSystemMessage(text);
        RTOSHttpServer.sendPass(exchange, null);
    }

    @HttpRequest("/msg")
    public static void msg(@NotNull HttpExchange exchange) throws IOException {
        int size = HttpRequests.QUEUE.size();
        JsonArray array = new JsonArray(size);
        for (int i = 0; i < size; i++) {
            PlayerMsg msg = HttpRequests.QUEUE.poll();
            assert msg != null;
            JsonObject json = new JsonObject();
            json.addProperty("player", msg.player());
            json.addProperty("msg", msg.msg());
            array.add(json);
        }
        RTOSHttpServer.sendPass(exchange, json -> json.add("msgs", array));
    }
}
