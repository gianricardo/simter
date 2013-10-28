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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    
    private File _arquivo;
    private FileWriter _fw;
    private BufferedWriter _bw;

    public BercoToRotaSaidaRt(String idRoute, JSimSimulation simulation, int capacidade, Berco berco, RotaSaidaNaviosRt rotaSaidaPorto, DistributionFunctionStream stream)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException {
        super(idRoute, simulation, capacidade, stream);
        _berco = berco;
        _rotaSaidaPorto = rotaSaidaPorto;
        
        criarArquivo();
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
            _navios.get(0).escreverArquivo("\r\n -Colocado na " + this.getName() + " no momento " + myParent.getCurrentTime());
            escreverArquivo("\r\n -Navio " + _navios.get(0).getIdNavio() + " entrou no momento " + myParent.getCurrentTime());
            return true;
        }
    }

    public boolean ElementOut() {
        if (_rotaSaidaPorto.GetNextElement(_navios.get(0))) {
            super.LiberarRota();
            escreverArquivo(" -Navio " + _navios.get(0).getIdNavio() + " saiu da rota no momento " + myParent.getCurrentTime());
            _navios.remove(0);
            return true;
        }
        return false;
    }
    
    private void criarArquivo() {
        if (_arquivo == null) {
            try {
                _arquivo = new File("../Rotas/arquivo" + this.getName() + ".txt");
                _fw = new FileWriter(_arquivo, false);
                _bw = new BufferedWriter(_fw);
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    public void escreverArquivo(String texto) {
        try {
            _bw.write("\r\n" + texto);
            _bw.flush();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }
}
