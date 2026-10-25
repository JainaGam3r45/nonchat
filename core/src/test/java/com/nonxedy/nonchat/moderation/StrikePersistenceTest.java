package com.nonxedy.nonchat.moderation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StrikePersistenceTest {

    private final UUID id = UUID.randomUUID();
    private final Player player = mock(Player.class);

    private SanctionsManager manager(File file, int strikesRequired) {
        when(player.getName()).thenReturn("Alice");
        when(player.getUniqueId()).thenReturn(id);
        ViolationDetector detector = new ViolationDetector() {
            @Override
            public String bypassPermission() {
                return null;
            }

            @Override
            public Optional<Violation> inspect(Player p, String message) {
                return Optional.of(new Violation(ViolationType.CAPS, id, message));
            }
        };
        ViolationRule rule = new ViolationRule(ViolationType.CAPS, null, null, null, false,
                List.of(new SanctionTier(strikesRequired, Duration.ofHours(1), List.of())));
        return new SanctionsManager(List.of(detector), type -> rule, line -> { }, file);
    }

    @Test
    void strikesSurviveARestart(@TempDir Path dir) {
        File file = dir.resolve("strikes.yml").toFile();

        SanctionsManager first = manager(file, 3);
        first.process(player, "ONE");
        first.process(player, "TWO");
        first.close();

        SanctionsManager second = manager(file, 3);
        assertEquals(2, second.getStrikes(id).size());
        assertEquals(ViolationType.CAPS, second.getStrikes(id).get(0).type());
        second.close();
    }

    @Test
    void clearedStrikesStayCleared(@TempDir Path dir) {
        File file = dir.resolve("strikes.yml").toFile();

        SanctionsManager first = manager(file, 3);
        first.process(player, "ONE");
        first.clearStrikes(id);
        first.close();

        assertTrue(manager(file, 3).getStrikes(id).isEmpty());
    }

    @Test
    void expiredAndBrokenEntriesAreIgnored(@TempDir Path dir) throws Exception {
        File file = dir.resolve("strikes.yml").toFile();
        long now = Instant.now().toEpochMilli();
        Files.writeString(file.toPath(), String.join("\n",
                id + ":",
                "  - {type: CAPS, issued: " + (now - 7200_000) + ", expires: " + (now - 3600_000) + "}",
                "  - {type: NOPE, issued: " + now + ", expires: " + (now + 3600_000) + "}",
                "  - {type: CAPS, issued: " + now + ", expires: " + (now + 3600_000) + "}",
                "not-a-uuid:",
                "  - {type: CAPS, issued: 1, expires: 2}",
                ""));

        Map<UUID, List<Strike>> loaded = new StrikeStorage(file).load();

        assertEquals(1, loaded.size());
        assertEquals(1, loaded.get(id).size());
    }

    @Test
    void clearingOneTypeKeepsTheOthers(@TempDir Path dir) {
        File file = dir.resolve("strikes.yml").toFile();
        SanctionsManager manager = manager(file, 3);
        manager.process(player, "ONE");
        manager.process(player, "TWO");

        assertEquals(0, manager.clearStrikes(id, ViolationType.ADVERTISING));
        assertEquals(2, manager.getStrikes(id).size());

        assertEquals(2, manager.clearStrikes(id, ViolationType.CAPS));
        assertTrue(manager.getStrikes(id).isEmpty());
        manager.close();
        assertTrue(manager(file, 3).getStrikes(id).isEmpty());
    }
}
