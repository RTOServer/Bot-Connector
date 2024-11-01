package net.cjsah.rtos.bot.http;

import java.util.Objects;

public record CommandBody(String command) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandBody that = (CommandBody) o;
        return Objects.equals(command, that.command);
    }

    @Override
    public String toString() {
        return "CommandBody{" +
                "command='" + command + '\'' +
                '}';
    }
}
