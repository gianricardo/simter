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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eduardo
 */
public class Portainer extends JSimProcess {

    public List FilasContainers = new ArrayList();
    PosicaoCargaDescargaBerco PosicaoCargaDescarga;
    private double mu;
    private double p;
    private FilaContainers queueIn;
    private FilaContainers queueOut;
    private int counter;
    private double transTq;
    private double horaSaidaContainer;
    private double tempoTotalAtendimento;
    private double horaMovimentacao;
    
    private File arquivo;
    private FileWriter fw;
    private BufferedWriter bw;
    private DecimalFormat df = new DecimalFormat("#0.##");
    
    private String NomePortainer;
    private JSimProcess Berco;
    private Container c;
    private JSimLink container;

    //CaminhoesPatio
    //BercosAtende
    //IdentificadoresNavios
    public Portainer(String name, JSimSimulation sim, double parMu, double parP, JSimProcess berco, PosicaoCargaDescargaBerco posicao)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException {
        super(name, sim);
        mu = parMu;
        p = parP;
        NomePortainer = name;
        Berco = berco;
        PosicaoCargaDescarga = posicao;

        counter = 0;
        transTq = 0.0;
    } // constructor

    protected void life() {
        queueIn = (FilaContainers) FilasContainers.get(0);
        queueIn.setHoraFinalAtendimento(0);
        CriarArquivo();
        try {
            while (true) {
                if (queueIn.empty()) {
                    // If we have nothing to do, we sleep.
                    passivate();
                } else {
                    container = queueIn.first();
                    horaMovimentacao = myParent.getCurrentTime();
                    c = (Container) container.getData();

                    if (PosicaoCargaDescarga.isIdle()) {
                        PosicaoCargaDescarga.activate(myParent.getCurrentTime());
                    }

                    if (PosicaoCargaDescarga.caminhao == null) {
                        passivate();
                    } else {                        
                        hold(JSimSystem.uniform(10, 10));
                        PosicaoCargaDescarga.caminhao.container = c;
                        PosicaoCargaDescarga.caminhao.HoraRecebimentoContainer = myParent.getCurrentTime();
                        PosicaoCargaDescarga.activateNow();

                        //hold(JSimSystem.uniform(10, 10));

                        // Now we must decide whether to throw the transaction away or to insert it into another queue.
                        if (JSimSystem.uniform(0.0, 1.0) > p || queueOut == null) {
                            counter++;
                            horaSaidaContainer = myParent.getCurrentTime();
                            tempoTotalAtendimento = horaSaidaContainer - horaMovimentacao;
                            transTq += horaSaidaContainer - c.getCreationTime();
                            try {
                                EscreverArquivo();
                            } catch (IOException ex) {
                                Logger.getLogger(Portainer.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            container.out();
                            if (queueIn.empty()) {
                                queueIn.setHoraFinalAtendimento(horaSaidaContainer);
                                FilasContainers.remove(queueIn);
                                try {;
                                    closeBw();
                                } catch (IOException ex) {
                                    Logger.getLogger(Portainer.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                if (FilasContainers.isEmpty()) {
                                    if (Berco.isIdle()) {
                                        Berco.activate(horaSaidaContainer);
                                    }
                                } else {
                                    queueIn = (FilaContainers) FilasContainers.get(0);
                                    CriarArquivo();
                                }
                            }
                            container = null;
                        } /*else {
                         container.out();
                         container.into(queueOut);
                         if (queueOut.getPortainer().isIdle()) {
                         queueOut.getPortainer().activate(myParent.getCurrentTime());
                         }
                         }*/ // else throw away / insert
                    }
                } // else queue is empty / not empty
            } // while            
        } // try
        catch (JSimException e) {
            e.printStackTrace();
            e.printComment(System.err);
        }
    } // life

    public void setFilas(FilaContainers parQueueIn, FilaContainers parQueueOut) {
        queueIn = parQueueIn;
        queueIn.setHoraInicioAtendimento(myParent.getCurrentTime());
        FilasContainers.add(queueIn);
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

    public void CriarArquivo() {
        arquivo = new File("../arquivoContainers" + NomePortainer + " " + queueIn.nomeFila + ".txt");

        if (arquivo.delete() == true) {
            arquivo = new File("../arquivoContainers" + NomePortainer + " " + queueIn.nomeFila + ".txt");
        }

        if (!arquivo.exists()) {
            try {
                arquivo.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(Portainer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            fw = new FileWriter(arquivo, true);
        } catch (IOException ex) {
            Logger.getLogger(Portainer.class.getName()).log(Level.SEVERE, null, ex);
        }
        bw = new BufferedWriter(fw);
    }

    public void EscreverArquivo() throws IOException {
        try {
            bw.write("\r\nContainer " + c.idContainer + " Criado no momento " + df.format(c.getCreationTime())
                    + "\r\n colocado na fila " + queueIn.nomeFila
                    + "\r\n ficando na fila por " + df.format(horaMovimentacao - container.getEnterTime())
                    + "\r\n iniciando a movimentação no momento " + df.format(horaMovimentacao)
                    + "\r\n e finalizando a movimentação no momento " + df.format(horaSaidaContainer)
                    + "\r\n movimentando por " + tempoTotalAtendimento + " \r\n");
        } catch (IOException ex) {
            CriarArquivo();
            bw.write("\r\nContainer " + c.idContainer + " Criado no momento " + df.format(c.getCreationTime())
                    + " no momento " + df.format(container.getEnterTime())
                    + " colocado na fila " + queueIn.nomeFila
                    + " ficando na fila por " + df.format(horaMovimentacao - container.getEnterTime())
                    + " iniciando a movimentação no momento " + df.format(horaMovimentacao)
                    + " e finalizando a movimentação no momento " + df.format(horaSaidaContainer)
                    + " movimentando por " + tempoTotalAtendimento + " \r\n");
        }
    }
}
