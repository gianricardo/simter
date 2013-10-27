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
import shipyard.land.move.Portainer;
import simulador.queues.FilaCaminhoesInternos;
import simulador.rotas.DecisaoPosicaoToPosicaoBercoRt;
import simulador.rotas.PosicaoBercoToDecisaoPosicaoEstacaoRt;

/**
 *
 * @author Eduardo
 */
public class PosicaoCargaDescargaBerco extends JSimProcess {

    private CaminhaoPatio _caminhao;
    private boolean _posicaoOcupada;
    private JSimSimulation _simulation;
    private Portainer _portainer;
    private String _nome;
    private File _arquivo;
    private FileWriter _fw;
    private BufferedWriter _bw;
    private boolean _navioOcupandoPosicao;
    private int _numeroContainersDescarregarNavio;
    private int _numeroContainersCarregarNavio;
    private DecisaoPosicaoToPosicaoBercoRt _rotaDecisaoPosicaoCargaDescargaBerco;
    private PosicaoBercoToDecisaoPosicaoEstacaoRt _rotaPosicaoDecisaoPosicaoEstacao;

    public PosicaoCargaDescargaBerco(String name, JSimSimulation sim, Portainer p)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException {
        super(name, sim);
        _simulation = sim;
        _portainer = p;
        _nome = name;
    } // constructor

    @Override
    protected void life() {
        try {
            while (true) {
                if (_caminhao == null) {
                    passivate();
                } else {
                    _caminhao.setFinalizado(false);
                    if (_caminhao.getContainer() == null) {
                        _caminhao.escreverArquivo(" -Chegou na posição " + getName() + " sem container.");
                    } else {
                        _caminhao.escreverArquivo(" -Chegou na posição " + getName() + " com o container " + _caminhao.getContainer().getId());
                    }

                    if (_portainer.isIdle()) {
                        _portainer.activate(myParent.getCurrentTime());
                    }

                    passivate();

                    while (true) {
                        if (_caminhao.isFinalizado()) {
                            try {
                                if (!liberarCaminhao(_caminhao)) {
                                    passivate();
                                } else {
                                    if (_rotaDecisaoPosicaoCargaDescargaBerco.isIdle()) {
                                        _rotaDecisaoPosicaoCargaDescargaBerco.activate(myParent.getCurrentTime());
                                    }
                                    if (_rotaPosicaoDecisaoPosicaoEstacao.isIdle()) {
                                        _rotaPosicaoDecisaoPosicaoEstacao.activate(myParent.getCurrentTime());
                                    }
                                    break;
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(PosicaoCargaDescargaBerco.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } else {
                            passivate();
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

    public void setPortainer(Portainer p) {
        _portainer = p;
    }

    public boolean liberarCaminhao(CaminhaoPatio caminhao) throws IOException {
        if (!_rotaPosicaoDecisaoPosicaoEstacao.AddCaminhao(caminhao)) {
            return false;
        } else {
            _caminhao.setFinalizado(false);
            _caminhao = null;
            _posicaoOcupada = false;
            return true;
        }
    }

    public CaminhaoPatio getCaminhao() {
        return _caminhao;
    }

    public void setCaminhao(CaminhaoPatio _caminhao) {
        this._caminhao = _caminhao;
    }

    private void criarArquivo() {
        if (_arquivo == null) {
            try {
                _arquivo = new File("../ArquivoPosicaoCargaDescargaBerco" + _nome + ".txt");
                _fw = new FileWriter(_arquivo, false);
                _bw = new BufferedWriter(_fw);
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    private void escreverArquivo(FilaCaminhoesInternos fila) {
        try {
            _bw.write("\r\nCaminhao " + _caminhao.getIdCaminhao()
                    + "\r\n colocado na posicao " + _nome
                    + "\r\n -Colocado na fila " + fila.getHeadName()
                    + " \r\n");
            _bw.flush();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    public void closeBw() throws IOException {
        _bw.close();
    }

    public boolean isNavioOcupandoPosicao() {
        return _navioOcupandoPosicao;
    }

    public void setNavioOcupandoPosicao(boolean _navioPosicao) {
        this._navioOcupandoPosicao = _navioPosicao;
    }

    public int getNumeroContainersDescarregarNavio() {
        return _numeroContainersDescarregarNavio;
    }

    public int getNumeroContainersCarregarNavio() {
        return _numeroContainersCarregarNavio;
    }

    public void setNumeroContainersDescarregarNavio(int _numeroContainersDescarregarNavio) {
        this._numeroContainersDescarregarNavio = _numeroContainersDescarregarNavio;
    }

    public void setNumeroContainersCarregarNavio(int _numeroContainersCarregarNavio) {
        this._numeroContainersCarregarNavio = _numeroContainersCarregarNavio;
    }

    public DecisaoPosicaoToPosicaoBercoRt getRotaDecisaoPosicaoCargaDescargaBerco() {
        return _rotaDecisaoPosicaoCargaDescargaBerco;
    }

    public void setRotaDecisaoPosicaoCargaDescargaBerco(DecisaoPosicaoToPosicaoBercoRt _rotaDecisaoPosicaoCargaDescargaBerco) {
        this._rotaDecisaoPosicaoCargaDescargaBerco = _rotaDecisaoPosicaoCargaDescargaBerco;
    }

    public boolean isPosicaoOcupada() {
        return _posicaoOcupada;
    }

    public void setPosicaoOcupada(boolean _posicaoOcupada) {
        this._posicaoOcupada = _posicaoOcupada;
    }

    public void setRotaPosicaoDecisaoPosicaoEstacao(PosicaoBercoToDecisaoPosicaoEstacaoRt _rotaPosicaoDecisaoPosicaoEstacao) {
        this._rotaPosicaoDecisaoPosicaoEstacao = _rotaPosicaoDecisaoPosicaoEstacao;
    }
}
