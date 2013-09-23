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
import cz.zcu.fav.kiv.jsim.JSimTooManyProcessesException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eduardo
 */
public class Portainer extends JSimProcess {

    public int CapacidadeMovimentacao;
    public float TempoMovimentacaoContainer;
    public float VelocidadeDeslocamento;
    private double mu;
    private double p;
    private FilaContainers queueIn;
    private FilaContainers queueOut;
    private int counter;
    private double transTq;
    private double horaSaida;
    private double tempoTotalAtendimento;
    private double horaMovimentacao;
    private File arquivo;
    private FileWriter fw;
    private BufferedWriter bw;
    private DecimalFormat df = new DecimalFormat("#0.##");
    //CaminhoesPatio
    //BercosAtende
    //IdentificadoresNavios

    public Portainer(String name, JSimSimulation sim, double parMu, double parP)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException {
        super(name, sim);
        mu = parMu;
        p = parP;
        
        counter = 0;
        transTq = 0.0;
        arquivo = new File("../arquivoContainers" + name + ".txt");

        if (arquivo.delete() == true) {
            arquivo = new File("../arquivoContainers" + name + ".txt");
        }

        if (!arquivo.exists()) {
            arquivo.createNewFile();
        }
        fw = new FileWriter(arquivo, true);
        bw = new BufferedWriter(fw);
    } // constructor

    protected void life() {
        queueIn.setHoraFinalAtendimento(0);        
        Container c;
        JSimLink container;

        try {
            while (true) {
                if (queueIn.empty()) {
                    // If we have nothing to do, we sleep.
                    passivate();
                } else {
                    container = queueIn.first();
                    horaMovimentacao = myParent.getCurrentTime();
                    c = (Container) container.getData();
                    
                    hold(JSimSystem.uniform(10, 10));

                    // Now we must decide whether to throw the transaction away or to insert it into another queue.
                    if (JSimSystem.uniform(0.0, 1.0) > p || queueOut == null) {
                        counter++;
                        horaSaida = myParent.getCurrentTime();
                        tempoTotalAtendimento = horaSaida - horaMovimentacao;
                        transTq += horaSaida - c.getCreationTime();
                        try {
                            bw.write("\r\nContainer " + c.idContainer + " Criado no momento " + df.format(c.getCreationTime())
                                    + " no momento " + df.format(container.getEnterTime())
                                    + " ficando na fila por " + df.format(horaMovimentacao - container.getEnterTime())
                                    + " iniciando a movimentação no momento " + df.format(horaMovimentacao)
                                    + " e finalizando a movimentação no momento " + df.format(horaSaida)
                                    + " movimentando por " + tempoTotalAtendimento + " \r\n");
                        } catch (IOException ex) {
                            Logger.getLogger(GeradorNavios.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        container.out();
                        if(queueIn.empty())
                        {
                            queueIn.setHoraFinalAtendimento(horaSaida);
                        }                        
                        container = null;                        
                    } else {
                        container.out();
                        container.into(queueOut);
                        if (queueOut.getPortainer().isIdle()) {
                            queueOut.getPortainer().activate(myParent.getCurrentTime());
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
    
    public void setFilas(FilaContainers parQueueIn, FilaContainers parQueueOut)
    {
        queueIn = parQueueIn;
        queueIn.setHoraInicioAtendimento(myParent.getCurrentTime());
        queueOut = parQueueOut;
    }

    public int getCounter() {
        return counter;
    } // getCounter

    public double getTransTq() {
        return transTq;
    } // getTransTq

    public void closeBw() throws IOException {
        bw.close();
    }
}
