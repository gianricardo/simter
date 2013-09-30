/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package EntidadesPorto;

import cz.zcu.fav.kiv.jsim.JSimSimulation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eduardo
 */
public class Berco {
    
    public int numeroPortainers = 0;
    public double horaSaida;
    public double tempoTotalAtendimento;
    public double tempoAtendimentoPortainers;
    public double tempoMovimentacao;
    public double horaInicioMovimentacao;
    public double horaAtracacao;
    public int numeroRegioesBerco;
    public List ListaPortainers = new ArrayList();
    
    
    public JSimSimulation simulation;
    public double mu;
    public double p;
    public int counter;
    public double transTq;
    public FilaNavios queueIn;
    public FilaNavios queueOut;
    
    public int IdBerco;
    
    public File arquivo;
    public FileWriter fw;
    public BufferedWriter bw;
    public DecimalFormat df = new DecimalFormat("#0.##");

}
