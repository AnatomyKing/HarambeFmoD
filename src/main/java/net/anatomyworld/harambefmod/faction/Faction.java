package net.anatomyworld.harambefmod.faction;

import java.util.Locale;

public enum Faction {
    BELMONT,
    DYNASTY,
    IMPERIUM,
    MISCHIEF;

    /** Map a scoreboard team name to a faction (null if not a known faction). */
    public static Faction fromTeamName(String teamName) {
        if (teamName == null) return null;
        try {
            return Faction.valueOf(teamName.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
