/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shipyard.land.move;

import Enumerators.CaminhaoOperacao;
import cz.zcu.fav.kiv.jsim.JSimLink;
import cz.zcu.fav.kiv.jsim.JSimSecurityException;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import shipyard.load.Container;
import simulador.rotas.DecisaoPosicaoToPosicaoBercoRt;

/**
 *
 * @author Eduardo
 */
public class CaminhaoPatio extends JSimLink{   
    private int _capacidade;
    private Container _container;
    private double _timeOfCreation;
    private String _idCaminhao;
    private boolean _carregado;
    private double _horaRecebimentoContainer;
    
    private DecisaoPosicaoToPosicaoBercoRt _rotaPosicaoBercoAposDecisao;
    private CaminhaoOperacao _operacao;        
    
    private boolean _movimentacaoFinalizada;
    
    private File _arquivo;
    private FileWriter _fw;
    private BufferedWriter _bw;
    
     public CaminhaoPatio(double time, String id, JSimSimulation simulation, int capacidade) throws JSimSecurityException
    {
            _timeOfCreation = time;
            _idCaminhao = id;           
            _capacidade = capacidade;
            
            criarArquivo();
            escreverArquivo("Caminhão "+id+"\r\nCriado no momento " + time);
    } // constructor

    public int getCapacidade() {
        return _capacidade;
    }   

    public Container getContainer() {
        return _container;
    }

    public double getTimeOfCreation() {
        return _timeOfCreation;
    }

    public String getIdCaminhao() {
        return _idCaminhao;
    }

    public boolean isCarregado() {
        return _carregado;
    }

    public double getHoraRecebimentoContainer() {
        return _horaRecebimentoContainer;
    }     

    public void setContainer(Container _container) {
        this._container = _container;
    }

    public void setCarregado(boolean _carregado) {
        this._carregado = _carregado;
    }

    public void setHoraRecebimentoContainer(double _horaRecebimentoContainer) {
        this._horaRecebimentoContainer = _horaRecebimentoContainer;
    }
    
    private void criarArquivo() {
        if (_arquivo == null) {
            try {
                _arquivo = new File("../arquivoCaminhaoPatio" + _idCaminhao + ".txt");
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

    public boolean isMovimentacaoFinalizada() {
        return _movimentacaoFinalizada;
    }

    public void setMovimentacaoFinalizada(boolean _movimentacaoFinalizada) {
        this._movimentacaoFinalizada = _movimentacaoFinalizada;
    }

    public DecisaoPosicaoToPosicaoBercoRt getRotaPosicaoBercoAposDecisao() {
        return _rotaPosicaoBercoAposDecisao;
    }

    public void setRotaPosicaoBercoAposDecisao(DecisaoPosicaoToPosicaoBercoRt rotaPosicaoBercoAposDecisao) {
        this._rotaPosicaoBercoAposDecisao = rotaPosicaoBercoAposDecisao;
    }

    public CaminhaoOperacao getOperacao() {
        return _operacao;
    }

    public void setOperacao(CaminhaoOperacao _operacao) {
        this._operacao = _operacao;
    }
}
