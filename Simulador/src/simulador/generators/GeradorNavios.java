/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador.generators;

import cz.zcu.fav.kiv.jsim.JSimException;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimProcess;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimSimulationAlreadyTerminatedException;
import cz.zcu.fav.kiv.jsim.JSimTooManyProcessesException;
import java.io.IOException;
import shipyard.sea.Navio;
import simulador.queues.FilaNavios;
import simulador.random.DistributionFunctionStream;

/**
 *
 * @author Eduardo
 */
public class GeradorNavios extends JSimProcess {

    private DistributionFunctionStream _tempoEntreNavios,_quantidadeContainerNavio;
    private FilaNavios _queue;
    private int _numeroNavio = 1;
    private JSimSimulation _simulation;

    public GeradorNavios(String name, JSimSimulation sim, DistributionFunctionStream stream, FilaNavios q)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException {
        super(name, sim);
        _tempoEntreNavios = stream;
        _quantidadeContainerNavio = stream;
        _queue = q;
        _simulation = sim;
    } // constructor

    public void setQuantidadeContainerNavio(DistributionFunctionStream _quantidadeContainerNavio) {
        this._quantidadeContainerNavio = _quantidadeContainerNavio;
    }
    
    @Override
    protected void life() {
        try {
            while (true) {
                // Periodically creating new navios and putting them into the queue.
                Navio novo = new Navio(myParent.getCurrentTime(), String.valueOf(_numeroNavio), 2, _simulation);
                novo.setNumeroContainersDescarregar((int)Math.ceil(_quantidadeContainerNavio.getNext()));
                novo.into(_queue);
                if (_queue.getBerco().isIdle()) {
                    _queue.getBerco().activate(myParent.getCurrentTime());
                }
                _numeroNavio++;
                hold(this._tempoEntreNavios.getNext());
            } // while
        } // try
        catch (JSimException e) {
            e.printStackTrace(System.err);
        } // catch
    } // life
}
