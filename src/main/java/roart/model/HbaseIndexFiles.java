package roart.model;

import java.util.List;
import java.util.Set;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.HColumnDescriptor;
//import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;

import roart.model.IndexFiles;
import roart.model.FileLocation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HbaseIndexFiles {

    private static Log log = LogFactory.getLog("HbaseIndexFiles");

    // column families
    private static byte[] indexcf = Bytes.toBytes("if");
    private static byte[] flcf = Bytes.toBytes("fl");
    private static byte[] filescf = Bytes.toBytes("fi");

    // column qualifiers
    private static byte[] md5q = Bytes.toBytes("md5");
    private static byte[] indexedq = Bytes.toBytes("indexed");
    private static byte[] timestampq = Bytes.toBytes("timestamp");
    private static byte[] convertswq = Bytes.toBytes("convertsw");
    private static byte[] converttimeq = Bytes.toBytes("converttime");
    private static byte[] failedq = Bytes.toBytes("failed");
    private static byte[] failedreasonq = Bytes.toBytes("failedreason");
    private static byte[] timeoutreasonq = Bytes.toBytes("timeoutreason");
    private static byte[] nodeq = Bytes.toBytes("node");
    private static byte[] filenameq = Bytes.toBytes("filename");
    private static byte[] filelocationq = Bytes.toBytes("filelocation");

    private static HTableInterface filesTable = null;
    private static HTableInterface indexTable = null;

    public HbaseIndexFiles() {
	try {
	Configuration conf = HBaseConfiguration.create();
	String quorum = roart.util.Prop.getProp().getProperty("hbasequorum");
	String port = roart.util.Prop.getProp().getProperty("hbaseport");
	String master = roart.util.Prop.getProp().getProperty("hbasemaster");
	conf.set("hbase.zookeeper.quorum", quorum);
	conf.set("hbase.zookeeper.property.clientPort", port);
	conf.set("hbase.master", master);

	HTablePool pool = new HTablePool();
	HBaseAdmin admin = new HBaseAdmin(conf);
	HTableDescriptor tableDesc = new HTableDescriptor("index");
	if (admin.tableExists(tableDesc.getName())) {
	    //admin.disableTable(table.getName());
	    //admin.deleteTable(table.getName());
	} else {
	    admin.createTable(tableDesc);
	}
	indexTable = pool.getTable("index");
	if (admin.isTableEnabled("index")) {
	    admin.disableTable("index");
	}
	if (!indexTable.getTableDescriptor().hasFamily(indexcf)) {
	    admin.addColumn("index", new HColumnDescriptor("if"));
	    //tableDesc.addFamily(new HColumnDescriptor("if"));
	}
	if (!indexTable.getTableDescriptor().hasFamily(flcf)) {
	    admin.addColumn("index", new HColumnDescriptor("fl"));
	    //tableDesc.addFamily(new HColumnDescriptor("fl"));
	}
	admin.enableTable("index");

	HTableDescriptor filesTableDesc = new HTableDescriptor("files");
	if (admin.tableExists(filesTableDesc.getName())) {
	    //admin.disableTable(table.getName());
	    //admin.deleteTable(table.getName());
	} else {
	    admin.createTable(filesTableDesc);
	}
	filesTable = pool.getTable("files");
	if (admin.isTableEnabled("files")) {
	    admin.disableTable("files");
	}
	if (!filesTable.getTableDescriptor().hasFamily(filescf)) {
	    admin.addColumn("files", new HColumnDescriptor("fi"));
	    //filesTableDesc.addFamily(new HColumnDescriptor("fi"));
	}
	admin.enableTable("files");
	//HTable table = new HTable(conf, "index");
	} catch (IOException e) {
	    log.error("Exception", e);
	}
    }

    public static void put(IndexFiles ifile) {
	try {
	    //HTable /*Interface*/ filesTable = new HTable(conf, "index");
	    Put put = new Put(Bytes.toBytes(ifile.getMd5()));
	    put.add(indexcf, md5q, Bytes.toBytes(ifile.getMd5()));
	    if (ifile.getIndexed() != null) {
		put.add(indexcf, indexedq, Bytes.toBytes("" + ifile.getIndexed()));
	    }
	    if (ifile.getTimestamp() != null) {
		put.add(indexcf, timestampq, Bytes.toBytes(ifile.getTimestamp()));
	    }
	    if (ifile.getConvertsw() != null) {
		put.add(indexcf, convertswq, Bytes.toBytes(ifile.getConvertsw()));
	    }
	    if (ifile.getConverttime() != null) {
		put.add(indexcf, converttimeq, Bytes.toBytes(ifile.getConverttime()));
	    }
	    if (ifile.getFailed() != null) {
		put.add(indexcf, failedq, Bytes.toBytes("" + ifile.getFailed()));
	    }
	    if (ifile.getFailedreason() != null) {
		put.add(indexcf, failedreasonq, Bytes.toBytes(ifile.getFailedreason()));
	    }
	    if (ifile.getTimeoutreason() != null) {
		put.add(indexcf, timeoutreasonq, Bytes.toBytes(ifile.getTimeoutreason()));
	    }
	    int i = -1;
	    for (FileLocation file : ifile.getFilelocations()) {
		i++;
		String filename = getFile(file);
		put.add(flcf, Bytes.toBytes("q" + i), Bytes.toBytes(filename));
	    }
	    put(ifile.getMd5(), ifile.getFilelocations());
	    indexTable.put(put);
	} catch (IOException e) {
	    log.error("Exception", e);
	}
    }

    public static void put(String md5, Set<FileLocation> files) {
	try {
	    //HTable /*Interface*/ filesTable = new HTable(conf, "index");
	    for (FileLocation file : files) {
		String filename = getFile(file);
		Put put = new Put(Bytes.toBytes(filename));
		put.add(filescf, md5q, Bytes.toBytes(md5));
		filesTable.put(put);
	    }
	} catch (IOException e) {
	    log.error("Exception", e);
	}
    }

    public static IndexFiles get(Result index) {
	String md5 = bytesToString(index.getValue(indexcf, md5q));
	IndexFiles ifile = new IndexFiles(md5);
	//ifile.setMd5(bytesToString(index.getValue(indexcf, md5q)));
	ifile.setIndexed(new Boolean(bytesToString(index.getValue(indexcf, indexedq))));
	ifile.setTimestamp(bytesToString(index.getValue(indexcf, timestampq)));
	ifile.setConvertsw(bytesToString(index.getValue(indexcf, convertswq)));
	ifile.setConverttime(bytesToString(index.getValue(indexcf, converttimeq)));
	ifile.setFailed(new Integer(convert0(bytesToString(index.getValue(indexcf, failedq)))));
	ifile.setFailedreason(bytesToString(index.getValue(indexcf, failedreasonq)));
	ifile.setTimeoutreason(bytesToString(index.getValue(indexcf, timeoutreasonq)));
	List<KeyValue> list = index.list();
	if (list != null) {
	for (KeyValue kv : list) {
	    byte[] family = kv.getFamily();
	    String fam = new String(family);
	    if (fam.equals("fl")) {
		String loc = Bytes.toString(kv.getValue());
		FileLocation fl = getFileLocation(loc);
		ifile.addFile(fl);
	    }
	}
	}
	return ifile;
    }

    public static IndexFiles get(String md5) {
	try {
	    //HTable /*Interface*/ filesTable = new HTable(conf, "index");
	    Get get = new Get(Bytes.toBytes(md5));
	    get.addFamily(indexcf);
	    get.addFamily(flcf);
	    Result index = indexTable.get(get);
	    if (index.isEmpty()) {
		return null;
	    }
	    IndexFiles ifile = get(index);
	    return ifile;
	} catch (IOException e) {
	    log.error("Exception", e);
	}
	return null;
    }

    public static IndexFiles getIndexByFilelocation(FileLocation fl) {
	String md5 = getMd5ByFilelocation(fl);
	if (md5.length() == 0) {
	    return null;
	}
	return get(md5);
    }

    public static String getMd5ByFilelocation(FileLocation fl) {
	String name = getFile(fl);
	try {
	    //HTable /*Interface*/ filesTable = new HTable(conf, "index");
	    Get get = new Get(Bytes.toBytes(name));
	    //get.addColumn(filescf, md5q);
	    get.addFamily(filescf);
	    Result result = filesTable.get(get);
	    //log.info("res " + new String(result.getValue(filescf, md5q)));
	    if (result.isEmpty()) {
		return null;
	    }
	    return bytesToString(result.getValue(filescf, md5q));
	} catch (IOException e) {
	    log.error("Exception", e);
	}
	return null;
    }

    public static IndexFiles ensureExistenceNot(String md5) throws Exception {
	try {
	    //HTable /*Interface*/ filesTable = new HTable(conf, "index");
	    Put put = new Put(Bytes.toBytes(md5));
	    indexTable.put(put);
	} catch (IOException e) {
	    log.error("Exception", e);
	}
	IndexFiles i = new IndexFiles(md5);
	//i.setMd5(md5);
	return i;
    }

    public static String getFile(FileLocation fl) {
	String node = fl.getNode();
	String file = fl.getFilename();
	if (node != null && node.length() > 0) {
	    file = "file://" + file + "/";
	}
	return file;
    }

    public static FileLocation getFileLocation(String fl) {
	String node = null;
	String file = fl;
	if (fl.startsWith("file://")) {
	    file = file.substring(7);
	    int split = file.indexOf("/");
	    node = file.substring(0, split);
	    file = file.substring(split + 1);
	}
        return new FileLocation(node, file);
    }

    private static String convertNullNot(String s) {
	if (s == null) {
	    return "";
	}
	return s;
    }

    private static String convert0(String s) {
	if (s == null) {
	    return "0";
	}
	return s;
    }

    private static String bytesToString(byte[] bytes) {
	if (bytes == null) {
	    return null;
	}
	return new String(bytes);
    }

    public static void flush() {
	try {
	    filesTable.flushCommits();
	    indexTable.flushCommits();
	} catch (IOException e) {
	    log.error("Exception", e);
	}
    }

    public static void close() {
	try {
	    filesTable.close();
	    indexTable.close();
	} catch (IOException e) {
	    log.error("Exception", e);
	}
    }

}
