package roart.dao;

import java.util.List;
import java.util.TreeMap;
import java.util.Map;
import java.util.Set;

import roart.model.IndexFiles;
import roart.model.FileLocation;
import roart.service.ControlService;
import roart.util.ConfigConstants;

import roart.jpa.DataNucleusIndexFilesJpa;
import roart.jpa.HibernateIndexFilesJpa;
import roart.jpa.HbaseIndexFilesJpa;
import roart.jpa.IndexFilesJpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexFilesDao {

    private static Logger log = LoggerFactory.getLogger("IndexFilesDao");

    private static Map<String, IndexFiles> all = new TreeMap<String, IndexFiles>();

    private static IndexFilesJpa indexFilesJpa = null;

    public static void instance(String type) {
	if (indexFilesJpa == null) {
	    if (type.equals(ConfigConstants.HIBERNATE)) {
		indexFilesJpa = new HibernateIndexFilesJpa();
	    }
	    if (type.equals(ConfigConstants.HBASE)) {
		indexFilesJpa = new HbaseIndexFilesJpa();
	    }
	    if (type.equals(ConfigConstants.DATANUCLEUS)) {
		indexFilesJpa = new DataNucleusIndexFilesJpa();
	    }
	}
    }

    public static IndexFiles getByMd5(String md5, boolean create) throws Exception {
	if (md5 == null) {
	    return null;
	}
	if (all.containsKey(md5)) {
	    return all.get(md5);
	}
	IndexFiles i = indexFilesJpa.getByMd5(md5);
	if (i == null && create) {
	    i = new IndexFiles(md5);
	}
	if (i != null) {
	all.put(md5, i);
	}
	return i;
    }

    public static IndexFiles getByMd5(String md5) throws Exception {
    	return getByMd5(md5, true);
    }

    public static IndexFiles getExistingByMd5(String md5) throws Exception {
    	return getByMd5(md5, false);
    }

    public static Set<FileLocation> getFilelocationsByMd5(String md5) throws Exception {
	if (md5 == null) {
	    return null;
	}
	return indexFilesJpa.getFilelocationsByMd5(md5);
    }

    public static IndexFiles getByFilenameNot(String filename) throws Exception {
	String nodename = ControlService.nodename;
	FileLocation fl = new FileLocation(nodename, filename);
	return indexFilesJpa.getByFilelocation(fl);
    }

    public static IndexFiles getByFilelocationNot(FileLocation fl) throws Exception {
	return indexFilesJpa.getByFilelocation(fl);
    }

    public static String getMd5ByFilename(String filename) throws Exception {
	String nodename = ControlService.nodename;
	FileLocation fl = new FileLocation(nodename, filename);
	return indexFilesJpa.getMd5ByFilelocation(fl);
    }

    public static List<IndexFiles> getAll() throws Exception {
	all.clear();
	List<IndexFiles> iAll = indexFilesJpa.getAll();
	for (IndexFiles i : iAll) {
	    all.put(i.getMd5(), i);
	}
	return iAll;
    }

    /*
    public static IndexFiles ensureExistence(String md5) throws Exception {
	IndexFiles fi = getByMd5(md5);
	if (fi == null) {
	    indexFilesJpa.ensureExistence(md5);
	}
	return fi;
    }
    */

    public static IndexFiles ensureExistenceNot(FileLocation filename) throws Exception {
	/*
	IndexFiles fi = getByMd5(md5);
	if (fi == null) {
	    indexFilesJpa.ensureExistence(md5);
	}
	*/
	return null;
    }

    public static void save(IndexFiles i) {
	if (i.hasChanged()) {
	    log.info("saving " + i.getMd5());
	    indexFilesJpa.save(i);
	    i.setUnchanged();
	} else {
	    //log.info("not saving " + i.getMd5());
	}
    }

    public static IndexFiles instanceNot(String md5) {
	IndexFiles i = all.get(md5);
	if (i == null) {
	    i = new IndexFiles(md5);
	    all.put(md5, i);
	}
	return i;
    }

    public static void commit() {
	close();
    }

    public static void close() {
	for (String k : all.keySet()) {
	    IndexFiles i = all.get(k);
	    IndexFilesDao.save(i);
	}
	//all.clear();
	indexFilesJpa.close();
    }

    public static void flush() {
	indexFilesJpa.flush();
    }

}
