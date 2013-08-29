/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim;

/**
 * A JSimCalendarEvent represents one wake-up event in a J-Sim calendar. It contains information about a process and a time point when the
 * process should be activated. You should never need to create an instance of this class. Note: This class has a natural ordering that is
 * inconsistent with equals().
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.0.1
 */
public class JSimCalendarEvent implements Comparable<JSimCalendarEvent>
{
	/**
	 * The simulation time point the process is scheduled for.
	 */
	private double time;

	/**
	 * The process to be activated (woken up).
	 */
	private JSimProcess process;

	/**
	 * The process's number. This field is serialized instead of the process itself.
	 */
	private long processNumber;

	/**
	 * The real (wallclock) time of the event's creation. Used only for deterministic ordering of events in the calendar (later created,
	 * later interpreted).
	 */
	private long creationTime;

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a new calendar event with the specified time and process.
	 * 
	 * @param time
	 *            The absolute simulation time (not a delta) of activation.
	 * @param process
	 *            The process to be activated.
	 * 
	 * @exception NullPointerException
	 *                This exception is thrown out if the process to be activated is null.
	 */
	public JSimCalendarEvent(double time, JSimProcess process) throws NullPointerException
	{
		if (process == null)
			throw new NullPointerException("JSimCalendarEvent.JSimCalendarEvent(): process");
		this.time = time;
		this.process = process;
		this.processNumber = process.getProcessNumber();
		this.creationTime = System.currentTimeMillis();
	} // constructor

	/**
	 * Returns the absolute activation time of the event.
	 * 
	 * @return The activation time of the event.
	 */
	public double getTime()
	{
		return time;
	} // getTime

	/**
	 * Returns the process referred to by the event.
	 * 
	 * @return The process referred to by the event.
	 */
	public JSimProcess getProcess()
	{
		return process;
	} // getProcess

	/**
	 * Returns the number of the process referred to by the event.
	 * 
	 * @return The number of the process referred to by the event.
	 */
	public long getProcessNumber()
	{
		return processNumber;
	} // getProcessNumber

	/**
	 * Returns this event's creation time. The creation time is measured in milliseconds and it is equal to the difference between the
	 * current time and midnight January 1 1970.
	 * 
	 * @return The event's creation time.
	 */
	protected long getCreationTime()
	{
		return creationTime;
	} // getCreationTime

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Compares this event with another one. Returns a negative integer, zero, or a positive integer as this event is less than, equal to,
	 * or greater than the specified object. It is assumed that the argument is also a JSimCalendarEvent. Note: This class has a natural
	 * ordering that is inconsistent with equals().
	 * 
	 * @return Zero (0) if both the corresponding simulation time and the real time of creation are equal, (-1) if the simulation time (or
	 *         the creation time when the simulation times are equal) of this event is less than the other event's simulation time, and (+1)
	 *         if the simulation time of this event is greater than the other event's time.
	 * 
	 * @exception ClassCastException
	 *                This exception is thrown out when the specified object cannot be typecasted to JSimCalendarEvent.
	 */
	public int compareTo(JSimCalendarEvent ce)
	{
		if (this.time == ce.time)
		{
			if (this.creationTime == ce.creationTime)
				return 0;
			else
				if (this.creationTime < ce.creationTime)
					return -1;
				else
					return +1;
		} // if time == time
		else
			if (this.time < ce.time)
				return -1;
			else
				return +1;
	} // compareTo

	/**
	 * Returns a string representation of the calendar event. It consists of the corresponding process name, the simulation time when the
	 * process will be activated and the real time of the event's creation.
	 * 
	 * @return A string representation of the calendar event.
	 */
	public String toString()
	{
		return process.getName() + "->" + Double.toString(time) + "[created at " + Long.toString(creationTime) + "]";
	} // toString

} // class JSimCalendarEvent
