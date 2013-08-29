/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim.ipc;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimProcess;

/**
 * A message box is a data structure able to keep messages sent between processes. Every J-Sim process has its own message box that serves
 * as a message store for messages sent directly to the process. However, a message box can be also created explicitly by the user to
 * provide a mean of indirect communication where neither the sender nor the receiver need to be known to the opposite side.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.3.0
 */
public class JSimMessageBox
{
	/**
	 * Common logger for all instances of this class. By default, all logging information goes to a file. Only severe events go to the
	 * console, in addition to a file.
	 */
	private static final Logger logger;

	/**
	 * This message box's name.
	 */
	private String myName;

	/**
	 * A list of all messages that this message box keeps.
	 */
	protected LinkedList<JSimMessage> messages;

	/**
	 * A list of all processes that got suspended when they sent a message to this message box.
	 */
	protected LinkedList<JSimProcess> suspendedSenders;

	/**
	 * A list of all processes that got suspended when they tried to receive a message from this message box.
	 */
	protected LinkedList<JSimProcess> suspendedReceivers;

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * The static block initializes all static attributes.
	 */
	static
	{
		logger = Logger.getLogger(JSimMessageBox.class.getName());
	} // static

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a new message box with the specified name.
	 * 
	 * @param name
	 *            The name of the message box.
	 */
	public JSimMessageBox(String name)
	{
		myName = name;
		messages = new LinkedList<JSimMessage>();
		suspendedSenders = new LinkedList<JSimProcess>();
		suspendedReceivers = new LinkedList<JSimProcess>();
	} // constructor

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Returns the name of the message box.
	 * 
	 * @return The name of the message box.
	 */
	public String getName()
	{
		return myName;
	} // getName

	/**
	 * Adds a new message to the message box.
	 * 
	 * @param message
	 *            The message to be added to the box.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the specified message is null.
	 */
	public void addMessage(JSimMessage message) throws JSimInvalidParametersException
	{
		if (message == null)
			throw new JSimInvalidParametersException("JSimMessageBox.addMessage(): message");

		messages.addLast(message);
	} // addMessage

	/**
	 * Returns the first message in the message box and removes it from there. If there are no messages in the message box, null is
	 * returned.
	 * 
	 * @return The first message in the message box or null.
	 */
	public JSimMessage getFirstMessage()
	{
		try
		{
			if (messages.size() < 1)
				return null;
			else
				return messages.removeFirst();
		} // try
		catch (NoSuchElementException e1)
		{
			logger.log(Level.WARNING, "A message was not found in a message box.", e1);
			return null;
		} // catch
		catch (ClassCastException e2)
		{
			logger.log(Level.WARNING, "A message of a wrong type was found.", e2);
			return null;
		} // catch
	} // getFirstMessage

	/**
	 * Return the first message whose sender is equal to the specified sender. The message is removed from the message box. The returned
	 * message is always an instance of a class implementing JSimMessageWithKnownSender. If no such message is found, null is returned.
	 * 
	 * @param sender
	 *            The process that sent the message we are looking for. Must not be null.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the sender is not specified.
	 */
	public JSimMessage getFirstMessageFromSender(JSimProcess sender) throws JSimInvalidParametersException
	{
		Iterator<JSimMessage> it;
		JSimMessage m;
		JSimMessageWithKnownSender mwks;

		if (sender == null)
			throw new JSimInvalidParametersException("JSimMessageBox.getFirstMessageFromSender(): sender");

		it = messages.iterator();
		while (it.hasNext())
		{
			m = it.next();
			if (m instanceof JSimMessageWithKnownSender)
			{
				mwks = (JSimMessageWithKnownSender) m;
				if (mwks.getSender() == sender)
				{
					it.remove();
					return m;
				}
			} // if
		} // while

		return null;
	} // getFirstMessageFromSender

	/**
	 * Return the first message whose receiver is equal to the specified receiver. The message is removed from the message box. The returned
	 * message is always an instance of a class implementing JSimMessageWithKnownReceiver. If no such message is found, null is returned.
	 * 
	 * @param receiver
	 *            The process that the message we are looking for was sent to. Must not be null.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the receiver is not specified.
	 */
	public JSimMessage getFirstMessageForReceiver(JSimProcess receiver) throws JSimInvalidParametersException
	{
		Iterator<JSimMessage> it;
		JSimMessage m;
		JSimMessageWithKnownReceiver mwkr;

		if (receiver == null)
			throw new JSimInvalidParametersException("JSimMessageBox.getFirstMessageForReceiver(): receiver");

		it = messages.iterator();
		while (it.hasNext())
		{
			m = it.next();
			if (m instanceof JSimMessageWithKnownReceiver)
			{
				mwkr = (JSimMessageWithKnownReceiver) m;
				if (mwkr.getReceiver() == receiver)
				{
					it.remove();
					return m;
				}
			} // if
		} // while

		return null;
	} // getFirstMessageForReceiver

	/**
	 * Returns the first message whose receiver is equal to the specified receiver and whose sender is equal to the specified sender. If the
	 * sender is not specified (UNKNOWN_SENDER), the sender of the message has no importance and messages will be filtered by receiver only.
	 * If the receiver is not specified (UNKNOWN_RECEIVER), the receiver of the message has no importance and messages will be filtered by
	 * sender only. If the message does not have its sender/receiver specified, it will be accepted whatever the value of the desired
	 * sender/receiver is. The message is removed from the box. If no such message is found, null is returned.
	 * 
	 * @param possibleSender
	 *            The sender of the message we are looking for, or UNKNOWN_SENDER for any sender.
	 * @param possibleReceiver
	 *            The receiver of the message we are looking for, or UNKNOWN_RECEIVER for any receiver.
	 * 
	 * @return The first message whose receiver is equal to the specified receiver and whose sender is equal to the specified sender.
	 */
	public JSimMessage getFirstMessageFromAndFor(JSimProcess possibleSender, JSimProcess possibleReceiver)
	{
		Iterator<JSimMessage> it;
		JSimMessage m;

		it = messages.iterator();
		while (it.hasNext())
		{
			m = it.next();
			if (((possibleSender == JSimMessage.UNKNOWN_SENDER) || (possibleSender == m.getSender()) || (m.getSender() == JSimMessage.UNKNOWN_SENDER)) && ((possibleReceiver == JSimMessage.UNKNOWN_RECEIVER) || (possibleReceiver == m.getReceiver()) || (m.getReceiver() == JSimMessage.UNKNOWN_RECEIVER)))
			{
				it.remove();
				return m;
			} // if
		} // while

		return null;
	} // getFirstMessageFromAndFor

	/**
	 * Returns the number of messages in the message box.
	 * 
	 * @return The number of messages in the message box.
	 */
	public int getNumberOfMessages()
	{
		return messages.size();
	} // getNumberOfMessages

	/**
	 * Indicates whether this message box is empty.
	 * 
	 * @return True if the message box is empty, false otherwise.
	 */
	public boolean isEmpty()
	{
		return (messages.size() == 0);
	} // isEmpty

	// --------------------------------------------------------------------------------------------------

	/**
	 * Indicates whether the specified process is among senders suspended on this message box. If it is, true is returned and the sender is
	 * removed from the set of suspended senders. It is assumed that the calling process will supply the real sender of the message being
	 * received not the sender specified by the user.
	 * 
	 * @param sender
	 *            The sender that is or is not among suspended senders. Must not be null.
	 * 
	 * @return True if the specified sender is among senders suspended on this message box, false otherwise.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the sender is not specified (null).
	 */
	public boolean containsSuspendedSender(JSimProcess sender) throws JSimInvalidParametersException
	{
		Iterator<JSimProcess> it;
		JSimProcess p;

		if (sender == null)
			throw new JSimInvalidParametersException("JSimMessageBox.containsSuspendedSender(): sender");

		it = suspendedSenders.iterator();
		while (it.hasNext())
		{
			p = it.next();
			if (p == sender)
			{
				it.remove();
				return true;
			} // if process found
		} // while

		return false;
	} // containsSuspendedSender

	/**
	 * Returns the first process that is suspended on receiving a message from this message box. It there are no suspended receivers on this
	 * message box, null is returned. The receiver is removed from the collection of suspended receivers. Certain criteria must be met:
	 * 
	 * <ol>
	 * 
	 * <li>
	 * If the receiver is specified, no other process can be returned. If it not specified, any process can be returned.
	 * </li>
	 * 
	 * <li>
	 * If the sender is specified, it must be equal to the sender that the selected process is waiting for. If a suspended process does
	 * not have its sender specified, it can be selected without any regard to the possibleSender argument.
	 * </li>
	 * 
	 * <li>
	 * The selected process must have its message clipboard empty. Full clipboard means that the process has just got a message but has
	 * not run since then yet. Actually, no process with full clipboard should be in the queue.
	 * </li>
	 * 
	 * </ol>
	 * 
	 * @param possibleReceiver
	 *            A process that can be returned by this method or UNKNOWN_RECEIVER for any receiver.
	 * @param possibleSender
	 *            A process that the selected process must be waiting for of UNKNOWN_SENDER for any sender. If a suspended process has not
	 *            its waiting-for-sender specified, this value will be ignored for the suspended process.
	 * 
	 * @return The first process that is suspended on receiving a message from this message box, or null.
	 */
	public JSimProcess getFirstSuspendedReceiver(JSimProcess possibleReceiver, JSimProcess possibleSender)
	{
		Iterator<JSimProcess> it;
		JSimProcess p;
		JSimProcess selectedProcess = null;

		it = suspendedReceivers.iterator();
		while ((it.hasNext()) && (selectedProcess == null))
		{
			p = it.next();
			if ((p.hasEmptyMessageClipboard()) && ((possibleReceiver == JSimMessage.UNKNOWN_RECEIVER) || (possibleReceiver == p)) && ((possibleSender == JSimMessage.UNKNOWN_SENDER) || (p.getSenderIAmWaitingFor() == JSimMessage.UNKNOWN_SENDER) || (possibleSender == p.getSenderIAmWaitingFor())))
			{
				selectedProcess = p;
				it.remove();
			} // if
		} // while

		return selectedProcess;
	} // getFirstSuspendedReceiver

	/**
	 * Adds a new process to the collection of processes suspended on sending a message. The method itself does not block the process, this
	 * must be done outside. The method assumes that the real sender is passed as the first argument, not the process specified as sender in
	 * the message (which can be null). Null values are not accepted.
	 * 
	 * @param sender
	 *            The process getting blocked on sending a message via this message box.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the sender is not specified.
	 */
	public void addSuspendedSender(JSimProcess sender) throws JSimInvalidParametersException
	{
		if (sender == null)
			throw new JSimInvalidParametersException("JSimMessageBox.addSuspendedSender(): sender");

		suspendedSenders.addLast(sender);
	} // addSuspendedSender

	/**
	 * Adds a new process to the collection of processes suspended on receiving a message. The method itself does not block the process,
	 * this must be done outside.
	 * 
	 * @param receiver
	 *            The process getting blocked on receiving a message via this message box.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the receiver is not specified.
	 */
	public void addSuspendedReceiver(JSimProcess receiver) throws JSimInvalidParametersException
	{
		if (receiver == null)
			throw new JSimInvalidParametersException("JSimMessageBox.addSuspendedSender(): receiver");

		suspendedReceivers.addLast(receiver);
	} // addSuspendedReceiver

} // class JSimMessageBox
