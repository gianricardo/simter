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
import shipyard.land.move.Portainer;
import shipyard.land.move.Transtainer;
import shipyard.land.staticplace.Berco;
import shipyard.land.staticplace.DecisaoCaminhaoPatioPosicaoBerco;
import shipyard.land.staticplace.DecisaoCaminhaoPatioPosicaoEstacao;
import shipyard.land.staticplace.EstacaoArmazenamento;
import shipyard.land.staticplace.PosicaoCargaDescargaBerco;
import shipyard.land.staticplace.PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoExterno;
import shipyard.land.staticplace.PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoInterno;
import shipyard.sea.Pratico;
import simulador.generators.GeradorCaminhoesExternos;
import simulador.generators.GeradorNavios;
import simulador.queues.FilaCaminhoesExternos;
import simulador.queues.FilaCaminhoesInternos;
import simulador.queues.FilaNavios;
import simulador.random.UniformDistributionStream;
import simulador.rotas.BercoToRotaSaidaRt;
import simulador.rotas.DecisaoEstacaoToDecisaoBercoRt;
import simulador.rotas.DecisaoPosicaoToEstacaoArmazenamentoIntRt;
import simulador.rotas.DecisaoPosicaoToEstacaoArmazenamentoRt;
import simulador.rotas.DecisaoPosicaoToPosicaoBercoRt;
import simulador.rotas.FilaCaminhoesExternosToDecisaoPosicaoRt;
import simulador.rotas.FilaNaviosEntradaToPraticoRt;
import simulador.rotas.PosicaoBercoToDecisaoPosicaoEstacaoRt;
import simulador.rotas.PosicaoEstacaoToDecisaoPosicaoBercoRt;
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

                /*Objetos do pátio*/
                GeradorCaminhoesExternos geradorCaminhoes;
                FilaCaminhoesExternos filaCaminhoes1;
                FilaCaminhoesExternosToDecisaoPosicaoRt rotaEntradaCaminhoes;
                DecisaoPosicaoToEstacaoArmazenamentoRt rotaDecisaoPosicaoEstacaoArmazenamentoExt;
                DecisaoPosicaoToEstacaoArmazenamentoIntRt rotaDecisaoPosicaoEstacaoArmazenamentoInt;
                EstacaoArmazenamento estacaoArmazenamentoContainers;
                PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoExterno posicaoCargaDescargaEstacaoExterna;
                PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoInterno posicaoCargaDescargaEstacaoInterna;
                Transtainer transtainer;
                PosicaoEstacaoToDecisaoPosicaoBercoRt rotaPosicaoEstacaoInternaToDecisaoBerco;
                RotaSaidaCaminhoesRt rotaSaidaCaminhoes;

                /*Objetos do Cais*/
                GeradorNavios geradorNavios;
                FilaNavios filaNavios1;
                FilaNaviosEntradaToPraticoRt rotaEntradaToPratico;
                Pratico pratico;
                BercoToRotaSaidaRt rotaBercoSaida;
                RotaSaidaNaviosRt rotaSaidaNavios;
                PraticoToBercoRt rotaPraticoBerco;
                Berco berco;
                FilaCaminhoesInternos _filaCaminhoesPatioVazios;

                /*Objetos intermediarios*/
                DecisaoCaminhaoPatioPosicaoEstacao _decisaoCaminhoesEstacaoArmazenamento;
                DecisaoCaminhaoPatioPosicaoBerco _decisaoCaminhoesBerco;
                DecisaoEstacaoToDecisaoBercoRt _rotaDecisaoEstacaoToDecisaoBerco;

                int numeroBercos = 1;
                int numeroPortainers = 2;
                int numeroTranstainers = 2;

                System.out.println("Iniciado a simulação\r\n");
                bw.write("Iniciado a simulação\r\n");

                simulation = new JSimSimulation("Enfileirando Navios\r\n");
                bw.write("Enfileirando Navios\r\n");

                // <editor-fold defaultstate="collapsed" desc="Intermediário">

                _filaCaminhoesPatioVazios = new FilaCaminhoesInternos("Fila de Caminhões do Pátio Vazios", simulation, 10);

                _decisaoCaminhoesBerco = new DecisaoCaminhaoPatioPosicaoBerco("Decisão de Caminhões do Pátio para Posicões do Berco", simulation);

                _rotaDecisaoEstacaoToDecisaoBerco =
                        new DecisaoEstacaoToDecisaoBercoRt("Rota entre Decisão de Posições da Estação para Decisão de Posições do Berço",
                        simulation, 1, new UniformDistributionStream(new JSimUniformStream(50, 50.1)), _decisaoCaminhoesBerco);

                _decisaoCaminhoesEstacaoArmazenamento = new DecisaoCaminhaoPatioPosicaoEstacao("Decisão de Caminhões do Pátio para Posicões da Estacão", simulation,
                        _filaCaminhoesPatioVazios, _rotaDecisaoEstacaoToDecisaoBerco);
                
                _rotaDecisaoEstacaoToDecisaoBerco.setDecisaoToPosicoesEstacao(_decisaoCaminhoesEstacaoArmazenamento);
                
                _decisaoCaminhoesBerco.setRotaDecisaoEstacaoDecisaoBerco(_rotaDecisaoEstacaoToDecisaoBerco);                

                // </editor-fold>

                // <editor-fold defaultstate="collapsed" desc="Terra">

                filaCaminhoes1 = new FilaCaminhoesExternos("Fila de Entrada de Caminhões Externos no Porto", simulation);

                geradorCaminhoes = new GeradorCaminhoesExternos("Gerador de Caminhões Externos", simulation,
                        new UniformDistributionStream(new JSimUniformStream(50, 50.1)), filaCaminhoes1, 0, 0);

                rotaEntradaCaminhoes = new FilaCaminhoesExternosToDecisaoPosicaoRt("Rota de Entrada de Caminhões Externos no Porto", simulation,
                        1, filaCaminhoes1, new UniformDistributionStream(new JSimUniformStream(50, 50.1)));

                geradorCaminhoes.setRotaEntradaCaminhoes(rotaEntradaCaminhoes);

                rotaSaidaCaminhoes = new RotaSaidaCaminhoesRt("Rota de Saída de Caminhões Externos do Porto", simulation, 1/*capacidade*/,
                        new UniformDistributionStream(new JSimUniformStream(50, 50.1)));

                estacaoArmazenamentoContainers = new EstacaoArmazenamento(simulation, "Estação de Armazenamento", 30);

                for (int i = 0; i < numeroTranstainers; i++) {
                    posicaoCargaDescargaEstacaoExterna = new PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoExterno("Posição de Carga e Descarga Externa " + (i+1) + " da Estação de Armazenamento", simulation);

                    posicaoCargaDescargaEstacaoInterna = new PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoInterno("Posição de Carga e Descarga Interna " + (i+1) + " da Estação de Armazenamento", simulation);

                    transtainer = new Transtainer("Transtainer " + (i+1), simulation, estacaoArmazenamentoContainers, posicaoCargaDescargaEstacaoExterna, posicaoCargaDescargaEstacaoInterna);

                    posicaoCargaDescargaEstacaoExterna.setTranstainer(transtainer);

                    posicaoCargaDescargaEstacaoInterna.setTranstainer(transtainer);
                    
                    posicaoCargaDescargaEstacaoInterna.setFilaCaminhoesVazios(_filaCaminhoesPatioVazios);

                    rotaDecisaoPosicaoEstacaoArmazenamentoExt = new DecisaoPosicaoToEstacaoArmazenamentoRt
                            ("Rota da Decisão de Caminhões Externos para Posição Carga Descarga " + (i+1), simulation,
                            1/*capacidade*/, new UniformDistributionStream(new JSimUniformStream(50, 50.1)));
                    
                    rotaDecisaoPosicaoEstacaoArmazenamentoInt = new DecisaoPosicaoToEstacaoArmazenamentoIntRt
                            ("Rota da Decisão de Caminhões Internos para Posição Carga Descarga " + (i+1), simulation,
                            1/*capacidade*/, new UniformDistributionStream(new JSimUniformStream(25, 25.1)));

                    rotaEntradaCaminhoes.addRotasEstacaoArmazenamento(rotaDecisaoPosicaoEstacaoArmazenamentoExt);

                    rotaDecisaoPosicaoEstacaoArmazenamentoExt.setPosicao(posicaoCargaDescargaEstacaoExterna);

                    _decisaoCaminhoesEstacaoArmazenamento.addListaRotasPosicoesEstacaoDestino(rotaDecisaoPosicaoEstacaoArmazenamentoInt);

                    rotaDecisaoPosicaoEstacaoArmazenamentoInt.setDecisaoToPosicoesEstacao(_decisaoCaminhoesEstacaoArmazenamento);

                    rotaDecisaoPosicaoEstacaoArmazenamentoInt.setPosicaoEstacaoInterna(posicaoCargaDescargaEstacaoInterna);

                    posicaoCargaDescargaEstacaoExterna.setRotaAtePosicao(rotaDecisaoPosicaoEstacaoArmazenamentoExt);

                    posicaoCargaDescargaEstacaoExterna.setRotaSaidaCaminhoes(rotaSaidaCaminhoes);

                    posicaoCargaDescargaEstacaoInterna.setRotaAtePosicao(rotaDecisaoPosicaoEstacaoArmazenamentoInt);

                    rotaPosicaoEstacaoInternaToDecisaoBerco = new PosicaoEstacaoToDecisaoPosicaoBercoRt
                            ("Rota da Posicao Interna " + (i+1) + " da Estação até Decisão de Posções do Berço", simulation,
                            1/*capacidade*/, new UniformDistributionStream(new JSimUniformStream(25, 25.1)));
                    
                    posicaoCargaDescargaEstacaoInterna.setRotaAteDecisaoBerco(rotaPosicaoEstacaoInternaToDecisaoBerco);
                    
                    rotaPosicaoEstacaoInternaToDecisaoBerco.setDecisaoCaminhoesBerco(_decisaoCaminhoesBerco);
                    
                    rotaPosicaoEstacaoInternaToDecisaoBerco.setPosicaoInterna(posicaoCargaDescargaEstacaoInterna);
                    
                    _decisaoCaminhoesBerco.addListaRotasEstacao(rotaPosicaoEstacaoInternaToDecisaoBerco);

                    rotaSaidaCaminhoes.addPosicoesCargaDescargaEstacaoArmazenamentos(posicaoCargaDescargaEstacaoExterna);
                }

                // </editor-fold>

                // <editor-fold defaultstate="collapsed" desc="Mar">                

                filaNavios1 = new FilaNavios("Fila de Entrada de Navios no Porto", simulation);

                geradorNavios = new GeradorNavios("Gerador de Navios 1", simulation,
                        new UniformDistributionStream(new JSimUniformStream(250, 250.1)), filaNavios1);

                pratico = new Pratico("Pratico", simulation);

                rotaEntradaToPratico =
                        new FilaNaviosEntradaToPraticoRt("Rota da Fila de Entrada de Navios até o Prático", simulation,
                        1, filaNavios1, pratico, new UniformDistributionStream(new JSimUniformStream(50, 50.1)));

                rotaSaidaNavios = new RotaSaidaNaviosRt("Rota de Saída de Navios do Porto", simulation, 1,
                        new UniformDistributionStream(new JSimUniformStream(50, 50.1)));

                pratico.setRotaNavioPratico(rotaEntradaToPratico);

                filaNavios1.setRotaEntradaPratico(rotaEntradaToPratico);

                for (int i = 0; i < numeroBercos; i++) {
                    berco = new Berco(simulation, i + 1, _decisaoCaminhoesEstacaoArmazenamento, _decisaoCaminhoesBerco);

                    rotaPraticoBerco = new PraticoToBercoRt("Rota do Prático até o Berco " + berco.getName(), simulation, 1, berco, pratico,
                            new UniformDistributionStream(new JSimUniformStream(50, 50.1)));

                    pratico.addListaRotasBerco(rotaPraticoBerco);

                    berco.setRotaPraticoToBerco(rotaPraticoBerco);

                    rotaBercoSaida = new BercoToRotaSaidaRt("Rota do Berco até Rota de Saída do Porto" + berco.getName(), simulation, 1, berco, rotaSaidaNavios,
                            new UniformDistributionStream(new JSimUniformStream(50, 50.1)));

                    berco.setRotaBercoToSaida(rotaBercoSaida);

                    rotaSaidaNavios.AddRotaBercoToRotaSaida(rotaBercoSaida);

                    for (int j = 0; j < numeroPortainers; j++) {

                        PosicaoCargaDescargaBerco posicaoCargaDescarga = new PosicaoCargaDescargaBerco("Posicao de Carga e Descarga " + j + " do Berço " + berco.getName(), simulation, null);

                        DecisaoPosicaoToPosicaoBercoRt rotaDecisaoToPosBerco = new DecisaoPosicaoToPosicaoBercoRt
                                ("Rota da Decisão de Caminhões do Pátio para Posicão de Carga e Descarga " + j + " do Berço " + berco.getName(), simulation, 1,
                                new UniformDistributionStream(new JSimUniformStream(25, 25.1)));

                        PosicaoBercoToDecisaoPosicaoEstacaoRt rotaPosicaoBercoToDecisaoPosicaoEstacao = new PosicaoBercoToDecisaoPosicaoEstacaoRt
                                ("Rota da Posicão de Carga e Descarga " + j + " do Berço " + berco.getName() + " para Decisão de Posições da Estação", simulation, 1,
                                new UniformDistributionStream(new JSimUniformStream(25, 25.1)));

                        posicaoCargaDescarga.setRotaDecisaoPosicaoCargaDescargaBerco(rotaDecisaoToPosBerco);

                        posicaoCargaDescarga.setRotaPosicaoDecisaoPosicaoEstacao(rotaPosicaoBercoToDecisaoPosicaoEstacao);

                        rotaDecisaoToPosBerco.setPosicaoBerco(posicaoCargaDescarga);

                        rotaPosicaoBercoToDecisaoPosicaoEstacao.setPosicaoBerco(posicaoCargaDescarga);

                        rotaPosicaoBercoToDecisaoPosicaoEstacao.setDecisaoPosicaoEstacao(_decisaoCaminhoesEstacaoArmazenamento);

                        _decisaoCaminhoesEstacaoArmazenamento.addListaRotasPosicaoBercoOrigem(rotaPosicaoBercoToDecisaoPosicaoEstacao);

                        rotaDecisaoToPosBerco.setDecisaoPosicoesBerco(_decisaoCaminhoesBerco);                        
                        
                        Portainer portainerBerco = new Portainer("Portainer " + j, simulation, berco, posicaoCargaDescarga);

                        portainerBerco.setDecisaoSolicitacoes(_decisaoCaminhoesEstacaoArmazenamento);

                        posicaoCargaDescarga.setPortainer(portainerBerco);

                        berco.getListaPortainers().add(portainerBerco);
                        berco.getListaPosicoes().add(posicaoCargaDescarga);
                    }
                }

                // </editor-fold>

                simulation.message("Ativando os Geradores");
                bw.write("Ativando os Geradores\r\n");

                geradorCaminhoes.activate(0.0);
                geradorNavios.activate(0.0);

                simulation.message("Executando a simulação.");
                bw.write("Executando a simulação.\r\n");

                while ((simulation.getCurrentTime() < 10000.0) && (simulation.step() == true)) {
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

                simulation.shutdown();
                bw.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } catch (JSimException e) {
            e.printComment(System.err.append("erro"));
        }
    }
}
