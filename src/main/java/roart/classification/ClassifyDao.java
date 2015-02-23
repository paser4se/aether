package roart.classification;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;


import roart.model.ResultItem;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassifyDao {
    private static Logger log = LoggerFactory.getLogger(ClassifyDao.class);

    private static ClassifyAccess classify = null;

    public static void instance(String type) {
	System.out.println("instance " + type);
	log.info("instance " + type);
	if (type == null) {
	  return;
	}
	if (classify == null) {
	    if (type.equals("mahout")) {
		classify = new MahoutClassifyAccess();
	    }
	    if (type.equals("opennlp")) {
		classify = new OpennlpClassifyAccess();
	    }
	}
    }

    public static String classify(String type, String language) {
	if (classify == null) {
	    return null;
	}
	return classify.classify(type, language);
    }

}
