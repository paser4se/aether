package roart.search;

import roart.model.ResultItem;
import roart.model.SearchDisplay;
import roart.model.IndexFiles;

import java.util.List;

import java.io.InputStream;

import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuceneSearchAccess extends SearchAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public int indexme(String type, String md5, InputStream inputStream, String dbfilename, Metadata metadata, String lang, String content, String classification, IndexFiles index) {
	return SearchLucene.indexme(type, md5, inputStream, dbfilename, metadata, lang, content, classification, index);
    }

    public ResultItem[] searchme(String str, String searchtype, SearchDisplay display) {
	String type = "all";
	int stype = new Integer(searchtype).intValue();
	return SearchLucene.searchme(str, searchtype, display);
    }

    public ResultItem[] searchsimilar(String md5i, String searchtype, SearchDisplay display) {
	return SearchLucene.searchmlt(md5i, searchtype, display);
    }

    public void delete(String str) {
        SearchLucene.deleteme(str);
    }
}

