package roart.jpa;

import roart.model.ResultItem;

import java.util.List;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class SearchJpa {

    private Log log = LogFactory.getLog(this.getClass());

    public abstract int indexme(String type, String md5, InputStream inputStream, String dbfilename, String metadata, List<String> retlist);

    public abstract void indexme(String type);

    public abstract ResultItem[] searchme(String type, String str);

    public abstract ResultItem[] searchme2(String str, String searchtype);

}

