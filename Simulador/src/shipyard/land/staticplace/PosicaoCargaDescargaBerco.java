/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shipyard.land.staticplace;

import cz.zcu.fav.kiv.jsim.JSimException;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimLink;
import cz.zcu.fav.kiv.jsim.JSimProcess;
import cz.zcu.fav.kiv.jsim.JSimSecurityException;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimSimulationAlreadyTerminatedException;
import cz.zcu.fav.kiv.jsim.JSimTooManyHeadsException;
import cz.zcu.fav.kiv.jsim.JSimTooManyProcessesException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import shipyard.land.move.CaminhaoPatio;
import shipyard.land.move.Portainer;
import simulador.queues.FilaCaminhoesInternos;

/**
 *
 * @author Eduardo
 */
public class PosicaoCargaDescargaBerco extends JSimProcess {

    private double _lambda;
    private FilaCaminhoesInternos _queueIn;
    private CaminhaoPatio _caminhao;
    private JSimSimulation _simulation;
    private Portainer _portainer;
    private EstacaoCaminhoesInternos _estacao;
    private String _nome;

    public PosicaoCargaDescargaBerco(String name, JSimSimulation sim, double l, Portainer p, EstacaoCaminhoesInternos estacaoCaminhoes)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException {
        super(name, sim);
        _lambda = l;
        _simulation = sim;
        _portainer = p;
        _estacao = estacaoCaminhoes;
        _nome = name;
    } // constructor

    @Override
    protected void life() {
        setFila();

        try {
            while (true) {
                if (_queueIn.empty()) {
                    // If we have nothing to do, we sleep.
                    passivate();
                } else {
                    _caminhao = getNextCaminhao();
                    _caminhao.out();
                    if (_portainer.isIdle()) {
                        _portainer.activate(myParent.getCurrentTime());
                    }
                    passivate();
                    try {
                        liberarCaminhao(_caminhao);
                    } catch (IOException ex) {
                        Logger.getLogger(PosicaoCargaDescargaBerco.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    _portainer.activate(myParent.getCurrentTime());
                } // else queue is empty / not empty
            } // while            
        } // try
        catch (JSimException e) {
            e.printStackTrace();
            e.printComment(System.err);
        }
    }

    public void setFila() {
        _queueIn = _estacao.getFilaCaminhoesEstacao();
    }

    public void setPortainer(Portainer p) {
        _portainer = p;
    }

    public void liberarCaminhao(CaminhaoPatio caminhao) throws IOException {       
        try {
            caminhao.setCarregado(false);
            caminhao.into(_estacao.getFilaCaminhoesEstacao());
        } catch (JSimSecurityException ex) {
            Logger.getLogger(PosicaoCargaDescargaBerco.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public CaminhaoPatio getNextCaminhao() {
        JSimLink jsl = _queueIn.first();
        if (jsl instanceof CaminhaoPatio) {
            CaminhaoPatio novoCaminhao = (CaminhaoPatio) jsl;
            _caminhao = novoCaminhao;
            return _caminhao;
        } else {
            System.out.println(jsl.getClass());
            return null;
        }
    }

    public CaminhaoPatio getCaminhao() {
        return _caminhao;
    }
}
