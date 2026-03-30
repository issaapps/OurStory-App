package com.love.essahazama;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Single source of truth for the two key dates.
 *
 *  FIRST_TALK  = 25 Oct 2025  → "بداية المعرفة"  (counter starts here)
 *  CONFESSION  = 11 Dec 2025  → "الاعتراف بالحب" (special milestone)
 */
public final class LoveDates {

    // ── أول محادثة ─────────────────────────────────────────
    public static final int TALK_YEAR  = 2025;
    public static final int TALK_MONTH = Calendar.OCTOBER;   // 0-indexed
    public static final int TALK_DAY   = 25;

    // ── الاعتراف بالحب ────────────────────────────────────
    public static final int CONF_YEAR  = 2025;
    public static final int CONF_MONTH = Calendar.DECEMBER;  // 0-indexed
    public static final int CONF_DAY   = 11;

    private LoveDates() {}

    // ── helpers ───────────────────────────────────────────

    /** Calendar for first-talk date, midnight. */
    public static Calendar talkCalendar() {
        Calendar c = Calendar.getInstance();
        c.set(TALK_YEAR, TALK_MONTH, TALK_DAY, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    /** Calendar for confession date, midnight. */
    public static Calendar confessionCalendar() {
        Calendar c = Calendar.getInstance();
        c.set(CONF_YEAR, CONF_MONTH, CONF_DAY, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    /** Total days since first talk (never negative). */
    public static long daysSinceTalk() {
        long ms = System.currentTimeMillis() - talkCalendar().getTimeInMillis();
        return ms > 0 ? TimeUnit.MILLISECONDS.toDays(ms) : 0;
    }

    /** Total days since confession (never negative). */
    public static long daysSinceConfession() {
        long ms = System.currentTimeMillis() - confessionCalendar().getTimeInMillis();
        return ms > 0 ? TimeUnit.MILLISECONDS.toDays(ms) : 0;
    }

    /** Milliseconds elapsed since first talk. */
    public static long msSinceTalk() {
        long ms = System.currentTimeMillis() - talkCalendar().getTimeInMillis();
        return ms > 0 ? ms : 0;
    }

    /**
     * Breaks msSinceTalk() into { years, months, days, hours, minutes, seconds }.
     * Returns int[6].
     */
    public static int[] breakdown() {
        Calendar start = talkCalendar();
        Calendar now   = Calendar.getInstance();

        int years = 0, months = 0;
        Calendar temp = (Calendar) start.clone();

        while (true) {
            Calendar next = (Calendar) temp.clone();
            next.add(Calendar.YEAR, 1);
            if (!next.after(now)) { years++; temp = next; } else break;
        }
        while (true) {
            Calendar next = (Calendar) temp.clone();
            next.add(Calendar.MONTH, 1);
            if (!next.after(now)) { months++; temp = next; } else break;
        }

        long remMs   = now.getTimeInMillis() - temp.getTimeInMillis();
        long remDays = TimeUnit.MILLISECONDS.toDays(remMs);
        long hours   = TimeUnit.MILLISECONDS.toHours(remMs)   % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(remMs) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(remMs) % 60;

        return new int[]{ years, months, (int) remDays,
                          (int) hours, (int) minutes, (int) seconds };
    }
}
