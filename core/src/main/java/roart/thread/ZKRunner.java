package roart.thread;

import java.util.List;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.util.Constants;
import roart.zkutil.ZKInitialize;
import roart.zkutil.ZKMessageUtil;
import roart.config.MyConfig;
import roart.database.IndexFilesDao;
import roart.service.ControlService;

public class ZKRunner implements Runnable {
	
    static Logger log = LoggerFactory.getLogger(ZKRunner.class);

    public void run() {

    	List<String> children = null;

    	ZKInitialize.initZK(MyConfig.conf.zookeeper, new DummyWatcher(), ControlService.nodename);
    	String dir = "/" + Constants.AETHER + "/" + Constants.NODES + "/" + ControlService.nodename;

    	while (true) {
    		log.info("get children");
    		ZKMessageUtil.MessageWatcher msgwatcher = new ZKMessageUtil.MessageWatcher();
    		try {
    			children = ZKMessageUtil.getChildren(dir, msgwatcher);
    		} catch (Exception e) {
    			log.error(Constants.EXCEPTION, e);
    		}
    		try {
    			if (children.size() == 0) {
    				msgwatcher.await();
    				continue;
    			} else {
    				readMsg(dir, children);
    			}
    		} catch (Exception e) {
    			log.error(Constants.EXCEPTION, e);
    		}
     	}
    }

    public class DummyWatcher implements Watcher {

   	public void process(WatchedEvent event) {
   		log.info("Process " + event.getPath() + " state " + event.getState() + " type " + event.getType());
   		log.info("dummy watcher");
   	}
    	}
    
    public void readMsg(String dir, List<String> children) {
    try {
        for (String child : children) {
        if (child.equals(Constants.REFRESH)) {
            IndexFilesDao.getAll();
            log.info(Constants.REFRESH + " " + ControlService.nodename);
            ClientRunner.notify("Finished refresh");
        } else if (child.equals(Constants.RECONFIG)) {
                MyConfig.instance().reconfig();
                log.info(Constants.RECONFIG + " " + ControlService.nodename);
            ClientRunner.replace();
                ClientRunner.notify("Finished reconfig");
        } else {
            log.info("unknown command " + child);
        }
        ZKInitialize.zk.delete(dir + "/" + child, 0);
        }
    } catch (Exception e) {
        log.error(Constants.EXCEPTION, e);
    }
    }


}