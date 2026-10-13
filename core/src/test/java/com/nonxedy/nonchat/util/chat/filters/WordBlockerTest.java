package com.nonxedy.nonchat.util.chat.filters;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class WordBlockerTest {

    @Test
    void blocksConfiguredWordsWithoutCaseSensitivity() {
        WordBlocker blocker = new WordBlocker(List.of("spam"), List.of());

        assertFalse(blocker.isMessageAllowed("This is SPAM"));
        assertTrue(blocker.isMessageAllowed("This is legitimate"));
    }

    @Test
    void blocksConfiguredRegularExpressions() {
        WordBlocker blocker = new WordBlocker(List.of(), List.of("free\\s+coins"));

        assertFalse(blocker.isMessageAllowed("Get FREE coins now"));
    }

    @Test
    void permitsMessageWhenConfiguredExpressionIsInvalid() {
        WordBlocker blocker = new WordBlocker(List.of(), List.of("[invalid"));

        assertTrue(blocker.isMessageAllowed("ordinary message"));
    }

    @Test
    void wholeWordModeIgnoresBannedWordInsideOtherWords() {
        WordBlocker blocker = new WordBlocker(List.of("ass", "плохой"), List.of(), true, List.of());

        assertTrue(blocker.isMessageAllowed("first class"));
        assertTrue(blocker.isMessageAllowed("оченьплохой день"));
        assertFalse(blocker.isMessageAllowed("you ASS!"));
        assertFalse(blocker.isMessageAllowed("плохой день"));
    }

    @Test
    void exceptionsAllowWordsContainingBannedWord() {
        WordBlocker blocker = new WordBlocker(List.of("ass"), List.of(), false, List.of("class"));

        assertTrue(blocker.isMessageAllowed("First CLASSES today"));
        assertFalse(blocker.isMessageAllowed("class ass"));
        assertFalse(blocker.isMessageAllowed("classass"));
    }
}
