/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador;

import EntidadesPorto.Berco;
import EntidadesPorto.FilaContainers;
import EntidadesPorto.FilaNavios;
import EntidadesPorto.GeradorNavios;
import EntidadesPorto.Portainer;
import cz.zcu.fav.kiv.jsim.JSimException;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimMethodNotSupportedException;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eduardo
 */
public class Simulador {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws JSimInvalidParametersException, JSimMethodNotSupportedException {
        try {
            File arquivo = new File("../arquivo.txt");

            if (arquivo.delete() == true) {
                arquivo = new File("../arquivo.txt");
            }

            if (!arquivo.exists()) {
                arquivo.createNewFile();
            }
            try (FileWriter fw = new FileWriter(arquivo, true)) {
                BufferedWriter bw = new BufferedWriter(fw);

                JSimSimulation simulation;
                FilaNavios queueNavio1/*, queue2*/;
                Berco berco1/*, berco2*/;
                GeradorNavios generator1/*, generator2*/;
                double mu1 = 1.0/*, mu2 = 1.0*/;
                double lambda1 = 0.4/*, lambda2 = 0.4*/;
                double p1 = 0.5/*, p2 = 0.5*/;
                int containersDescarregar = 10;

                System.out.println("Iniciado a simulação\r\n");
                bw.write("Iniciado a simulação\r\n");

                simulation = new JSimSimulation("Enfileirando Navios\r\n");
                bw.write("Enfileirando Navios\r\n");

                queueNavio1 = new FilaNavios("Fila Entrada de Navios no Porto", simulation, null);
                //queue2 = new FilaNavios("Fila 2", simulation, null);

                //queueContainer1 = new FilaContainers("Fila de Containers de um navio", simulation, null);
                
                //portainer1 = new Portainer("Portainer 1", simulation, mu1, p1, queueContainer1, null);
                
                //listaPortainer.add(queueContainer1);
                
                berco1 = new Berco("Berco 1", simulation, mu1, p1, queueNavio1, null);
                //berco2 = new Berco("Berco 2", simulation, mu2, p2, queue2, queue1);                

                generator1 = new GeradorNavios("Gerador 1", simulation, lambda1, queueNavio1, containersDescarregar);
                //generator2 = new GeradorNavios("Gerador 2", simulation, lambda2, queue2);

                // We must set the servers now because they didn't exist at the time the queues were created.
                queueNavio1.setBerco(berco1);
                //queue2.setBerco(berco2);
                
               // queueContainer1.setPortainer(portainer1);

                simulation.message("Ativando os Geradores");
                bw.write("Ativando os Geradores\r\n");

                generator1.activate(0.0);
                //generator2.activate(0.0);

                simulation.message("Executando a simulação.");
                bw.write("Executando a simulação.\r\n");

                while ((simulation.getCurrentTime() < 1000.0) && (simulation.step() == true)) {
                    continue;
                }

                // Now, some boring numbers.
                simulation.message("Simulação interrompida no momento " + simulation.getCurrentTime());
                bw.write("\r\nSimulação interrompida no momento " + simulation.getCurrentTime() + "\r\n");
                simulation.message("Estatísticas das Filas:");
                bw.write("\r\nEstatísticas das Filas: ");
                simulation.message("Fila 1: Tamanho médio da fila = " + queueNavio1.getLw() + ", Tempo médio de espera na fila dos navios que já deixaram o porto = " + queueNavio1.getTw() + ", Tempo médio de espera na fila dos navios = " + queueNavio1.getTwForAllLinks());
                bw.write("\r\nFila 1: Tamanho médio da fila = " + queueNavio1.getLw() + ", \r\nTempo médio de espera na fila dos navios que já deixaram o porto = " + queueNavio1.getTw() + ", \r\nTempo médio de espera na fila dos navios = " + queueNavio1.getTwForAllLinks() + "\r\n");
                //simulation.message("Fila 2: Tamanho médio da fila = " + queue2.getLw() + ", Tempo médio de espera na fila dos navios que já deixaram o porto = " + queue2.getTw() + ", Tempo médio de espera na fila dos navios = " + queue2.getTwForAllLinks());
                //bw.write("\r\nFila 2: Tamanho médio da fila = " + queue2.getLw() + ", \r\nTempo médio de espera na fila dos navios que já deixaram o porto = " + queue2.getTw() + ", \r\nTempo médio de espera na fila dos navios = " + queue2.getTwForAllLinks() + "\r\n");
                simulation.message("Estatísticas dos Berços:");
                bw.write("\r\nEstatísticas dos Berços: ");
                simulation.message("Berco 1: Número de navios que já saíram do berço = " + berco1.getCounter() + ", sum of Tq (for transactions thrown away by this server) = " + berco1.getTransTq());
                bw.write("\r\nBerco 1: Número de navios que já saíram do berço = " + berco1.getCounter() + ", sum of Tq (for transactions thrown away by this server) = " + berco1.getTransTq() + "\r\n");
                //simulation.message("Berco 2: Número de navios que já saíram do berço = " + berco2.getCounter() + ", sum of Tq (for transactions thrown away by this server) = " + berco2.getTransTq());
                // bw.write("\r\nBerco 2: Número de navios que já saíram do berço = " + berco2.getCounter() + ", sum of Tq (for transactions thrown away by this server) = " + berco2.getTransTq()+ "\r\n");
                //simulation.message("Tempo de resposta Médio = " + ((berco1.getTransTq() + berco2.getTransTq()) / (berco1.getCounter() + berco2.getCounter())));
                //bw.write("\r\nTempo de resposta Médio = " + ((berco1.getTransTq() + berco2.getTransTq()) / (berco1.getCounter() + berco2.getCounter()))+ "\r\n");

                simulation.shutdown();
                berco1.closeBw();
                //berco2.closeBw();
                bw.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (JSimException e) {
            e.printComment(System.err.append("erro"));
        }
    }
}
