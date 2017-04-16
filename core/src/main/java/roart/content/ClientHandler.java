package roart.content;

import com.vaadin.ui.UI;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.queue.Queues;
import roart.queue.ClientQueueElement;
import roart.service.ControlService;
import roart.service.ServiceParam;
import roart.service.ServiceParam.Function;
import roart.util.Constants;

public class ClientHandler {
	
	private static Logger log = LoggerFactory.getLogger(ClientHandler.class);

    public static final int timeout = 3600;
	
    public static Map<UI, List> doClient()  {
    	ClientQueueElement el = Queues.clientQueue.poll();
    	if (el == null) {
	    log.error("empty queue " + System.currentTimeMillis());
    	    return null;
    	}
    	// vulnerable spot
    	Queues.incClients();
	ServiceParam.Function function = el.function;
	List list = null;
	if (function == ServiceParam.Function.FILESYSTEM || function == ServiceParam.Function.FILESYSTEMLUCENENEW || function == ServiceParam.Function.INDEX || function == ServiceParam.Function.REINDEXDATE || function == ServiceParam.Function.REINDEXSUFFIX || function ==  ServiceParam.Function.REINDEXLANGUAGE) {
	    list = client(el);
	}
	if (function == ServiceParam.Function.NOTINDEXED) {
	    list = notindexed(el);
	}
	if (function == ServiceParam.Function.OVERLAPPING) {
	    list = overlapping();
	}
	if (function == ServiceParam.Function.MEMORYUSAGE) {
	    list = memoryusage();
	}
	if (function == ServiceParam.Function.SEARCH) {
	    list = search(el);
	}
	if (function == ServiceParam.Function.SEARCHSIMILAR) {
	    list = searchsimilar(el);
	}
	if (function == ServiceParam.Function.DBINDEX) {
	    list = dbindex(el);
	}
	if (function == ServiceParam.Function.DBSEARCH) {
	    list = dbsearch(el);
	}
	if (function == ServiceParam.Function.CONSISTENTCLEAN) {
	    list = consistentclean(el);
	}
	if (function == ServiceParam.Function.DELETEPATH) {
	    list = deletepath(el);
	}
	Queues.decClients();
	Map map = new HashMap<String, List>();
	map.put(el.uiid, list);
	return map;
    }

    private static List search(ClientQueueElement el) {
	roart.service.SearchService maininst = new roart.service.SearchService();
	try {
	    return maininst.searchmeDo(el);
	} catch (Exception e) {
	    return null;
	}
    }

    private static List searchsimilar(ClientQueueElement el) {
	roart.service.SearchService maininst = new roart.service.SearchService();
	try {
	    return maininst.searchsimilarDo(el);
	} catch (Exception e) {
	    return null;
	}
    }

    private static List client(ClientQueueElement el) {
	ControlService maininst = new ControlService();
	try {
	    return maininst.clientDo(el);
	} catch (Exception e) {
	    log.error(Constants.EXCEPTION, e);
	    return null;
	}
    }

    private static List notindexed(ClientQueueElement el) {
	ControlService maininst = new ControlService();
	try {
	    return maininst.notindexedDo(el);
	} catch (Exception e) {
	    log.error(Constants.EXCEPTION, e);
	    return null;
	}
    }

    private static List overlapping() {
	ControlService maininst = new ControlService();
	try {
	    return maininst.overlappingDo();
	} catch (Exception e) {
	    log.error(Constants.EXCEPTION, e);
	    return null;
	}
    }

    private static List memoryusage() {
	ControlService maininst = new ControlService();
	try {
	    return maininst.memoryusageDo();
	} catch (Exception e) {
	    log.error(Constants.EXCEPTION, e);
	    return null;
	}
    }

    private static List dbindex(ClientQueueElement el) {
	ControlService maininst = new ControlService();
	try {
	    return maininst.dbindexDo(el);
	} catch (Exception e) {
	    return null;
	}
    }

    private static List dbsearch(ClientQueueElement el) {
	ControlService maininst = new ControlService();
	try {
	    return maininst.dbsearchDo(el);
	} catch (Exception e) {
	    return null;
	}
    }

    private static List consistentclean(ClientQueueElement el) {
	ControlService maininst = new ControlService();
	try {
	    return maininst.consistentcleanDo(el);
	} catch (Exception e) {
	    return null;
	}
    }

    private static List deletepath(ClientQueueElement el) {
    ControlService maininst = new ControlService();
    try {
        return maininst.deletepathdbDo(el);
    } catch (Exception e) {
        return null;
    }
    }

}