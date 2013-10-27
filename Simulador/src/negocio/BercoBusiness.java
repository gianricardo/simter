/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package negocio;

import cz.zcu.fav.kiv.jsim.JSimException;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimSecurityException;
import cz.zcu.fav.kiv.jsim.JSimSystem;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import shipyard.land.move.Portainer;
import shipyard.land.staticplace.Berco;
import shipyard.sea.Navio;
import simulador.queues.FilaContainers;
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
                                        
                    _berco.setHoraAtracacao(_berco.getSimulation().getCurrentTime());                    
                    
                    navio.escreverArquivo("\r\n iniciando as atividades no berço " + _berco.getName() +" no momento " + _berco.getSimulation().getCurrentTime());

                    updateFilasPortainers();

                    verificaFinalizacaoAtendimentoNavio();

                    // Now we must decide whether to throw the transaction away or to insert it into another queue.
                    if (JSimSystem.uniform(0.0, 1.0) > _berco.getP() || _berco.getQueueOut() == null) {                       
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

    /*public double verificarHoraSaidaNavio(List<FilaContainers> lista) {
        double tempoPortainers = 0;
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getHoraFinalAtendimento() > tempoPortainers) {
                tempoPortainers = lista.get(i).getHoraFinalAtendimento();
            }
        }
        return tempoPortainers;
    }*/

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
                + "\r\n -Tempo de movimentação " + Formatters.df.format((this._berco.getHoraAtracacao()) - this._berco.getShip().getEnterTime())
                + "\r\n -Hora de Atracação " + Formatters.df.format(this._berco.getHoraAtracacao())
                + "\r\n -Tempo de Atendimento pelo Portainer " + this._berco.getTempoAtendimentoPortainers()
                + "\r\n -Hora de Saída do Berço " + Formatters.df.format(this._berco.getSimulation().getCurrentTime()) + " \r\n");
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
        
        for (int portainer = 0; portainer < _berco.getListaPortainers().size(); portainer++) {
            _berco.getListaPortainers().get(portainer).setMovimentacaoFinalizada(false);
            for (nfila = auxFila; nfila < numeroFilasPortainer + auxFila; nfila++) {
                filaBerco = (FilaContainers) _berco.getShip().getFilasContainers().get(nfila);                

                portainerBerco = _berco.getListaPortainers().get(portainer);

                portainerBerco.setFilas(filaBerco, null);
                
                portainerBerco.setMovimentacaoFinalizada(false);
                
                portainerBerco.criarSolicitacaoCaminhoes();

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

                    if (portainerBerco.isIdle()) {
                        portainerBerco.activate(this._berco.getSimulation().getCurrentTime());
                    }
                }
            }
            auxFila = nfila;
        }
    }

    private void verificaFinalizacaoAtendimentoNavio() throws JSimSecurityException {
        
        while (true) {
            
            FilaContainers fila;
            boolean finalizado = true;
            
            for (int i = 0; i < _berco.getListaPortainers().size()/*getShip().getFilasContainers().size()*/; i++) {
                /*fila = (FilaContainers) _berco.getShip().getFilasContainers().get(i);
                
                if(fila.getNumeroContainers() != 0){
                    finalizado = false;
                }*/
                
                if(!_berco.getListaPortainers().get(i).isMovimentacaoFinalizada()){
                    finalizado = false;
                    break;
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
