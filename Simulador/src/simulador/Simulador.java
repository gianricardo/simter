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
import shipyard.land.move.Transtainer;
import shipyard.land.staticplace.Berco;
import shipyard.land.staticplace.EstacaoArmazenamento;
import shipyard.land.staticplace.EstacaoCaminhoesInternos;
import shipyard.land.staticplace.PosicaoCargaDescargaEstacaoArmazenamento;
import shipyard.sea.Pratico;
import simulador.generators.GeradorCaminhoesExternos;
import simulador.generators.GeradorNavios;
import simulador.queues.FilaCaminhoesExternos;
import simulador.queues.FilaNavios;
import simulador.random.UniformDistributionStream;
import simulador.rotas.BercoToRotaSaidaRt;
import simulador.rotas.DecisaoPosicaoToEstacaoArmazenamentoRt;
import simulador.rotas.FilaCaminhoesExternosToDecisaoPosicaoRt;
import simulador.rotas.FilaNaviosEntradaToPraticoRt;
import simulador.rotas.PraticoToBercoRt;
import simulador.rotas.RotaSaidaCaminhoesRt;
import simulador.rotas.RotaSaidaNaviosRt;

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
                double mu1 = 1.0/*, mu2 = 1.0*/;
                double lambda1 = 0.4/*, lambda2 = 0.4*/;
                double p1 = 0.5/*, p2 = 0.5*/;
                
                /*Objetos do pátio*/                
                GeradorCaminhoesExternos geradorCaminhoes;
                FilaCaminhoesExternos filaCaminhoes1;
                FilaCaminhoesExternosToDecisaoPosicaoRt rotaEntradaCaminhoes;
                DecisaoPosicaoToEstacaoArmazenamentoRt rotaDecisaoPosicaoEstacaoArmazenamento;
                EstacaoArmazenamento estacaoArmazenamentoContainers;
                PosicaoCargaDescargaEstacaoArmazenamento posicaoCargaDescargaEstacaoArmazenamento;
                Transtainer transtainer;
                RotaSaidaCaminhoesRt rotaSaidaCaminhoes;
                EstacaoCaminhoesInternos estacaoCaminhoesInternos;
                int numeroTranstainers=2;
                
                /*Objetos do Cais*/
                GeradorNavios geradorNavios;
                FilaNavios filaNavios1;
                FilaNaviosEntradaToPraticoRt rotaEntradaToPratico;
                Pratico pratico;                
                BercoToRotaSaidaRt rotaBercoSaida;
                RotaSaidaNaviosRt rotaSaidaNavios;
                PraticoToBercoRt rotaPraticoBerco;    
                Berco berco;
                int numeroBercos = 2;

                System.out.println("Iniciado a simulação\r\n");
                bw.write("Iniciado a simulação\r\n");

                simulation = new JSimSimulation("Enfileirando Navios\r\n");
                bw.write("Enfileirando Navios\r\n");
                
                filaCaminhoes1 = new FilaCaminhoesExternos("Fila Entrada de Caminhões no Porto", simulation);
                
                geradorCaminhoes = new GeradorCaminhoesExternos("Gerador Caminhoes 1", simulation,
                        new UniformDistributionStream(new JSimUniformStream(20, 20.1)), filaCaminhoes1);
                
                rotaEntradaCaminhoes = new FilaCaminhoesExternosToDecisaoPosicaoRt("rota Entrada Caminhões -> Decisão Posição Carga Descarga",
                        simulation, 1/*capacidade*/, filaCaminhoes1, 
                        new UniformDistributionStream(new JSimUniformStream(10, 10.1)));
                
                geradorCaminhoes.setRotaEntradaCaminhoes(rotaEntradaCaminhoes);
                
                rotaSaidaCaminhoes = new RotaSaidaCaminhoesRt("rota Saída Caminhões do Porto", simulation, 1/*capacidade*/,
                        new UniformDistributionStream(new JSimUniformStream(10, 10.1)));
                
                estacaoArmazenamentoContainers = new EstacaoArmazenamento(simulation, "Estação de armazenamento", 500, 2);
                
                for(int i=0;i<numeroTranstainers;i++){
                    posicaoCargaDescargaEstacaoArmazenamento = new PosicaoCargaDescargaEstacaoArmazenamento("Posicao Carga Descarga Estacao de Armazenamento" +i, simulation);
                    
                    estacaoArmazenamentoContainers.addListaPosicoes(posicaoCargaDescargaEstacaoArmazenamento);
                    
                    transtainer = new Transtainer("Transtainer 1", simulation, estacaoArmazenamentoContainers, posicaoCargaDescargaEstacaoArmazenamento);
                    
                    posicaoCargaDescargaEstacaoArmazenamento.setTranstainer(transtainer);
                    
                    rotaDecisaoPosicaoEstacaoArmazenamento = new DecisaoPosicaoToEstacaoArmazenamentoRt("rota Decisão Posição Carga Descarga To Posição Carga Descarga " +i, simulation,
                                                                                                        1/*capacidade*/, new UniformDistributionStream(new JSimUniformStream(10, 10.1)));
                    
                    rotaEntradaCaminhoes.addRotasEstacaoArmazenamento(rotaDecisaoPosicaoEstacaoArmazenamento);
                    
                    rotaDecisaoPosicaoEstacaoArmazenamento.setPosicao(posicaoCargaDescargaEstacaoArmazenamento);
                    
                    posicaoCargaDescargaEstacaoArmazenamento.setRotaAtePosicao(rotaDecisaoPosicaoEstacaoArmazenamento);
                    
                    posicaoCargaDescargaEstacaoArmazenamento.setRotaSaidaCaminhoes(rotaSaidaCaminhoes);
                    
                    rotaSaidaCaminhoes.addPosicoesCargaDescargaEstacaoArmazenamentos(posicaoCargaDescargaEstacaoArmazenamento);
                }                
                
                // <editor-fold defaultstate="collapsed" desc="Mar">

                filaNavios1 = new FilaNavios("Fila Entrada de Navios no Porto", simulation);
                
                geradorNavios = new GeradorNavios("Gerador 1", simulation,
                        new UniformDistributionStream(new JSimUniformStream(350, 350.1)), filaNavios1);
                
                pratico = new Pratico("pratico1", simulation);

                rotaEntradaToPratico =
                        new FilaNaviosEntradaToPraticoRt("rota Entrada de Navios -> Pratico", simulation,
                        1, filaNavios1, pratico, new UniformDistributionStream(new JSimUniformStream(5, 5.1)));
                
                rotaSaidaNavios = new RotaSaidaNaviosRt("rota Saída de Navios do Porto ", simulation, 1, 
                                                        new UniformDistributionStream(new JSimUniformStream(5, 5.1)));
                
                pratico.setRotaNavioPratico(rotaEntradaToPratico);

                filaNavios1.setRotaEntradaPratico(rotaEntradaToPratico);                
                
                estacaoCaminhoesInternos = new EstacaoCaminhoesInternos("Estação de caminhões do porto", simulation, 0, 50); 
                
                for(int i=0; i<numeroBercos; i++){
                    berco = new Berco(simulation, i+1, 2, estacaoCaminhoesInternos);
                    
                    rotaPraticoBerco = new PraticoToBercoRt("rota Pratico -> Berco" + berco.getName(), simulation, 1, berco, pratico,
                                                          new UniformDistributionStream(new JSimUniformStream(5, 5.1)));
                    
                    pratico.addListaRotasBerco(rotaPraticoBerco);
                    
                    berco.setRotaPraticoToBerco(rotaPraticoBerco);
                    
                    rotaBercoSaida = new BercoToRotaSaidaRt("rota Berco -> Rota Saída Porto" + berco.getName(), simulation, 1, berco, rotaSaidaNavios,
                                                            new UniformDistributionStream(new JSimUniformStream(5, 5.1)));
                    
                    berco.setRotaBercoToSaida(rotaBercoSaida);
                    
                    rotaSaidaNavios.AddRotaBercoToRotaSaida(rotaBercoSaida);
                }  
                
                // </editor-fold>

                simulation.message("Ativando os Geradores");
                bw.write("Ativando os Geradores\r\n");  
                
                geradorCaminhoes.activate(0.0);
                geradorNavios.activate(0.0);

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
                simulation.message("Fila 1: Tamanho médio da fila = " + filaNavios1.getLw() + ", Tempo médio de espera na fila dos navios que já deixaram o porto = " + filaNavios1.getTw() + ", Tempo médio de espera na fila dos navios = " + filaNavios1.getTwForAllLinks());
                bw.write("\r\nFila 1: Tamanho médio da fila = " + filaNavios1.getLw() + ", \r\nTempo médio de espera na fila dos navios que já deixaram o porto = " + filaNavios1.getTw() + ", \r\nTempo médio de espera na fila dos navios = " + filaNavios1.getTwForAllLinks() + "\r\n");
                simulation.message("Estatísticas dos Berços:");
                bw.write("\r\nEstatísticas dos Berços: ");
                /*simulation.message("Berco 1: Número de navios que já saíram do berço = " + berco1.getCounter() + ", sum of Tq (for transactions thrown away by this server) = " + berco1.getTransTq());
                bw.write("\r\nBerco 1: Número de navios que já saíram do berço = " + berco1.getCounter() + ", sum of Tq (for transactions thrown away by this server) = " + berco1.getTransTq() + "\r\n");*/

                simulation.shutdown();
                /*berco1.getBw().close();
                berco2.getBw().close();*/
                bw.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } catch (JSimException e) {
            e.printComment(System.err.append("erro"));
        }
    }    
}
