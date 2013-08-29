/*
 * Copyright (c) 2006 Jaroslav Kačer <jaroslav@kacer.biz>
 * Licensed under the Academic Free License version 2.1
 * J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 * 
 */

package cz.zcu.fav.kiv.jsim;

/**
 * Process state can be one of the following constants:
 * <ul>
 * <li>New</li>
 * <li>Passive</li>
 * <li>Scheduled</li>
 * <li>Active</li>
 * <li>Blocked on Semaphore</li>
 * <li>Blocked on Message Send</li>
 * <li>Blocked on Message Received</li>
 * <li>Terminated</li>
 * </ul>
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.6.0
 */
public enum JSimProcessState
{
	/**
	 *	A process is new if it has been created but not started and not scheduled yet.
	 */
	NEW("New"),

	/**
	 *	A process is passive if it has been started and it has no event in the calendar.
	 */
	PASSIVE("Passive"),

	/**
	 *	A process is scheduled if it has an event in the calendar so it will run in the future.
	 */
	SCHEDULED("Scheduled"),

	/**
	 *	A process is active if it is running just now.
	 */
	ACTIVE("Active"),

	/**
	 *	A process gets blocked on a semaphore if it invokes its P() method and the semaphore's
	 *	internal counter is equal to zero.
	 */
	BLOCKED_ON_SEMAPHORE("Blocked on Semaphore"),

	/**
	 *	A process gets blocked on sending a message if the blocking version of the send method is used
	 *	and the receiver is not ready yet to read the message.
	 */
	BLOCKED_ON_MESSAGE_SEND("Blocked on Message Send"),

	/**
	 *	A process gets blocked on receiving a message if the blocking version of the receive method is used
	 *	and no message is currently available to be read.
	 */
	BLOCKED_ON_MESSAGE_RECEIVE("Blocked on Message Receive"),

	/**
	 *	A process is terminated if it has reached the end of its life() method.
	 */
	TERMINATED("Terminated");

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * A human-readable description of the process state.
	 */
	private final String humanReadableDescription;

	/**
	 * Creates a process state.
	 * 
	 * @param humanReadableDescription
	 *            A human-readable description.
	 */
	private JSimProcessState(String humanReadableDescription)
	{
		this.humanReadableDescription = humanReadableDescription;
	} // constructor

	/**
	 * Returns a human-readable description of the process state.
	 * 
	 * @return A human-readable description of the process state.
	 * 
	 * @see java.lang.Enum#toString()
	 */
	public String toString()
	{
		return humanReadableDescription;
	} // toString

} // enum JSimProcessState
