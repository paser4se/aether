package roart.thread;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.client.MyVaadinUI;
import roart.queue.Queues;
import roart.service.ServiceParam;
import roart.service.ServiceResult;
import roart.util.Constants;
import roart.util.EurekaConstants;
import roart.util.EurekaUtil;

import com.vaadin.ui.UI;
import com.vaadin.ui.UIDetachedException;

public class ClientRunner implements Runnable {

    private static Logger log = LoggerFactory.getLogger(ClientRunner.class);

    public static ConcurrentMap<UI, String> uiset = new ConcurrentHashMap<UI, String>();

    final int update = 60;
    static long lastupdate = 0;

    public void run() {
        Set<Future<Object>> set = new HashSet<Future<Object>>();
        int nThreads = 4;
        ThreadPoolExecutor /*ExecutorService*/ executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(nThreads);
        if (Queues.getClients() > 0) {
            log.info("resetting clients");
            Queues.resetClients();
        }

        while (true) {
            final long now = System.currentTimeMillis();
            if ((now - lastupdate) >= update * 1000) {
                for (final UI ui : uiset.keySet()) {
                    try {
                        ui.access(new Runnable() {
                            @Override
                            public void run() {
                                String db = ""; //IndexFilesDao.webstat();
                                // TODO fix
                                //((roart.client.MyVaadinUI) ui).statLabel.setValue(Queues.webstat() + "\n" + db);
                            }
                        });
                    } catch (UIDetachedException e) {
                        log.error("UIDetachedException", e);
                        uiset.remove(ui, "value");
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                }
                lastupdate = now;
            }
            List<Future> removes = new ArrayList<Future>();
            if (Queues.clientQueue.isEmpty()) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    log.error(Constants.EXCEPTION, e);
                }
            } else {
                Callable<Object> callable = new Callable<Object>() {
                    public Object call() /* throws Exception*/ {
                        ServiceResult result = null;
                        try {
                            ServiceParam param = Queues.clientQueue.poll();
                            result = EurekaUtil.sendMe(ServiceResult.class, param, getAppName(), param.webpath);
                        } catch (Exception e) {
                            log.error(Constants.EXCEPTION, e);
                        } catch (Error e) {
                            System.gc();
                            log.error("Error " + Thread.currentThread().getId());
                            log.error(Constants.ERROR, e);
                        }
                        finally {
                            //log.info("myend");
                        }
                        return result; //myMethod();
                    }	
                };	

                Future task = executorService.submit(callable);
                set.add(task);
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    log.error(Constants.EXCEPTION, e);
                }
            }

            for (Future future : set) {
                if (future.isDone()) {
                    removes.add(future);
                    try {
                        List list = (List) future.get();
                        if (list == null) {
                            continue;
                        }
                        UI ui = com.vaadin.ui.UI.getCurrent();
                        endFuture(ui, list);
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                }
            }
            set.removeAll(removes);
        }
    }

    private void endFuture(final UI ui, List lists) {
        ui.access(new Runnable() {
            @Override
            public void run() {
                // TODO fix
                ((roart.client.MyVaadinUI) ui).displayResultListsTab(lists);
            }
        });
    }

    public static void notify(final String text) {
        for (final UI ui : uiset.keySet()) {
            try {
                ui.access(new Runnable() {
                    @Override
                    public void run() {
                        // TODO fix
                        //((roart.client.MyVaadinUI) ui).notify(text);
                    }
                });
            } catch (UIDetachedException e) {
                log.error("UIDetachedException", e);
                uiset.remove(ui, "value");
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
    }

    public static void replace() {
        for (final UI ui : uiset.keySet()) {
            try {
                ui.access(new Runnable() {
                    @Override
                    public void run() {
                        // TODO fix
                        //((roart.client.MyVaadinUI) ui).replace();
                    }
                });
            } catch (UIDetachedException e) {
                log.error("UIDetachedException", e);
                uiset.remove(ui, "value");
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
    }

    public String getAppName() {
        return EurekaConstants.AETHER;
    }

    // not yet
    /*
    public static void abort() {
	for (Future future : set) {
	    future.cancel(true);
	    //future.interrupt();
	}
    }
     */

}