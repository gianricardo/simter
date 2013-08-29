/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2003 Pavel Domecký
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */
package cz.zcu.fav.kiv.jsim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;

import cz.zcu.fav.kiv.jsim.gui.JSimDetailedInfoWindow;
import cz.zcu.fav.kiv.jsim.gui.JSimDisplayable;
import cz.zcu.fav.kiv.jsim.gui.JSimMainWindow;
import cz.zcu.fav.kiv.jsim.gui.JSimPair;
import java.io.IOException;

/**
 * The JSimHead class is an equivalent of Simula's or C-Sim's HEAD. It is a
 * double-ended queue that can contain any number of JSimLink instances. It
 * provides some useful statistics, too.
 *
 * @author Jarda KAČER
 * @author Pavel DOMECKÝ
 *
 * @version J-Sim version 0.6.0
 *
 * @since J-Sim version 0.1.0 (since J-Sim version 0.0.2 as JSimQueue)
 */
public class JSimHead implements JSimDisplayable, Comparable<JSimHead> {

    /**
     * Common logger for all instances of this class. By default, all logging
     * information goes to a file. Only severe events go to the console, in
     * addition to a file.
     */
    private static final Logger logger;
    /**
     * The name of the queue.
     */
    private final String myName;
    /**
     * This queue's number. Queue numbers are unique for a given simulation.
     */
    private final long myNumber;
    /**
     * The number of links in the queue.
     */
    private long noOfItems;
    /**
     * The simulation in which the queue is placed. Protected because child
     * classes will possibly want to use it.
     */
    protected final JSimSimulation myParent;
    /**
     * The first link in the queue.
     */
    private JSimLink head;
    /**
     * The last link in the queue.
     */
    private JSimLink tail;
    /**
     * The simulation time this queue was created.
     */
    private final double creationTime;
    /**
     * The sum of time spent by all links in the queue from its creation to its
     * last change.
     */
    protected double sumLwUntilLastChange;
    /**
     * The simulation time when the last change was made.
     */
    protected double lastChange;
    /**
     * The sum of time spent in the queue by all links already removed from it.
     */
    protected double sumTwRemoved;
    /**
     * The number of links removed from the queue.
     */
    protected long noOfItemsRemoved;

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * The static block initializes all static attributes.
     */
    static {
        logger = Logger.getLogger(JSimHead.class.getName());
    } // static

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new JSimHead queue belonging to a simulation and having a name.
     * The queue is initially empty.
     *
     * @param name The name of the queue.
     * @param parent The parent simulation.
     *
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the parent simulation is null.
     * @exception JSimTooManyHeadsException This exception is thrown out if no
     * other queue can be added to the simulation specified.
     */
    public JSimHead(String name, JSimSimulation parent) throws JSimInvalidParametersException, JSimTooManyHeadsException, IOException {
        if (parent == null) {
            throw new JSimInvalidParametersException("JSimHead.JSimHead(): parent");
        }

        myParent = parent;
        myNumber = myParent.getFreeQueueNumber();
        if (myNumber == JSimSimulation.NEW_QUEUE_FORBIDDEN) {
            throw new JSimTooManyHeadsException("JSimHead.JSimHead()");
        }

        myName = name;
        noOfItems = 0;
        head = null;
        tail = null;

        creationTime = myParent.getCurrentTime();
        lastChange = creationTime;
        sumLwUntilLastChange = 0.0;
        sumTwRemoved = 0.0;
        noOfItemsRemoved = 0;
        myParent.addQueue(this);
    } // constructor

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Tests whether the queue is empty.
     *
     * @return True if the queue is empty, false otherwise.
     */
    public boolean empty() {
        if (noOfItems == 0) {
            return true;
        } else {
            return false;
        }
    } // empty

    /**
     * Returns the number of links in the queue.
     *
     * @return The number of links in the queue.
     */
    public long cardinal() {
        return noOfItems;
    } // cardinal

    /**
     * Returns the first link in the queue.
     *
     * @return The first link in the queue if the queue is not empty, null
     * otherwise.
     */
    public JSimLink first() {
        return head;
    } // first

    /**
     * Returns the last link in the queue.
     *
     * @return The last link in the queue if the queue is not empty, null
     * otherwise.
     */
    public JSimLink last() {
        return tail;
    } // last

    /**
     * Removes all links from the queue. The links may survive if there is a
     * reference to them. This is an "intelligent" removal. All links are
     * removed one-by-one so that characteristics are properly updated.
     */
    public void clear() {
        JSimLink link = null;

        try {
            while (head != null) {
                link = head;
                link.out();
            } // while


            if (noOfItems != 0) {
                logger.log(Level.WARNING, getHeadName() + ": Unexpected inconsistency. noOfItems should be 0 but is " + noOfItems);
            }
            if (head != null) {
                logger.log(Level.WARNING, getHeadName() + ": Unexpected inconsistency. Head not null.");
            }
            if (tail != null) {
                logger.log(Level.WARNING, getHeadName() + ": Unexpected inconsistency. Tail not null.");
            }

            noOfItems = 0;
            head = null;
            tail = null;
        } // try
        catch (JSimSecurityException e) {
            logger.log(Level.SEVERE, "An error occured when removing a link from a queue.", e);
            throw new JSimKernelPanicException(e);
        } // catch
    } // clear

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Returns the queue's number.
     *
     * @return The queue's number.
     */
    public final long getHeadNumber() {
        return myNumber;
    } // getHeadNumber

    /**
     * Returns the queue's name.
     *
     * @return The queue's name.
     */
    public String getHeadName() {
        return myName;
    } // getHeadName

    /**
     * Returns the simulation that this queue is a part of.
     *
     * @return The simulation that this queue is a part of.
     */
    public JSimSimulation getParent() {
        return myParent;
    } // getParent

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Returns the first links's data. If the first link is an instance of a
     * JSimLink subclass with user-defined content, the return value of this
     * method depends on the creator of the subclass. If the link is used as a
     * data wrapper (the data is specified as a constructor parameter), the
     * wrapped data is returned.
     *
     * @return The first link's data if the queue is not empty, null otherwise.
     */
    public Object getFirstData() {
        if (head == null) {
            return null;
        } else {
            return head.getData();
        }
    } // getFirstData

    /**
     * Returns the first link's data type. If the first link is an instance of a
     * JSimLink subclass with user-defined content, the return value of this
     * method depends on the creator of the subclass. If the link is used as a
     * data wrapper (the data is specified as a constructor parameter), the
     * wrapped data's type is returned.
     *
     * @return The first link's data type if the queue is not empty, null
     * otherwise.
     */
    public String getFirstDataType() {
        if (head == null) {
            return null;
        } else {
            return head.getDataType();
        }
    } // getFirstDataType

    /**
     * Returns the last links's data. If the last link is an instance of a
     * JSimLink subclass with user-defined content, the return value of this
     * method depends on the creator of the subclass. If the link is used as a
     * data wrapper (the data is specified as a constructor parameter), the
     * wrapped data is returned.
     *
     * @return The last link's data if the queue is not empty, null otherwise.
     */
    public Object getLastData() {
        if (tail == null) {
            return null;
        } else {
            return tail.getData();
        }
    } // getLastData

    /**
     * Returns the last link's data type. If the last link is an instance of a
     * JSimLink subclass with user-defined content, the return value of this
     * method depends on the creator of the subclass. If the link is used as a
     * data wrapper (the data is specified as a constructor parameter), the
     * wrapped data's type is returned.
     *
     * @return The last link's data type if the queue is not empty, null
     * otherwise.
     */
    public String getLastDataType() {
        if (tail == null) {
            return null;
        } else {
            return tail.getDataType();
        }
    } // getLastDataType

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Returns the current time of the simulation that this queue belongs to.
     *
     * @return The current time of the simulation that this queue belongs to.
     */
    public double getCurrentTime() {
        return myParent.getCurrentTime();
    } // getCurrentTime

    /**
     * Returns the mean length of the queue. It makes no sense to calculate this
     * value for a queue that has just been created. If no simulation time has
     * passed since its creation NaN (not-a-number) will be returned.
     *
     * @return The mean length of the queue or NaN.
     */
    public double getLw() {
        double now;
        double tempSumLw;

        // We must count the result without affecting the statistics
        now = myParent.getCurrentTime();
        tempSumLw = sumLwUntilLastChange + ((now - lastChange) * noOfItems); // The stored value + delta since last insertion/deletion

        if (now != creationTime) {
            return tempSumLw / (now - creationTime);
        } else {
            return Double.NaN; // Is not Infinity -- tempSumLw is also 0
        }
    } // getLw

    /**
     * Returns the mean waiting time spent in the queue by all links already
     * removed from the queue. It makes no sense to calculate this value for a
     * queue without any elements revoved from it. In such a case, NaN
     * (not-a-number) will be returned.
     *
     * @return The mean waiting time spent in the queue by all links already
     * removed from the queue or NaN.
     */
    public double getTw() {
        if (noOfItemsRemoved != 0) {
            return sumTwRemoved / ((double) noOfItemsRemoved);
        } else {
            return Double.NaN; // Is not Infinity -- sumTwRemoved is also 0
        }
    } // getTw

    /**
     * Returns the mean waiting time spent in the queue by all links ever
     * inserted to the queue. Both links already removed from the queue and
     * links just now present in the queue are counted. It makes no sense to
     * calculate this value if no links have been inserted to the queue yet. In
     * such a case, NaN (not-a-number) is returned.
     *
     * If this head is used as a FIFO (first in, first out) queue, which it
     * usually is in queueing network simulations, the returned value is less
     * than the value produced by getTw(). The reason for this behavior is that
     * all links that have ever been in the queue have the same mean time spent
     * in it and the links that currently are in the queue have not reached this
     * time yet. So they push the overall mean time down because they are
     * counted with the same weight as links already removed from the queue.
     *
     * @return The mean waiting time spent in the queue by all links ever
     * inserted the queue or NaN.
     */
    public double getTwForAllLinks() {
        double sumTwOfLiving;
        double now;
        JSimLink link;

        try {
            now = myParent.getCurrentTime();
            sumTwOfLiving = 0.0;
            link = head;

            // All links currently inserted in the queue must be added to the sum.
            while (link != null) {
                sumTwOfLiving += (now - link.getEnterTime()); // May go to Infinity
                link = link.getNext();
            } // while

            if ((noOfItemsRemoved + noOfItems) != 0) {
                return (sumTwRemoved + sumTwOfLiving) / (noOfItemsRemoved + noOfItems);
            } else {
                return Double.NaN; // Is not Infinity -- the sum is usually also 0
            }
        } // try
        catch (JSimSecurityException e) {
            logger.log(Level.WARNING, "An error occured when computing Tw.", e);
            return 0.0;
        } // catch
    } // getTwForAllLinks

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Returns the head of the queue. You should never use this method.
     *
     * @return The head of the queue.
     */
    protected final JSimLink getHead() {
        return head;
    } // getHead

    /**
     * Sets a new head of the queue. You should never use this method. It only
     * updates the "head" field, it does not actually add anything to the head.
     *
     * @param newHead The new head of the queue.
     */
    protected final void setHead(JSimLink newHead) {
        head = newHead;
    } // setHead

    /**
     * Adds a link to the head of the queue, doing necessary adjustments. You
     * should never use this method.
     *
     * @param newHead The new head of the queue.
     *
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the link that has to to become the new head is null.
     */
    protected final void putAtHead(JSimLink newHead) throws JSimInvalidParametersException {
        if (newHead == null) {
            throw new JSimInvalidParametersException("JSimHead.putAtHead(): newHead");
        }

        newHead.setNext(head);
        if (head != null) {
            head.setPrevious(newHead);
        }
        head = newHead;

        // If the queue was empty, we must set the tail too.
        if (tail == null) {
            tail = head;
        }
    } // putAtHead

    /**
     * Returns the tail of the queue. You should never use this method.
     *
     * @return The tail of the queue.
     */
    protected final JSimLink getTail() {
        return tail;
    } // getTail

    /**
     * Sets a new tail of the queue. You should never use this method. It only
     * updates the "tail" field, it does not actually add anything to the tail.
     *
     * @param newTail The new tail of the queue.
     */
    protected final void setTail(JSimLink newTail) {
        tail = newTail;
    } // setTail

    /**
     * Sets a new tail of the queue, doing necessary adjustments. You should
     * never use this method.
     *
     * @param newTail The new tail of the queue.
     *
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the link that has to to become the new tail is null.
     */
    protected final void putAtTail(JSimLink newTail) throws JSimInvalidParametersException {
        if (newTail == null) {
            throw new JSimInvalidParametersException("JSimHead.putAtTail(): newTail");
        }

        newTail.setPrevious(tail);
        if (tail != null) {
            tail.setNext(newTail);
        }
        tail = newTail;

        // If the queue was empty, we must set the head too.
        if (head == null) {
            head = tail;
        }
    } // putAtTail

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Increments internal counter of links and updates statistics. You should
     * never directly use this method. It is assumed that the new link has
     * already been added to the list.
     */
    protected final void incNoOfItems() {
        double now;

        // We must update the statistics
        now = myParent.getCurrentTime();
        sumLwUntilLastChange += (now - lastChange) * noOfItems; // May go to Infinity
        lastChange = now;

        if (noOfItems < Long.MAX_VALUE) {
            noOfItems++;
        } else {
            logger.log(Level.WARNING, myName + ": At the limits of Java's possibilities. (noOfItems)");
        }
    } // incNoOfItems

    /**
     * Decrements internal counter of links and updates statistics. You should
     * never directly use this method. It is assumed that the link has already
     * been removed from the list.
     *
     * @param whenEntered The simulation time when the link being removed
     * entered the queue.
     */
    protected final void decNoOfItems(double whenEntered) {
        double now;

        // We must update the statistics for Lw
        now = myParent.getCurrentTime();
        sumLwUntilLastChange += (now - lastChange) * noOfItems; // May go to Infinity
        lastChange = now;

        // And then the statistics for Tw
        sumTwRemoved += now - whenEntered; // May go to Infinity
        if (noOfItemsRemoved < Long.MAX_VALUE) {
            noOfItemsRemoved++;
        } else {
            logger.log(Level.WARNING, myName + ": At the limits of Java's possibilities. (noOfItemsRemoved)");
        }

        // Decrement the counter of links
        if (noOfItems > 0) {
            noOfItems--;
        } else {
            logger.log(Level.WARNING, myName + ": Unexpected inconsistency. (noOfItems)");
        }
    } // decNoOfItems

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Returns a string containing basic information about the queue. The string
     * can be displayed in a JSimMainWindowList component.
     *
     * @return A string containing basic information about the queue.
     */
    public String getObjectListItemDescription() {
        return toString();
    } // getObjectListItemDescription

    /**
     * Returns a collection with the queue's characteristics. Every
     * characteristics contains a name and a value. The collection can be
     * displayed in a JSimGUIDetailedInfoWindow table.
     *
     * @return A collection of the queue's characteristics.
     */
    public Collection<JSimPair> getDetailedInformationArray() {
        Collection<JSimPair> c = new ArrayList<JSimPair>(5);
        c.add(new JSimPair("Number:", Long.toString(getHeadNumber())));
        c.add(new JSimPair("Name:", getHeadName()));
        c.add(new JSimPair("Link count:", Long.toString(cardinal())));
        c.add(new JSimPair("Mean length:", Double.toString(getLw())));
        c.add(new JSimPair("Mean waiting time:", Double.toString(getTw())));
        return c;
    } // getDetailedInformationArray

    /**
     * Creates a detailed info window that shows information about the queue.
     * Returns a reference to the created window.
     *
     * @return A reference to the created info window.
     */
    public JDialog createDetailedInfoWindow(JSimMainWindow parentWindow) {
        JSimDetailedInfoWindow dWindow = new JSimDetailedInfoWindow(parentWindow, this);
        return dWindow;
    } // createDetailedInfoWindow

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Returns a string representation of the queue. Provided information:
     * number, name, link count.
     *
     * @return A string representation of the queue.
     */
    public String toString() {
        return ("No.: " + getHeadNumber() + " Name: " + getHeadName() + " Link count: " + cardinal());
    } // toString

    /**
     * Compares this head with another one. Returns a negative integer, zero, or
     * a positive integer as this head is less than, equal to, or greater than
     * the specified object. It is assumed that the argument is also a JSimHead.
     * This class has a natural ordering that is fully consistent with equals().
     * If equals() returns true for h1 and h2, then compareTo() will return 0
     * for the same h1 and h2, and vice versa.
     *
     * @return Zero if the numbers of both heads are equal, a negative number if
     * the number of this head is less than the other heads's number, and a
     * positive number if the number of this head is greater than the other
     * heads's number.
     *
     * @exception ClassCastException This exception is thrown out when the
     * specified object cannot be typecasted to JSimHead.
     */
    public int compareTo(JSimHead h) {
        if (this.myParent.getSimulationNumber() == h.myParent.getSimulationNumber()) {
            if (this.myNumber == h.myNumber) {
                return 0;
            } else if (this.myNumber < h.myNumber) {
                return -1;
            } else {
                return +1;
            }
        } else if (this.myParent.getSimulationNumber() < h.myParent.getSimulationNumber()) {
            return -1;
        } else {
            return +1;
        }
    } // compareTo

    /**
     * Indicates whether some other object is equal to this one. This
     * implementation compares head numbers and their simulations' numbers which
     * is actually equal to simple reference comparison because head numbers are
     * unique for a given simulation and simulation numbers are unique for a
     * given JVM instance. Unique head numbers are assured by the constructor
     * and the JSimSimulation.getFreeQueueNumber() method. Unique simulation
     * numbers are assured by the JSimSimulation constructor.
     *
     * @param o The reference object with which to compare.
     *
     * @return True if this object is the same as the obj argument, false
     * otherwise.
     */
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if ((o instanceof JSimHead) == false) {
            return false;
        }

        JSimHead h = (JSimHead) o;

        if ((this.myNumber == h.myNumber) && (this.myParent.getSimulationNumber() == h.myParent.getSimulationNumber())) {
            return true;
        } else {
            return false;
        }
    } // equals

    /**
     * Returns a hash code value for the object. The hash code is computed from
     * the head's number and its simulation's number using the algorithm
     * described in [UJJ3/166]. This implementation of hash code computation is
     * fully consistent with equals().
     *
     * @return A hash code for this head.
     */
    public int hashCode() {
        int temp = 17; // Magic number 17
        int myNumberAsInt = (int) (myNumber ^ (myNumber >>> 32));
        int simulationNumber = myParent.getSimulationNumber();

        temp = 37 * temp + myNumberAsInt; // Another magic number 37
        temp = 37 * temp + simulationNumber;

        return temp;
    } // hashCode
} // class JSimHead
