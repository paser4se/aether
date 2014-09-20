package roart.search;

import roart.model.IndexFiles;
import roart.model.HibernateUtil;
import roart.queue.IndexQueueElement;
import roart.queue.Queues;
import roart.lang.LanguageDetect;

import roart.dao.SearchDao;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
 
import org.apache.tika.metadata.Metadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Search {
    private static Log log = LogFactory.getLog("Search");

    //public static int indexme(String type, String md5, InputStream inputStream) {
    public static void indexme() {
    	IndexQueueElement el = Queues.indexQueue.poll();
    	if (el == null) {
    		log.error("empty queue");
    	    return;
    	}
    	// vulnerable spot
    	Queues.incIndexs();
    	long now = System.currentTimeMillis();
    	
    	String type = el.type;
     	String md5 = el.md5;
    	InputStream inputStream = el.inputStream;
    	IndexFiles dbindex = el.index;
    	String dbfilename = el.dbfilename;
	Metadata metadata = el.metadata;
    	List<String> retlist = el.retlist;

    int retsize = 0;

    retsize = SearchDao.indexme(type, md5, inputStream, dbfilename, metadata.toString(), retlist);

    log.info("size2 " + retsize);
	el.size = retsize;
	dbindex.setIndexed(Boolean.TRUE);
	dbindex.setTimestamp("" + System.currentTimeMillis());
	dbindex.setConvertsw(el.convertsw);
	//dbindex.save();
	long time = System.currentTimeMillis() - now;
	dbindex.setTimeindex(time);
	log.info("timerStop filename " + time);
	retlist.add("Indexed " + dbfilename + " " + md5 + " " + retsize + " " + el.convertsw + " " + time);
    try {
		inputStream.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		log.error("Exception", e);
	}
    Queues.decIndexs();
    
	}

    public static void indexme(String type) {
	SearchDao.indexme(type);
    }

    public static String [] searchme(String type, String str) {
		String[] strarr = new String[0];
		strarr = SearchDao.searchme(type, str);
    return strarr;
}

    public static String [] searchme2(String str, String searchtype) {
	String type = "all";
	int stype = new Integer(searchtype).intValue();
		String[] strarr = new String[0];
		
		strarr = SearchDao.searchme2(str, searchtype);
    return strarr;
}

    // not yet usable, lacking termvector
    public static String [] searchsimilar(String md5i) {
	String type = "all";
		String[] strarr = new String[0];
    return strarr;
}

    // not yet usable, lacking termvector
    public static void docsLike(int id, int max) throws IOException {
    }

    public static void deleteme(String str) {
    }

    // outdated, did run once, had a bug which made duplicates
    public static List<String> removeDuplicate() throws Exception {
	return null;
    }//End of removeDuplicate method

    // outdated, used once, when bug added filename instead of md5
    public static List<String> cleanup2() throws Exception {
	return null;
    }//End of removeDuplicate method

}