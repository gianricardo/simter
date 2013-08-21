/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package EntidadesPorto;

import EntidadesPorto.FilaNavios;
import cz.zcu.fav.kiv.jsim.JSimException;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimLink;
import cz.zcu.fav.kiv.jsim.JSimProcess;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimSimulationAlreadyTerminatedException;
import cz.zcu.fav.kiv.jsim.JSimSystem;
import cz.zcu.fav.kiv.jsim.JSimTooManyProcessesException;

/**
 *
 * @author Eduardo
 */
public class GeradorNavios extends JSimProcess {
    private double lambda;
    private FilaNavios queue;

	public GeradorNavios(String name, JSimSimulation sim, double l, FilaNavios q)
                throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException
	{
            super(name, sim);
            lambda = l;
            queue = q;
	} // constructor

    @Override
	protected void life()
	{
            JSimLink link;
            try
            {
                while (true)
                {                    
                    // Periodically creating new transactions and putting them into the queue.
                    link = new JSimLink(new Navio(myParent.getCurrentTime()));
                    link.into(queue);
                    if (queue.getBerco().isIdle())
                    {
                        queue.getBerco().activate(myParent.getCurrentTime());
                    }
                    hold(JSimSystem.negExp(lambda));
                } // while
            } // try
            catch (JSimException e)
            {
                e.printStackTrace();
                e.printComment(System.err);
            } // catch
	} // life
}
