package fun.rtos.bot.http;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record PlayerMsg(String player, String msg) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerMsg playerMsg = (PlayerMsg) o;
        return Objects.equals(msg, playerMsg.msg) && Objects.equals(player, playerMsg.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, msg);
    }

    @Override
    public @NotNull String toString() {
        return "PlayerMsg{player='%s', msg='%s'}".formatted(player, msg);
    }
}
