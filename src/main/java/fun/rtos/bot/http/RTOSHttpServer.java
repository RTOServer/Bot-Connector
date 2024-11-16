package fun.rtos.bot.http;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import fun.rtos.bot.RTOSConnector;
import fun.rtos.bot.util.JsonUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class RTOSHttpServer {
    private final HttpServer http;

    public RTOSHttpServer(int port, Class<?>... classes) throws IOException {
        this.http = HttpServer.create(new InetSocketAddress(port), 0);
        this.loadClasses(classes);
    }

    public void loadClasses(Class<?>... classes) {
        if (classes == null) return;
        for (Class<?> clazz : classes) {
            this.loadClass(clazz);
        }
    }

    public void loadClass(Class<?> clazz) {
        if (clazz == null) return;
        for (Method method : clazz.getMethods()) {
            if (!method.isAnnotationPresent(HttpRequest.class)) continue;
            if (!method.accessFlags().contains(AccessFlag.PUBLIC)) continue;
            if (!method.accessFlags().contains(AccessFlag.STATIC)) continue;
            HttpRequest request = method.getAnnotation(HttpRequest.class);
            this.http.createContext(request.value(), exchange -> {
                if (verifyIsError(exchange, request.method())) return;
                try {
                    if (method.getParameterCount() == 1) method.invoke(null, exchange);
                    else method.invoke(null);
                } catch (Exception e) {
                    RTOSHttpServer.sendError(exchange, e.getMessage());
                }
            });
            RTOSConnector.LOGGER.info("Http method [{}] {}${}() is loaded!", request.method(), clazz.getSimpleName(), method.getName());
        }
    }

    public void start() {
        this.http.start();
        RTOSConnector.LOGGER.info("HttpServer started on *:{}.", this.http.getAddress().getPort());
    }

    public void stop() {
        this.http.stop(1);
        RTOSConnector.LOGGER.info("HttpServer stopped.");
    }

    public static boolean verifyIsError(@NotNull HttpExchange exchange, @NotNull String method) throws IOException {
        String currentMethod = exchange.getRequestMethod();
        if (!method.equals(currentMethod)) {
            RTOSHttpServer.sendError(exchange, "404 Not Found");
            return true;
        }
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        if (!RTOSConnector.CONFIG.token.equals(token)) {
            RTOSHttpServer.sendError(exchange, "令牌验证失败");
            return true;
        }
        return false;
    }

    public static void sendPass(HttpExchange exchange, Consumer<JsonObject> dataFactory) throws IOException {
        RTOSHttpServer.send(exchange, 0, "", dataFactory);
    }

    public static void sendError(HttpExchange exchange, String msg) throws IOException {
        RTOSHttpServer.send(exchange, -1, msg, null);
    }

    public static void send(HttpExchange exchange, int code, String msg, Consumer<JsonObject> dataFactory) throws IOException {
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
