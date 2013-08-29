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
 * An assymetric message is a message where only the sender or the receiver is known but not both. This is an abstract class that serves as
 * a base for MessageFromSender and MessageForReceiver.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.3.0
 */
public abstract class JSimAssymetricMessage extends JSimMessage
{
	/**
	 * Creates a new assymetric message with the specified sender, receiver, and user data. The default value of message type will be used.
	 * 
	 * @param sender
	 *            The sending process. Must not be specified if the receiver is specified.
	 * @param receiver
	 *            The receiver. Must not be specified if the sender is specified.
	 * @param data
	 *            User data that the message will carry.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if none or both processes are specified.
	 */
	public JSimAssymetricMessage(JSimProcess sender, JSimProcess receiver, Object data) throws JSimInvalidParametersException
	{
		super(sender, receiver, data);

		if (((sender == JSimMessage.UNKNOWN_SENDER) && (receiver == JSimMessage.UNKNOWN_RECEIVER)) || ((sender != JSimMessage.UNKNOWN_SENDER) && (receiver != JSimMessage.UNKNOWN_RECEIVER)))
			throw new JSimInvalidParametersException("JSimAssymetricMessage.JSimAssymetricMessage(): Just one process should be specified.");
	} // constructor

	/**
	 * Creates a new assymetric message with the specified sender, receiver, user data, and message type.
	 * 
	 * @param sender
	 *            The sending process. Must not be specified if the receiver is specified.
	 * @param receiver
	 *            The receiver. Must not be specified if the sender is specified.
	 * @param data
	 *            User data that the message will carry.
	 * @param messageType
	 *            The type of the message. Must be non-negative.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the message type is negative or when none or both processes are specified.
	 */
	public JSimAssymetricMessage(JSimProcess sender, JSimProcess receiver, Object data, int messageType) throws JSimInvalidParametersException
	{
		super(sender, receiver, data, messageType);

		if (((sender == JSimMessage.UNKNOWN_SENDER) && (receiver == JSimMessage.UNKNOWN_RECEIVER)) || ((sender != JSimMessage.UNKNOWN_SENDER) && (receiver != JSimMessage.UNKNOWN_RECEIVER)))
			throw new JSimInvalidParametersException("JSimAssymetricMessage.JSimAssymetricMessage(): Just one process should be specified.");
	} // constructor

} // class JSimAssymetricMessage
