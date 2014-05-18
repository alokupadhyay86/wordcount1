package in.freecharge.wordcount.server;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHttpContext;

/**
 * HttpServer : Listen to client request on supplied port. Will search for words in the CORPUS_DIRECTORY_PATH
 * 
 * @author alok
 *
 */
public class FCHttpServer 
{
    /**
     * Number of threads depends on Server resources so makes sense to keep in Server object, independent of the no. of clients/requests 
     */
    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(10);
    
    public static String CORPUS_DIRECTORY_PATH;
    
    private static int _port;
    
    public static void main(String[] args) 
    {
        initialize(args);
        
        try 
        {            
            
            Server server = new Server();
            SocketListener listener = new SocketListener();      

            listener.setHost("localhost");
            listener.setPort(_port);
            listener.setMinThreads(5);
            listener.setMaxThreads(250);
            server.addListener(listener);            

            ServletHttpContext context = (ServletHttpContext) server.getContext("/");
            context.addServlet("/", "in.freecharge.wordcount.server.FCServlet");

            server.start();
            server.join();

        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        finally
        {
            EXECUTOR.shutdown();
        }
    }
    
    private static void initialize(String[] args)
    {
        System.out.println("args = " + args[0] + " " + args[1]);
        if (args.length != 2)
        {
            System.out.println("Please provide required Args : <port> <corpusDirectroyPath>");
            System.exit(1);
        }
        
        try
        {
            _port = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e)
        {
            System.out.println("Please enter valid integer for <port>");
            System.exit(1);
        }
        
        CORPUS_DIRECTORY_PATH = args[1];
        if (!new File(CORPUS_DIRECTORY_PATH).isDirectory())
        {
            System.out.println("Please enter a valid directory path for <corpusDirectroyPath>");
            System.exit(1);
        }
        
        ClientRequestHandler.getInstance().initialize();
    }

} 


