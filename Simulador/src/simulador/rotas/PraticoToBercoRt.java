/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador.rotas;

import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimLink;
import cz.zcu.fav.kiv.jsim.JSimSecurityException;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimSimulationAlreadyTerminatedException;
import cz.zcu.fav.kiv.jsim.JSimTooManyProcessesException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import shipyard.land.staticplace.Berco;
import shipyard.sea.Navio;
import shipyard.sea.Pratico;
import simulador.random.DistributionFunctionStream;

/**
 *
 * @author Eduardo
 */
public class PraticoToBercoRt extends RouteBase {

    private List<Navio> _navios = new ArrayList();
    private Berco _berco;
    private Pratico _pratico;

    public PraticoToBercoRt(String idRoute, JSimSimulation simulation, int capacidade, Berco berco, Pratico pratico, DistributionFunctionStream stream)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException {
        super(idRoute, simulation, capacidade, stream);
        _berco = berco;
        _pratico = pratico;
    }

    @Override
    protected void life() {
        while (true) {            
            if (_navios.isEmpty()) {
                try {
                    // If we have nothing to do, we sleep.
                    passivate();
                    _pratico.activateNow();
                } catch (JSimSecurityException ex) {
                    Logger.getLogger(FilaNaviosEntradaToPraticoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    hold(super.getStream().getNext());
                    ElementOut();
                } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                    Logger.getLogger(FilaNaviosEntradaToPraticoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public boolean GetNextElement(JSimLink elemento) {
        if (super.isOcupada()) {
            return false;
        } else {
            if (elemento instanceof Navio) {
                Navio novonavio = (Navio) elemento;
                _navios.add(novonavio);
                _navios.get(0).escreverArquivo("\r\n colocado na rota " + this.getName() + " no momento " + myParent.getCurrentTime() +
                        " pelo prático " + _pratico.getName());
                _berco.setOcupado(true);
            } else {
                System.out.println(elemento.getClass());
            }
            return true;
        }
    }

    public void ElementOut() {
        try {
            _berco.setShip(_navios.get(0));            
            super.LiberarRota();
            _navios.remove(0);    
            _pratico.setOcupado(false);
            _pratico.activate(myParent.getCurrentTime());
            if (_berco.isIdle()) {
                try {
                    _berco.activate(myParent.getCurrentTime());
                } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                    Logger.getLogger(PraticoToBercoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (JSimSecurityException | JSimInvalidParametersException ex) {
            Logger.getLogger(PraticoToBercoRt.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Berco getBerco() {
        return _berco;
    }
}
