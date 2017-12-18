package roart.filesystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roart.config.ConfigConstants;
import roart.config.NodeConfig;
import roart.model.FileObject;
import roart.util.Constants;
import roart.util.FileSystemConstants;

import org.javaswift.joss.client.factory.AccountConfig;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.client.factory.AuthenticationMethod;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.Directory;
import org.javaswift.joss.model.DirectoryOrObject;
import org.javaswift.joss.model.StoredObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Swift extends FileSystemOperations {

	private static final Logger log = LoggerFactory.getLogger(Swift.class);
	
	private SwiftConfig conf;
	
	private Map<String, DirectoryOrObject> dooMap = new HashMap<>();
	
	public Swift(String nodename, NodeConfig nodeConf) {
	    try {
	    conf = new SwiftConfig();
		String url = nodeConf.getSwiftUrl();
		String username = nodeConf.getSwiftUser();
		String password = nodeConf.getSwiftKey();
		log.info("INFO " + url + " " + username + "  " + password);
		if (url != null) {
		    AccountConfig config;
			config = new AccountConfig();
		    config.setUsername( username);
		    config.setPassword(password);
		    config.setAuthUrl(url);
		    config.setAuthenticationMethod(AuthenticationMethod.BASIC);
		    Account account = new AccountFactory(config).createAccount();
		    conf.account = account;
		    log.info("here");
		}
    } catch (Exception e) {
        log.error("Exception", e);
        //return null;
    }
	}
	
    @Override
	public FileSystemFileObjectResult listFiles(FileSystemFileObjectParam param) {
	    FileObject f = param.fo;
		List<FileObject> foList = new ArrayList<FileObject>();
		DirectoryOrObject mydir = dooMap.get(f.object);
		try {
			String containerName = param.conf.getSwiftContainer();
			Container container = conf.account.getContainer(containerName);
			if (mydir.isObject()) {
				foList.add(f);
			} else {
				Directory dir = mydir.getAsDirectory();
				Collection<DirectoryOrObject> list = container.listDirectory(dir);
				for (DirectoryOrObject doo : list) {
		            log.info("name" + doo.getBareName()  + " " + doo.getName());
					FileObject fo = new FileObject(doo.getName());
					foList.add(fo);
				}
			}
	        FileSystemFileObjectResult result = new FileSystemFileObjectResult();
	        result.fileObject = (FileObject[]) foList.toArray();
	        return result;
		} catch (Exception e) {
			log.error(Constants.EXCEPTION, e);
			return null;
		}
	}

    @Override
	public FileSystemBooleanResult exists(FileSystemFileObjectParam param) {
	    FileObject f = param.fo;
		DirectoryOrObject path = dooMap.get(f.object);
		boolean exist;
		try {
			DirectoryOrObject mydir = dooMap.get(f.object);
			String dirName = mydir.getName();
			String containerName = param.conf.getSwiftContainer();
			Container container = conf.account.getContainer(containerName);
			StoredObject so = container.getObject(dirName);
			// note that a directory does not exist, only files
			if (so.exists()) {
				exist = true;
			} else {
			    Collection<DirectoryOrObject> list = container.listDirectory(dirName, '/', null, 1);
			    exist = !list.isEmpty();
			}
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
		DirectoryOrObject path = dooMap.get(f.object);
		String p = path.getName();
        FileSystemPathResult result = new FileSystemPathResult();
        result.path = FileSystemConstants.SWIFT + p;
        return result;
	}

    @Override
	public FileSystemBooleanResult isDirectory(FileSystemFileObjectParam param) {
	    FileObject f = param.fo;
	    boolean isDirectory;
	    try {
			DirectoryOrObject doo = dooMap.get(f.object);
			isDirectory = doo.isDirectory();
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
		try {
			DirectoryOrObject doo = dooMap.get(f.object);
			StoredObject so = doo.getAsObject();
            FileSystemByteResult result = new FileSystemByteResult();
            result.bytes = so.downloadObject();
            return result;
	} catch (Exception e) {
			log.error(Constants.EXCEPTION, e);
			return null;
		}
	}

    @Override
	public FileSystemFileObjectResult getParent(FileSystemFileObjectParam param) {
	    FileObject f = param.fo;
		DirectoryOrObject doo = dooMap.get(f.object);
		String name = doo.getName();
		File fi = new File(name);
		String parent = fi.getParent();
		DirectoryOrObject pardoo = new Directory(parent, '/');
        FileSystemFileObjectResult result = new FileSystemFileObjectResult();
        FileObject[] fo = new FileObject[1];
        log.info("name" + pardoo.getBareName());
        fo[0] = new FileObject(pardoo.getName());
        result.fileObject = fo;
        return result;
	}

    @Override
	public FileSystemFileObjectResult get(FileSystemPathParam param) {
        try {
            log.info("here");
	    String string = param.path;
	    if (string.startsWith(FileSystemConstants.SWIFT)) {
	    	string = string.substring(FileSystemConstants.SWIFTLEN);
	    }
	    // Joss directories don't start with /
	    if (string.startsWith("/")) {
	    	string = string.substring(1);
	    }
        log.info("here");
		String containerName = param.conf.getSwiftContainer();
		Container container = conf.account.getContainer(containerName);
		StoredObject so = container.getObject(string);
		FileObject fo;
		// if it exists, it is a file and not a dir
		if (so.exists()) {
		    log.info("name" + so.getBareName() + " " + so.getPath() + " " + so.getName());
			fo = new FileObject(so.getName());
		} else {
            log.info("name" + string);
			fo = new FileObject(string);
		}
        log.info("here");
        FileSystemFileObjectResult result = new FileSystemFileObjectResult();
        FileObject[] fos = new FileObject[1];
        fos[0] = fo;
        result.fileObject = fos;
        log.info("here");
        return result;
        } catch (Exception e) {
            log.info("here");
            log.error("Exception", e);
            return null;
        }
	}

    @Override
    public FileSystemConstructorResult destroy() {
        return null;
    }

}
