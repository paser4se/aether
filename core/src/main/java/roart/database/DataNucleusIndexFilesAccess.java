package roart.database;

import roart.util.EurekaConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataNucleusIndexFilesAccess extends IndexFilesAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public String getAppName() {
	return EurekaConstants.DATANUCLEUS;
    }

}
