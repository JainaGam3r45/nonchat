package com.nonxedy.nonchat.util.chat.filters;

import static junit.framework.Assert.*;

import java.util.List;

import org.junit.Test;

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
}
