/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package negocio;

import java.io.File;
import java.io.IOException;
import shipyard.land.staticplace.EstacaoArmazenamento;

/**
 *
 * @author Eduardo
 */
public class EstacaoArmazenamentoBusiness {
    
    private EstacaoArmazenamento _estacaoArmazenamento;
    
    public EstacaoArmazenamentoBusiness(EstacaoArmazenamento estacao){
        _estacaoArmazenamento = estacao;
        
        criarArquivo(""+_estacaoArmazenamento.getName());
    }

    private void criarArquivo(String name) {
        this._estacaoArmazenamento.setArquivo(new File("../arquivo Navios Berço " + name + ".txt"));

        if (!this._estacaoArmazenamento.getArquivo().exists()) {
            try {
                this._estacaoArmazenamento.getArquivo().createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    private void escreverArquivo() {
        try {
            this._estacaoArmazenamento.getBw().write("\r\n");
            this._estacaoArmazenamento.getBw().flush();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    public void closeBw() throws IOException {
        this._estacaoArmazenamento.getBw().close();
    }    
}

