/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim.ipc;

import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimProcess;

/**
 * A message whose sender is known but the receiver is not. Such a message cannot be sent directly to a process, only via a message box. Any
 * process will be then able to pick up the message from the message box. The receiving process will be able to determine the sender.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.3.0
 */
public class JSimMessageFromSender extends JSimAssymetricMessage implements JSimMessageWithKnownSender
{
	/**
	 * Creates a new message from sender with the specified sender and user data. The default value of message type will be used.
	 * 
	 * @param sender
	 *            The sender. Must always be specified.
	 * @param data
	 *            User data that the message will carry.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the sender is not specified.
	 */
	public JSimMessageFromSender(JSimProcess sender, Object data) throws JSimInvalidParametersException
	{
		super(sender, JSimMessage.UNKNOWN_RECEIVER, data);
	} // constructor

	/**
	 * Creates a new message from sender with the specified sender, user data, and message type.
	 * 
	 * @param sender
	 *            The sender. Must always be specified.
	 * @param data
	 *            User data that the message will carry.
	 * @param messageType
	 *            The type of the message. Must be non-negative.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the message type is negative or if the sender is not specified.
	 */
	public JSimMessageFromSender(JSimProcess sender, Object data, int messageType) throws JSimInvalidParametersException
	{
		super(sender, JSimMessage.UNKNOWN_RECEIVER, data, messageType);
	} // constructor

} // class JSimMessageFromSender
