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
import shipyard.land.move.CaminhaoExterno;
import shipyard.land.staticplace.PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoExterno;
import simulador.random.DistributionFunctionStream;

/**
 *
 * @author Eduardo
 */
public class RotaSaidaCaminhoesRt extends RouteBase {

    private List<CaminhaoExterno> _caminhoes = new ArrayList();
    private List<PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoExterno> _posicoesCargaDescargaEstacaoArmazenamentos = new ArrayList();
    
    private File _arquivo;
    private FileWriter _fw;
    private BufferedWriter _bw;

    public RotaSaidaCaminhoesRt(String idRoute, JSimSimulation simulation, int capacidade, DistributionFunctionStream stream)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException {
        super(idRoute, simulation, capacidade, stream);
        
        criarArquivo();
    }

    @Override
    protected void life() {
        while (true) {
            if (_caminhoes == null) {
                try {
                    passivate();
                } catch (JSimSecurityException ex) {
                    Logger.getLogger(RotaSaidaCaminhoesRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    hold(super.getStream().getNext());
                    while (true) {
                        if (ElementOut()) {
                            for (int i = 0; i < _posicoesCargaDescargaEstacaoArmazenamentos.size(); i++) {
                                if (_posicoesCargaDescargaEstacaoArmazenamentos.get(i).isIdle()) {
                                    _posicoesCargaDescargaEstacaoArmazenamentos.get(i).activate(myParent.getCurrentTime());
                                }
                            }
                        } else {
                            passivate();
                        }
                    }

                } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                    Logger.getLogger(RotaSaidaCaminhoesRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public boolean ElementOut() {
        if (!_caminhoes.isEmpty()) {
            super.LiberarRota();
            escreverArquivo(" -Caminhao " + _caminhoes.get(0).getIdCaminhao() + " saiu da rota no momento " + myParent.getCurrentTime());
            _caminhoes.remove(0);
            return true;
        }
        return false;
    }

    public boolean addCaminhoes(CaminhaoExterno _caminhao) {
        if (super.isOcupada()) {
            return false;
        } else {
            this._caminhoes.add(_caminhao);
            escreverArquivo("\r\n -Caminhao " + _caminhoes.get(0).getIdCaminhao() + " entrou na rota no momento " + myParent.getCurrentTime());
            _caminhoes.get(0).escreverArquivo(" -Entrou na " + this.getName() + " no momento " + myParent.getCurrentTime());
            super.OcuparRota();
            return true;
        }
    }

    public void addPosicoesCargaDescargaEstacaoArmazenamentos(PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoExterno _posicaoCargaDescargaEstacaoArmazenamentos) {
        this._posicoesCargaDescargaEstacaoArmazenamentos.add(_posicaoCargaDescargaEstacaoArmazenamentos);
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
