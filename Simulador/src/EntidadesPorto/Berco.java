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
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eduardo
 */
public class Berco extends JSimProcess{
    
    public int IdBerco;
    public Navio IdNavioAtracado;
    
    private double mu;
    private double p;
    private FilaNavios queueIn;
    private FilaNavios queueOut;

    private int counter;
    private double transTq;
    
    private File arquivo;
    private FileWriter fw;
    private BufferedWriter bw;
    
    private DecimalFormat df = new DecimalFormat("#0.##");   
    
    public Berco(String name, JSimSimulation sim, double parMu, double parP, FilaNavios parQueueIn, FilaNavios parQueueOut)
            throws JSimSimulationAlreadyTerminatedException, JSimInvalidParametersException, JSimTooManyProcessesException, IOException
	{
            super(name, sim);
            mu = parMu;
            p = parP;
            queueIn = parQueueIn;
            queueOut = parQueueOut;

            counter = 0;
            transTq = 0.0;
            
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
    
    protected void life()
	{
            Navio n;
            JSimLink link;

            try
            {
                while (true)
                {
                    if (queueIn.empty())
                    {
                        // If we have nothing to do, we sleep.
                        passivate();
                    }
                    else
                    {
                        // Simulating hard work here...
                        hold(JSimSystem.uniform(0, 10));
                        link = queueIn.first();

                        // Now we must decide whether to throw the transaction away or to insert it into another queue.
                        if (JSimSystem.uniform(0.0, 1.0) > p)
                        {
                            n = (Navio) link.getData();
                            counter++;
                            transTq += myParent.getCurrentTime() - n.getCreationTime();
                             try {
                                bw.write("\r\nNavio " + n.idNavio + " Criado no momento " + df.format(n.getCreationTime()) +
                                        " e colocado na fila " + queueIn.getHeadName() +
                                        " no momento " + df.format(link.getEnterTime()) + "\r\n");
                            } catch (IOException ex) {
                                Logger.getLogger(GeradorNavios.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            link.out();                           
                            link = null;                           
                        }
                        else
                        {
                            link.out();
                            link.into(queueOut);
                            if (queueOut.getBerco().isIdle())
                            {
                                queueOut.getBerco().activate(myParent.getCurrentTime());
                            }
                        } // else throw away / insert
                    } // else queue is empty / not empty
                } // while                
            } // try
            catch (JSimException e)
            {
                    e.printStackTrace();
                    e.printComment(System.err);
            }
	} // life

	public int getCounter()
	{
            return counter;
	} // getCounter

	public double getTransTq()
	{
            return transTq;
	} // getTransTq
        
        public void closeBw() throws IOException
        {
            bw.close();
        }
    
}
