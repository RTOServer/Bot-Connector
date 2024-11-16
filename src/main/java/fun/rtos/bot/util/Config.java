package fun.rtos.bot.util;

import fun.rtos.bot.RTOSConnector;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private final Path path;
    public String token = "RTOS_PWD";
    public int port = 25585;

    public Config(Path path) {
        this.path = path;
    }

    public void loadOrCreate() {
        if (!Files.exists(this.path)) this.save();
        else this.load();
    }

     public void load(){
        try (var reader = Files.newBufferedReader(path)) {
            this.parseRecord(RTOSConnector.GSON.fromJson(reader, Record.class));
        } catch (IOException e) {
            RTOSConnector.LOGGER.error(e.getMessage(), e);
        }
     }

    public void save() {
        try (var writer = Files.newBufferedWriter(this.path)) {
            writer.write(RTOSConnector.GSON.toJson(this.toRecord()));
        } catch (IOException e) {
            RTOSConnector.LOGGER.error(e.getMessage(), e);
        }
    }

    public Record toRecord() {
        return new Record(this.token, String.valueOf(this.port));
    }

    public void parseRecord(@NotNull Record record) {
        this.token = record.token;
        this.port = Integer.parseInt(record.port);
    }

    public record Record(String token, String port) {
    }
}
