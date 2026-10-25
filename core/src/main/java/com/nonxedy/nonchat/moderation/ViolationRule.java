package com.nonxedy.nonchat.moderation;

import java.util.List;

/**
 * How the system reacts to one {@link ViolationType}
 * @param type Violation this rule applies to
 * @param warnMessage Message sent to the player on every violation (may be null or empty)
 * @param staffNotifyMessage Template for the 'notify-staff' action; {player} and {message} are replaced
 * @param staffNotifyPermission Permission needed to receive the staff notification (OPs always receive it)
 * @param consoleNotify Whether the staff notification is also sent to the console
 * @param tiers Sanction tiers, from lightest to heaviest
 */
public record ViolationRule(
        ViolationType type,
        String warnMessage,
        String staffNotifyMessage,
        String staffNotifyPermission,
        boolean consoleNotify,
        List<SanctionTier> tiers) {
}
