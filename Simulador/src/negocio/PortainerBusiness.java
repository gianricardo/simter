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
import utils.Formatters;
import utils.SolicitacaoCaminhoesPatio;

/**
 *
 * @author Eduardo
 */
public class PortainerBusiness {

    private Portainer _portainer;

    public PortainerBusiness(Portainer portainer) {

        this._portainer = portainer;

    }

    public void life() throws JSimSecurityException, JSimInvalidParametersException {

        _portainer.setQueueIn(_portainer.getFilasContainers().get(0));
        _portainer.getQueueIn().setHoraFinalAtendimento(0);
        _portainer.setNumeroContainersDescarregar(_portainer.getQueueIn().getNumeroContainers());
        
        SolicitacaoCaminhoesPatio solicitacao = new SolicitacaoCaminhoesPatio();
        solicitacao.setNumeroCaminhoesDescarregar(_portainer.getQueueIn().getNumeroContainers());
        solicitacao.setNumeroCaminhoesCarregar(_portainer.getQueueIn().getNumeroContainersCarregar());
        solicitacao.setRotaDecisaoPosicaoBerco(_portainer.getPosicaoCargaDescarga().getRotaDecisaoPosicaoCargaDescargaBerco());

        _portainer.getDecisaoSolicitacoes().AdicionarSolicitacao(solicitacao);
        
        if(_portainer.getDecisaoSolicitacoes().isIdle()){
            _portainer.getDecisaoSolicitacoes().activate(_portainer.getSimulation().getCurrentTime());
        }
        
        
        if (_portainer.getNumeroContainersDescarregar() > 0) {
            _portainer.setDescarregar(true);
        }

        criarArquivo();

        try {
            while (true) {
                descarregarFilaContainersNavio();

                //carregarFilaContainersNavio();

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
                } else {
                    _portainer.setQueueIn(_portainer.getFilasContainers().get(0));
                    
                    
                    solicitacao = new SolicitacaoCaminhoesPatio();
                    solicitacao.setNumeroCaminhoesDescarregar(_portainer.getQueueIn().getNumeroContainers());
                    solicitacao.setNumeroCaminhoesCarregar(_portainer.getQueueIn().getNumeroContainersCarregar());
                    solicitacao.setRotaDecisaoPosicaoBerco(_portainer.getPosicaoCargaDescarga().getRotaDecisaoPosicaoCargaDescargaBerco());

                    _portainer.getDecisaoSolicitacoes().AdicionarSolicitacao(solicitacao);                    
                    
                    _portainer.setNumeroContainersDescarregar(_portainer.getQueueIn().getNumeroContainers());
                    criarArquivo();
                }

                _portainer.getQueueIn().setHoraFinalAtendimento(_portainer.getHoraSaidaContainer());
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

    private void descarregarFilaContainersNavio() throws JSimSecurityException, JSimInvalidParametersException {
        while (true) {
            if (_portainer.getQueueIn().empty()) {
                _portainer.passivo();
            } else {
                if (_portainer.isDescarregar()) {
                    if (_portainer.getPosicaoCargaDescarga().isIdle() && _portainer.getPosicaoCargaDescarga().getCaminhao() == null) {
                        _portainer.getPosicaoCargaDescarga().activate(_portainer.getSimulation().getCurrentTime());
                    }
                }

                if (_portainer.getPosicaoCargaDescarga().getCaminhao() == null) {
                    _portainer.passivo();
                } else {
                    _portainer.getNextContainer();
                    _portainer.setHoraMovimentacao(_portainer.getSimulation().getCurrentTime());

                    _portainer.segurar(JSimSystem.uniform(10, 10));

                    _portainer.getPosicaoCargaDescarga().getCaminhao().setContainer(_portainer.getContainer());
                    _portainer.getPosicaoCargaDescarga().getCaminhao().setHoraRecebimentoContainer(_portainer.getSimulation().getCurrentTime());

                    if (_portainer.getPosicaoCargaDescarga().isIdle()) {
                        _portainer.getPosicaoCargaDescarga().activate(_portainer.getSimulation().getCurrentTime());
                    }

                    _portainer.passivo();
                    
                    _portainer.setHoraSaidaContainer(_portainer.getSimulation().getCurrentTime());
                    _portainer.setTempoTotalAtendimento(_portainer.getHoraSaidaContainer() - _portainer.getHoraMovimentacao());

                    try {
                        escreverArquivo();
                    } catch (IOException ex) {
                        Logger.getLogger(Portainer.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    _portainer.getContainer().out();
                    _portainer.getQueueIn().setNumeroContainers(_portainer.getQueueIn().getNumeroContainers() - 1);

                    if (_portainer.getQueueIn().empty()) {
                        _portainer.getQueueIn().setHoraFinalDescarregamento(_portainer.getHoraSaidaContainer());
                        break;
                    }
                }
            }
        }
    }

    private void carregarFilaContainersNavio() throws JSimSecurityException, JSimInvalidParametersException {
        while (true) {
            if (_portainer.getPosicaoCargaDescarga().getCaminhao() == null) {
                _portainer.passivo();
            } else {
                if (_portainer.isCarregar()) {
                    if (_portainer.getPosicaoCargaDescarga().isIdle() && _portainer.getPosicaoCargaDescarga().getCaminhao() == null) {
                        _portainer.getPosicaoCargaDescarga().activate(_portainer.getSimulation().getCurrentTime());
                    }
                }
                
                _portainer.setContainer(_portainer.getPosicaoCargaDescarga().getCaminhao().getContainer());
                _portainer.setHoraMovimentacao(_portainer.getSimulation().getCurrentTime());

                _portainer.segurar(JSimSystem.uniform(10, 10));

                _portainer.getPosicaoCargaDescarga().getCaminhao().setContainer(null);
                _portainer.getContainer().into(_portainer.getQueueIn());
                
                 _portainer.getQueueIn().setNumeroContainers(_portainer.getQueueIn().getNumeroContainers() + 1);

                if (_portainer.getQueueIn().getNumeroContainersCarregar() == _portainer.getQueueIn().getNumeroContainers()) {
                    _portainer.getQueueIn().setHoraFinalCarregamento(_portainer.getHoraSaidaContainer());
                    break;
                }

                if (_portainer.getPosicaoCargaDescarga().isIdle()) {
                    _portainer.getPosicaoCargaDescarga().activate(_portainer.getSimulation().getCurrentTime());
                }

                _portainer.passivo();
               
                try {
                    escreverArquivo();
                } catch (IOException ex) {
                    Logger.getLogger(Portainer.class.getName()).log(Level.SEVERE, null, ex);
                }               
            }
        }
    }
}
