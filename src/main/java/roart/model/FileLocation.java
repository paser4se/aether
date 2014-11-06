package roart.model;

import java.util.HashSet;
import java.util.Set;

import roart.filesystem.FileSystemDao;
import roart.service.ControlService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileLocation {
	private Logger log = LoggerFactory.getLogger(this.getClass());

	private String node;
	private String filename;

    public FileLocation(String mynode, String filename) {
	if (mynode == null) {
	    mynode = ControlService.nodename;
	}
	this.node = mynode;
	this.filename = filename;
    }

    public FileLocation(String filename) {
    	String file = filename;
    	String prefix = "";
    	if (filename.startsWith(FileSystemDao.FILESLASH) || filename.startsWith(FileSystemDao.HDFSSLASH)) {
    		prefix = file.substring(0, FileSystemDao.FILELEN); // no double slash
    	    file = file.substring(FileSystemDao.FILESLASHLEN);
    	    int split = file.indexOf("/");
    	    this.node = file.substring(0, split);
    	    this.filename = prefix + file.substring(split);
    	} else {
	    this.node = ControlService.nodename;
	    this.filename = filename;
	}
    }

        public String getNode() {
	    return node;
	}

	public void setNode(String node) {
	    this.node = node;
	}

        public String getFilename() {
	    return filename;
	}

	public void setFilename(String filename) {
	    this.filename = filename;
	}

    @Override
    public String toString() {
	if (node == null || node.length() == 0) {
	    return filename;
	}
	if (filename.startsWith(FileSystemDao.FILE) || filename.startsWith(FileSystemDao.HDFS)) {
		String prefix = filename.substring(0, FileSystemDao.FILELEN);
		return prefix + "//" + node + filename.substring(FileSystemDao.FILELEN);
	} else {
		return FileSystemDao.FILESLASH + node + filename;
	}
    }

    public String toPrintString() {
	if (node == null) {
	    return filename;
	}
	return node + ":" + filename;
    }

    public String getNodeNoLocalhost() {
    	String mynode = getNode();
    	if (mynode != null && mynode.equals(ControlService.nodename)) {
    		return null;
    	}
    return mynode;
}

    public boolean isLocal() {
    	if (node == null) {
    		return true;
    	}
    	return ControlService.nodename.equals(node);
    }

        @Override
        public int hashCode() {
	    String str = toString();
	    if (str == null) {
		return 0;
	    }
	    return str.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
	    if (this == obj) {
		return true;
	    }
	    String str = toString();
	    if (str == null) {
		return false;
	    }
	    return str.equals(obj.toString());
	}

		public static Set<String> getFilelocationsToString(Set<FileLocation> files) {
			Set<String> set = new HashSet<String>();
			for (FileLocation fl : files) {
				set.add(fl.toString());
			}
			return set;
		}
    
		public static Set<FileLocation> getFilelocations(Set<String> files) {
			Set<FileLocation> set = new HashSet<FileLocation>();
			for (String fl : files) {
				set.add(new FileLocation(fl));
			}
			return set;
		}
    
}
