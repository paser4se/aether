package roart.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;

import roart.config.ConfigConstants;
import roart.config.NodeConfig;
import roart.model.FileObject;
import roart.util.Constants;
import roart.util.FileSystemConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HDFS extends FileSystemOperations {

	private static final Logger log = LoggerFactory.getLogger(HDFS.class);
	
	private HDFSConfig conf;
	
	public HDFS(String nodename, NodeConfig nodeConf) {
	    conf = new HDFSConfig();
	    Configuration configuration = new Configuration();
		conf.configuration = configuration;
		String fsdefaultname = nodeConf.hdfsdefaultname;
		if (fsdefaultname != null) {
		    configuration.set("fs.default.name", fsdefaultname);
		    log.info("Setting hadoop fs.default.name " + fsdefaultname);
		}
	}
	
	@Override
    public FileSystemConstructorResult destroy() throws IOException {
        // TODO right?
        conf.configuration.clear();
        return null;
    }

    @Override
	public FileSystemFileObjectResult listFiles(FileSystemFileObjectParam param) {
	    FileObject f = param.fo;
	    FileSystemFileObjectResult result = new FileSystemFileObjectResult();
		List<FileObject> foList = new ArrayList<FileObject>();
		FileSystem fs;
		try {
			fs = FileSystem.get(conf.configuration);
		Path dir = (Path) f.object;
		FileStatus[] status = fs.listStatus(dir);
		Path[] listedPaths = FileUtil.stat2Paths(status);
		for (Path path : listedPaths) {
			FileObject fo = new FileObject(path);
			foList.add(fo);
		}
		result.fileObject = (FileObject[]) foList.toArray();
		return result;
		} catch (IOException e) {
			log.error(Constants.EXCEPTION, e);
			return null;
		}
	}

    @Override
	public FileSystemBooleanResult exists(FileSystemFileObjectParam param) {
	    FileObject f = param.fo;
		Path path = (Path) f.object;
		boolean exist;
		try {
			FileSystem fs = FileSystem.get(conf.configuration);
			exist = fs.exists(path);
		} catch (Exception e) {
			log.error(Constants.EXCEPTION, e);
			exist = false;
		}
        FileSystemBooleanResult result = new FileSystemBooleanResult();
        result.bool = exist;
        return result;
	}

    @Override
	public FileSystemPathResult getAbsolutePath(FileSystemFileObjectParam param) {
	    FileObject f = param.fo;
		Path path = (Path) f.object;
		//log.info("mypath " + path.getName() + " " + path.getParent().getName() + " " + path.toString());
		// this is hdfs://server/path
		String p = path.toString();
		p = p.substring(7);
		int i = p.indexOf("/");
		p = p.substring(i);
		//log.info("p " + p);
        FileSystemPathResult result = new FileSystemPathResult();
		result.path = FileSystemConstants.HDFS + p;
		return result;
		/*
		try {
			FileSystem fs = FileSystem.get(configuration);
			FileStatus fstat = fs.getFileStatus(path);
			
		} catch (Exception e) {
			log.error(Constants.EXCEPTION, e);
			return null;
		}
		*/
	}

    @Override
	public FileSystemBooleanResult isDirectory(FileSystemFileObjectParam param) {
	    FileObject f = param.fo;
		Path path = (Path) f.object;
		boolean isDirectory;
		try {
			FileSystem fs = FileSystem.get(conf.configuration);
			isDirectory = fs.isDirectory(path);
		} catch (Exception e) {
			log.error(Constants.EXCEPTION, e);
			isDirectory = false;
		}
		FileSystemBooleanResult result = new FileSystemBooleanResult();
		result.bool = isDirectory;
		return result;
	}

    @Override
	public FileSystemByteResult getInputStream(FileSystemFileObjectParam param) {
	    FileObject f = param.fo;
		FileSystem fs;
		try {
			fs = FileSystem.get(conf.configuration);
			InputStream is = fs.open((Path) f.object);
			FileSystemByteResult result = new FileSystemByteResult();
			result.bytes = IOUtils.toByteArray(is);
			return result;
	} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error(Constants.EXCEPTION, e);
			return null;
		}
	}

    @Override
	public FileSystemFileObjectResult getParent(FileSystemFileObjectParam param) {
	    FileObject f = param.fo;
        FileSystemFileObjectResult result = new FileSystemFileObjectResult();
        FileObject[] fo = new FileObject[1];
        fo[0] = new FileObject(((Path) f.object).getParent());
        result.fileObject = fo;
        return result;
	}

    @Override
	public FileSystemFileObjectResult get(FileSystemPathParam param) {
	    String string = param.path;
	    if (string.startsWith(FileSystemConstants.HDFS)) {
	    	string = string.substring(FileSystemConstants.HDFSLEN);
	    }
        FileSystemFileObjectResult result = new FileSystemFileObjectResult();
        FileObject[] fo = new FileObject[1];
		fo[0] = new FileObject(new Path(string));
		result.fileObject = fo;
		return result;
	}

}