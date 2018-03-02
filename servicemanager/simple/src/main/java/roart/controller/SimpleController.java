package roart.controller;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.RestController;

import roart.util.JarThread;
import roart.util.RunUtil;

@RestController
@SpringBootApplication
@EnableDiscoveryClient
public class SimpleController implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SimpleController.class, args);
    }

    @Override
    public void run(String... args) throws InterruptedException {
        //MyXMLConfig instance = MyXMLConfig.instance();
        //instance.config();
        Runnable eureka = new JarThread("aether-eureka-0.10-SNAPSHOT.jar");
        new Thread(eureka).start();
        Runnable core = new JarThread("aether-core-0.10-SNAPSHOT.jar");
        new Thread(core).start();
        Runnable local = new JarThread("aether-local-0.10-SNAPSHOT.jar");
        new Thread(local).start();
    }
}
