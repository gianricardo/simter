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
    private double horaSaida;
    private double tempoTotalAtendimento;
    private double tempoAtendimentoPortainers;
    private double tempoMovimentacao;
    private double horaInicioMovimentacao;
    private double horaAtracacao;
    private int numeroRegioesBerco;
    private List<Portainer> _listaPortainers = new ArrayList<>();
    private List<PosicaoCargaDescargaBerco> _listaPosicoes = new ArrayList<>();
    private EstacaoCaminhoesInternos _estacaoCaminhoes;
    private JSimSimulation simulation;
    private double mu;
    private double p;
    private int counter;
    private double transTq;
    private FilaNavios queueIn;
    private FilaNavios queueOut;
    private int _idBerco;
    private File arquivo;
    private FileWriter fw;
    private BufferedWriter bw;
    private BercoBusiness _bercoNegocio;
    private Navio _ship;

    public Berco(JSimSimulation simulation, int idBerco, int numeroPortainers, EstacaoCaminhoesInternos estacao)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException {
        super(Integer.toString(idBerco), simulation);
        this.simulation = simulation;
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
        return horaSaida;
    }

    public void setHoraSaida(double horaSaida) {
        this.horaSaida = horaSaida;
    }

    public double getTempoTotalAtendimento() {
        return tempoTotalAtendimento;
    }

    public void setTempoTotalAtendimento(double tempoTotalAtendimento) {
        this.tempoTotalAtendimento = tempoTotalAtendimento;
    }

    public double getTempoAtendimentoPortainers() {
        return tempoAtendimentoPortainers;
    }

    public void setTempoAtendimentoPortainers(double tempoAtendimentoPortainers) {
        this.tempoAtendimentoPortainers = tempoAtendimentoPortainers;
    }

    public double getTempoMovimentacao() {
        return tempoMovimentacao;
    }

    public void setTempoMovimentacao(double tempoMovimentacao) {
        this.tempoMovimentacao = tempoMovimentacao;
    }

    public double getHoraInicioMovimentacao() {
        return horaInicioMovimentacao;
    }

    public void setHoraInicioMovimentacao(double horaInicioMovimentacao) {
        this.horaInicioMovimentacao = horaInicioMovimentacao;
    }

    public double getHoraAtracacao() {
        return horaAtracacao;
    }

    public void setHoraAtracacao(double horaAtracacao) {
        this.horaAtracacao = horaAtracacao;
    }

    public int getNumeroRegioesBerco() {
        return numeroRegioesBerco;
    }

    public void setNumeroRegioesBerco(int numeroRegioesBerco) {
        this.numeroRegioesBerco = numeroRegioesBerco;
    }

    public List<Portainer> getListaPortainers() {
        return _listaPortainers;
    }

    public void setListaPortainers(List<Portainer> listaPortainers) {
        this._listaPortainers = listaPortainers;
    }

    public JSimSimulation getSimulation() {
        return simulation;
    }

    public double getMu() {
        return mu;
    }

    public void setMu(double mu) {
        this.mu = mu;
    }

    public double getP() {
        return p;
    }

    public void setP(double p) {
        this.p = p;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public double getTransTq() {
        return transTq;
    }

    public void setTransTq(double transTq) {
        this.transTq = transTq;
    }

    public FilaNavios getQueueIn() {
        return queueIn;
    }

    public void setQueueIn(FilaNavios queueIn) {
        this.queueIn = queueIn;
    }

    public FilaNavios getQueueOut() {
        return queueOut;
    }

    public void setQueueOut(FilaNavios queueOut) {
        this.queueOut = queueOut;
    }

    public int getIdBerco() {
        return _idBerco;
    }

    public File getArquivo() {
        return arquivo;
    }

    public void setArquivo(File arquivo) {
        try {
            this.arquivo = arquivo;
            this.setFw(new FileWriter(this.arquivo, false));
            this.setBw(new BufferedWriter(this.getFw()));
        } catch (IOException ex) {
            Logger.getLogger(Berco.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public FileWriter getFw() {
        return fw;
    }

    private void setFw(FileWriter fw) {
        this.fw = fw;
    }

    public BufferedWriter getBw() {
        return bw;
    }

    private void setBw(BufferedWriter bw) {
        this.bw = bw;
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
