package roart.classification;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

import roart.config.NodeConfig;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@RestController
@SpringBootApplication
@EnableDiscoveryClient
public class MahoutSparkController extends MachineLearningAbstractController {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(MahoutSparkController.class, args);
	}

	@Override
	protected MachineLearningAbstractClassifier createClassifier(String nodename, NodeConfig nodeConf) {
		return new MahoutSparkClassify(nodename, nodeConf);
	}
}