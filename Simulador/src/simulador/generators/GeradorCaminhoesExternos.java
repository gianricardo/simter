/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador.generators;

import Enumerators.CaminhaoOperacao;
import Enumerators.ContainerTipos;
import cz.zcu.fav.kiv.jsim.JSimException;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimProcess;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimSimulationAlreadyTerminatedException;
import cz.zcu.fav.kiv.jsim.JSimTooManyProcessesException;
import java.io.IOException;
import shipyard.land.move.CaminhaoExterno;
import shipyard.load.Container;
import simulador.queues.FilaCaminhoesExternos;
import simulador.random.DistributionFunctionStream;
import simulador.rotas.FilaCaminhoesExternosToDecisaoPosicaoRt;

/**
 *
 * @author Eduardo
 */
public class GeradorCaminhoesExternos extends JSimProcess {

    private DistributionFunctionStream _tempoEntreCaminhoes;
    private FilaCaminhoesExternos _queue;
    private int _numeroCaminhao = 1;
    private JSimSimulation _simulation;
    private Container _container;
    private FilaCaminhoesExternosToDecisaoPosicaoRt _rotaEntradaCaminhoes;

    public GeradorCaminhoesExternos(String name, JSimSimulation sim, DistributionFunctionStream stream, FilaCaminhoesExternos q)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException {
        super(name, sim);
        _tempoEntreCaminhoes = stream;
        _queue = q;
        _simulation = sim;
        _numeroCaminhao = 1;
    } // constructor  
    
    @Override
    protected void life() {
        try {
            while (true) {
                // Periodically creating new navios and putting them into the queue.
                CaminhaoExterno novo = new CaminhaoExterno(myParent.getCurrentTime(), String.valueOf(_numeroCaminhao), _simulation);
                
                _container = new Container(myParent.getCurrentTime(), "Container do Caminhão Externo " + _numeroCaminhao, ContainerTipos.CaminhaoExterno, ContainerTipos.Navio);                
                novo.setContainer(_container);
                novo.setCarregado(true);
                novo.setOperacao(CaminhaoOperacao.DescarregarCarregar);
                novo.into(_queue);
                
                if(_rotaEntradaCaminhoes.isIdle()){
                    _rotaEntradaCaminhoes.activate(myParent.getCurrentTime());
                }
                
                _numeroCaminhao++;
                hold(this._tempoEntreCaminhoes.getNext());
            } // while
        } // try
        catch (JSimException e) {
            e.printStackTrace(System.err);
        } // catch
    } // life    

    public void setRotaEntradaCaminhoes(FilaCaminhoesExternosToDecisaoPosicaoRt _rotaEntradaCaminhoes) {
        this._rotaEntradaCaminhoes = _rotaEntradaCaminhoes;
    }
}

