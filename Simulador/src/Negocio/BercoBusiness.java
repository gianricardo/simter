/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Negocio;

import EntidadesPorto.Berco;
import EntidadesPorto.Container;
import EntidadesPorto.FilaContainers;
import EntidadesPorto.FilaNavios;
import EntidadesPorto.GeradorNavios;
import EntidadesPorto.Navio;
import EntidadesPorto.Portainer;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eduardo
 */
public class BercoBusiness extends JSimProcess {

    Berco Berco = new Berco();
    FilaContainers filaBerco;

    public BercoBusiness(String name, JSimSimulation sim, double parMu, double parP, FilaNavios parQueueIn, FilaNavios parQueueOut, int NumeroRegioesBerco)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException {
        super(name, sim);

        Berco.simulation = sim;
        Berco.mu = parMu;
        Berco.p = parP;
        Berco.queueIn = parQueueIn;
        Berco.queueOut = parQueueOut;
        Berco.numeroRegioesBerco = NumeroRegioesBerco;

        Berco.counter = 0;
        Berco.transTq = 0.0;

        Berco.arquivo = new File("../arquivoNavios" + name + ".txt");

        if (Berco.arquivo.delete() == true) {
            Berco.arquivo = new File("../arquivoNavios" + name + ".txt");
        }

        if (!Berco.arquivo.exists()) {
            Berco.arquivo.createNewFile();
        }
        Berco.fw = new FileWriter(Berco.arquivo, true);
        Berco.bw = new BufferedWriter(Berco.fw);
    } // constructor

    protected void life() {
        Navio n;
        JSimLink navio;

        try {
            while (true) {
                if (Berco.queueIn.empty()) {
                    // If we have nothing to do, we sleep.
                    passivate();
                } else {

                    //Simulating hard work here...
                    //Tempo de atendimento no BERÇO = SOMA DE TODOS OS CARREGAMENTOS E DESCARREGAMENTOS DE CONTAINERS
                    navio = Berco.queueIn.first();

                    Berco.horaInicioMovimentacao = myParent.getCurrentTime();
                    //hold(JSimSystem.uniform(1, 1));                    
                    Berco.horaAtracacao = myParent.getCurrentTime();
                    Berco.tempoMovimentacao = Berco.horaAtracacao - Berco.horaInicioMovimentacao;
                    n = (Navio) navio.getData();

                    for (int fila = 0; fila < n.FilasContainers.size(); fila++) {
                         filaBerco = (FilaContainers) n.FilasContainers.get(fila);

                        try {
                            Portainer portainerBerco = new Portainer("Portainer " + fila, Berco.simulation, 0, 0, this);
                            portainerBerco.setFilas(filaBerco, null);

                            filaBerco.setPortainer(portainerBerco);

                            if (portainerBerco.isIdle()) {
                                portainerBerco.activate(myParent.getCurrentTime());
                            }

                            Berco.ListaPortainers.add(portainerBerco);
                        } catch (JSimSimulationAlreadyTerminatedException | JSimInvalidParametersException | JSimTooManyProcessesException | IOException ex) {
                            Logger.getLogger(EntidadesPorto.Berco.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                                        
                    while (true) {
                        Berco.tempoAtendimentoPortainers = 0;

                        double horaFimAtendPort1 = (((FilaContainers) n.FilasContainers.get(0)).getHoraFinalAtendimento());
                        double horaFimAtendPort2 = (((FilaContainers) n.FilasContainers.get(1)).getHoraFinalAtendimento());
                        double horaIniAtendPort1 = (((FilaContainers) n.FilasContainers.get(0)).getHoraInicioAtendimento());
                        double horaIniAtendPort2 = (((FilaContainers) n.FilasContainers.get(1)).getHoraInicioAtendimento());
                        
                        if (horaFimAtendPort1 == 0 || horaFimAtendPort2 == 0) {
                            // If we have nothing to do, we sleep.
                            passivate();
                        } else {                            
                            Berco.tempoAtendimentoPortainers = Math.max(horaFimAtendPort1, horaFimAtendPort2)
                                    - Math.min(horaIniAtendPort1, horaIniAtendPort2);                            
                            break;
                        }
                    }                    

                    // Now we must decide whether to throw the transaction away or to insert it into another queue.
                    if (JSimSystem.uniform(0.0, 1.0) > Berco.p || Berco.queueOut == null) {
                        Berco.counter++;
                        Berco.horaSaida = Berco.tempoMovimentacao + Berco.tempoAtendimentoPortainers + Berco.horaInicioMovimentacao;
                        Berco.transTq += Berco.horaSaida - n.getCreationTime();
                        
                        try {
                            Berco.bw.write("\r\nNavio " + n.idNavio + ":\r\n -Criado no momento " + Berco.df.format(n.getCreationTime())
                                    + "\r\n -" + n.NumeroContainersDescarregar + " Containers a descarregar"
                                    + "\r\n -Colocado na fila " + Berco.queueIn.getHeadName()
                                    + " no momento " + Berco.df.format(navio.getEnterTime())
                                    + "\r\n -Tempo de espera na fila " + Berco.df.format((Berco.horaAtracacao - Berco.tempoMovimentacao) - navio.getEnterTime())
                                    + "\r\n -Hora Inicio Movimentacao até o Berco " + Berco.df.format(Berco.horaInicioMovimentacao)
                                    + "\r\n -Hora de Atracação " + Berco.df.format(Berco.horaAtracacao)
                                    + "\r\n -Tempo de Movimentacao da Fila até o Berço " + Berco.df.format(Berco.tempoMovimentacao)
                                    + "\r\n -Hora de Saída do Porto " + Berco.df.format(Berco.horaSaida)
                                    + "\r\n -Tempo de Atendimento pelo Portainer " + Berco.tempoAtendimentoPortainers + " \r\n");
                        } catch (IOException ex) {
                            Logger.getLogger(GeradorNavios.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        navio.out();
                        navio = null;
                        
                    } else {
                        navio.out();
                        navio.into(Berco.queueOut);
                        if (Berco.queueOut.getBerco().isIdle()) {
                            Berco.queueOut.getBerco().activate(myParent.getCurrentTime());
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
        return Berco.counter;
    } // getCounter

    public double getTransTq() {
        return Berco.transTq;
    } // getTransTq

    public void closeBw() throws IOException {
        Berco.bw.close();
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
