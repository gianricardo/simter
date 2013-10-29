/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shipyard.land.staticplace;

import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimProcess;
import cz.zcu.fav.kiv.jsim.JSimSecurityException;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimSimulationAlreadyTerminatedException;
import cz.zcu.fav.kiv.jsim.JSimTooManyProcessesException;
import estatisticas.EstatisticasPorto;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import negocio.BercoBusiness;
import shipyard.land.move.Portainer;
import shipyard.sea.Navio;
import simulador.queues.FilaNavios;
import simulador.rotas.BercoToRotaSaidaRt;
import simulador.rotas.PraticoToBercoRt;

/**
 *
 * @author Eduardo
 */
public class Berco extends JSimProcess {
    
    private int _numeroPortainers = 0;
    private double _horaSaida;
    private double _tempoTotalAtendimento;
    private double _tempoAtendimentoPortainers;
    private double _horaAtracacao;
    private int _numeroRegioesBerco;
    private List<Portainer> _listaPortainers = new ArrayList<>();
    private List<PosicaoCargaDescargaBerco> _listaPosicoes = new ArrayList<>();
    private JSimSimulation _simulation;
    private FilaNavios _queueOut;
    private int _idBerco;    
    private BercoBusiness _bercoNegocio;
    private Navio _ship;
    private boolean _ocupado = false;
    private PraticoToBercoRt _rotaPraticoToBerco;
    private BercoToRotaSaidaRt _rotaBercoToSaida;
    
    private DecisaoCaminhaoPatioPosicaoEstacao _decisaoCaminhoesPatioEstacao;
    private DecisaoCaminhaoPatioPosicaoBerco _decisaoCaminhoesPatioBerco;
    
    private EstatisticasPorto _estatisticas;
    
    private File _arquivo;
    private FileWriter _fw;
    private BufferedWriter _bw;    
    
    public Berco(JSimSimulation simulation, int idBerco, DecisaoCaminhaoPatioPosicaoEstacao decisaoCaminhoesPatio, 
            DecisaoCaminhaoPatioPosicaoBerco decisaoCaminhoesPatioPosicaoBerco, EstatisticasPorto estatisticas)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException {
        super(Integer.toString(idBerco), simulation);
        this._simulation = simulation;
        this._idBerco = idBerco;
        this._decisaoCaminhoesPatioEstacao = decisaoCaminhoesPatio;
        this._decisaoCaminhoesPatioBerco = decisaoCaminhoesPatioPosicaoBerco;
        this._estatisticas = estatisticas;
        this._bercoNegocio = new BercoBusiness(this);        
    }    
    
    public int getNumeroPortainers() {
        return _numeroPortainers;
    }
    
    public void setNumeroPortainers(int numeroPortainers) {
        this._numeroPortainers = numeroPortainers;
    }
    
    public double getHoraSaida() {
        return _horaSaida;
    }
    
    public void setHoraSaida(double horaSaida) {
        this._horaSaida = horaSaida;
    }
    
    public double getTempoTotalAtendimento() {
        return _tempoTotalAtendimento;
    }
    
    public void setTempoTotalAtendimento(double tempoTotalAtendimento) {
        this._tempoTotalAtendimento = tempoTotalAtendimento;
    }
    
    public double getTempoAtendimentoPortainers() {
        return _tempoAtendimentoPortainers;
    }
    
    public void setTempoAtendimentoPortainers(double tempoAtendimentoPortainers) {
        this._tempoAtendimentoPortainers = tempoAtendimentoPortainers;
    }
    
    public double getHoraAtracacao() {
        return _horaAtracacao;
    }
    
    public void setHoraAtracacao(double horaAtracacao) {
        this._horaAtracacao = horaAtracacao;
    }
    
    public int getNumeroRegioesBerco() {
        return _numeroRegioesBerco;
    }
    
    public void setNumeroRegioesBerco(int numeroRegioesBerco) {
        this._numeroRegioesBerco = numeroRegioesBerco;
    }
    
    public List<Portainer> getListaPortainers() {
        return _listaPortainers;
    }
    
    public void setListaPortainers(List<Portainer> listaPortainers) {
        this._listaPortainers = listaPortainers;
    }
    
    public JSimSimulation getSimulation() {
        return _simulation;
    }    
    
    public FilaNavios getQueueOut() {
        return _queueOut;
    }
    
    public void setQueueOut(FilaNavios queueOut) {
        this._queueOut = queueOut;
    }
    
    public int getIdBerco() {
        return _idBerco;
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
    
    @Override
    protected void life() {
        this._bercoNegocio.life();
    }
    
    public Navio getShip() {
        return this._ship;
    }
    
    public void passivo() {
        try {
            this.passivate();
        } catch (JSimSecurityException ex) {
            ex.printStackTrace(System.err);
        }
    }
    
    public List<PosicaoCargaDescargaBerco> getListaPosicoes() {
        return _listaPosicoes;
    }    
    
    public void setShip(Navio _ship) {
        this._ship = _ship;
    }

    public boolean isOcupado() {
        return _ocupado;
    }

    public void setOcupado(boolean _ocupado) {
        this._ocupado = _ocupado;
    }

    public PraticoToBercoRt getRotaPraticoToBerco() {
        return _rotaPraticoToBerco;
    }

    public void setRotaPraticoToBerco(PraticoToBercoRt rotaPraticoToBerco) {
        this._rotaPraticoToBerco = rotaPraticoToBerco;
    }

    public BercoToRotaSaidaRt getRotaBercoToSaida() {
        return _rotaBercoToSaida;
    }

    public void setRotaBercoToSaida(BercoToRotaSaidaRt _rotaBercoToSaida) {
        this._rotaBercoToSaida = _rotaBercoToSaida;
    }

    public DecisaoCaminhaoPatioPosicaoEstacao getDecisaoCaminhoesPatioEstacao() {
        return _decisaoCaminhoesPatioEstacao;
    }

    public DecisaoCaminhaoPatioPosicaoBerco getDecisaoCaminhoesPatioBerco() {
        return _decisaoCaminhoesPatioBerco;
    }

    public EstatisticasPorto getEstatisticas() {
        return _estatisticas;
    }
}
