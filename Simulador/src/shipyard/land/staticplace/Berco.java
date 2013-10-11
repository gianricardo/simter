/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shipyard.land.staticplace;

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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import negocio.BercoBusiness;
import shipyard.land.move.Portainer;
import shipyard.sea.Navio;
import simulador.queues.FilaNavios;

/**
 *
 * @author Eduardo
 */
public class Berco extends JSimProcess {

    private int _numeroPortainers = 0;
    private double _horaSaida;
    private double _tempoTotalAtendimento;
    private double _tempoAtendimentoPortainers;
    private double _tempoMovimentacao;
    private double _horaInicioMovimentacao;
    private double _horaAtracacao;
    private int _numeroRegioesBerco;
    private List<Portainer> _listaPortainers = new ArrayList<>();
    private List<PosicaoCargaDescargaBerco> _listaPosicoes = new ArrayList<>();
    private EstacaoCaminhoesInternos _estacaoCaminhoes;
    private JSimSimulation _simulation;
    private double _mu;
    private double _p;
    private int _counter;
    private double _transTq;
    private FilaNavios _queueIn;
    private FilaNavios _queueOut;
    private int _idBerco;
    private File _arquivo;
    private FileWriter _fw;
    private BufferedWriter _bw;
    private BercoBusiness _bercoNegocio;
    private Navio _ship;

    public Berco(JSimSimulation simulation, int idBerco, int numeroPortainers, EstacaoCaminhoesInternos estacao)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException {
        super(Integer.toString(idBerco), simulation);
        this._simulation = simulation;
        this._idBerco = idBerco;
        this._numeroPortainers = numeroPortainers;
        this._estacaoCaminhoes = estacao;
        this._bercoNegocio = new BercoBusiness(this);
    }

    public Navio getNextShip() {
        JSimLink jsl = this.getQueueIn().first();
        if (jsl instanceof Navio) {
            Navio novonavio = (Navio) jsl;
            this._ship = novonavio;
            return this._ship;
        } else {
            System.out.println(jsl.getClass());
            return null;
        }
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

    public double getTempoMovimentacao() {
        return _tempoMovimentacao;
    }

    public void setTempoMovimentacao(double tempoMovimentacao) {
        this._tempoMovimentacao = tempoMovimentacao;
    }

    public double getHoraInicioMovimentacao() {
        return _horaInicioMovimentacao;
    }

    public void setHoraInicioMovimentacao(double horaInicioMovimentacao) {
        this._horaInicioMovimentacao = horaInicioMovimentacao;
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

    public double getMu() {
        return _mu;
    }

    public void setMu(double mu) {
        this._mu = mu;
    }

    public double getP() {
        return _p;
    }

    public void setP(double p) {
        this._p = p;
    }

    public int getCounter() {
        return _counter;
    }

    public void setCounter(int counter) {
        this._counter = counter;
    }

    public double getTransTq() {
        return _transTq;
    }

    public void setTransTq(double transTq) {
        this._transTq = transTq;
    }

    public FilaNavios getQueueIn() {
        return _queueIn;
    }

    public void setQueueIn(FilaNavios queueIn) {
        this._queueIn = queueIn;
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

    public EstacaoCaminhoesInternos getEstacaoCaminhoes() {
        return _estacaoCaminhoes;
    }

    public void setEstacaoCaminhoes(EstacaoCaminhoesInternos estacaoCaminhoes) {
        this._estacaoCaminhoes = estacaoCaminhoes;
    }
}
