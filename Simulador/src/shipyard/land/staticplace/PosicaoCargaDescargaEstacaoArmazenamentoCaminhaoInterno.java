/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shipyard.land.staticplace;

import cz.zcu.fav.kiv.jsim.JSimException;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimProcess;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimSimulationAlreadyTerminatedException;
import cz.zcu.fav.kiv.jsim.JSimTooManyProcessesException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import shipyard.land.move.CaminhaoPatio;
import shipyard.land.move.Transtainer;
import simulador.queues.FilaCaminhoesInternos;
import simulador.rotas.DecisaoPosicaoToEstacaoArmazenamentoIntRt;
import simulador.rotas.PosicaoEstacaoToDecisaoPosicaoBercoRt;

/**
 *
 * @author Eduardo
 */
public class PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoInterno extends JSimProcess {

    private CaminhaoPatio _caminhao;
    private JSimSimulation _simulation;
    private Transtainer _transtainer;
    private DecisaoPosicaoToEstacaoArmazenamentoIntRt _rotaAtePosicao;
    private PosicaoEstacaoToDecisaoPosicaoBercoRt _rotaAteDecisaoBerco;
    private FilaCaminhoesInternos _filaCaminhoesVazios;
    private File _arquivo;
    private FileWriter _fw;
    private BufferedWriter _bw;

    public PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoInterno(String name, JSimSimulation sim)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException {
        super(name, sim);
        _simulation = sim;
    } // constructor

    @Override
    protected void life() {
        try {
            while (true) {
                if (_caminhao == null) {
                    // If we have nothing to do, we sleep.
                    passivate();
                } else {
                    _caminhao.setFinalizado(false);
                    if (_caminhao.getContainer() == null) {
                        _caminhao.escreverArquivo(" -Chegou na posição " + getName() + " sem container.");
                    } else {
                        _caminhao.escreverArquivo(" -Chegou na posição " + getName() + " com o " + _caminhao.getContainer().getId());
                    }

                    if (_transtainer.isIdle()) {
                        _transtainer.activate(myParent.getCurrentTime());
                    }

                    passivate();

                    while (true) {
                        try {
                            if (_caminhao.isFinalizado()) {
                                
                                if (_caminhao.getRotaPosicaoBercoAposDecisao() == null) {
                                    _caminhao.into(_filaCaminhoesVazios);
                                    if (_caminhao.getContainer() == null) {
                                        _caminhao.escreverArquivo(" -Deixou a posição " + getName() + " sem container.");
                                    } else {
                                        _caminhao.escreverArquivo(" -Deixou a posição " + getName() + " com o " + _caminhao.getContainer().getId());
                                    }
                                    _caminhao.escreverArquivo("\r\n -Entrou na Fila de Caminhões Vazios no momento " + myParent.getCurrentTime());
                                    _caminhao = null;                                    
                                    if (_rotaAtePosicao.isIdle()) {
                                        _rotaAtePosicao.activate(myParent.getCurrentTime());
                                    }
                                    break;
                                }
                                
                                else if (!liberarCaminhao(_caminhao)) {
                                    passivate();                                    
                                }
                                
                                else {                                    
                                    if (_rotaAteDecisaoBerco.isIdle()) {
                                        _rotaAteDecisaoBerco.activate(myParent.getCurrentTime());
                                    }
                                    if (_rotaAtePosicao.isIdle()) {
                                        _rotaAtePosicao.activate(myParent.getCurrentTime());
                                    }
                                    break;
                                }
                            }
                            else{
                                passivate();
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoExterno.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } // else queue is empty / not empty
            } // while            
        } // try
        catch (JSimException e) {
            e.printStackTrace();
            e.printComment(System.err);
        }
    }

    public void setTranstainer(Transtainer t) {
        _transtainer = t;
    }

    public boolean liberarCaminhao(CaminhaoPatio caminhao) throws IOException {
        if (_rotaAteDecisaoBerco.GetNextElement(caminhao)) {            
            _caminhao = null;
            return true;
        }
        return false;
    }

    public void setCaminhao(CaminhaoPatio _caminhao) {
        this._caminhao = _caminhao;
    }

    public CaminhaoPatio getCaminhao() {
        return _caminhao;
    }

    public void setRotaAtePosicao(DecisaoPosicaoToEstacaoArmazenamentoIntRt _rotaAtePosicao) {
        this._rotaAtePosicao = _rotaAtePosicao;
    }

    public void setRotaAteDecisaoBerco(PosicaoEstacaoToDecisaoPosicaoBercoRt _rotaAteDecisaoBerco) {
        this._rotaAteDecisaoBerco = _rotaAteDecisaoBerco;
    }

    public FilaCaminhoesInternos getFilaCaminhoesVazios() {
        return _filaCaminhoesVazios;
    }

    public void setFilaCaminhoesVazios(FilaCaminhoesInternos _filaCaminhoesVazios) {
        this._filaCaminhoesVazios = _filaCaminhoesVazios;
    }
}
