/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shipyard.land.move;

import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimLink;
import cz.zcu.fav.kiv.jsim.JSimProcess;
import cz.zcu.fav.kiv.jsim.JSimSecurityException;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimSimulationAlreadyTerminatedException;
import cz.zcu.fav.kiv.jsim.JSimTooManyProcessesException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import negocio.PortainerBusiness;
import shipyard.land.staticplace.DecisaoCaminhaoPatioPosicaoEstacao;
import shipyard.land.staticplace.PosicaoCargaDescargaBerco;
import shipyard.load.Container;
import simulador.queues.FilaContainers;

/**
 *
 * @author Eduardo
 */
public class Portainer extends JSimProcess {

    public List<FilaContainers> _filasContainers = new ArrayList();
    PosicaoCargaDescargaBerco _posicaoCargaDescarga;
    private double mu;
    private double p;
    
    private int _counter;
    private double _transTq;
    private double _horaSaidaContainer;
    private double _tempoTotalAtendimento;
    private double _horaMovimentacao;    
    
    private File _arquivo;
    private FileWriter _fw;
    private BufferedWriter _bw;
    private DecimalFormat df = new DecimalFormat("#0.##");
    
    private FilaContainers _queueIn;
    private FilaContainers _queueOut;
    private String _nomePortainer;
    private JSimProcess _berco;
    private JSimSimulation _simulation;
    private int _numeroContainersDescarregar;
    private int _numeroContainersCarregar;
    private boolean _descarregar;
    private boolean _carregar;
    private Container _container;
    private PortainerBusiness _portainerNegocio;
    
    private DecisaoCaminhaoPatioPosicaoEstacao _decisaoSolicitacoes;

    //CaminhoesPatio
    //BercosAtende
    //IdentificadoresNavios
    public Portainer(String name, JSimSimulation sim, double parMu, double parP, JSimProcess berco, PosicaoCargaDescargaBerco posicao)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException {
        super(name, sim);
        this.mu = parMu;
        this.p = parP;
        this._nomePortainer = name;
        this._berco = berco;
        this._posicaoCargaDescarga = posicao;
        this._simulation = sim;
        _counter = 0;
        _transTq = 0.0;
        
        this._portainerNegocio = new PortainerBusiness(this);
    } // constructor    

    public List<FilaContainers> getFilasContainers() {
        return _filasContainers;
    }

    public PosicaoCargaDescargaBerco getPosicaoCargaDescarga() {
        return _posicaoCargaDescarga;
    }

    public double getMu() {
        return mu;
    }

    public double getP() {
        return p;
    }

    public FilaContainers getQueueIn() {
        return _queueIn;
    }

    public void setQueueIn(FilaContainers _queueIn) {
        this._queueIn = _queueIn;
    }    

    public FilaContainers getQueueOut() {
        return _queueOut;
    }

    public int getCounter() {
        return _counter;
    }

    public void setCounter(int _counter) {
        this._counter = _counter;
    }

    public double getTransTq() {
        return _transTq;
    }

    public void setTransTq(double _transTq) {
        this._transTq = _transTq;
    }    

    public double getHoraSaidaContainer() {
        return _horaSaidaContainer;
    }

    public void setHoraSaidaContainer(double _horaSaidaContainer) {
        this._horaSaidaContainer = _horaSaidaContainer;
    }    

    public double getTempoTotalAtendimento() {
        return _tempoTotalAtendimento;
    }

    public void setTempoTotalAtendimento(double _tempoTotalAtendimento) {
        this._tempoTotalAtendimento = _tempoTotalAtendimento;
    }    

    public double getHoraMovimentacao() {
        return _horaMovimentacao;
    }

    public void setHoraMovimentacao(double _horaMovimentacao) {
        this._horaMovimentacao = _horaMovimentacao;
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
            Logger.getLogger(Portainer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public FileWriter getFw() {
        return _fw;
    }

    public void setFw(FileWriter _fw) {
        this._fw = _fw;
    }

    public BufferedWriter getBw() {
        return _bw;
    }

    public void setBw(BufferedWriter _bw) {
        this._bw = _bw;
    }

    public DecimalFormat getDf() {
        return df;
    }

    public String getNomePortainer() {
        return _nomePortainer;
    }

    public JSimProcess getBerco() {
        return _berco;
    }
    
    public void passivo() {
        try {
            this.passivate();
        } catch (JSimSecurityException ex) {
            ex.printStackTrace(System.err);
        }
    }
    
    public void segurar(double tempo) throws JSimInvalidParametersException {
        try {
            this.hold(tempo);
        } catch (JSimSecurityException ex) {
            ex.printStackTrace(System.err);
        }
    }
    
    public Container getNextContainer()
    {
        JSimLink jsl = this.getQueueIn().first();
        if (jsl instanceof Container) {
            Container novoContainer = (Container) jsl;
            this._container = novoContainer;
            return this._container;
        } else {
            System.out.println(jsl.getClass());
            return null;
        }
    }

    public JSimSimulation getSimulation() {
        return _simulation;
    }

    public Container getContainer() {
        return _container;
    }

    public void setContainer(Container _container) {
        this._container = _container;
    }
    
    public void setFilas(FilaContainers parQueueIn, FilaContainers parQueueOut) {
        _queueIn = parQueueIn;
        _queueIn.setHoraInicioAtendimento(myParent.getCurrentTime());
        _filasContainers.add(_queueIn);
        _queueOut = parQueueOut;
    }

    public int getNumeroContainersDescarregar() {
        return _numeroContainersDescarregar;
    }

    public void setNumeroContainersDescarregar(int _numeroContainersDescarregar) {
        this._numeroContainersDescarregar = _numeroContainersDescarregar;
    }

    public int getNumeroContainersCarregar() {
        return _numeroContainersCarregar;
    }

    public void setNumeroContainersCarregar(int _numeroContainersCarregar) {
        this._numeroContainersCarregar = _numeroContainersCarregar;
    }
    
    @Override
    protected void life() {
        try {
            this._portainerNegocio.life();
        } catch (JSimSecurityException | JSimInvalidParametersException ex) {
            Logger.getLogger(Portainer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isDescarregar() {
        return _descarregar;
    }

    public void setDescarregar(boolean _descarregar) {
        this._descarregar = _descarregar;
    }

    public boolean isCarregar() {
        return _carregar;
    }

    public void setCarregar(boolean _carregar) {
        this._carregar = _carregar;
    }

    
    
    public DecisaoCaminhaoPatioPosicaoEstacao getDecisaoSolicitacoes() {
        return _decisaoSolicitacoes;
    }

    public void setDecisaoSolicitacoes(DecisaoCaminhaoPatioPosicaoEstacao _decisaoSolicitacoes) {
        this._decisaoSolicitacoes = _decisaoSolicitacoes;
    }    
}
