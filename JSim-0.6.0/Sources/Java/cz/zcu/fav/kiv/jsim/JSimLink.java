/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */

package cz.zcu.fav.kiv.jsim;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The JSimLink class is an equivalent of Simula's and C-Sim's LINK. It is supposed to encapsulate user data inserted into a queue
 * (JSimHead). You can use the JSimLink class in two different ways:
 * 
 * <ol>
 * 
 * <li>
 * You can use it directly "as is" as a wrapper of your data. You just create a new JSimLink and give your data as a parameter to the
 * constructor.
 * </li>
 * 
 * <li>
 * You can subclass JSimLink and add new fields to the new class. In this case, you must ensure that the methods getData() and
 * getDataType() return meaningful values. And you must create your own constructor, of course.
 * </li>
 * 
 * </ol>
 * 
 * @author Jarda KAČER
 * 
 * @version J-Sim version 0.6.0
 * 
 * @since J-Sim version 0.1.0 (since J-Sim version 0.0.2 as JSimQueueItem)
 */
public class JSimLink
{
	/**
	 * Common logger for all instances of this class. By default, all logging information goes to a file. Only severe events go to the
	 * console, in addition to a file.
	 */
	private static final Logger logger;

	/**
	 * The queue that this link is currently inserted in.
	 */
	private JSimHead myQueue;

	/**
	 * The wrapped data. If you subclass JSimLink and use the constructor without parameters, this field will always be null! You will have
	 * to overwrite the getData() method if you don't plan to use it in a special way.
	 */
	private final Object data;

	/**
	 * The class name of the wrapped data. If you subclass JSimLink and use the constructor without parameters, this field will not have a
	 * meaningful value. You will have to overwrite the getDataType() method.
	 */
	protected final String dataType;

	/**
	 * The simulation time that this link entered its current queue.
	 */
	private double enterTime;

	/**
	 * Reference to the next link in the queue. For performance purposes, J-Sim uses real bidirectional list, as you know it from C. Using
	 * container classes (such as ArrayList or LinkedList) leads to complexity <i>O(n)</i> of getNext(), getPrevious(), follow(), and
	 * precede(), which is unacceptable. This implementation ensures complexity <i>O(1)</i> of all operations.
	 */
	private JSimLink next;

	/**
	 * Reference to the previous link in the queue. For performance purposes, J-Sim uses real bidirectional list, as you know it from C.
	 * Using container classes (such as ArrayList or LinkedList) leads to complexity <i>O(n)</i> of getNext(), getPrevious(), follow(), and
	 * precede(), which is unacceptable. This implementation ensures complexity <i>O(1)</i> of all operations.
	 */
	private JSimLink previous;

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * The static block initializes all static attributes.
	 */
	static
	{
		logger = Logger.getLogger(JSimLink.class.getName());
	} // static

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Creates a new JSimLink object (a queue item) containing user data. When this constructor version is used, JSimLink acts as a
	 * "wrapper" of your data.
	 * 
	 * @param object
	 *            The data that the queue item will contain.
	 */
	public JSimLink(Object object)
	{
		myQueue = null;
		data = object;
		if (data != null)
			dataType = new String(object.getClass().getName());
		else
			dataType = "<No Data>";
		enterTime = 0.0; // Will be adjusted when enters a queue.
		next = null;
		previous = null;
	} // constructor

	/**
	 * Creates a new JSimLink object (a queue item) containing no data. Use this constructor in a subclass of JSimLink where you will supply
	 * your own data and methods.
	 */
	public JSimLink()
	{
		myQueue = null;
		data = null;
		dataType = "<User data of a JSimLink subclass is stored separately.>";
		enterTime = 0.0; // Will be adjusted when enters a queue.
		next = null;
		previous = null;
	} // constructor

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Inserts the item into a queue. The link must not be inserted in any queue.
	 * 
	 * @param queue
	 *            The queue the item is to be inserted to.
	 * 
	 * @exception JSimSecurityException
	 *                This exception is thrown out when the item is already inserted in a queue.
	 */
	public final void into(JSimHead queue) throws JSimSecurityException
	{
		try
		{
			if (myQueue != null)
				throw new JSimSecurityException("JSimLink.into(): Already in a queue.");
			else
			{
				myQueue = queue;
				myQueue.putAtTail(this);
				enterTime = myQueue.getCurrentTime();
				myQueue.incNoOfItems();
			} // else myQueue not null
		} // try
		catch (JSimInvalidParametersException e)
		{
			logger.log(Level.WARNING, "The head refused the link. The link was not inserted.", e);
			myQueue = null;
		} // catch
	} // into

	/**
	 * Inserts the link into a queue after another link. The link being inserted must not be in a queue while the other one must be inserted
	 * in a queue.
	 * 
	 * @param otherLink
	 *            The link that will be followed by this link.
	 * 
	 * @exception JSimSecurityException
	 *                This exception is thrown out if the link is already inserted in a queue or the other link is null or not inserted in
	 *                any queue.
	 */
	public final void follow(JSimLink otherLink) throws JSimSecurityException
	{
		if (myQueue != null)
			throw new JSimSecurityException("JSimLink.follow(): Already in a queue.");
		if (otherLink == null)
			throw new JSimSecurityException("JSimLink.follow(): otherLink");
		if (otherLink.getQueue() == null)
			throw new JSimSecurityException("JSimLink.follow(): otherLink is not in a queue.");

		myQueue = otherLink.getQueue();

		// Insert itself between the other link and its successor.
		setNext(otherLink.getNext());
		setPrevious(otherLink);
		otherLink.setNext(this);
		if (getNext() != null)
			getNext().setPrevious(this);

		// Are we becoming new tail?
		if (myQueue.getTail() == otherLink)
			myQueue.setTail(this);

		enterTime = myQueue.getCurrentTime();
		myQueue.incNoOfItems();
	} // follow

	/**
	 * Inserts the item into a queue before another item. The link being inserted must not be in a queue while the other one must be
	 * inserted in a queue.
	 * 
	 * @param otherLink
	 *            The link that will be preceded by this item.
	 * 
	 * @exception JSimSecurityException
	 *                This exception is thrown out if the link is already inserted in a queue or the other link is null or not inserted in
	 *                any queue.
	 */
	public final void precede(JSimLink otherLink) throws JSimSecurityException
	{
		if (myQueue != null)
			throw new JSimSecurityException("JSimLink.precede(): Already in a queue.");
		if (otherLink == null)
			throw new JSimSecurityException("JSimLink.precede(): otherLink");
		if (otherLink.getQueue() == null)
			throw new JSimSecurityException("JSimLink.precede(): otherLink is not in a queue.");

		myQueue = otherLink.getQueue();

		// Insert itself between the other link and its predecessor.
		setPrevious(otherLink.getPrevious());
		setNext(otherLink);
		otherLink.setPrevious(this);
		if (getPrevious() != null)
			getPrevious().setNext(this);

		// Are we becoming new head?
		if (myQueue.getHead() == otherLink)
			myQueue.setHead(this);

		enterTime = myQueue.getCurrentTime();
		myQueue.incNoOfItems();
	} // precede

	/**
	 * Removes the link from its queue. The link must be inserted in a queue.
	 * 
	 * @exception JSimSecurityException
	 *                This exception is thrown out if the link is not inserted in a queue.
	 */
	public final void out() throws JSimSecurityException
	{
		if (myQueue == null)
			throw new JSimSecurityException("JSimLink.out(): Not in a queue.");

		if ((myQueue.getHead() == this) && (myQueue.getTail() == this))
		{
			// The link being removed is the only link in the queue.
			myQueue.setHead(null);
			myQueue.setTail(null);
		} // if the only link
		else
		{
			if ((myQueue.getHead() != this) && (myQueue.getTail() != this))
			{
				// The link is neither the head nor the tail.
				getPrevious().setNext(getNext());
				getNext().setPrevious(getPrevious());

				setNext(null);
				setPrevious(null);
			} // if neither head nor tail
			else
			{
				// Either the head or the tail is equal to this link but not both.

				if (myQueue.getHead() == this)
				{
					myQueue.setHead(getNext());
					if (getNext() != null)
						getNext().setPrevious(null);
					setNext(null);
				} // if this link is the head

				if (myQueue.getTail() == this)
				{
					myQueue.setTail(getPrevious());
					if (getPrevious() != null)
						getPrevious().setNext(null);
					setPrevious(null);
				} // if this link is the tail
			} // else neither head nor tail
		} // else the only link

		// Now the link is physically removed from the list.
		myQueue.decNoOfItems(enterTime);
		myQueue = null;
		enterTime = 0.0;
	} // out

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Returns the queue that this link is currently inserted in.
	 * 
	 * @return The queue this link is inserted in, null if it is not inserted in any queue.
	 */
	public final JSimHead getQueue()
	{
		return myQueue;
	} // getQueue

	/**
	 * Returns the simulation time when this link was inserted into its current queue.
	 * 
	 * @return The simulation time when this link was inserted into its current queue.
	 */
	public final double getEnterTime()
	{
		return enterTime;
	} // getEnterTime

	/**
	 * Returns data wrapped by this link. The data is immutable. Remember that you are responsible for proper redefinition of this method if
	 * you subclass JSimLink!
	 * 
	 * @return The data wrapped by this link.
	 */
	public Object getData()
	{
		return data;
	} // getData

	/**
	 * Returns the type of data wrapped by this link. Since the data itself is immutable, so is the data type. Remember that you are
	 * responsible for proper redefinition of this method if you subclass JSimLink!
	 * 
	 * @return The type of data wrapped by this link.
	 */
	public String getDataType()
	{
		return dataType;
	} // getDataType

	// ------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Returns the next link in the queue. This link must be inserted in a queue.
	 * 
	 * @return The next link in the queue.
	 * 
	 * @exception JSimSecurityException
	 *                This exception is thrown out if the link is not inserted in a queue.
	 */
	public final JSimLink getNext() throws JSimSecurityException
	{
		if (myQueue == null)
			throw new JSimSecurityException("JSimLink.getNext(): Not in a queue.");

		return next;
	} // getNext

	/**
	 * Returns the previous link in the queue. This link must be inserted in a queue.
	 * 
	 * @return The previous link in the queue.
	 * 
	 * @exception JSimSecurityException
	 *                This exception is thrown out if the link is not inserted in a queue.
	 */
	public final JSimLink getPrevious() throws JSimSecurityException
	{
		if (myQueue == null)
			throw new JSimSecurityException("JSimLink.getPrevious(): Not in a queue.");

		return previous;
	} // getPrevious

	/**
	 * Sets the next item in the queue. This method is called by other links. You should never use this method.
	 * 
	 * @param nextLink
	 *            The link that will follow this one.
	 */
	protected final void setNext(JSimLink nextLink)
	{
		next = nextLink;
	} // setNext

	/**
	 * Sets the previous link in the queue. This method is called by other links. You should never use this method.
	 * 
	 * @param previousLink
	 *            The link that will precede this one.
	 */
	protected final void setPrevious(JSimLink previousLink)
	{
		previous = previousLink;
	} // setPrevious

} // class JSimLink
