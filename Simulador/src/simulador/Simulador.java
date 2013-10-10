/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulador;

import cz.zcu.fav.kiv.jsim.JSimException;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimMethodNotSupportedException;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.random.JSimUniformStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import shipyard.land.staticplace.Berco;
import shipyard.land.staticplace.EstacaoCaminhoesInternos;
import simulador.generators.GeradorNavios;
import simulador.queues.FilaNavios;
import simulador.random.GaussianDistributionStream;
import simulador.random.UniformDistributionStream;

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
                FilaNavios queueNavio1;
                Berco berco1;
                GeradorNavios generator1;
                EstacaoCaminhoesInternos estacao1;
                double mu1 = 1.0/*, mu2 = 1.0*/;
                double lambda1 = 0.4/*, lambda2 = 0.4*/;
                double p1 = 0.5/*, p2 = 0.5*/;

                System.out.println("Iniciado a simulação\r\n");
                bw.write("Iniciado a simulação\r\n");

                simulation = new JSimSimulation("Enfileirando Navios\r\n");
                bw.write("Enfileirando Navios\r\n");

                queueNavio1 = new FilaNavios("Fila Entrada de Navios no Porto", simulation, null);
                
                estacao1 = new EstacaoCaminhoesInternos("Estação de caminhões do porto", simulation, 0, 10);
                
                berco1 = new Berco(simulation,1,2, estacao1);            

                generator1 = new GeradorNavios("Gerador 1", simulation, 
                    new UniformDistributionStream(new JSimUniformStream(29.5, 29.6)), queueNavio1);

                // We must set the servers now because they didn't exist at the time the queues were created.
                queueNavio1.setBerco(berco1);
                berco1.setQueueIn(queueNavio1);

                simulation.message("Ativando os Geradores");
                bw.write("Ativando os Geradores\r\n");

                generator1.activate(0.0);

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
                simulation.message("Estatísticas dos Berços:");
                bw.write("\r\nEstatísticas dos Berços: ");
                simulation.message("Berco 1: Número de navios que já saíram do berço = " + berco1.getCounter() + ", sum of Tq (for transactions thrown away by this server) = " + berco1.getTransTq());
                bw.write("\r\nBerco 1: Número de navios que já saíram do berço = " + berco1.getCounter() + ", sum of Tq (for transactions thrown away by this server) = " + berco1.getTransTq() + "\r\n");
                
                simulation.shutdown();
                berco1.getBw().close();
                bw.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } catch (JSimException e) {
            e.printComment(System.err.append("erro"));
        }
    }
}
