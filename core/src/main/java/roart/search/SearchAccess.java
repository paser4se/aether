package roart.search;

import roart.service.ControlService;
import roart.common.config.MyConfig;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.model.FileLocation;
import roart.common.model.IndexFiles;
import roart.common.model.ResultItem;
import roart.common.searchengine.SearchEngineConstructorParam;
import roart.common.searchengine.SearchEngineConstructorResult;
import roart.common.searchengine.SearchEngineDeleteParam;
import roart.common.searchengine.SearchEngineDeleteResult;
import roart.common.searchengine.SearchEngineIndexParam;
import roart.common.searchengine.SearchEngineIndexResult;
import roart.common.searchengine.SearchEngineParam;
import roart.common.searchengine.SearchEngineSearchParam;
import roart.common.searchengine.SearchEngineSearchResult;
import roart.common.searchengine.SearchResult;
import roart.common.searchengine.SearchEngineResult;
import roart.database.IndexFilesDao;
import roart.dir.Traverse;
import roart.eureka.util.EurekaUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.InputStream;

import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;

//@EnableEurekaClient
//@EnableDiscoveryClient
public abstract class SearchAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    //@Autowired
    private DiscoveryClient discoveryClient;

    public abstract String getAppName();
    
    public String constructor() {
        SearchEngineConstructorParam param = new SearchEngineConstructorParam();
        param.nodename = ControlService.nodename;
        param.conf = MyConfig.conf;
        SearchEngineConstructorResult result = EurekaUtil.sendMe(SearchEngineConstructorResult.class, param, getAppName(), EurekaConstants.CONSTRUCTOR);
        return result.error;
    }
    
    public String destructor() {
        SearchEngineConstructorParam param = new SearchEngineConstructorParam();
        param.nodename = ControlService.nodename;
        param.conf = MyConfig.conf;
        SearchEngineConstructorResult result = EurekaUtil.sendMe(SearchEngineConstructorResult.class, param, getAppName(), EurekaConstants.DESTRUCTOR);
        return result.error;
    }
    
    public int indexme(String type, String md5, InputStream inputStream, String dbfilename, Metadata metadata, String lang, String content, String classification, IndexFiles index) {
        Metadata md = metadata;
        String[] str = new String[md.names().length];
        int i = 0;
        for (String name : md.names()) {
            String value = md.get(name);
            str[i++] = value;
        }
        SearchEngineIndexParam param = new SearchEngineIndexParam();
        param.nodename = ControlService.nodename;
        param.conf = MyConfig.conf;
        param.type = type;
        param.md5 = md5;
        param.dbfilename = dbfilename;
        param.metadata = str;
        param.lang = lang;
        param.content = content;
        param.classification = classification;
        
        SearchEngineIndexResult result = EurekaUtil.sendMe(SearchEngineIndexResult.class, param, getAppName(), EurekaConstants.INDEX);

        if (result == null) {
        	return -1;
        }
        
        if (result.size == -1) {
        	index.setNoindexreason(result.noindexreason);
        }
        
        return result.size;
    }

    public ResultItem[] searchme(String str, String searchtype) {
        SearchEngineSearchParam param = new SearchEngineSearchParam();
        param.nodename = ControlService.nodename;
        param.conf = MyConfig.conf;
        param.str = str;
        param.searchtype = searchtype;
        
        SearchEngineSearchResult result = EurekaUtil.sendMe(SearchEngineSearchResult.class, param, getAppName(), EurekaConstants.SEARCH);
    	return getResultItems(result);
    }

	private ResultItem[] getResultItems(SearchEngineSearchResult result) {
		SearchResult[] results = result.results;
    	ResultItem[] strarr = new ResultItem[results.length + 1];
    	strarr[0] = IndexFiles.getHeaderSearch();
    	try {
    		int i = 1;
    		Set<String> md5s = new HashSet<>();
                for (SearchResult res : results) {
                    md5s.add(res.md5);
                }
                Map<String, IndexFiles> indexmd5s = IndexFilesDao.getByMd5(md5s);
    		for (SearchResult res : results) {
    			String md5 = res.md5;
    			IndexFiles indexmd5 = indexmd5s.get(md5);

    			String filename = indexmd5.getFilelocation();
    			log.info("Hit {}.{} : {} {}",i ,md5, filename, res.score);
    			FileLocation maybeFl = null;
    			try {
    			    maybeFl = Traverse.getExistingLocalFilelocationMaybe(indexmd5);
    			} catch (Exception e) {
    			    log.error(Constants.EXCEPTION, e);
    			}
                        strarr[i] = IndexFiles.getSearchResultItem(indexmd5, res.lang, res.score, res.highlights, res.metadata, ControlService.nodename, maybeFl);
                        i++;
    		}
    	} catch (Exception e) {
    		log.error(roart.common.constants.Constants.EXCEPTION, e);
    	}
    	return strarr;
	}

    public ResultItem[] searchsimilar(String id, String searchtype) {
        SearchEngineSearchParam param = new SearchEngineSearchParam();
        param.nodename = ControlService.nodename;
        param.conf = MyConfig.conf;
        param.str = id;
        param.searchtype = searchtype;
        
        SearchEngineSearchResult result = EurekaUtil.sendMe(SearchEngineSearchResult.class, param, getAppName(), EurekaConstants.SEARCHMLT);
       	return getResultItems(result);
    }

    /**
     * Delete the entry with given id
     * 
     * @param str md5 id
     */
    
    public void delete(String str) {
        SearchEngineDeleteParam param = new SearchEngineDeleteParam();
        param.nodename = ControlService.nodename;
        param.conf = MyConfig.conf;
        param.delete = str;
        
        SearchEngineDeleteResult result = EurekaUtil.sendMe(SearchEngineDeleteResult.class, param, getAppName(), EurekaConstants.DELETE);

    }
    
}

