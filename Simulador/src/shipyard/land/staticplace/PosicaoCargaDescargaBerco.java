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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import shipyard.land.move.CaminhaoPatio;
import simulador.queues.FilaCaminhoesInternos;
import shipyard.land.move.Portainer;

/**
 *
 * @author Eduardo
 */
public class PosicaoCargaDescargaBerco extends JSimProcess {

    private double _lambda;
    private FilaCaminhoesInternos _queueIn;
    private JSimLink _caminhaoNaPosicao;
    public CaminhaoPatio _caminhao;
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
        try {
            setFila(_estacao.SolicitarEstacao(this));
        } catch (JSimInvalidParametersException | JSimTooManyHeadsException | IOException | JSimSecurityException ex) {
            Logger.getLogger(PosicaoCargaDescargaBerco.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            while (true) {
                if (_queueIn.empty()) {
                    // If we have nothing to do, we sleep.
                    passivate();
                } else {
                    _caminhaoNaPosicao = _queueIn.first();
                    _caminhao = (CaminhaoPatio) _caminhaoNaPosicao.getData();
                    if (_portainer.isIdle()) {
                        _portainer.activate(myParent.getCurrentTime());
                    }
                    passivate();
                    try {
                        LiberarCaminhao(_caminhao);
                    } catch (IOException ex) {
                        Logger.getLogger(PosicaoCargaDescargaBerco.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } // else queue is empty / not empty
            } // while            
        } // try
        catch (JSimException e) {
            e.printStackTrace();
            e.printComment(System.err);
        }
    }

    public void setFila(FilaCaminhoesInternos QueueIn) {
        _queueIn = QueueIn;
    }

    public void setPortainer(Portainer p) {
        _portainer = p;
    }

    public void LiberarCaminhao(CaminhaoPatio caminhao) throws IOException {
        caminhao.carregado = false;
        _estacao.getCaminhoesEstacao().add(caminhao);
    }
}
