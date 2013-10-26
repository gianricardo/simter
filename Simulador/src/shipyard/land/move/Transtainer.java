/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shipyard.land.move;

import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimProcess;
import cz.zcu.fav.kiv.jsim.JSimSecurityException;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimSimulationAlreadyTerminatedException;
import cz.zcu.fav.kiv.jsim.JSimTooManyProcessesException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import negocio.TranstainerBusiness;
import shipyard.land.staticplace.EstacaoArmazenamento;
import shipyard.land.staticplace.PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoExterno;
import shipyard.land.staticplace.PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoInterno;

/**
 *
 * @author Eduardo
 */
public class Transtainer extends JSimProcess {
    
    private File _arquivo;
    private FileWriter _fw;
    private BufferedWriter _bw;
    
    private String _nomeTranstainer;
    private EstacaoArmazenamento _estacaoArmazenamento;
    private JSimSimulation _simulation;    
    private TranstainerBusiness _transtainerNegocio;
    private PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoExterno _posicaoCargaDescargaExterna;
    private PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoInterno _posicaoCargaDescargaInterna;

    //CaminhoesPatio
    //BercosAtende
    //IdentificadoresNavios
    public Transtainer(String name, JSimSimulation sim, EstacaoArmazenamento estacaoArmazenamento, PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoExterno posicaoExterna, PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoInterno posicaoInterna)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException {
        super(name, sim);        
        this._nomeTranstainer = name;
        this._estacaoArmazenamento = estacaoArmazenamento;
        this._simulation = sim;
        this._posicaoCargaDescargaExterna = posicaoExterna;
        this._posicaoCargaDescargaInterna = posicaoInterna;
        
        this._transtainerNegocio = new TranstainerBusiness(this);
    } // constructor 

    public PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoExterno getPosicaoCargaDescarga() {
        return _posicaoCargaDescargaExterna;
    }

    public PosicaoCargaDescargaEstacaoArmazenamentoCaminhaoInterno getPosicaoCargaDescargaInterna() {
        return _posicaoCargaDescargaInterna;
    }
    
    public void passivo() {
        try {
            this.passivate();
        } catch (JSimSecurityException ex) {
            ex.printStackTrace(System.err);
        }
    }
    
    public void segurar(double tempo) throws JSimInvalidParametersException {
        try {
            this.hold(tempo);
        } catch (JSimSecurityException ex) {
            ex.printStackTrace(System.err);
        }
    }

    public EstacaoArmazenamento getEstacaoArmazenamento() {
        return _estacaoArmazenamento;
    }

    public JSimSimulation getSimulation() {
        return _simulation;
    }
    
    @Override
    protected void life() {
        this._transtainerNegocio.life();
    }
    
}
