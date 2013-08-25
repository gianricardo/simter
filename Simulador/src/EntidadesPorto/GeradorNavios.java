/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package EntidadesPorto;

import cz.zcu.fav.kiv.jsim.JSimException;
import cz.zcu.fav.kiv.jsim.JSimInvalidParametersException;
import cz.zcu.fav.kiv.jsim.JSimLink;
import cz.zcu.fav.kiv.jsim.JSimProcess;
import cz.zcu.fav.kiv.jsim.JSimSimulation;
import cz.zcu.fav.kiv.jsim.JSimSimulationAlreadyTerminatedException;
import cz.zcu.fav.kiv.jsim.JSimSystem;
import cz.zcu.fav.kiv.jsim.JSimTooManyProcessesException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eduardo
 */
public class GeradorNavios extends JSimProcess {
    private double lambda;
    private FilaNavios queue;
    private int numeroNavio = 1;
    private File arquivo;
    private FileWriter fw;
    private BufferedWriter bw;

	public GeradorNavios(String name, JSimSimulation sim, double l, FilaNavios q)
                throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException
	{
            super(name, sim);
            lambda = l;
            queue = q;
            
            arquivo = new File("../arquivoNavios"+ name +".txt");
            
            if(arquivo.delete()==true){            
                arquivo = new File("../arquivoNavios"+ name +".txt");
            }
            
            if(!arquivo.exists())
            {
                arquivo.createNewFile();
            }
            fw = new FileWriter(arquivo, true);
            bw = new BufferedWriter(fw);
            
	} // constructor

    @Override
	protected void life()
	{
            JSimLink link;
            try
            {
                while (true)
                {                    
                    // Periodically creating new navios and putting them into the queue.
                    link = new JSimLink(new Navio(myParent.getCurrentTime()));                    
                    link.into(queue);
                    if (queue.getBerco().isIdle())
                    {
                        queue.getBerco().activate(myParent.getCurrentTime());
                    }
                    try {
                        bw.write("\r\nNavio " + numeroNavio + " Criado e colocado na fila " + queue.getHeadName() + " no momento " + link.getEnterTime() +"\r\n");
                    } catch (IOException ex) {
                        Logger.getLogger(GeradorNavios.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    hold(JSimSystem.negExp(lambda));
                    numeroNavio++ ;
                } // while
            } // try
            catch (JSimException e)
            {
                e.printStackTrace();
                e.printComment(System.err);
            } // catch
	} // life
}
