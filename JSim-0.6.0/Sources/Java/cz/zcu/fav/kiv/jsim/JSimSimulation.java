/*
 *	Copyright (c) 2000-2006 Jaroslav Kačer <jaroslav@kacer.biz>
 *	Copyright (c) 2003 Pavel Domecký
 *	Copyright (c) 2004 University of West Bohemia, Pilsen, Czech Republic
 *	Licensed under the Academic Free License version 2.1
 *	J-Sim source code can be downloaded from http://www.j-sim.zcu.cz/
 *
 */
package cz.zcu.fav.kiv.jsim;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.zcu.fav.kiv.jsim.gui.JSimChange;
import cz.zcu.fav.kiv.jsim.gui.JSimDisplayable;
import cz.zcu.fav.kiv.jsim.gui.JSimMainWindow;
import cz.zcu.fav.kiv.jsim.ipc.JSimSemaphore;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The JSimSimulation class instances represent theoretical simulation models,
 * containing processes, queues, and other elements.
 *
 * Every process and queue must have a simulation specified as a constructor
 * parameter, otherwise the creation will not be successful. To the user, the
 * JSimSimulation class provides the step() method &ndash; execution of one
 * simulation step. Alternatively, the runGUI() method can be used, causing the
 * simulation to run in graphic mode.
 *
 * @author Jarda KAČER
 * @author Pavel DOMECKÝ
 *
 * @version J-Sim version 0.6.0
 *
 * @since J-Sim version 0.1.0
 */
public class JSimSimulation {
    // Importance of printed strings

    public static final int PRN_MESSAGE = 0;
    public static final int PRN_ERROR = 1;
    // Error constans
    public static final long NEW_PROCESS_FORBIDDEN = -1;
    public static final long NEW_QUEUE_FORBIDDEN = -1;
    public static final long NEW_SEMAPHORE_FORBIDDEN = -1;
    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Common logger for all instances of this class. By default, all logging
     * information goes to a file. Only severe events go to the console, in
     * addition to a file.
     */
    private static final Logger logger;
    /**
     * The simulation's number. These numbers are unique for a given JVM
     * instance.
     */
    private final int myNumber;
    /**
     * The simulation's name.
     */
    private final String myName;
    /**
     * The simulation's mode &ndash; text, GUI batch, GUI interactive.
     */
    protected final JSimSimulationMode mode;
    /**
     * Number of processes currently present within the simulation.
     */
    private long numberOfProcesses;
    /**
     * Number of queues currently present within the simulation.
     */
    private long numberOfQueues;
    /**
     * Number of semaphores currently present within the simulation.
     */
    private long numberOfSemaphores;
    /**
     * The currently running process. At most one process can run at the same
     * time.
     */
    protected JSimProcess runningProcess;
    /**
     * Number for a newly created process. Alike numberOfProcesses, this number
     * is never decremented so every process has its unique number.
     */
    private long newProcessNumber;
    /**
     * Number for a newly created queue. Alike numberOfQueues, this number is
     * never decremented so every queue (JSimHead) has its unique number.
     */
    private long newQueueNumber;
    /**
     * Number for a newly created semaphore. Alike numberOfSemaphores, this
     * number is never decremented so every semaphore (JSimSemaphore) has its
     * unique number.
     */
    private long newSemaphoreNumber;
    /**
     * The simulation's calendar of events.
     */
    private JSimCalendar calendar;
    /**
     * The simulation's current time.
     */
    private double time;
    /**
     * The simulation's state &ndash; not started, in progress, terminated.
     */
    private JSimSimulationState simulationState;
    /**
     * All processes of the simulation.
     */
    private SortedSet<JSimProcess> processes;
    /**
     * All processes that have ever existed in the simulation. The list of
     * processes in the GUI window shows even processes that have terminated
     * already.
     */
    private SortedSet<JSimProcess> processesForGUI;
    /**
     * All queues of the simulation.
     */
    private SortedSet<JSimHead> queues;
    /**
     * All semaphores of the simulation.
     */
    private SortedSet<JSimSemaphore> semaphores;
    /**
     * A set containing processes created during last step and not started yet.
     * It is completely emptied every time startNewProcesses() is called and new
     * processes are started.
     */
    private SortedSet<JSimProcess> notStartedProcesses;
    /**
     * Lock used when switching between the simulation and its main GUI window.
     */
    private final Object graphicLock;
    /**
     * Lock used to synchronize (possible) concurrent calls to step(). The
     * implicit lock cannot be used since processes use callbacks to the
     * simulation's synchronized methods, e.g. getRunningProcess() in
     * getReady(), and the lock for synchonizing step() must not be released
     * when switching to a process's thread from the main thread.
     */
    protected final Object stepLock;
    /**
     * Lock used when the simulation waits in waitForWindowClosure(). This is
     * only possible when the simulation runs in GUI batch mode.
     */
    private final Object waitForWindowClosureLock;
    /**
     * The simulation's main window.
     */
    protected JSimMainWindow mainWindow;
    /**
     * Flag saying that the main window is open.
     */
    protected boolean windowOpen;
    /**
     * Flag saying that the simulation is just now waiting in
     * waitForWindowClosure().
     */
    private boolean waitingForWindowClosure;
    /**
     * Flag saying that the first step of simulation has been executed. This
     * flag is important in GUI batch mode where the simulation itself takes
     * care about the GUI main window.
     */
    protected boolean firstStepExecuted;
    /**
     * Flag indicating that the main window has already been opened (and maybe
     * closed). The flag is relevant in batch GUI mode only where it is
     * necessary to distinguish whether messages should go to the main window or
     * to the console. If an output/step request is received, the main window
     * must be open, but only if it has not been opened/closed yet.
     */
    protected boolean mainWindowHasAlreadyExisted;
    /**
     * Observable object notifying the GUI about possible changes.
     */
    protected JSimChange guiUpdate;
    /**
     * GUI update delta. This is a simulation time interval between two
     * consecutive updates of GUI output. Its value does not change during
     * simulation execution. If it is zero, the output is updated after every
     * simulation step. Otherwise, the output is updated only if the difference
     * between the current simulation time and lastGUIUpdate is equal to or
     * greater than this delta.
     */
    protected double nextGUIUpdateDelta;
    /**
     * Last GUI update time.
     */
    protected double lastGUIUpdate;
    /**
     * Number of simulations running in this JVM instance.
     */
    private static int noOfSimulations;
    
    private static File arquivo;
    
    private static FileWriter fw;
    
    private static BufferedWriter bw;

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * The static block initializes all static attributes.
     */
    static {
        logger = Logger.getLogger(JSimSimulation.class.getName());
        noOfSimulations = 0;
        arquivo = new File("../arquivoLogInfo.txt");        
    } // static

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new simulation with no processes, no queues and no graphic
     * window. The simulation will run in text (console) mode and no graphic
     * output will be allowed.
     *
     * @param name Name of the simulation.
     *
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the specified name is is null.
     */
    public JSimSimulation(String name) throws JSimInvalidParametersException, IOException {
        this(name, JSimSimulationMode.TEXT, 0.0);        
    } // constructor

    /**
     * Creates a new simulation with no processes, no queues and no graphic
     * window. According to the simulation mode specified as a parameter, it
     * will / will not be allowed to use graphic output later. If GUI batch or
     * interactive mode is used, the output will be updated after every step.
     *
     * @param name Name of the simulation.
     * @param mode Simulation mode.
     *
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the specified name is is null or an invalid simulation mode is given to
     * the constructor.
     */
    public JSimSimulation(String name, JSimSimulationMode mode) throws JSimInvalidParametersException, IOException {
        this(name, mode, 0.0);
    } // constructor

    /**
     * Creates a new simulation with no processes, no queues and no graphic
     * window. According to the simulation mode specified as a parameter, it
     * will / will not be allowed to use graphic output later.
     *
     * @param name Name of the simulation.
     * @param mode Simulation mode.
     * @param guiUpdateDelta A simulation time interval after which the GUI
     * output will be updated. If set to zero, the output will be updated after
     * every simulation step. This may be time consuming and inappropriate for
     * some situations.
     *
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the specified name is is null, an invalid simulation mode is given to the
     * constructor, or if the update interval is less than zero.
     */
    public JSimSimulation(String name, JSimSimulationMode mode, double guiUpdateDelta) throws JSimInvalidParametersException, IOException {
        if (name == null) {
            throw new JSimInvalidParametersException("JSimSimulation.JSimSimulation(): name");
        }
        if ((mode != JSimSimulationMode.TEXT) && (mode != JSimSimulationMode.GUI_BATCH) && (mode != JSimSimulationMode.GUI_INTERACTIVE)) {
            throw new JSimInvalidParametersException("JSimSimulation.JSimSimulation(): mode");
        }
        if (guiUpdateDelta < 0.0) {
            throw new JSimInvalidParametersException("JSimSimulation.JSimSimulation(): guiUpdateDelta");
        }

        this.myName = name;
        this.mode = mode;

        myNumber = noOfSimulations;
        noOfSimulations++;

        numberOfProcesses = 0;
        numberOfQueues = 0;
        numberOfSemaphores = 0;
        runningProcess = JSimCalendar.NOBODY;

        newProcessNumber = 0;
        newQueueNumber = 0;
        newSemaphoreNumber = 0;

        calendar = new JSimCalendar();
        time = 0.0;
        simulationState = JSimSimulationState.NOT_STARTED;

        processes = new TreeSet<JSimProcess>();
        processesForGUI = new TreeSet<JSimProcess>();
        queues = new TreeSet<JSimHead>();
        semaphores = new TreeSet<JSimSemaphore>();
        notStartedProcesses = new TreeSet<JSimProcess>();

        graphicLock = new Object();
        stepLock = new Object();
        waitForWindowClosureLock = new Object();

        mainWindow = null;
        windowOpen = false;
        firstStepExecuted = false;
        mainWindowHasAlreadyExisted = false;
        guiUpdate = new JSimChange();
        nextGUIUpdateDelta = guiUpdateDelta;
        lastGUIUpdate = 0.0;

        logger.log(Level.INFO, "A new simulation created. Name: " + myName);
        
        if(arquivo.delete()==true){            
            arquivo = new File("../arquivoLogInfo.txt");
        }

        if(!arquivo.exists())
        {
            arquivo.createNewFile();
        }
        
        try {
            fw = new FileWriter(arquivo, true);
        } catch (IOException ex) {
            Logger.getLogger(JSimSimulation.class.getName()).log(Level.SEVERE, null, ex);
        }
        bw = new BufferedWriter(fw);
        
        bw.write("\r\nINFO: A new simulation created. Name:" + myName);
    } // constructor

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Adds a new process to the simulation. You should never call this method.
     * It is called automatically from JSimProcess constructor.
     *
     * @param process The process that has to be added to the simulation.
     *
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the specified process is null.
     */
    protected synchronized final void addProcess(JSimProcess process) throws JSimInvalidParametersException, IOException {
        if (process == null) {
            throw new JSimInvalidParametersException("JSimSimulation.addProcess(): process");
        }

        // Welcome, process. Now we count with you.
        processes.add(process);
        processesForGUI.add(process);
        notStartedProcesses.add(process);
        numberOfProcesses++;
        logger.log(Level.INFO, "Process #" + process.getProcessNumber() + " (name `" + process.getName() + "') added to the simulation.");
        bw.write("\r\nINFO: Process #" + process.getProcessNumber() + " (name `" + process.getName() + "') added to the simulation.");
    } // addProcess

    /**
     * Adds a new queue to the simulation. You should never call this method. It
     * is called automatically from JSimHead constructor.
     *
     * @param queue The queue that has to be added to the simulation.
     *
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the specified queue is null.
     */
    protected synchronized final void addQueue(JSimHead queue) throws JSimInvalidParametersException, IOException {
        if (queue == null) {
            throw new JSimInvalidParametersException("JSimSimulation.addQueue(): queue");
        }

        // Welcome, queue. Now we count with you.
        queues.add(queue);
        numberOfQueues++;
        logger.log(Level.INFO, "Queue #" + queue.getHeadNumber() + " (name `" + queue.getHeadName() + "') added to the simulation.");
        bw.write("\r\nINFO: Queue #" + queue.getHeadNumber() + " (name `" + queue.getHeadName() + "') added to the simulation.");
    } // addQueue

    /**
     * Adds a new semaphore to the simulation. You should never call this
     * method. It is called automatically from JSimSemaphore constructor.
     *
     * @param semaphore The semaphore that has to be added to the simulation.
     *
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the specified semaphore is null.
     */
    public synchronized final void addSemaphore(JSimSemaphore semaphore) throws JSimInvalidParametersException, IOException {
        if (semaphore == null) {
            throw new JSimInvalidParametersException("JSimSimulation.addSemaphore(): semaphore");
        }

        // Welcome, semaphore. Now we count with you.
        semaphores.add(semaphore);
        numberOfSemaphores++;
        logger.log(Level.INFO, "Semaphore #" + semaphore.getSemaphoreNumber() + " (name `" + semaphore.getSemaphoreName() + "') added to the simulation.");
        bw.write("\r\nINFO: Semaphore #" + semaphore.getSemaphoreNumber() + " (name `" + semaphore.getSemaphoreName() + "') added to the simulation.");
    } // addSemaphore

    /**
     * Starts all new processes. You should never call this method. New
     * processes are those processes that were added to the simulation between
     * the last and this step from outside the simulation or during the last
     * step from a process. We do not check that the processes are in the
     * process set because a process cannot be removed from the simulation by
     * another process.
     *
     * @return True if one or more processes were started, false otherwise.
     * False does not mean a failure, just the fact that there were no
     * not-started-yet processes.
     */
    protected synchronized final boolean startNewProcesses() {
        Iterator<JSimProcess> it;
        JSimProcess process;
        boolean areThereAnyNew = false;

        it = notStartedProcesses.iterator();
        while (it.hasNext()) {
            process = it.next();
            if (process.hasBeenStarted() == false) // Every process should be not-started-yet
            {
                process.start(); // This will delete the `new' flag in getReady()
                Thread.yield(); // Let it spend some time to get blocked in getReady()
                areThereAnyNew = true;
                logger.log(Level.FINE, "Process #" + process.getProcessNumber() + " (name `" + process.getName() + "') started, its state is: " + process.getState() + " (" + process.getProcessStateAsString() + ").");
            } // Not started yet
            else {
                logger.log(Level.WARNING, "An already started process (#" + process.getProcessNumber() + ") found in the not-started-processes list, ignored.");
            }
        } // while

        it = null; // To prevent access via iterator after clear()
        notStartedProcesses.clear();

        return areThereAnyNew;
    } // startNewProcesses

    /**
     * Deletes a process from the simulation. You should never call this method.
     * This method is called by a process which is volunteering to die. Other
     * processes, killed during shutdown, do not call this method. We do not
     * care about notStartedProcesses because the process is volunteering to die
     * and therefore has been started already. There is no need to kill the
     * deleted process by interrupt(). It is at the end of its life in finish()
     * and will die as soon as it gets out from this method.
     *
     * @param process The process to be deleted from the simulation.
     */
    protected synchronized final void deleteProcess(JSimProcess process) {
        if (processes.contains(process)) {
            processes.remove(process);
            numberOfProcesses--;
            logger.log(Level.FINE, "Process #" + process.getProcessNumber() + " (name `" + process.getName() + "') deleted from the simulation.");
        } // if
        else {
            logger.log(Level.WARNING, "Cannot delete a process. The process specified was not found.");
        }

        logger.log(Level.FINE, "Number of processes left: " + getNumberOfProcesses() + "/" + processes.size() + ".");
        // There is no need to kill it by interrupt().
    } // deleteProcess

    /**
     * Deletes all processes from the simulation. Every process is interrupted
     * which causes it to wake-up and finish its run() method. You should never
     * call this method. It is called automatically from shutdown() when
     * sleeping processes (in hold() or passivate()) have to interrupted and
     * terminated.
     */
    private synchronized final void deleteAllProcesses() throws IOException {
        Iterator<JSimProcess> it;
        JSimProcess process;

        it = processes.iterator();
        while (it.hasNext()) {
            process = it.next();
            process.interrupt();
            numberOfProcesses--;
            logger.log(Level.FINE, "Process #" + process.getProcessNumber() + " (name `" + process.getName() + "') interrupted and deleted from the simulation.");
        } // while

        processes.clear();
        logger.log(Level.INFO, "All processes deleted from the simulation.");
        bw.write("\r\nINFO: All processes deleted from the simulation.");
        logger.log(Level.FINE, "Number of processes left: " + getNumberOfProcesses() + "/" + processes.size() + ". (Should be 0.)");
    } // deleteAllProcesses

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Executes one simulation step. A step is a unit of activity that is
     * terminated by a process' call to the passivate() or hold() method or by a
     * process' death. If there is no process within the simulation or no
     * processes are scheduled or the quit button in GUI batch mode is pressed,
     * simulation will terminate and false will be returned. In all other cases,
     * true will be returned.
     *
     * The method is synchronized with a special lock (stepLock) instead of the
     * implicit one. The reason for not using the implicit lock is that the
     * simulation would block before entering the synchronized(processLock)
     * section because a process would have this lock (processLock) acquired
     * while demanding to call a method synchronized with the implicit lock
     * (e.g. getRunningProcess() in getReady()) and the implicit lock would
     * never be released in step(), even not after passivation of the main
     * thread.
     *
     * @return False if the step could not be executed because there were no
     * events in the calendar or there were no processes in the simulation or
     * the quit button in the GUI was pressed, true otherwise (the step was
     * successufully completed).
     *
     * @exception JSimKernelPanicException This exception is thrown out when the
     * state of the simulation cannot be determined or when an inconsistency of
     * J-Sim internal structures (such as the calendar) is revealed.
     * @exception JSimMethodNotSupportedException This exception is thrown out
     * when the simulation is running in GUI interactive mode and step() is
     * called from elsewhere than GUI.
     */
    public boolean step() throws JSimMethodNotSupportedException, IOException {
        logger.log(Level.INFO, "Step requested.");

        // Calling step() from main() is forbidden if running in GUI interactive mode
        if ((mode == JSimSimulationMode.GUI_INTERACTIVE) && (!isRunningGUI())) {
            throw new JSimMethodNotSupportedException("JSimSimulation.step(): Wrong simulation mode. You cannot call step() directly in GUI interactive mode.");
        }

        // Start the GUI if we are running in GUI batch mode and step() is called for the first time
        if ((mode == JSimSimulationMode.GUI_BATCH) && (!firstStepExecuted) && (!isRunningGUI())) {
            firstStepExecuted = true;
            runBatchGUI();
        } // if

        synchronized (stepLock) {
            boolean selectAnother;
            JSimProcess process = null;
            boolean result;
            Object processLock = null;

            // Some special conditions related to the GUI batch mode must be handled first.
            if (mode == JSimSimulationMode.GUI_BATCH) {
                // Pause button pressed
                while (mainWindow.getPausePressed()) {
                    try {
                        logger.log(Level.FINE, "STEP: Simulation paused by the Pause button, suspending the simulation.");
                        // Waiting here until the continue button pressed
                        stepLock.wait();
                        logger.log(Level.FINE, "STEP: The Continue button pressed, going on with the simulation.");
                    } // try
                    catch (InterruptedException e) {
                        logger.log(Level.FINE, "STEP: Interrupted while waiting for user's permission to continue!");
                    } // catch
                } // while getPausePressed()

                // Quit button pressed --> exit step() immediately
                if (mainWindow.getQuitPressed()) {
                    return false;
                }
            } // if MODE_GUI_BATCH

            // We will probably wait here for a while until all new threads call their wait()
            logger.log(Level.FINE, "STEP: Starting new processes...");
            if (startNewProcesses() == true) {
                Thread.yield();
            }
            logger.log(Level.FINE, "STEP: New processes should have been started by now.");

            switch (simulationState) {
                // The simulation has not been started yet
                case NOT_STARTED:
                    logger.log(Level.FINE, "STEP: step() called for the first time.");
                    if (getNumberOfProcesses() == 0) {
                        logger.log(Level.FINE, "STEP: No processes within the simulation, terminating.");
                        simulationState = JSimSimulationState.TERMINATED;
                        result = false;
                        // We do not want to continue with selecting and running a process -> break the whole switch
                        break;
                    } // if
                    else {
                        logger.log(Level.FINE, "STEP: Processes exist, proceeding with the first step.");
                        simulationState = JSimSimulationState.IN_PROGRESS;
                        result = true;
                    } // else
                // Note that there is NO BREAK here! We simply continue to start a process.
                // End of NOT_STARTED

                // The simulation is running already
                case IN_PROGRESS:
                    logger.log(Level.FINE, "STEP: Simulation in progress, trying to select a process and run it.");
                    selectAnother = true;
                    result = true;

                    while (selectAnother) {
                        selectAnother = false;
                        if (calendar.isEmpty()) {
                            //selectAnother = false;
                            logger.log(Level.FINE, "STEP: No more scheduled processes, terminating the simulation.");
                            simulationState = JSimSimulationState.TERMINATED;
                            result = false;
                        } // if calendar is empty
                        else {
                            time = calendar.getFirstProcessTime();
                            process = calendar.getFirstProcess();
                            // The folowing condition should never be true here
/*							if ((process == JSimCalendar.NOBODY) || (time == JSimCalendar.NEVER))
                             {
                             logger.log(Level.SEVERE, "STEP: The calendar reported it is not empty but the first process or its time cannot be determined.");
                             logger.log(Level.SEVERE, "J-Sim Kernel in panic, please report.");
                             throw new JSimKernelPanicException();
                             } // if
                             */
                            // Is the selected process alive?
                            if (!process.isAlive()) {
                                selectAnother = true;
                                logger.log(Level.WARNING, "STEP: Process #" + process.getProcessNumber() + " (name `" + process.getName() + "') is not alive. Its state is " + process.getProcessStateAsString() + ", trying to select another one.");
                            } // if not alive
                            else {
                                // We must get the process's private lock now
                                logger.log(Level.INFO, "STEP: Running process #" + process.getProcessNumber() + " (name `" + process.getName() + "').");
                                bw.write("\r\nINFO: STEP: Running process #" + process.getProcessNumber() + " (name `" + process.getName() + "').");
                                processLock = process.getPrivateLock();
                                runningProcess = process;
                            } // else not alive
                        } // else calendar is empty

                        // We will pick up another item in next turn.
                        if (selectAnother == true) {
                            calendar.jump();
                        }
                    } // while (selectAnother)

                    // The state might have changed to TERMINATED since entering this branch of switch
                    // because the calendar could be empty.
                    if (simulationState == JSimSimulationState.IN_PROGRESS) {
                        // Now sending the wake-up signal to to the selected process
                        // and waiting in suspended state until it switches back
                        if (processLock == null) {
                            logger.log(Level.SEVERE, "STEP: Unexpected situation (processLock == null), J-Sim Kernel in panic. Please report.");
                            throw new JSimKernelPanicException();
                        } // if lock null

                        // Now we must prepare the calendar for the next iteration.
                        // This should be done BEFORE the selected process actually runs
                        // because of possible time rollbacks in InsecureSimulation.
                        // If it were called after, it would remove an event added after
                        // the event selected now (an event with a new simulation time
                        // in the past) and this one would remain there, although it should
                        // have been removed.
                        // Update 0.6.0: The Insecure package has been removed but let's keep it here.
                        calendar.jump();

                        synchronized (processLock) {
                            processLock.notify();

                            // Now waiting here until mainSwitchingRoutine() is called.
                            // NOBODY = this thread.
                            // The while cycle is not necessary, however, it is a good practice
                            // to wrap every wait() inside while. This thread should not be
                            // notified by anything else than the selected process that invokes
                            // myLock.notify().
                            while (runningProcess != JSimCalendar.NOBODY) {
                                try {
                                    logger.log(Level.FINE, "STEP: The main thread is sleeping.");
                                    processLock.wait();
                                } // try
                                catch (InterruptedException e) {
                                    // Just ignoring this exception.
                                    // Nobody should iterrupt the main thread.
                                    logger.log(Level.WARNING, "STEP: Unexpected interruption during the sleep, ignoring it.");
                                    logger.log(Level.WARNING, "STEP: Expect simulation malfunction.");
                                } // catch
                            } // while runningProcess == NOBODY

                            logger.log(Level.FINE, "STEP: Woken up, the step successfully completed.");
                        } // synchronized(processLock)
                    } // if state == SIMULATION_IN_PROGRESS
                    break; // IN_PROGRESS
                // End of IN_PROGRESS

                // The simulation has been terminated already.
                case TERMINATED:
                    logger.log(Level.FINE, "STEP: The simulation is already terminated, exiting.");
                    result = false;
                    break; // TERMINATED
                // End of TERMINATED

                default:
                    logger.log(Level.SEVERE, "STEP: Unexpected situation (Unknown state), J-Sim Kernel in panic. Please report.");
                    throw new JSimKernelPanicException();
            } // switch simulationState

            // Now, let's update the GUI
            if ((windowOpen) && (time >= (lastGUIUpdate + nextGUIUpdateDelta))) {
                guiUpdate.changed();
                lastGUIUpdate = time;
            } // if update necessary

            logger.log(Level.INFO, "Step completed, return value = " + result + ".");
            bw.write("\r\nINFO: Step completed, return value = " + result + ".");            
            return result;
            // Now returning to the method that called us -- usually main().
        } // synchronized(stepLock)
    } // step

    /**
     * Shutdowns the simulation by interrupting all living processes. You must
     * always call this function, preferably in the finally block at the end of
     * your main program.
     */
    public synchronized void shutdown() throws IOException {
        logger.log(Level.INFO, "Shutting down the simulation...");
        bw.write("\r\nINFO: Shutting down the simulation...");
        logger.log(Level.FINE, "Deleting J-Sim processes...");
        deleteAllProcesses(); // This should interrupt all processes
        logger.log(Level.FINE, "J-Sim processes deleted.");
        simulationState = JSimSimulationState.TERMINATED;
        // We let all the threads finish first
        Thread.yield();

        noOfSimulations--;
        logger.log(Level.INFO, "Shutdown completed.");
        bw.write("\r\nINFO: Shutdown completed.");
        bw.close();
    } // shutdown

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Returns the simulation's unique number.
     *
     * @return The Simulation's unique number.
     */
    public int getSimulationNumber() {
        return myNumber;
    } // getSimulationNumber

    /**
     * Returns the simulation's name.
     *
     * @return The simulation's name.
     */
    public String getSimulationName() {
        return myName;
    } // getSimulationName

    /**
     * Returns the number of processes present within the simulation.
     *
     * @return Number of processes present within the simulation.
     */
    public synchronized final long getNumberOfProcesses() {
        return numberOfProcesses;
    } // getNumberOfProcesses

    /**
     * Returns the number of queues present within the simulation.
     *
     * @return Number of queues present within the simulation.
     */
    public synchronized final long getNumberOfQueues() {
        return numberOfQueues;
    } // getNumberOfQueues

    /**
     * Returns the number of semaphores present within the simulation.
     *
     * @return Number of semaphores present within the simulation.
     */
    public synchronized final long getNumberOfSemaphores() {
        return numberOfSemaphores;
    } // getNumberOfSemaphores

    /**
     * Returns the currently running process. If no process is running
     * JSimCalendar.NOBODY (=null) will be returned.
     *
     * @return The currently running process or JSimCalendar.NOBODY.
     */
    public synchronized final JSimProcess getRunningProcess() {
        return runningProcess;
    } // getRunningProcess

    /**
     * Returns a unique number that can be assigned to a newly created process.
     * You should never call this method.
     *
     * @return A unique number that can be assigned to a newly created process.
     */
    public synchronized final long getFreeProcessNumber() {
        long l;

        if (newProcessNumber >= Long.MAX_VALUE) {
            l = NEW_PROCESS_FORBIDDEN;
        } else {
            l = newProcessNumber;
            newProcessNumber++;
        } // else

        return l;
    } // getFreeProcessNumber

    /**
     * Returns a unique number that can be assigned to a newly created queue.
     * You should never call this method.
     *
     * @return A unique number that can be assigned to a newly created queue.
     */
    public synchronized final long getFreeQueueNumber() {
        long l;

        if (newQueueNumber >= Long.MAX_VALUE) {
            l = NEW_QUEUE_FORBIDDEN;
        } else {
            l = newQueueNumber;
            newQueueNumber++;
        } // else
        return l;
    } // getFreeQueueNumber

    /**
     * Returns a unique number that can be assigned to a newly created
     * semaphore. You should never call this method.
     *
     * @return A unique number that can be assigned to a newly created
     * semaphore.
     */
    public synchronized final long getFreeSemaphoreNumber() {
        long l;

        if (newSemaphoreNumber >= Long.MAX_VALUE) {
            l = NEW_SEMAPHORE_FORBIDDEN;
        } else {
            l = newSemaphoreNumber;
            newSemaphoreNumber++;
        } // else
        return l;
    } // getFreeSemaphoreNumber

    /**
     * Switches the execution to the main thread. You should never call this
     * method. This method is called by a process from its hold(), passivate(),
     * or reactivate(). Calling this method does not guarantee the physical
     * thread switch itself, it just sets up the running thread variable.
     */
    protected synchronized final void switchToNobody() {
        runningProcess = JSimCalendar.NOBODY;
    } // switchToNobody

    /**
     * Returns the simulation's current time.
     *
     * @return The simulation's current time.
     */
    public synchronized final double getCurrentTime() {
        return time;
    } // getCurrentTime

    /**
     * Returns the simulation's state.
     *
     * @return The simulation's state.
     */
    public synchronized final JSimSimulationState getSimulationState() {
        return simulationState;
    } // getSimulationState

    /**
     * Returns the simulation's mode.
     *
     * @return The simulation's mode.
     */
    public synchronized final JSimSimulationMode getSimulationMode() {
        return mode;
    } // getSimulationMode

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Returns the lock object used for synchronization between the simulation
     * and its GUI window. You should never call this method.
     *
     * @return The lock object used for sychronization between the simulation
     * and its GUI window.
     */
    public final Object getGraphicLock() {
        return graphicLock;
    } // getGraphicLock

    /**
     * Returns the lock object that protects the step() method from being
     * invoked in parallel. It is also used for synchronization between the
     * simulation and its GUI window, while being paused. You should never call
     * this method.
     *
     * @return The lock object that protects the step() method.
     */
    public final Object getStepLock() {
        return stepLock;
    } // getStepLock

    /**
     * Returns the lock object used for synchronization between the simulation
     * and its GUI window, while waiting in waitForWindowClosure(). You should
     * never call this method.
     *
     * @return The lock object used for synchronization between the simulation
     * and its GUI window.
     */
    public final Object getWindowClosureLock() {
        return waitForWindowClosureLock;
    } // getEndLock

    /**
     * Returns an observable object notifying the GUI about possible changes.
     * You should never call this method.
     *
     * @return An observable object notifying the GUI about possible changes.
     */
    public final JSimChange getChange() {
        return guiUpdate;
    } // getChange

    /**
     * Returns a set containing information about simulation elements that are
     * to be displayed in the GUI. You should never call this method.
     *
     * @return A set containing information about simulation elements.
     *
     * @param elementType Specifies the type of elements whose information
     * should be returned. Available constants are JSimMainWindow.LIST_TYPE_*.
     *
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the elementType parameter does not specify a valid type of simulation
     * elements.
     */
    public SortedSet<? extends JSimDisplayable> getObjectsToBeDisplayed(int elementType) throws JSimInvalidParametersException {
        SortedSet<? extends JSimDisplayable> table = null;

        switch (elementType) {
            case JSimMainWindow.LIST_TYPE_PROCESS:
                table = processesForGUI;
                break;

            case JSimMainWindow.LIST_TYPE_QUEUE:
                table = queues;
                break;

            case JSimMainWindow.LIST_TYPE_SEMAPHORE:
                table = semaphores;
                break;

            default:
                throw new JSimInvalidParametersException("JSimSimulation: getObjectsToBeDisplayed(): elementType");
        } // switch

        return table;
    } // getObjectsToBeDisplayed

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Adds a new event to the calendar, with the specified absolute time and
     * the specified process. You should never call this method. Processes
     * themeselves use this method when they have to be scheduled.
     *
     * @param absoluteTime Absolute simulation time of the event being added to
     * the calendar.
     * @param process Process that will be activated at the specified time.
     *
     * @exception JSimInvalidParametersException This exception is thrown out if
     * the time or the process specified are invalid and were rejected by the
     * calendar.
     */
    protected synchronized void addEntryToCalendar(double absoluteTime, JSimProcess process) throws JSimInvalidParametersException {
        try {
            calendar.addEntry(absoluteTime, process);
            logger.log(Level.FINE, "A new event has been added to the calendar. (Process #" + process.getProcessNumber() + " at time " + absoluteTime + ").");
        } // try
        catch (JSimInvalidParametersException e) {
            logger.log(Level.WARNING, "An error occured when adding a new entry to the calendar.", e);
            throw e; // Throwing the same exception to the calling function.
        } // catch
    } // addEntryToCalendar

    /**
     * Deletes one or more entries in the calendar, concerning a process. You
     * should never call this method.
     *
     * @param process Process whose events are to be deleted.
     * @param all Flag saying whether just one (false) or all (true) events of
     * the process should be deleted.
     *
     * @return Number of events deleted.
     *
     * @exception JSimInvalidParametersException This exception is thrown out
     * when the process specified is invalid and was rejected by the calendar.
     */
    protected synchronized int deleteEntriesInCalendar(JSimProcess process, boolean all) throws JSimInvalidParametersException {
        try {
            int noOfDeleted;

            noOfDeleted = calendar.deleteEntries(process, all);
            logger.log(Level.FINE, noOfDeleted + " entries concerning process #" + process.getProcessNumber() + " have been deleted from the calendar.");
            return noOfDeleted;
        } // try
        catch (JSimInvalidParametersException e) {
            logger.log(Level.WARNING, "An error occured when deleting (an) entry(ies) from the calendar.", e);
            throw e; // Throwing the same exception to the calling function.
        } // catch
    } // deleteEntriesInCalendar

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Prints a standard message or an error message. If not running in a GUI
     * window, standard messages are printed to the standard output and error
     * messages to the error output. When running in a GUI window, all messages
     * are printed to the user text area at the bottom of the window. You should
     * never use this method since there are methods message() and error() in
     * the JSimProcess class.
     *
     * @param s The text message to be printed out.
     * @param code Code saying whether it is an info message (PRN_MESSAGE) or an
     * error (PRN_ERROR).
     * @param newLine Flag indicating that the new line character should be
     * appended to the text.
     */
    public synchronized void printString(String s, int code, boolean newLine) {
        // If message() is called between creation of the simulation and creation
        // of the main window (first call of step()) in GUI batch mode, the GUI must be started here.
        if ((mode == JSimSimulationMode.GUI_BATCH) && (!isRunningGUI()) && (!mainWindowHasAlreadyExisted)) {
            runBatchGUI();
        }

        if (!isRunningGUI()) {
            switch (code) {
                case PRN_MESSAGE:
                    if (newLine) {
                        System.out.println(s);
                    } else {
                        System.out.print(s);
                    }
                    break;

                case PRN_ERROR:
                    if (newLine) {
                        System.err.println(s);
                    } else {
                        System.err.print(s);
                    }
                    break;

                default:
                    break;
            } // switch code
        } else {
            mainWindow.printString(s, code, newLine);
        }
    } // printString

    /**
     * Returns true if the simulation is running in a GUI window.
     *
     * @return True if the simulation is running in a GUI window, false
     * otherwise.
     */
    public final boolean isRunningGUI() {
        synchronized (graphicLock) {
            return windowOpen;
        } // synchronized(graphicLock)
    } // isRunningGUI

    /**
     * Opens a graphic window and lets the user control the simulation's
     * execution using this window. Does not exit until the user presses the
     * `Quit' button. This method can be used in GUI interactive mode only.
     *
     * @return True if GUI creation was succesful, false otherwise.
     *
     * @exception JSimMethodNotSupportedException This exception is thrown out
     * if the simulation is not running in GUI interactive mode.
     */
    public final boolean runGUI() throws JSimMethodNotSupportedException {
        if (mode != JSimSimulationMode.GUI_INTERACTIVE) {
            throw new JSimMethodNotSupportedException("JSimSimulation.runGUI(): Wrong mode. This method can be used in GUI interactive mode only.");
        }

        synchronized (graphicLock) {
            if (windowOpen) {
                logger.log(Level.WARNING, "Unexpected inconsistency (windowOpen), exiting.");
                return false;
            }

            logger.log(Level.FINE, "Creating the main simulation window...");

            // If something goes wrong, we don't jump to the end but handle it here.
            try {
                mainWindow = new JSimMainWindow(this, JSimSimulationMode.GUI_INTERACTIVE);
            } // try
            catch (JSimException e) {
                logger.log(Level.WARNING, "An error occured while creating the window.", e);
                mainWindow = null;
            } // catch

            // If we are successful, we show the window up and then wait for its closure.
            if (mainWindow != null) {
                mainWindow.setVisible(true);
                logger.log(Level.FINE, "Window successfully opened, waiting for its closure.");
                windowOpen = true;
                mainWindowHasAlreadyExisted = true;
                guiUpdate.changed();

                // We shouldn't cycle here at all but if something goes wrong we will not run in paralell to the window.
                while (windowOpen) {
                    try {
                        logger.log(Level.FINE, "Main thread: Sleeping.");
                        graphicLock.wait();
                    } // try
                    catch (InterruptedException e) {
                        logger.log(Level.WARNING, "Main thread: Interrupted while waiting for the main window's closure!");
                        windowOpen = false; // We must get out
                    } // catch
                } // while

                logger.log(Level.FINE, "Woken up, destroying the window.");
                windowOpen = false; // This should be set by the window itself already
                if (mainWindow.isShowing()) {
                    mainWindow.setVisible(false);
                }
                mainWindow.dispose();
                mainWindow = null;
                return true;
            } // if window not null
            else {
                logger.log(Level.WARNING, "Cannot open the main window!");
                windowOpen = false;
                return false;
            } // else window not null
        } // synchronized(graphicLock)
    } // runGUI

    /**
     * Opens a graphic window for GUI batch mode and keeps it open. The window
     * will exist while the step() will be called from the main program. You
     * should never call this method. It is run automatically from step(),
     * message(), or error() when J-Sim detects that the window should be opened
     * up.
     *
     * @return True if the creation was succesful, false otherwise.
     */
    protected final boolean runBatchGUI() {
        synchronized (graphicLock) {
            if (windowOpen) {
                logger.log(Level.WARNING, "Unexpected inconsistency (windowOpen), exiting.");
                return false;
            }

            logger.log(Level.FINE, "Creating the main simulation window...");

            // If something goes wrong, we don't jump to the end but handle it here.
            try {
                mainWindow = new JSimMainWindow(this, JSimSimulationMode.GUI_BATCH);
            } // try
            catch (JSimException e) {
                logger.log(Level.WARNING, "An error occured while creating the window.", e);
                mainWindow = null;
            } // catch

            // If we are successful, we show the window up.
            if (mainWindow != null) {
                mainWindow.setVisible(true);
                logger.log(Level.FINE, "Window successfully opened.");
                windowOpen = true;
                mainWindowHasAlreadyExisted = true;
                return true;
            } else {
                logger.log(Level.WARNING, "Cannot open the main window!");
                windowOpen = false;
                return false;
            } // else
        } // synchronized(graphicLock)
    } // runBatchGUI

    /**
     * The simulation is informed about the main window's closure. You should
     * never use this method. Only the main window can call this method although
     * it is public. Used in GUI interactive mode.
     *
     * @param caller The main window informing about its closure.
     */
    public final void windowIsClosing(JSimMainWindow caller) {
        synchronized (graphicLock) {
            if ((caller != null) && (caller == mainWindow)) {
                windowOpen = false;
            }
        } // synchronized(graphicLock)
    } // windowIsClosing

    /**
     * Suspends the calling thread until the `Quit' button of the main window is
     * pressed. Use this method in GUI batch mode to keep the main window open
     * after the simulation is done (when step() is no longer called) to let the
     * user see the results. The simulation need not necessarily be in
     * terminated state.
     *
     * @exception JSimMethodNotSupportedException This exception is thrown out
     * if the simulation is not running in GUI batch mode.
     */
    public void waitForWindowClosure() throws JSimMethodNotSupportedException {
        if (mode != JSimSimulationMode.GUI_BATCH) {
            throw new JSimMethodNotSupportedException("JSimSimulation.waitForWindowClosure(): Wrong mode. This method can be used in GUI batch mode only.");
        }

        if (windowOpen) // We can wait only if we have something to wait for.
        {
            waitingForWindowClosure = true;
            guiUpdate.changed(); // The last GUI update
            synchronized (waitForWindowClosureLock) {
                try {
                    logger.log(Level.FINE, "WAITFORWINDOWCLOSURE: Waiting for the main window closure.");
                    waitForWindowClosureLock.wait();
                } // try
                catch (InterruptedException e) {
                    logger.log(Level.WARNING, "The main thread interrupted while waiting for the main window closure!");
                } // catch
            } // synchronized(waitForWindowClosureLock)
            waitingForWindowClosure = false;
            mainWindowHasAlreadyExisted = true;
        } // if running GUI & window open
        else {
            logger.log(Level.FINE, "WAITFORWINDOWCLOSURE: Not waiting, the main window already destroyed or never existed.");
        }
    } // waitForWindowClosure

    /**
     * Returns true if the simulation is waiting in waitForWindowClosure().
     *
     * @return True if the simulation is waiting in waitForWindowClosure(),
     * false otherwise.
     */
    public boolean getWaitingForWindowClosure() {
        return waitingForWindowClosure;
    } // getWaitingForWindowClosure

    // ------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Prints out a text message, either to the standard output or to the
     * simulation window.
     *
     * @param s The message to be printed out.
     */
    public void message(String s) {
        printString(s, JSimSimulation.PRN_MESSAGE, true);
    } // message

    /**
     * Prints out a text message, either to the standard output or to the
     * simulation window, but does not terminate the line.
     *
     * @param s The message to be printed out.
     */
    public void messageNoNL(String s) {
        printString(s, JSimSimulation.PRN_MESSAGE, false);
    } // message

    /**
     * Prints out a text error message, either to the error output or to the
     * simulation window.
     *
     * @param s The error message to be printed out.
     */
    public void error(String s) {
        printString(s, JSimSimulation.PRN_ERROR, true);
    } // error
} // class JSimSimulation
