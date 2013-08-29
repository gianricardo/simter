/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim.ipc;

import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;

/**
 * An indirect message is a message whose sender and receiver are not known. The message cannot be sent directly, only via a message box.
 * Any process can pick it up from the message box later.
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.3.0
 */
public class JSimIndirectMessage extends JSimMessage
{
	/**
	 * Creates a new indirect message with an unknown sender, an unspecified receiver, and the specified user data. The default value of
	 * message type will be used.
	 * 
	 * @param data
	 *            User data that the message will carry.
	 */
	public JSimIndirectMessage(Object data)
	{
		super(JSimMessage.UNKNOWN_SENDER, JSimMessage.UNKNOWN_RECEIVER, data);
	} // constructor

	/**
	 * Creates a new message with an unknown sender, an unspecified receiver, and the specified user data and message type.
	 * 
	 * @param data
	 *            User data that the message will carry.
	 * @param messageType
	 *            The type of the message. Must be non-negative.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the message type is negative.
	 */
	public JSimIndirectMessage(Object data, int messageType) throws JSimInvalidParametersException
	{
		super(JSimMessage.UNKNOWN_SENDER, JSimMessage.UNKNOWN_RECEIVER, data, messageType);
	} // constructor

} // class JSimIndirectMessage
