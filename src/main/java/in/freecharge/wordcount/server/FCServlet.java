package in.freecharge.wordcount.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

public class FCServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
    {
        String word = httpServletRequest.getParameter("query");
        
        System.out.println("Searching Word : " + word);

        FCRequest request = new FCRequest(word, httpServletResponse);

        ClientRequestHandler.getInstance().handleRequest(request);
        
        //Need to wait, otherwise the _httpServletResponse gets expired
        synchronized(request)
        {
            try
            {
                request.wait();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    
    }
    
    /**
     * Represents a client request
     * 
     * @author alok
     *
     */
    public static final class FCRequest
    {
        private final String _word;
        private HttpServletResponse _httpServletResponse;
        
        public FCRequest(String word, HttpServletResponse httpServletResponse)
        {
            _word = word;
            _httpServletResponse = httpServletResponse;
        }
        
        public String getWord()
        {
            return _word;
        }
        
        /**
         * CallBack from WordCount Searcher when search is complete
         */
        public void sendResponse(int count)
        {
            try
            {
                JSONObject json = new JSONObject();
                json.put(_word, count);
                System.out.println(count);

                _httpServletResponse.setContentType("text/x-json;charset=UTF-8");
                json.write(_httpServletResponse.getWriter());
                
                synchronized(this)
                {
                    this.notify();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    
}