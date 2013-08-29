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
 * A message whose receiver is known but the sender is not. Such a message can be sent directly to a process or sent via a message box. The
 * receiving process will never know the sender.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.3.0
 */
public class JSimMessageForReceiver extends JSimAssymetricMessage implements JSimMessageWithKnownReceiver
{
	/**
	 * Creates a new message for receiver with the specified receiver and user data. The default value of message type will be used.
	 * 
	 * @param receiver
	 *            The receiver. Must always be specified.
	 * @param data
	 *            User data that the message will carry.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the receiver is not specified.
	 */
	public JSimMessageForReceiver(JSimProcess receiver, Object data) throws JSimInvalidParametersException
	{
		super(JSimMessage.UNKNOWN_SENDER, receiver, data);
	} // constructor

	/**
	 * Creates a new message for receiver with the specified receiver, user data, and message type.
	 * 
	 * @param receiver
	 *            The receiver. Must always be specified.
	 * @param data
	 *            User data that the message will carry.
	 * @param messageType
	 *            The type of the message. Must be non-negative.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the message type is negative or when the receiver is not specified.
	 */
	public JSimMessageForReceiver(JSimProcess receiver, Object data, int messageType) throws JSimInvalidParametersException
	{
		super(JSimMessage.UNKNOWN_SENDER, receiver, data, messageType);
	} // constructor

} // class JSimMessageForReceiver
