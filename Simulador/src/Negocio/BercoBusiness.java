/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Negocio;

import EntidadesPorto.Berco;
import EntidadesPorto.EstacaoCaminhoesInternos;
import EntidadesPorto.FilaContainers;
import EntidadesPorto.FilaNavios;
import EntidadesPorto.Navio;
import EntidadesPorto.Portainer;
import EntidadesPorto.PosicaoCargaDescargaBerco;
import cz.zcu.fav.kiv.jsim.JSimException;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimLink;
import cz.zcu.fav.kiv.jsim.JSimProcess;
import cz.zcu.fav.kiv.jsim.JSimSecurityException;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimSimulationAlreadyTerminatedException;
import cz.zcu.fav.kiv.jsim.JSimSystem;
import cz.zcu.fav.kiv.jsim.JSimTooManyProcessesException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
    List FilaPortainers = new ArrayList();
    Portainer portainerBerco;
    List FilaPosicoes = new ArrayList();
    PosicaoCargaDescargaBerco posicaoCargaDescarga;
    Navio n;
    JSimLink navio;
    EstacaoCaminhoesInternos EstacaoCaminhoes;

    public BercoBusiness(String name, JSimSimulation sim,
            double parMu, double parP,
            FilaNavios parQueueIn,
            FilaNavios parQueueOut,
            EstacaoCaminhoesInternos estacao,
            int NumeroPortainers)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException {
        super(name, sim);

        Berco.simulation = sim;
        Berco.mu = parMu;
        Berco.p = parP;
        Berco.queueIn = parQueueIn;
        Berco.queueOut = parQueueOut;

        Berco.counter = 0;
        Berco.transTq = 0.0;
        
        EstacaoCaminhoes = estacao;

        CriarArquivo(name);

        CriarProcessos(NumeroPortainers);

    } // constructor

    protected void life() {
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

                    SetarFilasPortainers();

                    VerificaFinalizacaoAtendimentoNavio();

                    // Now we must decide whether to throw the transaction away or to insert it into another queue.
                    if (JSimSystem.uniform(0.0, 1.0) > Berco.p || Berco.queueOut == null) {
                        Berco.counter++;
                        Berco.horaSaida = Berco.tempoMovimentacao + Berco.tempoAtendimentoPortainers + Berco.horaInicioMovimentacao;
                        Berco.transTq += Berco.horaSaida - n.getCreationTime();

                        try {
                            EscreverArquivo();
                        } catch (IOException ex) {
                            Logger.getLogger(BercoBusiness.class.getName()).log(Level.SEVERE, null, ex);
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
    
    public double VerificarHoraSaidaNavio(List<FilaContainers> lista) {
        double tempoPortainers = 0;
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getHoraFinalAtendimento() > tempoPortainers) {
                tempoPortainers = lista.get(i).getHoraFinalAtendimento();
            }
        }
        return tempoPortainers;
    }

    private void CriarProcessos(int NumeroPortainers) throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException {
        for (int i = 0; i < NumeroPortainers; i++) {            
            posicaoCargaDescarga = new PosicaoCargaDescargaBerco("Posicao " + i, Berco.simulation, 0, null, EstacaoCaminhoes);
            
            portainerBerco = new Portainer("Portainer " + i, Berco.simulation, 0, 0, this, posicaoCargaDescarga);
            
            posicaoCargaDescarga.setPortainer(portainerBerco);
            
            FilaPortainers.add(portainerBerco);
            FilaPosicoes.add(posicaoCargaDescarga);
        }
    }

    private void CriarArquivo(String name) throws IOException{
        Berco.arquivo = new File("../arquivoNavios" + name + ".txt");

        if (Berco.arquivo.delete() == true) {
            Berco.arquivo = new File("../arquivoNavios" + name + ".txt");
        }

        if (!Berco.arquivo.exists()) {
            Berco.arquivo.createNewFile();
        }
        Berco.fw = new FileWriter(Berco.arquivo, true);
        Berco.bw = new BufferedWriter(Berco.fw);
    }
    
    private void EscreverArquivo() throws IOException {
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
    }
    
    public void CloseBw() throws IOException {
        Berco.bw.close();
    }

    private void SetarFilasPortainers() throws JSimSecurityException, JSimInvalidParametersException {
        int numeroFilasPortainer = Math.round(n.FilasContainers.size() / FilaPortainers.size());
        int auxFila = 0;
        int nfila = 0;

        for (int portainer = 0; portainer < FilaPortainers.size(); portainer++) {
            for (nfila = auxFila; nfila < numeroFilasPortainer + auxFila; nfila++) {
                filaBerco = (FilaContainers) n.FilasContainers.get(nfila);

                portainerBerco = (Portainer) FilaPortainers.get(portainer);

                portainerBerco.setFilas(filaBerco, null);

                filaBerco.setPortainer(portainerBerco);

                if (portainerBerco.isIdle()) {
                    portainerBerco.activate(myParent.getCurrentTime());
                }
            }

            if (portainer == FilaPortainers.size() - 1) {
                int auxiliarResto = n.FilasContainers.size() - nfila;
                for (int z = nfila; z < nfila + auxiliarResto; z++) {
                    filaBerco = (FilaContainers) n.FilasContainers.get(z);

                    portainerBerco = (Portainer) FilaPortainers.get(portainer);

                    portainerBerco.setFilas(filaBerco, null);

                    filaBerco.setPortainer(portainerBerco);

                    if (portainerBerco.isIdle()) {
                        portainerBerco.activate(myParent.getCurrentTime());
                    }
                }
            }

            Berco.ListaPortainers.add(portainerBerco);
            auxFila = nfila;
        }
    }

    private void VerificaFinalizacaoAtendimentoNavio() throws JSimSecurityException {
        while (true) {
            Berco.tempoAtendimentoPortainers = 0;
            FilaContainers fila;
            double[] ArrayHoraFimAten = new double[n.FilasContainers.size()];
            double[] ArrayHoraIniAten = new double[n.FilasContainers.size()];
            for (int i = 0; i < n.FilasContainers.size(); i++) {
                fila = (FilaContainers) n.FilasContainers.get(i);
                ArrayHoraFimAten[i] = fila.getHoraFinalAtendimento();
                ArrayHoraIniAten[i] = fila.getHoraInicioAtendimento();
            }

            boolean Finalizado = false;
            for (int j = 0; j < ArrayHoraFimAten.length; j++) {
                if (ArrayHoraFimAten[j] > 0) {
                    Finalizado = true;
                } else {
                    Finalizado = false;
                    break;
                }
            }

            if (!Finalizado) {
                // If we have nothing to do, we sleep.
                passivate();
            } else {
                double MaiorHoraFimAtendimento = 0;
                double MenorHoraInicioAtendimento = 0;
                for (int i = 0; i < n.FilasContainers.size(); i++) {
                    if (ArrayHoraFimAten[i] > MaiorHoraFimAtendimento) {
                        MaiorHoraFimAtendimento = ArrayHoraFimAten[i];
                    }
                    if (ArrayHoraIniAten[i] > MenorHoraInicioAtendimento) {
                        MenorHoraInicioAtendimento = ArrayHoraIniAten[i];
                    }
                }

                Berco.tempoAtendimentoPortainers = MaiorHoraFimAtendimento - MenorHoraInicioAtendimento;
                break;
            }
        }
    }
    
    public int getCounter() {
        return Berco.counter;
    } // getCounter

    public double getTransTq() {
        return Berco.transTq;
    } // getTransTq    
}
