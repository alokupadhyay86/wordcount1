package in.freecharge.wordcount.client;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

public class FCClient
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("Please provide port");
            return;
        }
        
        int port = Integer.parseInt(args[0]);
        
        String url = "http://localhost:" + port;
        
        HttpClient client = new HttpClient();

        GetMethod getWordCount = new GetMethod(url);
        getWordCount.setQueryString("?query=java");

        try
        {
            client.executeMethod(getWordCount);
            byte[] responseBody = getWordCount.getResponseBody();
            System.out.println(new String(responseBody));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            getWordCount.releaseConnection();
        }

    }

}
