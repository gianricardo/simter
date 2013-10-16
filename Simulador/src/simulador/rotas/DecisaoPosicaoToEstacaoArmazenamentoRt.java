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
import shipyard.land.staticplace.PosicaoCargaDescargaEstacaoArmazenamento;
import simulador.random.DistributionFunctionStream;

/**
 *
 * @author Eduardo
 */
public class DecisaoPosicaoToEstacaoArmazenamentoRt extends RouteBase {

    private List<CaminhaoExterno> _caminhoes = new ArrayList();
    private PosicaoCargaDescargaEstacaoArmazenamento _posicao;
    private String idRota;
    
    private File _arquivo;
    private FileWriter _fw;
    private BufferedWriter _bw;

    public DecisaoPosicaoToEstacaoArmazenamentoRt(String idRoute, JSimSimulation simulation, int capacidade, DistributionFunctionStream stream)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException {
        super(idRoute, simulation, capacidade, stream);
        idRota = new StringBuilder()
                    .append(idRoute)
                    .toString();
        criarArquivo();
    }

    @Override
    protected void life() {
        while (true) {
            if (_caminhoes.isEmpty()) {
                try {
                    // If we have nothing to do, we sleep.
                    passivate();
                } catch (JSimSecurityException ex) {
                    Logger.getLogger(DecisaoPosicaoToEstacaoArmazenamentoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    hold(super.getStream().getNext());
                    while (true) {
                        if (!ElementOut()) {
                            passivate();
                        } else {
                            if (_posicao.isIdle()) {
                                try {
                                    _posicao.activate(myParent.getCurrentTime());
                                } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                                    Logger.getLogger(DecisaoPosicaoToEstacaoArmazenamentoRt.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            break;
                        }
                    }
                } catch (JSimSecurityException | JSimInvalidParametersException ex) {
                    Logger.getLogger(DecisaoPosicaoToEstacaoArmazenamentoRt.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public boolean ElementOut() {
        if (_posicao.getCaminhao() == null) {
            _posicao.setCaminhao(_caminhoes.get(0));
            super.LiberarRota();
            escreverArquivo("\r\n caminhao " + _caminhoes.get(0).getIdCaminhao() + " saiu no momento " + myParent.getCurrentTime() + " para posicao " + _posicao.getName());
            _caminhoes.remove(0);            

            return true;
        }
        return false;
    }

    public boolean addCaminhoes(CaminhaoExterno _caminhao) {
        if (super.isOcupada()) {
            return false;
        }
        else{
            escreverArquivo("\r\n caminhao " + _caminhao.getIdCaminhao() + " entrou no momento " + myParent.getCurrentTime());
            this._caminhoes.add(_caminhao);
            _caminhoes.get(0).escreverArquivo("\r\nEntrou na " + this.getName() + " no momento " + myParent.getCurrentTime());            
            super.OcuparRota();
            return true;
        }        
    }

    public void setPosicao(PosicaoCargaDescargaEstacaoArmazenamento _posicao) {
        this._posicao = _posicao;
    }
    
    private void criarArquivo() {
        if (_arquivo == null) {
            try {
                _arquivo = new File("../arquivo" + idRota + ".txt");
                _fw = new FileWriter(_arquivo, false);
                _bw = new BufferedWriter(_fw);
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    public void escreverArquivo(String texto) {
        try {
            _bw.write("\r\n " + texto);
            _bw.flush();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }
}
