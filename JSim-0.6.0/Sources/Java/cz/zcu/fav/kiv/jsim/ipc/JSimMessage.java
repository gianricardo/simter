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
 *	A message is a piece of data sent from a process to another one. A message can have its sending and receiving
 *	process specified or not. Depending on the sender and receiver specification, the message can be sent
 *	directly between two processes or indirectly via a message box.
 *	So the following types of messages can be distinguished:
 *
 *	<ol>
 *		<li>
 *		Symmetric messages: They have both the sender and the receiver specified, they can therefore be sent
 *		directly from a process to another or indirectly via a message box.
 *		</li>
 *		<li>
 *		Assymetric messages: They have only the sender or the receiver specified. If the receiver is specified,
 *		the message can be sent directly to a process. If the sender is specified, the receiver can use
 *		a receive function that filters messages by their sender. They can also be sent indirectly.
 *		</li>
 *		<li>
 *		Indirect messages: They have neither the sender nor the receiver specified. They can be sent indirectly
 *		only, i.e. via a message box.
 *		</li>
 *	</ol>
 *
 *	This class is abstract, you are not allowed to create instances of it. Look for non-abstract subclasses
 *	that can be instantiated.
 *
 *	@author Jarda KAČER
 *
 *	@version J-Sim version 0.6.0
 *
 *	@since J-Sim version 0.3.0
 */
public abstract class JSimMessage
{
	/**
	 * This constant specifies an unknown sender.
	 */
	public static final JSimProcess UNKNOWN_SENDER = null;

	/**
	 * This constant specifies an unknown receiver.
	 */
	public static final JSimProcess UNKNOWN_RECEIVER = null;

	/**
	 * This constant specifies that no message type filter should be used upon message reception.
	 */
	public static final int ANY_MESSAGE_TYPE = -1;

	/**
	 * The default message type. If the user does not specifies his/her own message type, this value will be used.
	 */
	public static final int DEFAULT_JSIM_MESSAGE_TYPE = 0;

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * The sending process. Can be set to UNKNOWN_SENDER if the user does not want to publish it.
	 */
	protected JSimProcess sender;

	/**
	 * The process to whom this message is to be sent. Can be set to UNKNOWN_RECEIVER if any process can receive the message via a message
	 * box.
	 */
	protected JSimProcess receiver;

	/**
	 * The data sent inside the message.
	 */
	protected Object data;

	/**
	 * Type of the message. The default value is DEFAULT_JSIM_MESSAGE_TYPE. Message types can be useful if you want to filter incoming
	 * messages by a criterion.
	 */
	protected int messageType;

	/**
	 * The process that actually sent the message. Since the sender can be unknown, this field is necessary to determine the sender that can
	 * get suspended in case of a blocking send operation.
	 */
	private JSimProcess realSender;

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a new message with the specified sender, receiver, and user data. The default value of message type will be used.
	 * 
	 * @param sender
	 *            The sending process. Need not be specified.
	 * @param receiver
	 *            The receiver. Need not be specified.
	 * @param data
	 *            User data that the message will carry.
	 */
	public JSimMessage(JSimProcess sender, JSimProcess receiver, Object data)
	{
		this.sender = sender;
		this.receiver = receiver;
		this.data = data;
		this.messageType = DEFAULT_JSIM_MESSAGE_TYPE;
		realSender = null;
	} // constructor

	/**
	 * Creates a new message with the specified sender, receiver, user data, and message type.
	 * 
	 * @param sender
	 *            The sending process. Need not be specified.
	 * @param receiver
	 *            The receiver. Need not be specified.
	 * @param data
	 *            User data that the message will carry.
	 * @param messageType
	 *            The type of the message. Must be non-negative.
	 * 
	 * @exception JSimInvalidParametersException
	 *                This exception is thrown out if the message type is negative.
	 */
	public JSimMessage(JSimProcess sender, JSimProcess receiver, Object data, int messageType) throws JSimInvalidParametersException
	{
		if (messageType < 0)
			throw new JSimInvalidParametersException("JSimMessage.JSimMessage(): messageType");

		this.sender = sender;
		this.receiver = receiver;
		this.data = data;
		this.messageType = messageType;
		realSender = null;
	} // constructor

	/**
	 * Returns the user data that this message carries.
	 * 
	 * @return The user data that this message carries.
	 */
	public Object getData()
	{
		return data;
	} // getData

	/**
	 * Returns the type of the message.
	 * 
	 * @return The type of the message.
	 */
	public int getMessageType()
	{
		return messageType;
	} // getMessageType

	/**
	 * Returns the process that actually sent the message. The real sender is always known, even if the sender was not specified upon
	 * message creation. However, the message must be sent first to know the real sender. If it has not been sent yet, this method will
	 * return null.
	 * 
	 * @return The process that actually sent the message or null.
	 */
	public JSimProcess getRealSender()
	{
		return realSender;
	} // getRealSender

	/**
	 * Sets the real sender when the message is being sent. This method is invoked by the sending process.
	 * 
	 * @param realSender
	 *            The process that is just now sending the message.
	 */
	public void setRealSender(JSimProcess realSender)
	{
		this.realSender = realSender;
	} // setRealSender

	/**
	 * Returns the sender of the message. The sender need not always be an existing process.
	 * 
	 * @return The sender of the message.
	 */
	public JSimProcess getSender()
	{
		return sender;
	} // getSender

	/**
	 * Returns the receiver of the message. The receiver need not always be an existing process.
	 * 
	 * @return The receiver of the message.
	 */
	public JSimProcess getReceiver()
	{
		return receiver;
	} // getReceiver

} // class JSimMessage
