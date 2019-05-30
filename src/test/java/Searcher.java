import com.dave.invertedindex.document.Document;
import com.dave.invertedindex.document.Field;
import com.dave.invertedindex.document.FieldInfo;
import com.dave.invertedindex.index.CorruptIndexException;
import com.dave.invertedindex.index.Hit;
import com.dave.invertedindex.index.IndexReader;
import com.dave.invertedindex.parse.CommonTokenizer;
import com.dave.invertedindex.parse.TextParser;
import com.dave.invertedindex.store.TxtFileDirectory;
import com.dave.invertedindex.util.Benchmark;
import com.dave.invertedindex.util.Logger;
import com.dave.util.ConfigUtil;
import org.ansj.splitWord.analysis.IndexAnalysis;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Uses IndexReader to perform a search in the index
 */
public class Searcher {

    Logger log;

    IndexReader reader;


    public  Searcher() {
        this.log = new Logger();
    }

    /**
     * open the index
     * @throws IOException
     * @throws CorruptIndexException
     */
    public IndexReader openIndexReader() throws IOException, CorruptIndexException {
        //resolve directory path
        String currentDirectory = new File("").getAbsolutePath();
        String directoryPath = currentDirectory.concat("/index/");
        System.out.printf("Reading index files from this directory: %s \n\n", directoryPath);

        this.reader = new IndexReader(new TxtFileDirectory(directoryPath));
        this.reader.open();
        return reader;
    }

    /**
     * perform the search of one single term in the index
     * @param term
     * @return
     */
    public TreeSet<Hit> search(String term) {
        TreeSet<Hit> resultSet = null;
        IndexReader reader = null;
        try {
            //search for term occurrences in body field
            resultSet = this.reader.search(Indexer.FieldName.BODY.toString(), term);
        } catch (IOException e) {
            this.log.error("There was an IO error reading the index files ", e);
        } catch (CorruptIndexException e) {
            this.log.error("Index data is corrupt ", e);
        } catch (IllegalArgumentException e) {
            Logger.getInstance().error(e.getMessage());
        } finally {
            //close open resources
            if (reader != null) {
                reader.close();
            }
        }
        return resultSet;
    }


    public TreeSet<Hit> search() throws IOException, CorruptIndexException {
        Document doc = new Document();

        Field field = new Field("act", "布丽·拉尔森;塞缪尔·杰克逊;", new FieldInfo(true, false, CommonTokenizer.class));
        doc.addField(field);

        field = new Field("director", "瑞安·弗雷克;安娜·波顿;", new FieldInfo(true, false, CommonTokenizer.class));
        doc.addField(field);

        field = new Field("name", "东北雷神2", new FieldInfo(true, false, CommonTokenizer.class));
        doc.addField(field);


        TreeSet<Hit> resultSet = this.reader.search(doc);

       return resultSet;

    }

    /**
     * show the list of results
     * @param hits
     * @param term
     */
    public String printHits(TreeSet<Hit> hits, String term) {
        if(hits == null || hits.isEmpty()) {
            return String.format("No documents found matching the term %s \n", term);
        }

        String out = String.format("%d Documents found matching the term %s: \n", hits.size(), term);

        Iterator it = hits.descendingSet().iterator();
        int i = 1;
        while(it.hasNext()) {
            Hit hit = (Hit) it.next();
            out = out.concat(String.format("%d - %f - %s \n", i++, hit.score(), hit.document().fields().get("title").data()));
        }

        return out;
    }


    public static void main(String[] args) {
        IndexAnalysis.parse("test");
        System.setProperty(ConfigUtil.CONFIG_KEY, "index-config.properties");
        args = new String[]{"体验真人秀"};
        if (args.length == 0) {
            System.out.println("No query term specified!");
            System.exit(0);
        }

        Benchmark.getInstance().start("Searcher.main");
        try {
            Searcher searcher = new Searcher();
            searcher.openIndexReader();
            TreeSet<Hit> results = searcher.search(args[0]);
            System.out.println(searcher.printHits(results, args[0]));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CorruptIndexException e) {
            e.printStackTrace();
        }
        Benchmark.getInstance().end("Searcher.main");

        long t = Benchmark.getInstance().getTime("Searcher.main");
        System.out.printf("\ntotal time for this query: %d milliseconds\n", t);
        long mem = Benchmark.getInstance().getMemory("Searcher.main");
        System.out.printf("memory used: %f MB\n", (float) mem / 1024 / 1024);
    }
}