/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Negocio;

import cz.zcu.fav.kiv.jsim.JSimException;
import cz.zcu.fav.kiv.jsim.JSimSystem;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import shipyard.land.move.Portainer;
import utils.Formatters;

/**
 *
 * @author Eduardo
 */
public class PortainerBusiness {

    private Portainer _portainer;

    public PortainerBusiness(Portainer portainer) {

        this._portainer = portainer;

    }

    public void life() {
        
        _portainer.setQueueIn(_portainer.getFilasContainers().get(0));
        _portainer.getQueueIn().setHoraFinalAtendimento(0);
        _portainer.setNumeroContainersDescarregar(_portainer.getQueueIn().getNumeroContainers());
        
        criarArquivo();
        
        try {            
            while (true) {
               
                if (_portainer.getQueueIn().empty()) {
                    // If we have nothing to do, we sleep.
                    _portainer.passivo();
                    _portainer.getPosicaoCargaDescarga().setFila();
                }                 
                else {
                    if (_portainer.getPosicaoCargaDescarga().isIdle()&&_portainer.getPosicaoCargaDescarga().getCaminhao() == null) {                        
                        _portainer.getPosicaoCargaDescarga().activate(_portainer.getSimulation().getCurrentTime());                        
                    }

                    if (_portainer.getPosicaoCargaDescarga().getCaminhao() == null) {                        
                        _portainer.passivo();                        
                    } 
                    else {
                        _portainer.getNextContainer();
                        _portainer.setHoraMovimentacao(_portainer.getSimulation().getCurrentTime());
                        
                        _portainer.segurar(JSimSystem.uniform(10, 10));                        
                        
                        _portainer.getPosicaoCargaDescarga().getCaminhao().setContainer(_portainer.getContainer());
                        _portainer.getPosicaoCargaDescarga().getCaminhao().setHoraRecebimentoContainer(_portainer.getSimulation().getCurrentTime());
                        _portainer.getPosicaoCargaDescarga().activate(_portainer.getSimulation().getCurrentTime());
                        
                        _portainer.passivo();

                        //hold(JSimSystem.uniform(10, 10));

                        // Now we must decide whether to throw the transaction away or to insert it into another queue.
                        if (JSimSystem.uniform(0.0, 1.0) > _portainer.getP() || _portainer.getQueueOut() == null) {
                            
                            _portainer.setCounter(_portainer.getCounter() + 1);
                            _portainer.setHoraSaidaContainer(_portainer.getSimulation().getCurrentTime());
                            _portainer.setTempoTotalAtendimento(_portainer.getHoraSaidaContainer() - _portainer.getHoraMovimentacao());
                            _portainer.setTransTq(_portainer.getHoraSaidaContainer() - _portainer.getContainer().getTimeOfCreation());
                            
                            try {
                                
                                escreverArquivo();
                                
                            } catch (IOException ex) {
                                Logger.getLogger(Portainer.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            
                            _portainer.getContainer().out();
                            
                            if (_portainer.getQueueIn().empty()) {                                
                                _portainer.getQueueIn().setHoraFinalAtendimento(_portainer.getHoraSaidaContainer());
                                _portainer.getFilasContainers().remove(_portainer.getQueueIn());                                
                                try {                                    
                                    closeBw();                                    
                                } catch (IOException ex) {
                                    Logger.getLogger(Portainer.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                
                                if (_portainer.getFilasContainers().isEmpty()) {                                    
                                    if (_portainer.getBerco().isIdle()) {                                        
                                        _portainer.getBerco().activate(_portainer.getSimulation().getCurrentTime());                                        
                                    }                                    
                                } 
                                else {                                    
                                    _portainer.setQueueIn(_portainer.getFilasContainers().get(0));
                                    _portainer.setNumeroContainersDescarregar(_portainer.getQueueIn().getNumeroContainers());
                                    _portainer.getPosicaoCargaDescarga().setFila();
                                    criarArquivo();                                    
                                }
                            }
                            _portainer.setContainer(null);
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
            e.printStackTrace(System.err);
        }
    } // life   

    public void closeBw() throws IOException {
        _portainer.getBw().close();
    }

    public void criarArquivo() {
        _portainer.setArquivo(new File("../arquivoContainers" + _portainer.getNomePortainer() + " " + _portainer.getQueueIn().getNomeFila() + ".txt"));

        if (!this._portainer.getArquivo().exists()) {
            try {
                this._portainer.getArquivo().createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    public void escreverArquivo() throws IOException {
        try {
            _portainer.getBw().write("\r\nContainer " + _portainer.getContainer().getId() + " Criado no momento " + Formatters.df.format(_portainer.getContainer().getCreationTime())
                    + "\r\n colocado na fila " + _portainer.getQueueIn().getNomeFila()
                    + "\r\n ficando na fila por " + Formatters.df.format(_portainer.getHoraMovimentacao() - _portainer.getContainer().getEnterTime())
                    + "\r\n iniciando a movimentação no momento " + Formatters.df.format(_portainer.getHoraMovimentacao())
                    + "\r\n e finalizando a movimentação no momento " + Formatters.df.format(_portainer.getHoraSaidaContainer())
                    + "\r\n movimentando por " + _portainer.getTempoTotalAtendimento() + " \r\n");
        } catch (IOException ex) {
            criarArquivo();
            _portainer.getBw().write("\r\nContainer " + _portainer.getContainer().getId() + " Criado no momento " + Formatters.df.format(_portainer.getContainer().getCreationTime())
                    + "\r\n colocado na fila " + _portainer.getQueueIn().getNomeFila()
                    + "\r\n ficando na fila por " + Formatters.df.format(_portainer.getHoraMovimentacao() - _portainer.getContainer().getEnterTime())
                    + "\r\n iniciando a movimentação no momento " + Formatters.df.format(_portainer.getHoraMovimentacao())
                    + "\r\n e finalizando a movimentação no momento " + Formatters.df.format(_portainer.getHoraSaidaContainer())
                    + "\r\n movimentando por " + _portainer.getTempoTotalAtendimento() + " \r\n");
        }
    }
}
