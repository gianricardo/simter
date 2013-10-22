/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shipyard.sea;

import Enumerators.ContainerTipos;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimLink;
import cz.zcu.fav.kiv.jsim.JSimSecurityException;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimTooManyHeadsException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import shipyard.load.Container;
import simulador.queues.FilaContainers;

/**
 *
 * @author Eduardo
 */
public class Navio extends JSimLink {

    private JSimSimulation _simulation;
    private String _idNavio;
    private double _timeOfCreation;
    private Date _horaChegadaPorto;
    private Date _horaAtracacao;
    private Date _horaSaidaBerco;
    private Date _horaSaidaPorto;
    private float _tempoAtendimento;
    private int _numeroRegioesNavio;
    private int _numeroContainersDescarregar = 0;
    private int _numeroContainersCarregar = 0;
    private FilaContainers _filaContainer;
    private JSimLink _container;
    private List _filasContainers = new ArrayList();
    private List _idBercosPossiveis = new ArrayList();
    private int _idPratico;
    private int _comprimentoEmPosicoesCargaDescarga;
    
    private File _arquivo;
    private FileWriter _fw;
    private BufferedWriter _bw;

    public void setNumeroContainersDescarregar(int _numeroContainersDescarregar) {
        this._numeroContainersDescarregar = _numeroContainersDescarregar;
        this.updateShip();
    }

    public void setNumeroContainersCarregar(int _numeroContainersCarregar) {
        this._numeroContainersCarregar = _numeroContainersCarregar;
    }

    public Navio(double time, String id, int numeroRegioesNavio, JSimSimulation simulation) {
        _timeOfCreation = time;
        _idNavio = id + " ";
        //_numeroContainersDescarregar = (int) JSimSystem.uniform(10, 10);
        _simulation = simulation;
        _numeroRegioesNavio = numeroRegioesNavio;
        _comprimentoEmPosicoesCargaDescarga = 1;
        
        criarArquivo();
        escreverArquivo("Navio " + _idNavio + "\r\n Criado no momento " + _timeOfCreation);        
    } // constructor

    private void updateShip() {
        int aux = 1;
        int j;
        int contadorContainers;
        
        int numeroContainersCarregarPorFila;
        
        numeroContainersCarregarPorFila = _numeroContainersCarregar/_numeroRegioesNavio;

        try {
            for (int i = 1; i <= _numeroRegioesNavio; i++) {
                contadorContainers = 0;
                try {
                    _filaContainer = new FilaContainers("Fila de Containers " + i + " navio " + _idNavio, _simulation);
                } catch (JSimInvalidParametersException | JSimTooManyHeadsException | IOException ex) {
                    Logger.getLogger(shipyard.land.staticplace.Berco.class.getName()).log(Level.SEVERE, null, ex);
                }

                for (j = aux; j <= i * (_numeroContainersDescarregar / _numeroRegioesNavio); j++) {
                    _container = new Container(_timeOfCreation, "Container " + String.valueOf(j) + " do navio " + this._idNavio, ContainerTipos.Navio, ContainerTipos.CaminhaoExterno);
                    _container.into(_filaContainer);
                    contadorContainers++;
                }

                if (i == _numeroRegioesNavio) {
                    int auxiliarResto = _numeroContainersDescarregar - j;
                    for (int z = j; z <= j + auxiliarResto; z++) {
                        _container = new JSimLink(new Container(_timeOfCreation, "Container " + String.valueOf(z) + " do navio " + this._idNavio, ContainerTipos.Navio, ContainerTipos.CaminhaoExterno));
                        _container.into(_filaContainer);
                        contadorContainers++;
                    }
                    
                    numeroContainersCarregarPorFila = numeroContainersCarregarPorFila + (_numeroContainersCarregar%_numeroRegioesNavio);
                }
                aux = j;
                _filaContainer.setNumeroContainers(contadorContainers);
                _filaContainer.setNumeroContainersCarregar(numeroContainersCarregarPorFila);
                _filasContainers.add(_filaContainer);
            }            
        } catch (JSimSecurityException jse) {
            jse.printStackTrace(System.err);
        }
    }

    public double getCreationTime() {
        return _timeOfCreation;
    } // getCreationTime

    public JSimSimulation getSimulation() {
        return _simulation;
    }

    public String getIdNavio() {
        return _idNavio;
    }

    public float getTempoAtendimento() {
        return _tempoAtendimento;
    }

    public double getTimeOfCreation() {
        return _timeOfCreation;
    }

    public int getNumeroContainersDescarregar() {
        return _numeroContainersDescarregar;
    }

    public Date getHoraChegadaPorto() {
        return _horaChegadaPorto;
    }

    public Date getHoraSaidaPorto() {
        return _horaSaidaPorto;
    }

    public Date getHoraAtracacao() {
        return _horaAtracacao;
    }

    public Date getHoraSaidaBerco() {
        return _horaSaidaBerco;
    }

    public int getNumeroRegioesNavio() {
        return _numeroRegioesNavio;
    }

    public FilaContainers getFilaContainer() {
        return _filaContainer;
    }

    public JSimLink getContainer() {
        return _container;
    }

    public List getFilasContainers() {
        return _filasContainers;
    }

    public List getIdBercosPossiveis() {
        return _idBercosPossiveis;
    }

    public int getIdPratico() {
        return _idPratico;
    }

    public int getComprimentoEmPosicoesCargaDescarga() {
        return _comprimentoEmPosicoesCargaDescarga;
    }
    
    private void criarArquivo() {
        if (_arquivo == null) {
            try {
                _arquivo = new File("../arquivoNavio" + _idNavio + ".txt");
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
}
