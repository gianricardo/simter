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
import estatisticas.EstatisticasPorto;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
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
import utils.Formatters;

/**
 *
 * @author Eduardo
 */
public class Simulador {

    /**
     * @param args the command line arguments
     */
    JFrame janela = new JFrame();
    JLabel labelPorto = new JLabel("Porto");
    JLabel labelTempoSimulacao = new JLabel("Tempo de simulação");
    JTextField textTempoSimulacao = new JTextField();
    
    
    JLabel labelBerco = new JLabel("Berço");
    JLabel labelPortainer = new JLabel("Portainer");
    JLabel labelNumeroPortainers = new JLabel("Número de Portainers");
    JTextField textNumeroPortainers = new JTextField();
    JLabel labelPortainerIntervalo = new JLabel("Intervalo de Movimentação de Containers");
    JTextField textIPortainerIntervalo = new JTextField();
    JTextField textFPortainerIntervalo = new JTextField();
    
    JLabel labelPatio = new JLabel("Dados do pátio");
    JLabel labelGeradorCaminhoes = new JLabel("Gerador de Caminhões");
    JLabel labelGeradorCaminhoesIntervalo = new JLabel("Intervalo entre Caminhões");
    JTextField textIGeradorCaminhoesIntervalo = new JTextField();
    JTextField textFGeradorCaminhoesIntervalo = new JTextField();
    JLabel labelPorcentagemCarregar = new JLabel("Caminhões Carregar(%)");
    JTextField textPorcentagemCarregar = new JTextField();
    JLabel labelPorcentagemDescarregar = new JLabel("Caminhões Descarregar(%)");
    JTextField textPorcentagemDescarregar = new JTextField();
    JLabel labelPorcentagemCarregarDescarregar = new JLabel("O restante descarregará e carregará");        
    JLabel labelRotaFilaCaminhoes = new JLabel("Rotas");
    JLabel labelRotaFilaCaminhoesIntervalo = new JLabel("Intervalo de Movimentação");
    JTextField textIRotaFilaCaminhoesIntervalo = new JTextField();
    JTextField textFRotaFilaCaminhoesIntervalo = new JTextField();
    JLabel labelRotaFilaCaminhoesCapacidade = new JLabel("Capacidade");
    JTextField textCapacidadeRotaFilaCaminhoes = new JTextField();
    
    JLabel labelCais = new JLabel("Dados do cais");
    JLabel labelGeradorNavios = new JLabel("Gerador de Navios");
    JLabel labelGeradorNaviosIntervalo = new JLabel("Intervalo entre Navios");
    JTextField textIGeradorNaviosIntervalo = new JTextField();
    JTextField textFGeradorNaviosIntervalo = new JTextField();
    JLabel labelGeradorNaviosIntervaloContainersDescarregar = new JLabel("Intervalo nº containers Descarregar");
    JTextField textIGeradorNaviosIntervaloContainersDescarregar = new JTextField();
    JTextField textFGeradorNaviosIntervaloContainersDescarregar = new JTextField();
    JLabel labelGeradorNaviosIntervaloContainersCarregar = new JLabel("Intervalo nº containers Carregar");
    JTextField textIGeradorNaviosIntervaloContainersCarregar = new JTextField();
    JTextField textFGeradorNaviosIntervaloContainersCarregar = new JTextField();  
    JLabel labelPorcentagemContainersOutosNavios = new JLabel("Containers p/ outros navios(%)");
    JTextField textIPorcentagemContainersOutosNavios = new JTextField();
    JLabel labelRotaFilaNavios = new JLabel("Rotas");
    JLabel labelRotaFilaNaviosIntervalo = new JLabel("Intervalo de Movimentação");
    JTextField textIRotaFilaNaviosIntervalo = new JTextField();
    JTextField textFRotaFilaNaviosIntervalo = new JTextField();
    JLabel labelRotaFilaNaviosCapacidade = new JLabel("Capacidade");
    JTextField textCapacidadeRotaFilaNavios = new JTextField();
    
    JLabel labelEstacaoArmazenamento = new JLabel("Estação de Armazenamento");        
    JLabel labelTranstainer = new JLabel("Transtainer");
    JLabel labelNumeroPosicoesCargaDescargaEstacao = new JLabel("Número de Transtainers");
    JTextField textNumeroPosicoesCargaDescargaEstacao = new JTextField();        
    JLabel labelTranstainerIntervalo = new JLabel("Intervalo de Movimentação de Containers");
    JTextField textITranstainerIntervalo = new JTextField();
    JTextField textFTranstainerIntervalo = new JTextField();        
    JLabel labelPosicaoCargaDescarga = new JLabel("Posicao de Carga/Descarga ");        
    JLabel labelNumeroCaminhoes = new JLabel("Número de Caminhões Internos");
    JTextField textNumeroCaminhoes = new JTextField();
        
    //JPanel panelPosicao = new PosicaoCargaDescargaEstacao().showPanel(1);
    JButton botao = new JButton("OK");

    private Simulador() {
        Porto();
        //eventos();
    }

    public void Porto() {

        Eventos event = new Eventos();

        janela.setLayout(null);

        janela.setTitle("Dados Porto");
        janela.setSize(1450, 750);
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        labelPorto.setBounds(10, 10, 100, 20);
        
        labelPatio.setBounds(20, 70, 200, 20);
        labelGeradorCaminhoes.setBounds(20, 105, 200, 20);
        labelGeradorCaminhoesIntervalo.setBounds(250, 105, 200, 20);
        textIGeradorCaminhoesIntervalo.setBounds(500, 105, 50, 20);
        textFGeradorCaminhoesIntervalo.setBounds(550, 105, 50, 20);
        labelPorcentagemCarregar.setBounds(250, 140, 200, 20);
        textPorcentagemCarregar.setBounds(500,140,50,20);
        labelPorcentagemDescarregar.setBounds(250, 175, 200, 20);
        textPorcentagemDescarregar.setBounds(500,175,50,20);
        labelPorcentagemCarregarDescarregar.setBounds(250, 210, 300, 10);
        labelRotaFilaCaminhoes.setBounds(20, 245, 200, 20);
        labelRotaFilaCaminhoesIntervalo.setBounds(250, 245, 200, 20);
        textIRotaFilaCaminhoesIntervalo.setBounds(500, 245, 50, 20);        
        textFRotaFilaCaminhoesIntervalo.setBounds(550, 245, 50, 20);
        labelRotaFilaCaminhoesCapacidade.setBounds(250, 280, 200, 20);
        textCapacidadeRotaFilaCaminhoes.setBounds(500, 280, 50, 20);
        
        labelCais.setBounds(720, 70, 200, 20);
        labelGeradorNavios.setBounds(720, 105, 200, 20);
        labelGeradorNaviosIntervalo.setBounds(950, 105, 200, 20);
        textIGeradorNaviosIntervalo.setBounds(1200, 105, 50, 20);
        textFGeradorNaviosIntervalo.setBounds(1250, 105, 50, 20);        
        labelGeradorNaviosIntervaloContainersDescarregar.setBounds(950, 140, 250, 20);
        textIGeradorNaviosIntervaloContainersDescarregar.setBounds(1200, 140, 50, 20);
        textFGeradorNaviosIntervaloContainersDescarregar.setBounds(1250, 140, 50, 20);
        labelGeradorNaviosIntervaloContainersCarregar.setBounds(950, 175, 200, 20);
        textIGeradorNaviosIntervaloContainersCarregar.setBounds(1200, 175, 50, 20);
        textFGeradorNaviosIntervaloContainersCarregar.setBounds(1250, 175, 50, 20);
        labelPorcentagemContainersOutosNavios.setBounds(950, 210, 200, 20);
        textIPorcentagemContainersOutosNavios.setBounds(1200,210,50,20);        
        labelRotaFilaNavios.setBounds(720, 245, 200, 20);
        labelRotaFilaNaviosIntervalo.setBounds(950, 245, 200, 20);
        textIRotaFilaNaviosIntervalo.setBounds(1200, 245, 50, 20);        
        textFRotaFilaNaviosIntervalo.setBounds(1250, 245, 50, 20);
        labelRotaFilaNaviosCapacidade.setBounds(950, 280, 200, 20);
        textCapacidadeRotaFilaNavios.setBounds(1200, 280, 50, 20);
        
        labelEstacaoArmazenamento.setBounds(20, 370, 200, 20);
        labelTranstainer.setBounds(20, 405, 100, 20);
        labelNumeroPosicoesCargaDescargaEstacao.setBounds(250,405,250,20);        
        textNumeroPosicoesCargaDescargaEstacao.setBounds(500, 405, 50, 20);
        labelTranstainerIntervalo.setBounds(250, 440, 300, 20);
        textITranstainerIntervalo.setBounds(500, 440, 50, 20);
        textFTranstainerIntervalo.setBounds(550, 440, 50, 20);        
        labelPosicaoCargaDescarga.setBounds(20, 475, 200, 20);
        labelNumeroCaminhoes.setBounds(250, 475, 200, 20);
        textNumeroCaminhoes.setBounds(500, 475, 50, 20);
        
        labelBerco.setBounds(720, 370, 200, 20);
        labelPortainer.setBounds(720,405,100,20);
        labelNumeroPortainers.setBounds(950, 405, 200, 20);        
        textNumeroPortainers.setBounds(1200, 405, 50, 20);
        labelPortainerIntervalo.setBounds(950, 440, 300, 20);
        textIPortainerIntervalo.setBounds(1200, 440, 50, 20);
        textFPortainerIntervalo.setBounds(1250, 440, 50, 20);
        
        labelTempoSimulacao.setBounds(250, 10, 200, 20);
        textTempoSimulacao.setBounds(500, 10, 50, 20);        
        
        botao.setBounds(1100, 650, 100, 25);
        
        janela.add(labelPorto);        
        janela.add(labelTempoSimulacao);
        janela.add(textTempoSimulacao);
        
        janela.add(labelPatio);
        janela.add(labelGeradorCaminhoes);
        janela.add(labelGeradorCaminhoesIntervalo);
        janela.add(textIGeradorCaminhoesIntervalo);
        janela.add(textFGeradorCaminhoesIntervalo);
        janela.add(labelPorcentagemCarregar);
        janela.add(textPorcentagemCarregar);
        janela.add(labelPorcentagemDescarregar);
        janela.add(textPorcentagemDescarregar);
        janela.add(labelPorcentagemCarregarDescarregar);
        janela.add(labelRotaFilaCaminhoes);
        janela.add(labelRotaFilaCaminhoesIntervalo);
        janela.add(textIRotaFilaCaminhoesIntervalo);
        janela.add(textFRotaFilaCaminhoesIntervalo);
        janela.add(labelRotaFilaCaminhoesCapacidade);    
        janela.add(textCapacidadeRotaFilaCaminhoes);
        
        janela.add(labelCais);
        janela.add(labelGeradorNavios);
        janela.add(labelGeradorNaviosIntervalo);
        janela.add(textIGeradorNaviosIntervalo);
        janela.add(textFGeradorNaviosIntervalo);
        janela.add(labelGeradorNaviosIntervaloContainersDescarregar);
        janela.add(textIGeradorNaviosIntervaloContainersDescarregar);
        janela.add(textFGeradorNaviosIntervaloContainersDescarregar);
        janela.add(labelGeradorNaviosIntervaloContainersCarregar);
        janela.add(textIGeradorNaviosIntervaloContainersCarregar);
        janela.add(textFGeradorNaviosIntervaloContainersCarregar);
        janela.add(labelPorcentagemContainersOutosNavios);
        janela.add(textIPorcentagemContainersOutosNavios);
                
        janela.add(labelRotaFilaNavios);
        janela.add(labelRotaFilaNaviosIntervalo);
        janela.add(textIRotaFilaNaviosIntervalo);
        janela.add(textFRotaFilaNaviosIntervalo);
        janela.add(labelRotaFilaNaviosCapacidade);    
        janela.add(textCapacidadeRotaFilaNavios);
        
        janela.add(labelPosicaoCargaDescarga);
        janela.add(labelTranstainer);
        janela.add(labelTranstainerIntervalo);
        janela.add(textITranstainerIntervalo);
        janela.add(textFTranstainerIntervalo);
        janela.add(labelEstacaoArmazenamento);
        janela.add(labelNumeroPosicoesCargaDescargaEstacao);
        janela.add(textNumeroPosicoesCargaDescargaEstacao);   
        janela.add(labelNumeroCaminhoes);
        janela.add(textNumeroCaminhoes);
        
        janela.add(labelBerco);
        janela.add(labelPortainer);
        janela.add(labelNumeroPortainers);
        janela.add(textNumeroPortainers);
        janela.add(labelPortainerIntervalo);
        janela.add(textIPortainerIntervalo);
        janela.add(textFPortainerIntervalo);

        botao.addActionListener(event);
        janela.add(botao);

        janela.setVisible(true);
    }

    private class Eventos implements ActionListener {

        public void actionPerformed(ActionEvent event) {

            try {
                File arquivo = new File("../arquivoEstatisticas.txt");

                if (arquivo.delete() == true) {
                    arquivo = new File("../arquivoEstatisticas.txt");
                }

                if (!arquivo.exists()) {
                    arquivo.createNewFile();
                }
                try (FileWriter fw = new FileWriter(arquivo, true)) {
                    BufferedWriter bw = new BufferedWriter(fw);

                    JSimSimulation simulation;

                    EstatisticasPorto estatisticas = new EstatisticasPorto();

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

                    int numeroBercos = 2;
                    int numeroPortainers = 2;//Integer.parseInt(textNumeroPortainers.getText());
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
                            new UniformDistributionStream(new JSimUniformStream(50, 50.1)), estatisticas);

                    estacaoArmazenamentoContainers = new EstacaoArmazenamento(simulation, "Estação de Armazenamento", 30, estatisticas);

                    for (int i = 0; i < numeroTranstainers; i++) {
                        posicaoCargaDescargaEstacaoExterna = new PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoExterno("Posição de Carga e Descarga Externa " + (i + 1) + " da Estação de Armazenamento", simulation);

                        posicaoCargaDescargaEstacaoInterna = new PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoInterno("Posição de Carga e Descarga Interna " + (i + 1) + " da Estação de Armazenamento", simulation);

                        transtainer = new Transtainer("Transtainer " + (i + 1), simulation, estacaoArmazenamentoContainers, posicaoCargaDescargaEstacaoExterna, posicaoCargaDescargaEstacaoInterna);

                        posicaoCargaDescargaEstacaoExterna.setTranstainer(transtainer);

                        posicaoCargaDescargaEstacaoInterna.setTranstainer(transtainer);

                        posicaoCargaDescargaEstacaoInterna.setFilaCaminhoesVazios(_filaCaminhoesPatioVazios);

                        rotaDecisaoPosicaoEstacaoArmazenamentoExt = new DecisaoPosicaoToEstacaoArmazenamentoRt("Rota da Decisão de Caminhões Externos para Posição Carga Descarga " + (i + 1), simulation,
                                1/*capacidade*/, new UniformDistributionStream(new JSimUniformStream(50, 50.1)));

                        rotaDecisaoPosicaoEstacaoArmazenamentoInt = new DecisaoPosicaoToEstacaoArmazenamentoIntRt("Rota da Decisão de Caminhões Internos para Posição Carga Descarga " + (i + 1), simulation,
                                1/*capacidade*/, new UniformDistributionStream(new JSimUniformStream(25, 25.1)));

                        rotaEntradaCaminhoes.addRotasEstacaoArmazenamento(rotaDecisaoPosicaoEstacaoArmazenamentoExt);

                        rotaDecisaoPosicaoEstacaoArmazenamentoExt.setPosicao(posicaoCargaDescargaEstacaoExterna);

                        _decisaoCaminhoesEstacaoArmazenamento.addListaRotasPosicoesEstacaoDestino(rotaDecisaoPosicaoEstacaoArmazenamentoInt);

                        rotaDecisaoPosicaoEstacaoArmazenamentoInt.setDecisaoToPosicoesEstacao(_decisaoCaminhoesEstacaoArmazenamento);

                        rotaDecisaoPosicaoEstacaoArmazenamentoInt.setPosicaoEstacaoInterna(posicaoCargaDescargaEstacaoInterna);

                        posicaoCargaDescargaEstacaoExterna.setRotaAtePosicao(rotaDecisaoPosicaoEstacaoArmazenamentoExt);

                        posicaoCargaDescargaEstacaoExterna.setRotaSaidaCaminhoes(rotaSaidaCaminhoes);

                        posicaoCargaDescargaEstacaoInterna.setRotaAtePosicao(rotaDecisaoPosicaoEstacaoArmazenamentoInt);

                        rotaPosicaoEstacaoInternaToDecisaoBerco = new PosicaoEstacaoToDecisaoPosicaoBercoRt("Rota da Posicao Interna " + (i + 1) + " da Estação até Decisão de Posções do Berço", simulation,
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
                            new UniformDistributionStream(new JSimUniformStream(1000, 1000.1)), filaNavios1);

                    pratico = new Pratico("Pratico", simulation);

                    rotaEntradaToPratico =
                            new FilaNaviosEntradaToPraticoRt("Rota da Fila de Entrada de Navios até o Prático", simulation,
                            1, filaNavios1, pratico, new UniformDistributionStream(new JSimUniformStream(50, 50.1)));

                    rotaSaidaNavios = new RotaSaidaNaviosRt("Rota de Saída de Navios do Porto", simulation, 1,
                            new UniformDistributionStream(new JSimUniformStream(50, 50.1)), estatisticas);

                    pratico.setRotaNavioPratico(rotaEntradaToPratico);

                    filaNavios1.setRotaEntradaPratico(rotaEntradaToPratico);

                    for (int i = 0; i < numeroBercos; i++) {
                        berco = new Berco(simulation, i + 1, _decisaoCaminhoesEstacaoArmazenamento, _decisaoCaminhoesBerco, estatisticas);

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

                            DecisaoPosicaoToPosicaoBercoRt rotaDecisaoToPosBerco = new DecisaoPosicaoToPosicaoBercoRt("Rota da Decisão de Caminhões do Pátio para Posicão de Carga e Descarga " + j + " do Berço " + berco.getName(), simulation, 1,
                                    new UniformDistributionStream(new JSimUniformStream(25, 25.1)));

                            PosicaoBercoToDecisaoPosicaoEstacaoRt rotaPosicaoBercoToDecisaoPosicaoEstacao = new PosicaoBercoToDecisaoPosicaoEstacaoRt("Rota da Posicão de Carga e Descarga " + j + " do Berço " + berco.getName() + " para Decisão de Posições da Estação", simulation, 1,
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
                    bw.write("\r\nNúmero de caminhões externos que já deixaram o porto: " + estatisticas.getQtdeCaminhoesExternosDeixaramPorto());
                    bw.write("\r\nTempo médio de permanência de caminhões externos que já deixaram o porto: " + Formatters.df.format(estatisticas.calcularTempoMedioCaminhoes()));
                    bw.write("\r\nNúmero de navios que já deixaram o porto: " + estatisticas.getQtdeNaviosDeixaramPorto());
                    bw.write("\r\nTempo médio de permanência de navios que já deixaram o porto: " + Formatters.df.format(estatisticas.calcularTempoMedioNavios()));
                    bw.write("\r\nNúmero movimentações de containers no berço: " + estatisticas.getQtdeContainersMovimentadaBerco());
                    bw.write("\r\nNúmero movimentações de containers na estação: " + estatisticas.getQtdeContainersMovimentadaEstacao());
                    bw.write("\r\nTaxa de ocupação do berço: " + Formatters.df.format((estatisticas.calcularTaxaOcupacaoBerco(simulation.getCurrentTime())) * 100) + "%");

                    simulation.shutdown();
                    bw.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            } catch (JSimException e) {
                e.printComment(System.err.append("erro"));
            }

            JOptionPane.showMessageDialog(null, "Simulação concluída");
            System.exit(0);
        }
    }

    public static void main(String[] args) throws JSimInvalidParametersException, JSimMethodNotSupportedException {
        new Simulador();
    }
}
