/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package EntidadesPorto;

import cz.zcu.fav.kiv.jsim.JSimException;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimLink;
import cz.zcu.fav.kiv.jsim.JSimProcess;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimSimulationAlreadyTerminatedException;
import cz.zcu.fav.kiv.jsim.JSimSystem;
import cz.zcu.fav.kiv.jsim.JSimTooManyHeadsException;
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

/**
 *
 * @author Eduardo
 */
public class Berco extends JSimProcess {

    private FilaContainers queueContainer1;
    private JSimSimulation simulation;
    private Portainer portainerBerco;
    
    
    public double teste;
    public int IdBerco;
    public Navio IdNavioAtracado;
    private double mu;
    private double p;
    private FilaNavios queueIn;
    private FilaNavios queueOut;
    private int counter;
    private double transTq;
    private double horaSaida;
    private double tempoTotalAtendimento;
    private double tempoAtendimentoPortainers;
    private double tempoMovimentacao;
    private double horaInicioMovimentacao;
    private double horaAtracacao;
    private File arquivo;
    private FileWriter fw;
    private BufferedWriter bw;
    private DecimalFormat df = new DecimalFormat("#0.##");
    private List<FilaContainers> listaPortainer = new ArrayList<>();

    public Berco(String name, JSimSimulation sim, double parMu, double parP, FilaNavios parQueueIn, FilaNavios parQueueOut)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException {
        super(name, sim);
        simulation = sim;
        mu = parMu;
        p = parP;
        queueIn = parQueueIn;
        queueOut = parQueueOut;

        counter = 0;
        transTq = 0.0;

        arquivo = new File("../arquivoNavios" + name + ".txt");

        if (arquivo.delete() == true) {
            arquivo = new File("../arquivoNavios" + name + ".txt");
        }

        if (!arquivo.exists()) {
            arquivo.createNewFile();
        }
        fw = new FileWriter(arquivo, true);
        bw = new BufferedWriter(fw);
    } // constructor

    protected void life() {
        Navio n;
        JSimLink navio;
        JSimLink container;        

        try {
            while (true) {
                if (queueIn.empty()) {
                    // If we have nothing to do, we sleep.
                    passivate();
                } else {
                    
                    
                    try {
                        queueContainer1 = new FilaContainers("Fila de Containers de um navio", simulation, null);
                    } catch (JSimInvalidParametersException ex) {
                        Logger.getLogger(Berco.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (JSimTooManyHeadsException ex) {
                        Logger.getLogger(Berco.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(Berco.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        portainerBerco = new Portainer("Portainer 1", simulation, 0, 0, queueContainer1, null);
                    } catch (JSimSimulationAlreadyTerminatedException ex) {
                        Logger.getLogger(Berco.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (JSimInvalidParametersException ex) {
                        Logger.getLogger(Berco.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (JSimTooManyProcessesException ex) {
                        Logger.getLogger(Berco.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(Berco.class.getName()).log(Level.SEVERE, null, ex);
                    }        
                    queueContainer1.setPortainer(portainerBerco);                   
                    
                    //Simulating hard work here...
                    //Tempo de atendimento no BERÇO = SOMA DE TODOS OS CARREGAMENTOS E DESCARREGAMENTOS DE CONTAINERS
                    navio = queueIn.first();
                    horaInicioMovimentacao = myParent.getCurrentTime();
                    //hold(JSimSystem.uniform(1, 1));                    
                    horaAtracacao = myParent.getCurrentTime();
                    tempoMovimentacao = horaAtracacao - horaInicioMovimentacao;
                    n = (Navio) navio.getData();

                    for (int i = 1; i <= n.NumeroContainersDescarregar; i++) {
                        container = new JSimLink(new Container(myParent.getCurrentTime(), String.valueOf(i)));
                        container.into(queueContainer1);

                        if (queueContainer1.getPortainer().isIdle()) {
                            queueContainer1.getPortainer().activate(myParent.getCurrentTime());
                        }                        
                    }                   
                    
                    while (true) {
                        tempoAtendimentoPortainers = 0;
                        
                        if(queueContainer1.getHoraFinalAtendimento() == 0){
                            // If we have nothing to do, we sleep.
                            passivate();
                        }
                        else
                        {
                            tempoAtendimentoPortainers = queueContainer1.getHoraFinalAtendimento() - queueContainer1.getHoraInicioAtendimento();
                            break;
                        }
                    }
                    
                    //hold(JSimSystem.uniform(tempoAtendimentoPortainers, tempoAtendimentoPortainers));

                    // Now we must decide whether to throw the transaction away or to insert it into another queue.
                    if (JSimSystem.uniform(0.0, 1.0) > p || queueOut == null) {
                        counter++;
                        horaSaida = tempoMovimentacao + tempoAtendimentoPortainers + horaInicioMovimentacao;
                        transTq += horaSaida - n.getCreationTime();
                        
                        try {
                            bw.write("\r\nNavio " + n.idNavio + ":\r\n -Criado no momento " + df.format(n.getCreationTime())
                                    + "\r\n -" + n.NumeroContainersDescarregar + " Containers a descarregar"
                                    + "\r\n -Colocado na fila " + queueIn.getHeadName()
                                    + " no momento " + df.format(navio.getEnterTime())
                                    + "\r\n -Tempo de espera na fila " + df.format((horaAtracacao - tempoMovimentacao) - navio.getEnterTime())
                                    + "\r\n -Hora Inicio Movimentacao ao Berco " + df.format(horaInicioMovimentacao)
                                    + "\r\n -Hora de Atracação " + df.format(horaAtracacao)
                                    + "\r\n -Tempo de Movimentacao " + df.format(tempoMovimentacao)
                                    + "\r\n -Hora de Saída " + df.format(horaSaida)
                                    + "\r\n -Tempo de Atendimento " + tempoAtendimentoPortainers + " \r\n");
                        } catch (IOException ex) {
                            Logger.getLogger(GeradorNavios.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        navio.out();
                        navio = null;
                    } else {
                        navio.out();
                        navio.into(queueOut);
                        if (queueOut.getBerco().isIdle()) {
                            queueOut.getBerco().activate(myParent.getCurrentTime());
                        }
                    } // else throw away / insert
                } // else queue is empty / not empty
            } // while                
        } // try
        catch (JSimException e) {
            e.printStackTrace();
            e.printComment(System.err);
        } 
        
    } // life

    public int getCounter() {
        return counter;
    } // getCounter

    public double getTransTq() {
        return transTq;
    } // getTransTq

    public void closeBw() throws IOException {
        bw.close();
    }

    public double verificarHoraSaidaNavio(List<FilaContainers> lista) {
        double tempoPortainers = 0;
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getHoraFinalAtendimento() > tempoPortainers) {
                tempoPortainers = lista.get(i).getHoraFinalAtendimento();
            }
        }
        return tempoPortainers;
    }
}
