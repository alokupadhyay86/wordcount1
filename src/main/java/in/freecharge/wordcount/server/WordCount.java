package in.freecharge.wordcount.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Takes list of words and returns the wordCount
 * 
 * @author alok
 *
 */
public class WordCount
{
    
    private final Map<String, Integer> _wordToCountMap = new HashMap<String, Integer>();
    
    public WordCount(List<String> wordList)
    {
        //_word = word.trim();
        for (String word : wordList)
        {
            _wordToCountMap.put(word, 0);
        }
    }
    
    
    /**
     * Launches a separate thread to get wordCount from a specific file. Then, sums up the results from each thread and returns totalWordCount for all files
     * 
     * @return
     */
    public Map<String, Integer> getCount()
    {

        //TODO - Improve if the directory contains directories
        File directory = new File(FCHttpServer.CORPUS_DIRECTORY_PATH);      
        
        for (File file : directory.listFiles())
        {

            List<Future<Map<String, Integer>>> list = new ArrayList<Future<Map<String, Integer>>>();

            Callable<Map<String, Integer>> callable = new SingleFileWordCountCallable(_wordToCountMap.keySet(), file.getAbsolutePath());

            Future<Map<String, Integer>> future = FCHttpServer.EXECUTOR.submit(callable);

            list.add(future);

            for(Future<Map<String, Integer>> fut : list)
            {
                try 
                {
                    Map<String, Integer> fileWordToCountMap = fut.get();
                    for (String word : fileWordToCountMap.keySet())
                    {
                        _wordToCountMap.put(word, _wordToCountMap.get(word) + fileWordToCountMap.get(word));
                    }
                } 
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                catch(ExecutionException e)
                {
                    e.printStackTrace();
                }
            }
               
        }
    
        return _wordToCountMap;
    }
    
    
    /**
     * Responsible for counting words in a single file as a separate thread
     * 
     * @author alok
     *
     */
    private static class SingleFileWordCountCallable implements Callable<Map<String, Integer>>
    {
        private final Set<String> _wordList;
        
        private final String _fileName;
        
        private final Map<String, Integer> _wordToCountMap = new HashMap<String, Integer>();
        
        public SingleFileWordCountCallable(Set<String> wordList, String fileName)
        {
            _wordList = wordList;
            _fileName = fileName;
            
            for (String word : wordList)
            {
                _wordToCountMap.put(word, 0);
            }
        }
        
        @Override
        public Map<String, Integer> call() throws Exception 
        {
            return getWordCountFromFile();
        }
        
        private Map<String, Integer> getWordCountFromFile()
        {
            
            try
            {
                FileReader fr = new FileReader(new File(_fileName));
                BufferedReader br = new BufferedReader(fr);
                while (br.ready()) 
                {
                   String line = br.readLine();
                   String[] words = line.split(" ");
                   for (String word : words)
                   {
                       for (String wordBeingSearched : _wordList)
                       if (word.trim().equalsIgnoreCase(wordBeingSearched))
                       {
                           _wordToCountMap.put(wordBeingSearched, _wordToCountMap.get(wordBeingSearched) + 1);

                       }
                   }
                }
                
                return _wordToCountMap;
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
                return null;
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return null;
            }
        }
     
    }

}
