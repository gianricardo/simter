/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shipyard.land.move;

import Enumerators.CaminhaoExternoOperacao;
import cz.zcu.fav.kiv.jsim.JSimLink;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import shipyard.load.Container;

/**
 *
 * @author Eduardo
 */
public class CaminhaoExterno extends JSimLink{    
    private CaminhaoExternoOperacao _operacao;
    private double _timeOfCreation;
    private String _idCaminhao;
    private Container _container;
    private boolean _carregado;
    public boolean _finalizado;
    private double _horaRecebimentoContainer;
    
    private File _arquivo;
    private FileWriter _fw;
    private BufferedWriter _bw;
    
     public CaminhaoExterno(double time, String id, JSimSimulation simulation)
    {        
            _timeOfCreation = time;
            _idCaminhao = new StringBuilder()
                    .append(id)
                    .toString();
            
            criarArquivo();
            escreverArquivo("Caminhão "+id+" criado no momento " + time);
    } // constructor

    public Container getContainer() {
        return _container;
    }

    public void setContainer(Container _container) {
        this._container = _container;
    }

    public boolean isCarregado() {
        return _carregado;
    }

    public void setCarregado(boolean _carregado) {
        this._carregado = _carregado;
    }

    public CaminhaoExternoOperacao getOperacao() {
        return _operacao;
    }

    public void setOperacao(CaminhaoExternoOperacao _operacao) {
        this._operacao = _operacao;
    }

    public boolean isFinalizado() {
        return _finalizado;
    }

    public void setFinalizado(boolean _finalizado) {
        this._finalizado = _finalizado;
    }
    
    private void criarArquivo() {
        if (_arquivo == null) {
            try {
                _arquivo = new File("../arquivoCaminhaoExterno" + _idCaminhao + ".txt");
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

    public void closeBw() throws IOException {
        _bw.close();
    }

    public String getIdCaminhao() {
        return _idCaminhao;
    }
}
