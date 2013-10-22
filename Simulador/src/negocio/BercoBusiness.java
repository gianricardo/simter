/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package negocio;

import cz.zcu.fav.kiv.jsim.JSimException;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimSecurityException;
import cz.zcu.fav.kiv.jsim.JSimSimulationAlreadyTerminatedException;
import cz.zcu.fav.kiv.jsim.JSimSystem;
import cz.zcu.fav.kiv.jsim.JSimTooManyProcessesException;
import cz.zcu.fav.kiv.jsim.random.JSimUniformStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import shipyard.land.move.Portainer;
import shipyard.land.staticplace.Berco;
import shipyard.land.staticplace.PosicaoCargaDescargaBerco;
import shipyard.sea.Navio;
import simulador.queues.FilaContainers;
import simulador.random.UniformDistributionStream;
import simulador.rotas.DecisaoPosicaoToPosicaoBercoRt;
import simulador.rotas.PosicaoBercoToDecisaoPosicaoEstacaoRt;
import utils.Formatters;

/**
 *
 * @author Eduardo
 */
public class BercoBusiness {

    private Berco _berco;

    public BercoBusiness(Berco berco) {

        this._berco = berco;

        criarArquivo(""+_berco.getIdBerco());

        criarProcessos(_berco.getNumeroPortainers());

    } // constructor

    public void life() {
        try {
            while (true) {
                if (_berco.getShip() == null) {
                    // If we have nothing to do, we sleep.
                    _berco.passivo();
                } else {
                    //Simulating hard work here...
                    //Tempo de atendimento no BERÇO = SOMA DE TODOS OS CARREGAMENTOS E DESCARREGAMENTOS DE CONTAINERS
                    Navio navio;
                    navio = _berco.getShip();                    

                    _berco.setHoraInicioMovimentacao(_berco.getSimulation().getCurrentTime());
                    //hold(JSimSystem.uniform(1, 1));                    
                    _berco.setHoraAtracacao(_berco.getSimulation().getCurrentTime());
                    _berco.setTempoMovimentacao(_berco.getHoraAtracacao() - _berco.getHoraInicioMovimentacao());
                    
                    navio.escreverArquivo("\r\n iniciando as atividades no berço " + _berco.getName() +" no momento " + _berco.getSimulation().getCurrentTime());

                    updateFilasPortainers();

                    verificaFinalizacaoAtendimentoNavio();

                    // Now we must decide whether to throw the transaction away or to insert it into another queue.
                    if (JSimSystem.uniform(0.0, 1.0) > _berco.getP() || _berco.getQueueOut() == null) {
                        _berco.setCounter(_berco.getCounter() + 1);
                        _berco.setHoraSaida(_berco.getTempoMovimentacao() + _berco.getTempoAtendimentoPortainers() + _berco.getHoraInicioMovimentacao());
                        _berco.setTransTq(_berco.getTransTq() + _berco.getHoraSaida() - _berco.getShip().getCreationTime());

                        escreverArquivo();
                        
                        while(true)
                        {
                            if(!finalizarAtendimentoNavio()){
                                _berco.passivo();
                            }
                            else{
                                break;
                            }
                        }
                        
                    }
                } // else queue is empty / not empty
            } // while                
        } // try
        catch (JSimException e) {
            e.printStackTrace(System.err);
        }

    } // life

    public double verificarHoraSaidaNavio(List<FilaContainers> lista) {
        double tempoPortainers = 0;
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getHoraFinalAtendimento() > tempoPortainers) {
                tempoPortainers = lista.get(i).getHoraFinalAtendimento();
            }
        }
        return tempoPortainers;
    }

    private void criarProcessos(int NumeroPortainers) {        
        for (int i = 0; i < NumeroPortainers; i++) {
            try {
                PosicaoCargaDescargaBerco posicaoCargaDescarga = new PosicaoCargaDescargaBerco("Posicao " + i, this._berco.getSimulation(), null);
                
                DecisaoPosicaoToPosicaoBercoRt rotaDecisaoToPosBerco = new DecisaoPosicaoToPosicaoBercoRt("Rota Decisao para Posicao Carga Descarga Berço", _berco.getSimulation(), 1,
                                                                    new UniformDistributionStream(new JSimUniformStream(10, 10.1)));
                
                PosicaoBercoToDecisaoPosicaoEstacaoRt rotaPosicaoBercoToDecisaoPosicaoEstacao = new PosicaoBercoToDecisaoPosicaoEstacaoRt("Rota posicao carga descarga berco " + i + " para Decisao Estacao", _berco.getSimulation(), 1,
                                                                    new UniformDistributionStream(new JSimUniformStream(10, 10.1)));

                posicaoCargaDescarga.setRotaDecisaoPosicaoCargaDescargaBerco(rotaDecisaoToPosBerco);
                
                posicaoCargaDescarga.setRotaPosicaoDecisaoPosicaoEstacao(rotaPosicaoBercoToDecisaoPosicaoEstacao);
                
                rotaDecisaoToPosBerco.setPosicaoBerco(posicaoCargaDescarga);
                
                rotaPosicaoBercoToDecisaoPosicaoEstacao.setPosicaoBerco(posicaoCargaDescarga);
                
                rotaPosicaoBercoToDecisaoPosicaoEstacao.setDecisaoPosicaoEstacao(_berco.getDecisaoCaminhoesPatioEstacao());
                
                _berco.getDecisaoCaminhoesPatioEstacao().addListaRotasPosicaoBercoOrigem(rotaPosicaoBercoToDecisaoPosicaoEstacao);
                
                rotaDecisaoToPosBerco.setDecisaoPosicoesBerco(_berco.getDecisaoCaminhoesPatioBerco());                
                
                Portainer portainerBerco = new Portainer("Portainer " + i, _berco.getSimulation(), 0, 0, this._berco, posicaoCargaDescarga);
                
                portainerBerco.setDecisaoSolicitacoes(_berco.getDecisaoCaminhoesPatioEstacao());

                posicaoCargaDescarga.setPortainer(portainerBerco);                
                

                this._berco.getListaPortainers().add(portainerBerco);
                this._berco.getListaPosicoes().add(posicaoCargaDescarga);
            } catch (    JSimSimulationAlreadyTerminatedException | JSimInvalidParametersException | JSimTooManyProcessesException | IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    private void criarArquivo(String name) {
        this._berco.setArquivo(new File("../arquivo Navios Berço " + name + ".txt"));

        if (!this._berco.getArquivo().exists()) {
            try {
                this._berco.getArquivo().createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    private void escreverArquivo() {
        try {
            this._berco.getBw().write("\r\nNavio " + this._berco.getShip().getIdNavio() 
                + ":\r\n -Criado no momento " + Formatters.df.format(this._berco.getShip().getCreationTime())
                + "\r\n -" + this._berco.getShip().getNumeroContainersDescarregar() + " Containers a descarregar"
                + "\r\n -Tempo de espera na fila " + Formatters.df.format((this._berco.getHoraAtracacao() - this._berco.getTempoMovimentacao()) - this._berco.getShip().getEnterTime())
                + "\r\n -Hora Inicio Movimentacao até o Berco " + Formatters.df.format(this._berco.getHoraInicioMovimentacao())
                + "\r\n -Hora de Atracação " + Formatters.df.format(this._berco.getHoraAtracacao())
                + "\r\n -Tempo de Movimentacao da Fila até o Berço " + Formatters.df.format(this._berco.getTempoMovimentacao())
                + "\r\n -Hora de Saída do Porto " + Formatters.df.format(this._berco.getHoraSaida())
                + "\r\n -Tempo de Atendimento pelo Portainer " + this._berco.getTempoAtendimentoPortainers() + " \r\n");
            this._berco.getBw().flush();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    public void closeBw() throws IOException {
        this._berco.getBw().close();
    }

    private void updateFilasPortainers() throws JSimSecurityException, JSimInvalidParametersException {
        int numeroFilasPortainer = Math.round(this._berco.getShip().getFilasContainers().size() / this._berco.getListaPortainers().size());
        int auxFila = 0;
        int nfila;
        FilaContainers filaBerco; 
        Portainer portainerBerco;
        
        for (int portainer = 0; portainer < _berco.getNumeroPortainers(); portainer++) {
            for (nfila = auxFila; nfila < numeroFilasPortainer + auxFila; nfila++) {
                filaBerco = (FilaContainers) _berco.getShip().getFilasContainers().get(nfila);                

                portainerBerco = _berco.getListaPortainers().get(portainer);

                portainerBerco.setFilas(filaBerco, null);
                
                //filaBerco.setPortainer(portainerBerco);

                if (portainerBerco.isIdle()) {
                    portainerBerco.activate(this._berco.getSimulation().getCurrentTime());
                }
            }
 
            if (portainer == _berco.getListaPortainers().size() - 1) {
                int auxiliarResto = _berco.getShip().getFilasContainers().size() - nfila;
                for (int z = nfila; z < nfila + auxiliarResto; z++) {
                    filaBerco = (FilaContainers) _berco.getShip().getFilasContainers().get(z);

                    portainerBerco = _berco.getListaPortainers().get(portainer);

                    portainerBerco.setFilas(filaBerco, null);

                    //filaBerco.setPortainer(portainerBerco);

                    if (portainerBerco.isIdle()) {
                        portainerBerco.activate(this._berco.getSimulation().getCurrentTime());
                    }
                }
            }            
            //this._berco.getListaPortainers().add(portainerBerco);
            auxFila = nfila;
        }
    }

    private void verificaFinalizacaoAtendimentoNavio() throws JSimSecurityException {
        
        while (true) {
            
            FilaContainers fila;
            boolean finalizado = true;
            
            for (int i = 0; i < _berco.getShip().getFilasContainers().size(); i++) {
                fila = (FilaContainers) _berco.getShip().getFilasContainers().get(i);
                
                if(fila.getNumeroContainers() != 0){
                    finalizado = false;
                }
            }           
            
            this._berco.setTempoAtendimentoPortainers(0);
            
            if (!finalizado) {
                // If we have nothing to do, we sleep.
                this._berco.passivo();
            } else {                
                double[] ArrayHoraFimAten = new double[_berco.getShip().getFilasContainers().size()];
                double[] ArrayHoraIniAten = new double[_berco.getShip().getFilasContainers().size()];
                for (int i = 0; i < _berco.getShip().getFilasContainers().size(); i++) {
                    fila = (FilaContainers) _berco.getShip().getFilasContainers().get(i);
                    ArrayHoraFimAten[i] = fila.getHoraFinalAtendimento();
                    ArrayHoraIniAten[i] = fila.getHoraInicioAtendimento();
                }
                
                double MaiorHoraFimAtendimento = 0;
                double MenorHoraInicioAtendimento = 0;
                for (int i = 0; i < _berco.getShip().getFilasContainers().size(); i++) {
                    if (ArrayHoraFimAten[i] > MaiorHoraFimAtendimento) {
                        MaiorHoraFimAtendimento = ArrayHoraFimAten[i];
                    }
                    if (ArrayHoraIniAten[i] > MenorHoraInicioAtendimento) {
                        MenorHoraInicioAtendimento = ArrayHoraIniAten[i];
                    }
                }

                this._berco.setTempoAtendimentoPortainers(MaiorHoraFimAtendimento - MenorHoraInicioAtendimento);
                break;
            }
        }
    }
    
    private boolean finalizarAtendimentoNavio(){
        if(_berco.getRotaBercoToSaida().GetNextElement(_berco.getShip())){
            try {
                if(_berco.getRotaBercoToSaida().isIdle()){
                    _berco.getRotaBercoToSaida().activate(_berco.getSimulation().getCurrentTime());                        
                }
                if(_berco.getRotaPraticoToBerco().isIdle()){
                    _berco.getRotaPraticoToBerco().activate(_berco.getSimulation().getCurrentTime());
                }
                _berco.setShip(null);
                _berco.setOcupado(false);
                return true;
            } catch (    JSimSecurityException | JSimInvalidParametersException ex) {
                Logger.getLogger(BercoBusiness.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    public int getCounter() {
        return _berco.getCounter();
    } // getCounter

    public double getTransTq() {
        return _berco.getTransTq();
    } // getTransTq    
}
