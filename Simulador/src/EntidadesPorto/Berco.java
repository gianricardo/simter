/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package EntidadesPorto;

import cz.zcu.fav.kiv.jsim.JSimException;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimLink;
import cz.zcu.fav.kiv.jsim.JSimProcess;
import cz.zcu.fav.kiv.jsim.JSimSecurityException;
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

    private FilaContainers queueContainer1, queueContainer2;
    private Portainer portainerBerco1, portainerBerco2;
    private Portainer portainerBercoAdicional1;
    private int numeroPortainers = 0;
    private double horaSaida;
    private double tempoTotalAtendimento;
    private double tempoAtendimentoPortainers;
    private double tempoMovimentacao;
    private double horaInicioMovimentacao;
    private double horaAtracacao;
    private int numeroRegioesBerco;
    
    private JSimSimulation simulation;
    private double mu;
    private double p;
    private int counter;
    private double transTq;
    private FilaNavios queueIn;
    private FilaNavios queueOut;
    
    public int IdBerco;
    
    private File arquivo;
    private FileWriter fw;
    private BufferedWriter bw;
    private DecimalFormat df = new DecimalFormat("#0.##");

    public Berco(String name, JSimSimulation sim, double parMu, double parP, FilaNavios parQueueIn, FilaNavios parQueueOut, int NumeroRegioesBerco)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException {
        super(name, sim);
        simulation = sim;
        mu = parMu;
        p = parP;
        queueIn = parQueueIn;
        queueOut = parQueueOut;
        numeroRegioesBerco = NumeroRegioesBerco;

        counter = 0;
        transTq = 0.0;
        
        try {
            portainerBerco1 = new Portainer("Portainer 1", simulation, 0, 0);
        } catch (JSimSimulationAlreadyTerminatedException | JSimInvalidParametersException | JSimTooManyProcessesException | IOException ex) {
            Logger.getLogger(Berco.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            portainerBerco2 = new Portainer("Portainer 2", simulation, 0, 0);
        } catch (JSimSimulationAlreadyTerminatedException | JSimInvalidParametersException | JSimTooManyProcessesException | IOException ex) {
            Logger.getLogger(Berco.class.getName()).log(Level.SEVERE, null, ex);
        }

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
                        queueContainer1 = new FilaContainers("Fila de Containers 1 navio", simulation, null);
                    } catch (JSimInvalidParametersException | JSimTooManyHeadsException | IOException ex) {
                        Logger.getLogger(Berco.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    portainerBerco1.setFilas(queueContainer1, null);
                    
                    queueContainer1.setPortainer(portainerBerco1);
                    
                    try {
                        queueContainer2 = new FilaContainers("Fila de Containers 2 navio", simulation, null);
                    } catch (JSimInvalidParametersException | JSimTooManyHeadsException | IOException ex) {
                        Logger.getLogger(Berco.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    portainerBerco2.setFilas(queueContainer2, null);
                    
                    queueContainer2.setPortainer(portainerBerco2);

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
                        if(n.NumeroRegioesNavio > 1){
                            if(i<=(n.NumeroContainersDescarregar/numeroRegioesBerco)){
                                container.into(queueContainer1);
                            }
                            else{
                                container.into(queueContainer2);
                            }
                        }
                        else{
                            container.into(queueContainer1);
                        }

                        if (queueContainer1.getPortainer().isIdle()) {
                            queueContainer1.getPortainer().activate(myParent.getCurrentTime());
                        }
                        
                        if (queueContainer2.getPortainer().isIdle()) {
                            queueContainer2.getPortainer().activate(myParent.getCurrentTime());
                        }
                    }

                    while (true) {
                        tempoAtendimentoPortainers = 0;
                        
                        double horaFimAtendPort1 = queueContainer1.getHoraFinalAtendimento();
                        double horaFimAtendPort2 = queueContainer2.getHoraFinalAtendimento();

                        if (queueContainer1.getHoraFinalAtendimento() == 0 || queueContainer2.getHoraFinalAtendimento() == 0 ) {
                            // If we have nothing to do, we sleep.                            
                            passivate();
                        } else {                            
                            tempoAtendimentoPortainers = Math.max(queueContainer1.getHoraFinalAtendimento(), queueContainer2.getHoraFinalAtendimento()) -
                                                        Math.min(queueContainer1.getHoraInicioAtendimento(), queueContainer2.getHoraInicioAtendimento());
                            break;
                        }
                    }
                    
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
                                    + "\r\n -Hora Inicio Movimentacao até o Berco " + df.format(horaInicioMovimentacao)
                                    + "\r\n -Hora de Atracação " + df.format(horaAtracacao)
                                    + "\r\n -Tempo de Movimentacao da Fila até o Berço " + df.format(tempoMovimentacao)
                                    + "\r\n -Hora de Saída do Porto " + df.format(horaSaida)
                                    + "\r\n -Tempo de Atendimento pelo Portainer " + tempoAtendimentoPortainers + " \r\n");
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
