package roart.controller;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

import roart.config.NodeConfig;
import roart.util.RunUtil;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@RestController
@SpringBootApplication
@EnableDiscoveryClient
public class SimpleController implements CommandLineRunner {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(SimpleController.class, args);
	}
	
    @Override
    public void run(String... args) throws InterruptedException {
        RunUtil.run();
    }
}
