/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package EntidadesPorto;

import cz.zcu.fav.kiv.jsim.JSimException;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimLink;
import cz.zcu.fav.kiv.jsim.JSimProcess;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimSimulationAlreadyTerminatedException;
import cz.zcu.fav.kiv.jsim.JSimSystem;
import cz.zcu.fav.kiv.jsim.JSimTooManyProcessesException;
import java.io.IOException;

/**
 *
 * @author Eduardo
 */
public class GeradorNavios extends JSimProcess {

    private double lambda;
    private FilaNavios queue;
    private int numeroNavio = 1;
    private JSimSimulation simulation;

    public GeradorNavios(String name, JSimSimulation sim, double l, FilaNavios q)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException {
        super(name, sim);
        lambda = l;
        queue = q;
        simulation = sim;
    } // constructor

    @Override
    protected void life() {
        JSimLink link;
        try {
            while (true) {
                // Periodically creating new navios and putting them into the queue.
                link = new JSimLink(new Navio(myParent.getCurrentTime(), String.valueOf(numeroNavio), super.getName(), 1, simulation));
                link.into(queue);
                if (queue.getBerco().isIdle()) {
                    queue.getBerco().activate(myParent.getCurrentTime());
                }

                hold(JSimSystem.uniform(30, 30));
                numeroNavio++;
            } // while
        } // try
        catch (JSimException e) {
            e.printStackTrace();
            e.printComment(System.err);
        } // catch
    } // life
}
