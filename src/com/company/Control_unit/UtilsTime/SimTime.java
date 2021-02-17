package com.company.Control_unit.UtilsTime;

/**
 *  The SimTime object keeps simulated time, tracking the day of the week and
 *  the time to the second. It can also speed up and slow down, and stop and
 *  start, allowing control over the simulation.
 *
 *  SimTime is a singleton class, so there can only be one instance of it.
 *
 *  SimTime is also a subject in the Observer pattern, so many objects can
 *  register for time passage notification.  The SimTime object notifies
 *  its observers every simulated second.
 *
 *  When the SimTime instance is created, it begins at the present time.
 *  It begins stopped.
 *
 *  SimTime speeds can be set from 1 to 1000. A clock speed of 1 is real time;
 *  other speeds are multiples of real time.
 *
 *     @author  C. Fox
 *    @version  01/05
 *
 *  @invariant  tickInterval = 1000/speed
 *              speed is in range 0..1000
 *              hour is in range 0..23
 *              minute is in range 0..59
 *              second is in range 0..59
 */

import java.util.Calendar;
import java.util.Date;
import java.util.Observable;


public class SimTime extends Observable implements Runnable {

    /* class attributes
     *******************/

    private static SimTime instance                = null;  // the instance
    private static final int DEFAULT_TICK_INTERVAL = 1000;  // real time

    /* class methods
     ****************/

    /**
     *  Return the single instance of the class.
     *
     *     @pre none
     *    @post @result == the single instance
     */

    public static SimTime instance() {
        if ( instance == null ) instance = new SimTime();
        return( instance );
    }

      /* attributes
      /*************/

    private Day day;                // the current day
    private int     hour;               // the current hour
    private int     minute;             // the current minute
    private int     second;             // the current second
    private long    tickInterval;       // how long between notifications
    private boolean isRunning;          // for starting/stopping the clock
    private Thread  pulseThread;        // for heartbeat

      /* constructors
      /***************/

    /**
     *  Create the unique time simulation object.
     *
     *     @pre none
     *    @post simulated time is stopped
     */

    public SimTime() {

        // start out the day and time at right now
        Calendar now = Calendar.getInstance();
        second = now.get( Calendar.SECOND );
        minute = now.get( Calendar.MINUTE );
        hour   = now.get( Calendar.HOUR_OF_DAY );
        switch ( now.get(Calendar.DAY_OF_WEEK) )
        {
            case Calendar.MONDAY:    day = Day.MONDAY;    break;
            case Calendar.TUESDAY:   day = Day.TUESDAY;   break;
            case Calendar.WEDNESDAY: day = Day.WEDNESDAY; break;
            case Calendar.THURSDAY:  day = Day.THURSDAY;  break;
            case Calendar.FRIDAY:    day = Day.FRIDAY;    break;
            case Calendar.SATURDAY:  day = Day.SATURDAY;  break;
            case Calendar.SUNDAY:    day = Day.SUNDAY;    break;
            default:                 day = Day.MONDAY;    break;
        }

        // default speed is real time
        tickInterval = DEFAULT_TICK_INTERVAL;

        // time is not running to start with
        isRunning = false;
        pulseThread = null;
    }

      /* methods
      /**********/

    /**
     *  Set the day of the week and notify observers.
     *
     *     @pre none
     *    @post simulated time is changed
     *   @param newDay  The day of the week in Day.MONDAY..Day.SUNDAY.
     */

    public synchronized void setDay( Day newDay ) {
        day = newDay;
        setChanged();
        notifyObservers();
    }

    /**
     *  Get the simulated day.
     */

    public synchronized Day getDay() { return day; }

    /**
     *  Set the simulated hour and notify observers.
     *
     *     @pre newHour in range 0..23
     *    @post simulated time is changed
     *   @param newHour  The simulated hour on a 24-hour clock
     *  @throws IllegalArgumentException if precondition violated
     */

    public synchronized void setHour( int newHour ) {
        if ( (newHour < 0) || (23 < newHour) )
            throw new IllegalArgumentException();

        hour = newHour;
        setChanged();
        notifyObservers();
    }

    /**
     *  Get the simulated hour.
     */

    public synchronized int getHour() { return hour; }

    /**
     *  Set the simulated minute and notify observers.
     *
     *     @pre newMinute in range 0..59
     *    @post simulated time is changed
     *   @param newMinute  The minute on a clock
     *  @throws IllegalArgumentException if precondition violated
     */

    public synchronized void setMinute( int newMinute ) {
        if ( (newMinute < 0) || (59 < newMinute) )
            throw new IllegalArgumentException();

        minute = newMinute;
        setChanged();
        notifyObservers();
    }

    /**
     *  Get the simulated minute.
     */

    public synchronized int getMinute() {
        return minute;
    }

    /**
     *  Set the simulated second and notify observers.
     *
     *     @pre newSecond in range 0..59
     *    @post simulated time is changed
     *   @param newSecond  The second on a clock
     *  @throws IllegalArgumentException if precondition violated
     */

    public synchronized void setSecond( int newSecond ) {
        if ( (newSecond < 0) || (59 < newSecond) )
            throw new IllegalArgumentException();

        second = newSecond;
        setChanged();
        notifyObservers();
    }

    /**
     *  Get the simulated second.
     */

    public synchronized int getSecond() { return second; }

    /**
     *  Set the simulated time speed.
     *
     *     @pre speed in range 1..1000
     *    @post the rate of simulated time is changed
     *   @param speed  The speed of simulated time
     *  @throws IllegalArgumentException if precondition violated
     */

    public void setSpeed(int speed) {
        if ( (speed < 1) || (10000 < speed) )
            throw new IllegalArgumentException();

        tickInterval = 1000/speed;
    }

    /**
     *  Set the speed to its default.
     */

    void setSpeedToDefault() { tickInterval = DEFAULT_TICK_INTERVAL; }

    /**
     *  Get the speed of simulated time.
     */

    int getSpeed() { return (int)(1000/tickInterval); }

    /**
     *  Start (or resume) simulated time. Calling this operation when
     *  simulated time is already running has no effect.
     *
     *     @pre none
     *    @post simulated time is going
     */

    public void start() {

        // if time is already going, do nothing
        if ( isRunning ) return;

        // make a new thread and get it started
        pulseThread = new Thread( this );
        pulseThread.start();

        // note that time is running
        isRunning = true;
    }

    /**
     *  Stop (or pause) simulated time. Calling this operation when
     *  simulated time is already stopped has no effect.
     *
     *     @pre none
     *    @post simulated time is stopped
     */

    public void stop() { isRunning = false; }

    /**
     *  Run the heartbeat thread. This routine is executed in its own
     *  thread object.
     *
     *  Note: This is the operation that makes time go. It runs
     *  continuously in its own thread, and changes the time
     *  every simulated second, then notifies objservers.
     *
     *     @pre none
     *    @post the thread is defunct
     */

    public void run() {

        Date now;             // this moment
        long wakeUpTime;      // when to next awake
        long sleepTime;       // how long to sleep

        now = new Date();
        wakeUpTime = now.getTime();
        while ( isRunning ) {

            // notify observers
            setChanged();
            notifyObservers();

            // sleep until tickInterval has passed
            wakeUpTime += tickInterval;
            now = new Date();
            sleepTime = wakeUpTime-now.getTime();

            try {
                if ( 5 < sleepTime ) pulseThread.sleep( sleepTime );
            }
            catch (InterruptedException e) {}

            // advance time
            synchronized( this ) {
                second++;
                if ( 60 <= second ) { minute++; second = 0; }
                if ( 60 <= minute ) { hour++; minute = 0; }
                if ( 24 <= hour )   { day = day.succ(); hour = 0; }
            }
        }
    }

} // SimTime
