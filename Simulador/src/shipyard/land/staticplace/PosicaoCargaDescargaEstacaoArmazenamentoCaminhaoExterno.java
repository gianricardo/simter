/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shipyard.land.staticplace;

import cz.zcu.fav.kiv.jsim.JSimException;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimLink;
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
import shipyard.land.move.CaminhaoExterno;
import shipyard.land.move.Transtainer;
import simulador.rotas.DecisaoPosicaoToEstacaoArmazenamentoRt;
import simulador.rotas.RotaSaidaCaminhoesRt;

/**
 *
 * @author Eduardo
 */
public class PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoExterno extends JSimProcess {

    private double _lambda;
    private CaminhaoExterno _caminhao;
    private JSimSimulation _simulation;
    private String _nome;
    private Transtainer _transtainer;
    private DecisaoPosicaoToEstacaoArmazenamentoRt _rotaAtePosicao;
    private RotaSaidaCaminhoesRt _rotaSaidaCaminhoes;
    private File _arquivo;
    private FileWriter _fw;
    private BufferedWriter _bw;

    public PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoExterno(String name, JSimSimulation sim)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException {
        super(name, sim);
        _simulation = sim;
        _nome = name;

        criarArquivo();
    } // constructor

    @Override
    protected void life() {
        try {
            while (true) {
                if (_caminhao == null) {
                    // If we have nothing to do, we sleep.
                    passivate();
                } else {
                    escreverArquivo(" Iniciando Movimentação do Transtainer para caminhao no momento " + myParent.getCurrentTime());
                    if (_transtainer.isIdle()) {
                        _transtainer.activate(myParent.getCurrentTime());
                    }
                    passivate();
                    while (true) {
                        try {
                            if (!liberarCaminhao(_caminhao)) {
                                passivate();
                            } else {
                                escreverArquivo(" Liberando caminhao no momento " + myParent.getCurrentTime());
                                if (_rotaSaidaCaminhoes.isIdle()) {
                                    _rotaSaidaCaminhoes.activate(myParent.getCurrentTime());
                                }
                                if (_rotaAtePosicao.isIdle()) {
                                    _rotaAtePosicao.activate(myParent.getCurrentTime());
                                }
                                break;
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

    public boolean liberarCaminhao(JSimLink caminhao) throws IOException {
        JSimLink jsl = caminhao;
        if (jsl instanceof CaminhaoExterno) {
            CaminhaoExterno novoCaminhao = (CaminhaoExterno) jsl;
            if (novoCaminhao == null) {
                return false;
            }
            if (novoCaminhao.isFinalizado()) {
                novoCaminhao.escreverArquivo("\r\nCarregou " + novoCaminhao.getContainer().getId());
                if (_rotaSaidaCaminhoes.addCaminhoes(novoCaminhao)) {                    
                    _caminhao = null;
                    return true;
                }
            }
        }
        return false;
    }

    public void setCaminhao(CaminhaoExterno _caminhao) {
        this._caminhao = _caminhao;
    }

    public JSimLink getCaminhao() {
        return _caminhao;
    }

    public void setRotaAtePosicao(DecisaoPosicaoToEstacaoArmazenamentoRt _rotaAtePosicao) {
        this._rotaAtePosicao = _rotaAtePosicao;
    }

    public void setRotaSaidaCaminhoes(RotaSaidaCaminhoesRt _rotaSaidaCaminhoes) {
        this._rotaSaidaCaminhoes = _rotaSaidaCaminhoes;
    }

    private void criarArquivo() {
        if (_arquivo == null) {
            try {
                _arquivo = new File("../arquivo" + getName() + ".txt");
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
