package net.cjsah.rtos.bot.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.cjsah.rtos.bot.JsonUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class RTOSHttpServer {
    private static final ConcurrentLinkedQueue<PlayerMsg> Queue = new ConcurrentLinkedQueue<>();
    private static final Logger log = LoggerFactory.getLogger(RTOSHttpServer.class);
    private final HttpServer http;
    private final MinecraftServer server;

    public RTOSHttpServer(int port, MinecraftServer server) throws IOException {
        this.server = server;
        this.http = HttpServer.create(new InetSocketAddress(port), 0);

        this.http.createContext("/command", exchange -> {
            if (verifyIsError(exchange, "POST")) return;

            CommandBody body = JsonUtil.deserialize(exchange.getRequestBody(), CommandBody.class);
            if (body == null) {
                sendError(exchange, "请传入命令");
                return;
            }

            this.server.getCommandManager().executeWithPrefix(this.server.getCommandSource(), body.command());

            sendPass(exchange, null);
        });

        this.http.createContext("/send", exchange -> {
            if (verifyIsError(exchange, "POST")) return;

            PlayerMsg body = JsonUtil.deserialize(exchange.getRequestBody(), PlayerMsg.class);
            if (body == null) {
                sendError(exchange, "请传入消息");
                return;
            }

            String msg = "<%s> %s".formatted(body.player(), body.msg());
            MutableText text = Text.literal(msg).formatted(Formatting.DARK_GRAY);
            this.server.getPlayerManager().broadcast(text, false);
            this.server.sendMessage(text);

            sendPass(exchange, null);
        });

        this.http.createContext("/msg", exchange -> {
            if (verifyIsError(exchange, "GET")) return;

            int size = Queue.size();
            JsonArray array = new JsonArray(size);
            for (int i = 0; i < size; i++) {
                PlayerMsg msg = Queue.poll();
                assert msg != null;
                JsonObject json = new JsonObject();
                json.addProperty("player", msg.player());
                json.addProperty("msg", msg.msg());
                array.add(json);
            }
            sendPass(exchange, json -> json.add("msgs", array));
        });

        this.http.start();
        log.info("HttpServer started.");
    }

    public void stop() {
        this.http.stop(1);
        log.info("HttpServer stopped.");
    }

    public static void msg(String sender, String msg) {
        Queue.offer(new PlayerMsg(sender, msg));
    }

    private static boolean verifyIsError(HttpExchange exchange, String method) throws IOException {
        String currentMethod = exchange.getRequestMethod();
        if (!method.equals(currentMethod)) {
            sendError(exchange, "404 Not Found");
            return true;
        }
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        if (!"RTOS_PWD".equals(token)) {
            sendError(exchange, "令牌验证失败");
            return true;
        }
        return false;
    }

    private static void sendPass(HttpExchange exchange, Consumer<JsonObject> dataFactory) throws IOException {
        send(exchange, 0, "", dataFactory);
    }

    private static void sendError(HttpExchange exchange, String msg) throws IOException {
        send(exchange, -1, msg, null);
    }

    private static void send(HttpExchange exchange, int code, String msg, Consumer<JsonObject> dataFactory) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("code", code);
        json.addProperty("msg", msg);
        if (dataFactory != null) {
            JsonObject data = new JsonObject();
            json.add("data", data);
            dataFactory.accept(data);
        }
        byte[] res = JsonUtil.serialize(json).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json;charset=UTF-8");
        exchange.sendResponseHeaders(200, res.length);
        exchange.getResponseBody().write(res);
        exchange.close();
    }

}
