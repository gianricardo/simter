/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador.rotas;

import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
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
import simulador.random.DistributionFunctionStream;

/**
 *
 * @author Eduardo
 */
public class BercoToRotaSaidaRt extends RouteBase {

    private List<Navio> _navios = new ArrayList();
    private RotaSaidaNaviosRt _rotaSaidaPorto;
    private Berco _berco;

    public BercoToRotaSaidaRt(String idRoute, JSimSimulation simulation, int capacidade, Berco berco, RotaSaidaNaviosRt rotaSaidaPorto, DistributionFunctionStream stream)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException {
        super(idRoute, simulation, capacidade, stream);
        _berco = berco;
        _rotaSaidaPorto = rotaSaidaPorto;
    }

    @Override
    protected void life() {
        while (true) {
            if (_navios.isEmpty()) {
                try {
                    // If we have nothing to do, we sleep.
                    passivate();
                } catch (JSimSecurityException ex) {
                    Logger.getLogger(FilaNaviosEntradaToPraticoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    hold(super.getStream().getNext());
                    while (true) {
                        if (!_navios.isEmpty()) {
                            if (ElementOut()) {
                                if (_berco.isIdle()) {
                                    _berco.activate(myParent.getCurrentTime());
                                }
                                if (_rotaSaidaPorto.isIdle()) {
                                    _rotaSaidaPorto.activate(myParent.getCurrentTime());
                                }
                                break;
                            } else {
                                passivate();
                            }
                        }
                    }
                } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                    Logger.getLogger(FilaNaviosEntradaToPraticoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public boolean GetNextElement(Navio elemento) {
        if (super.isOcupada()) {
            return false;
        } else {
            _navios.add(elemento);
            _navios.get(0).escreverArquivo("\r\n colocado na " + this.getName() + " no momento " + myParent.getCurrentTime());
            return true;
        }
    }

    public boolean ElementOut() {
        if (_rotaSaidaPorto.GetNextElement(_navios.get(0))) {
            super.LiberarRota();
            _navios.remove(0);
            return true;
        }
        return false;
    }
}
