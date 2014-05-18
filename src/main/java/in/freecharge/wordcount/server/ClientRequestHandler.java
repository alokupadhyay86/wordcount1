package in.freecharge.wordcount.server;

import in.freecharge.wordcount.server.FCServlet.FCRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Singleton class responsible for handling Client requests. 
 * 
 * Maintains all present "queried words & client requests" and serve them optimally
 * 
 * @author alok
 *
 */
public class ClientRequestHandler
{
    /**
     * Maintaining list of client request, so if same word is requested by multiple clients, one search is sufficient 
     */
    private final Map<String, List<FCRequest>> _wordToFCRequestList = new HashMap<String, List<FCRequest>>();
    
    /**
     * Queue to hold all presently requested words before a search is launched
     */
    BlockingQueue<String> _presentlyRequestedWordsQueue = new ArrayBlockingQueue<String>(1024);
    
    private static ClientRequestHandler _instance = new ClientRequestHandler();
    
    private ClientRequestHandler()
    {
        
    }
    
    public static ClientRequestHandler getInstance()
    {
        return _instance;
    }
    
    public void initialize()
    {
        SearchWorker worker = new SearchWorker();
        worker.start();
    }
    
    /**
     * Adds the request to _wordToFCRequestList, and queried word to _queue
     * 
     * @param request
     */
    synchronized public void handleRequest(FCRequest request)
    {
        if (!_wordToFCRequestList.containsKey(request.getWord()))
        {
            _wordToFCRequestList.put(request.getWord(), new ArrayList<FCServlet.FCRequest>());
            try
            {
                _presentlyRequestedWordsQueue.put(request.getWord());
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        
        _wordToFCRequestList.get(request.getWord()).add(request);
        
    }
    
    /**
     * Worker to get wordCount for a wordList supplied.
     * 
     * Reads the BlockingQueue of presently requested words. Gets the wordCount for these words. Iterates on _wordToFCRequestList to inform the wordCount to all requesting clients
     * 
     * @author alok
     *
     */
    private class SearchWorker extends Thread
    {
        /**
         * Drains the queue and launches search for all the words. 
         * TODO - If corpus is huge and search takes time, can consider waiting for queueSize to become big enough (like min 10 words) before it launches a search 
         */
        public void run()
        {
            while(true)
            {
                List<String> wordList = new ArrayList<String>();
                String word;
                try
                {
                    word = _presentlyRequestedWordsQueue.take();
                    _presentlyRequestedWordsQueue.drainTo(wordList);
                    wordList.add(word);
                }
                catch (InterruptedException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                
                if (!wordList.isEmpty())
                {
                    WordCount wc = new WordCount(wordList);
                    Map<String, Integer> wordToCountMap = wc.getCount();
                    
                    synchronized (_instance)
                    {
                        for (String resultWord : wordToCountMap.keySet())
                        {
                            for (FCRequest request :  _wordToFCRequestList.get(resultWord))
                            {    
                                request.sendResponse(wordToCountMap.get(resultWord));    
                            }
                        }
                        
                        for (String resultWord : wordToCountMap.keySet())
                        {
                            _wordToFCRequestList.remove(resultWord);
                        }
                    }
                }
            }
        }
        
        
    }



}
