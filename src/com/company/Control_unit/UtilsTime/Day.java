package com.company.Control_unit.UtilsTime;


/**
 * The Day enumeration specifies enumeration values for the days of the week
 * used by any clock.
 *
 * @author C. Fox
 * @version 11/04
 */

public final class Day {

      /* enumeration values
      /*********************/

    public static final Day MONDAY = new Day(0, "Monday");
    public static final Day TUESDAY = new Day(1, "Tuesday");
    public static final Day WEDNESDAY = new Day(2, "Wednesday");
    public static final Day THURSDAY = new Day(3, "Thursday");
    public static final Day FRIDAY = new Day(4, "Friday");
    public static final Day SATURDAY = new Day(5, "Saturday");
    public static final Day SUNDAY = new Day(6, "Sunday");

      /* attributes
      /*************/

    private final int ordinal;   // count of the day of the week
    private final String name;      // what this day of the week is called

      /* constructors
      /***************/

    /**
     * Create the enumeration values.
     * Note that this constructor is private!
     *
     * @param ordinal Counter for the day of the week
     * @param name    The name of the day of the week
     * @pre 0 <= ordinal <= 6; name != null
     * @post new object has attributes set by parameters
     */

    private Day(int ordinal, String name) {
        this.ordinal = ordinal;
        this.name = name;
    }

      /* methods
      /**********/

    /**
     * Return the day preceding this day.
     *
     * @pre none
     * @post @result.toInt() + 1 == this.toInt() % 7
     */

    public Day pred() {
        return valueOf(ordinal + 6);
    }

    /**
     * Return the day succeeding this day.
     *
     * @pre none
     * @post @result.toInt() == (this.toInt()+1) % 7
     */

    public Day succ() {
        return valueOf(ordinal + 1);
    }

    /**
     * Get the ordinal of this day.
     *
     * @pre none
     * @post @return == ordinal number of this day, with Monday
     * having ordinal number 0.
     */

    public int toInt() {
        return ordinal;
    }

    /**
     * Get the name of this day.
     *
     * @pre none
     * @post @return == name of this day as a String.
     */

    public String toString() {
        return name;
    }

    /**
     * Get the day with a particular ordinal value.
     *
     * @param ord Any integer converted to an ordinal value. Monday has
     *            ordinal value 0.
     * @pre none
     * @post ord%7 == @result.toInt()
     */

    public static Day valueOf(int ord) {

        switch (ord % 7) {
            case 0:
                return MONDAY;
            case 1:
                return TUESDAY;
            case 2:
                return WEDNESDAY;
            case 3:
                return THURSDAY;
            case 4:
                return FRIDAY;
            case 5:
                return SATURDAY;
            case 6:
                return SUNDAY;
            default:
                throw new AssertionError();
        }
    }

    /**
     * Get the day from its name.
     *
     * @param str The string converted to a Day value
     * @pre str must be the name of a day; allowable values are fully
     * spelled out names (like "Monday") or three letter
     * abbreviations (like "Mon").
     * @post str == @result.toString() or
     * str == @result.toString().substring(0,2) or
     * throws IllegalArgumentException
     */

    public static Day valueOf(String str) {

        if (str.equalsIgnoreCase("Mon") ||
                str.equalsIgnoreCase("Monday")) return MONDAY;
        if (str.equalsIgnoreCase("Tue") ||
                str.equalsIgnoreCase("Tuesday")) return TUESDAY;
        if (str.equalsIgnoreCase("Wed") ||
                str.equalsIgnoreCase("Wednesday")) return WEDNESDAY;
        if (str.equalsIgnoreCase("Thu") ||
                str.equalsIgnoreCase("Thursday")) return THURSDAY;
        if (str.equalsIgnoreCase("Fri") ||
                str.equalsIgnoreCase("Friday")) return FRIDAY;
        if (str.equalsIgnoreCase("Sat") ||
                str.equalsIgnoreCase("Saturday")) return SATURDAY;
        if (str.equalsIgnoreCase("Sun") ||
                str.equalsIgnoreCase("Sunday")) return SUNDAY;

        throw new IllegalArgumentException();
    }

} // Day
