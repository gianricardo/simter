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
 * A symmetric message is a message whose sender and receiver are both known. The message can be sent directly from a process to another
 * process or indirectly via a message box. The receiving process will be able to determine the sender. Filtered reception (filtering by the
 * sender) can be used when receiving both directly and indirectly sent messages.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.3.0
 */
public class JSimSymmetricMessage extends JSimMessage implements JSimMessageWithKnownSender, JSimMessageWithKnownReceiver
{
	/**
	 * Creates a new symmetric message with the specified sender, receiver, and user data. The default value of message type will be used.
	 * 
	 * @param sender
	 *            The sending process. Must always be specified.
	 * @param receiver
	 *            The receiver. Must always be specified.
	 * @param data
	 *            User data that the message will carry.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the sender or the receiver is not specified.
	 */
	public JSimSymmetricMessage(JSimProcess sender, JSimProcess receiver, Object data) throws JSimInvalidParametersException
	{
		super(sender, receiver, data);

		if (sender == JSimMessage.UNKNOWN_SENDER)
			throw new JSimInvalidParametersException("JSimSymmetricMessage.JSimSymmetricMessage(): sender");
		if (receiver == JSimMessage.UNKNOWN_RECEIVER)
			throw new JSimInvalidParametersException("JSimSymmetricMessage.JSimSymmetricMessage(): receiver");
	} // constructor

	/**
	 * Creates a new message with the specified sender, receiver, user data, and message type.
	 * 
	 * @param sender
	 *            The sending process. Must always be specified.
	 * @param receiver
	 *            The receiver. Must always be specified.
	 * @param data
	 *            User data that the message will carry.
	 * @param messageType
	 *            The type of the message. Must be non-negative.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the message type is negative or if the sender or the receiver is not specified.
	 */
	public JSimSymmetricMessage(JSimProcess sender, JSimProcess receiver, Object data, int messageType) throws JSimInvalidParametersException
	{
		super(sender, receiver, data, messageType);

		if (sender == JSimMessage.UNKNOWN_SENDER)
			throw new JSimInvalidParametersException("JSimSymmetricMessage.JSimSymmetricMessage(): sender");
		if (receiver == JSimMessage.UNKNOWN_RECEIVER)
			throw new JSimInvalidParametersException("JSimSymmetricMessage.JSimSymmetricMessage(): receiver");
	} // constructor

} // class JSimSymmetricMessage
