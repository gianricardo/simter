/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shipyard.land.staticplace;

import Enumerators.ContainerTipos;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import shipyard.load.Container;
import simulador.queues.FilaContainers;

/**
 *
 * @author Eduardo
 */
public class EstacaoArmazenamento extends JSimProcess {

    private JSimSimulation _simulation;
    private int _idEstacaoArmazenamento;
    private int _quantidadeCargaMomento;
    private int _capacidadeTotalCarga;
    
    private FilaContainers _filaContainersParaCaminhoesExternos;
    private FilaContainers _filaContainersParaNavio;
    private Container _container;
    
    private File _arquivo;
    private FileWriter _fw;
    private BufferedWriter _bw;

    public EstacaoArmazenamento(JSimSimulation simulation, String idEstacaoArmazenamento, int numeroContainersInicial)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException {
        super(idEstacaoArmazenamento, simulation);
        
        try {
            _filaContainersParaCaminhoesExternos = new FilaContainers("Fila de Containers da Estação para Saída via Caminhões Externos", simulation);
            _filaContainersParaNavio = new FilaContainers("Fila de Containers da Estação para Saída via Navios", simulation);
        } catch (JSimTooManyHeadsException | IOException ex) {
            Logger.getLogger(EstacaoArmazenamento.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (int i = 0; i < numeroContainersInicial; i++) {
            try {
                _container = new Container(myParent.getCurrentTime(), "Container " + i + " da Estação de Armazenamento " , ContainerTipos.EstacaoArmazenamento, ContainerTipos.CaminhaoExterno);
                _container.into(_filaContainersParaCaminhoesExternos);
            } catch (JSimSecurityException ex) {
                Logger.getLogger(EstacaoArmazenamento.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public File getArquivo() {
        return _arquivo;
    }
    
    public void setArquivo(File arquivo) {
        try {
            this._arquivo = arquivo;
            this.setFw(new FileWriter(this._arquivo, false));
            this.setBw(new BufferedWriter(this.getFw()));
        } catch (IOException ex) {
            Logger.getLogger(Berco.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public FileWriter getFw() {
        return _fw;
    }
    
    private void setFw(FileWriter fw) {
        this._fw = fw;
    }
    
    public BufferedWriter getBw() {
        return _bw;
    }
    
    private void setBw(BufferedWriter bw) {
        this._bw = bw;
    }
    
    public JSimSimulation getSimulation() {
        return _simulation;
    }   

    public void incrementQuantidadeCargaMomento() {
        this._quantidadeCargaMomento++;
    }
    
    public void decrementQuantidadeCargaMomento() {
        this._quantidadeCargaMomento--;
    }

    public FilaContainers getFilaContainersParaCaminhoesExternos() {
        return _filaContainersParaCaminhoesExternos;
    }

    public FilaContainers getFilaContainersParaNavio() {
        return _filaContainersParaNavio;
    }
}
