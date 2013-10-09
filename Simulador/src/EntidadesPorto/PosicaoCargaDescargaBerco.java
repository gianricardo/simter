/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package EntidadesPorto;

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

/**
 *
 * @author Eduardo
 */
public class PosicaoCargaDescargaBerco extends JSimProcess {

    private double lambda;
    private FilaCaminhoesInternos queueIn;
    private JSimLink CaminhaoNaPosicao;
    public CaminhaoPatio caminhao;
    private JSimSimulation Simulation;
    private Portainer Portainer;
    private EstacaoCaminhoesInternos Estacao;
    private String nome;
    private File arquivo;
    private FileWriter fw;
    private BufferedWriter bw;
    private DecimalFormat df = new DecimalFormat("#0.##");

    public PosicaoCargaDescargaBerco(String name, JSimSimulation sim, double l, Portainer p, EstacaoCaminhoesInternos estacaoCaminhoes)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException {
        super(name, sim);
        lambda = l;
        Simulation = sim;
        Portainer = p;
        Estacao = estacaoCaminhoes;
        nome = name;
    } // constructor

    @Override
    protected void life() {
        try {
            setFila(Estacao.SolicitarEstacao(this));
        } catch (JSimInvalidParametersException | JSimTooManyHeadsException | IOException | JSimSecurityException ex) {
            Logger.getLogger(PosicaoCargaDescargaBerco.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            while (true) {
                if (queueIn.empty()) {
                    // If we have nothing to do, we sleep.
                    passivate();
                } else {
                    CaminhaoNaPosicao = queueIn.first();
                    caminhao = (CaminhaoPatio) CaminhaoNaPosicao.getData();
                    if (Portainer.isIdle()) {
                        Portainer.activate(myParent.getCurrentTime());
                    }
                    passivate();
                    try {
                        LiberarCaminhao(caminhao);
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
        queueIn = QueueIn;
    }

    public void setPortainer(Portainer p) {
        Portainer = p;
    }

    public void LiberarCaminhao(CaminhaoPatio caminhao) throws IOException {
        caminhao.carregado = false;
        Estacao.CaminhoesEstacao.add(caminhao);
    }
}
