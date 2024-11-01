package net.cjsah.rtos.bot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonUtil {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);

    public static String serialize(Object obj) {
        return GSON.toJson(obj);
    }

    public static <T> T deserialize(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    public static <T> T deserialize(InputStream is, Class<T> clazz) {
        try (InputStreamReader isr = new InputStreamReader(is)) {
            return GSON.fromJson(isr, clazz);
        } catch (IOException e) {
            log.error("Error deserializing json", e);
            return null;
        }
    }


}
