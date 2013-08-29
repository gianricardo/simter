/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim;

import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * The JSimCalendar class holds information about all scheduled events within a J-Sim simulation. It contains a list of JSimCalendarEvent
 * elements, ordered by their simulation time. J-Sim simulations use this class to schedule processes and to get information about a process
 * which should be run during the next simulation step. You should never need to create an instance of this class.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.0.1
 */
public class JSimCalendar
{
	/**
	 * Constant signalling that there is no process scheduled.
	 */
	public static final JSimProcess NOBODY = null;

	/**
	 * Constant signalling that there is no process scheduled.
	 */
	public static final double NEVER = -1.0;

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * This list holds all events of the calendar. Events are always kept ordered, because the list is sorted after every insertion.
	 */
	private LinkedList<JSimCalendarEvent> eventList;

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a new empty calendar.
	 */
	public JSimCalendar()
	{
		eventList = new LinkedList<JSimCalendarEvent>();
	} // constructor

	/**
	 * Adds a new event with the specified time and the specified process to the calendar.
	 * 
	 * @param absTime
	 *            The absolute simulation time of the event.
	 * @param process
	 *            The process to be later activated.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the absolute simulation time is negative or the process is not specified (null).
	 */
	public synchronized void addEntry(double absTime, JSimProcess process) throws JSimInvalidParametersException
	{
		JSimCalendarEvent newEntry;

		if (absTime < 0.0)
			throw new JSimInvalidParametersException("JSimCalendar.addEntry(): absTime");
		if (process == null)
			throw new JSimInvalidParametersException("JSimCalendar.addEntry(): process");

		newEntry = new JSimCalendarEvent(absTime, process);
		eventList.add(newEntry);
		Collections.sort(eventList);
	} // addEntry

	/**
	 * Adds the specified event to the calendar. This is useful when you already have an event (from outside) and want to add it to the
	 * calendar.
	 * 
	 * @param calendarEvent
	 *            The event to be added to the calendar.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the absolute simulation time is negative or the process is not specified (null).
	 */
	public synchronized void addWholeEvent(JSimCalendarEvent calendarEvent) throws JSimInvalidParametersException
	{
		if (calendarEvent.getTime() < 0.0)
			throw new JSimInvalidParametersException("JSimCalendar.addWholeEvent(): calendarEvent.absTime");
		if (calendarEvent.getProcess() == null)
			throw new JSimInvalidParametersException("JSimCalendar.addWholeEvent(): calendarEvent.process");

		eventList.add(calendarEvent);
		Collections.sort(eventList);
	} // addWholeEvent

	/**
	 * Deletes one or all events of a process from the calendar. The rest of J-Sim assures that a process will not have more than one event
	 * in the calendar so the second atribute is more or less useless.
	 * 
	 * @param process
	 *            The process whose event(s) are to be deleted.
	 * @param all
	 *            A flag saying that not only one but all events of the given process should be deleted.
	 * 
	 * @return The number of events deleted.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the process is not specified (null).
	 */
	public synchronized int deleteEntries(JSimProcess process, boolean all) throws JSimInvalidParametersException
	{
		JSimCalendarEvent entry;
		ListIterator<JSimCalendarEvent> it;
		int noOfDeleted = 0;

		if (process == null)
			throw new JSimInvalidParametersException("JSimCalendar.deleteEntries(): process");

		it = eventList.listIterator(0);
		while (it.hasNext() && ((all == true) || (noOfDeleted < 1)))
		{
			entry = it.next();
			if (entry.getProcess() == process)
			{
				it.remove();
				noOfDeleted++;
			} // if
		} // while

		return noOfDeleted;
	} // deleteEntries

	/**
	 * Returns the first scheduled process. Its calendar event is at the head of the event list because it is always sorted.
	 * 
	 * @return The first scheduled process or NOBODY if the calendar is empty.
	 */
	public synchronized JSimProcess getFirstProcess()
	{
		if (eventList.size() < 1)
			return NOBODY;
		else
			return (eventList.getFirst()).getProcess();
	} // getFirstProcess

	/**
	 * Returns the time of the event being at the head of the calendar.
	 * 
	 * @return The time of the event being at the head of the calendar or NEVER if the calendar is empty.
	 */
	public synchronized double getFirstProcessTime()
	{
		if (eventList.size() < 1)
			return NEVER;
		else
			return (eventList.getFirst()).getTime();
	} // getFirstProcessTime

	/**
	 * Deletes the event at the head of the calendar and sets the head to the event which follows the current head.
	 */
	public synchronized void jump()
	{
		if (eventList.size() > 0)
			eventList.removeFirst();
	} // jump

	/**
	 * Says whether the calendar is empty. The calendar is empty if it contains no events.
	 * 
	 * @return True if it is empty, false otherwise.
	 */
	public synchronized boolean isEmpty()
	{
		return (eventList.size() < 1);
	} // isEmpty

	/**
	 * Returns a string representation of the calendar.
	 * 
	 * @return A string representation of the calendar.
	 */
	public String toString()
	{
		return eventList.toString();
	} // toString

} // class JSimCalendar

