package com.vision;

import static com.vision.common.Constant.FULLNODE_HOST;
import static com.vision.common.Constant.HTTP_EVENT_HOST;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Strings;
import com.vision.client.OracleClient;
import com.vision.client.ReSender;
import com.vision.common.Constant;
import com.vision.job.JobCache;
import com.vision.job.JobSubscriber;
import com.vision.keystore.KeyStore;
import com.vision.keystore.VrfKeyStore;
import java.io.FileNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.vision.common.parameter.CommonParameter;

@Slf4j
@SpringBootApplication
@MapperScan("com.vision.web.mapper")
public class OracleApplication {

	public static void main(String[] args) {

		CommonParameter.getInstance().setValidContractProtoThreadNum(2);
		Args argv = new Args();
		JCommander jct = JCommander.newBuilder()
						.addObject(argv)
						.build();
		jct.setProgramName("victor-link");
		jct.setAcceptUnknownOptions(true);
		jct.parse(args);
		try {
			KeyStore.initKeyStore(argv.key);
		} catch (FileNotFoundException e) {
			log.error("init ECKey failed, err: {}", e.getMessage());
			System.exit(-1);
		}
		if(!Strings.isNullOrEmpty(argv.vrfKey)) { // optional
			try {
				VrfKeyStore.initKeyStore(argv.vrfKey);
			} catch (FileNotFoundException e) {
				log.error("init VRF ECKey failed, err: {}", e.getMessage());
				System.exit(-1);
			}
		}

		Constant.initEnv(argv.env);
		ConfigurableApplicationContext context = SpringApplication.run(OracleApplication.class, args);
		JobCache jobCache = context.getBean(JobCache.class);
		jobCache.run();
		OracleClient.init();

		ReSender reSender = new ReSender(JobSubscriber.jobRunner.visionTxService);
		reSender.run();
		log.info("==================Victor Link start success================");
	}

	static class Args {
		@Parameter(
						names = {"--key", "-k"},
						help = true,
						description = "specify the privatekey",
						order = 1)
		private String key;
		@Parameter(
						names = {"--env", "-e"},
						help = true,
						description = "specify the env",
						order = 2)
		private String env;
		@Parameter(
						names = "--help",
						help = true,
						order = 3)
		private boolean help;
		@Parameter(
				names = {"--vrfKey", "-vrfK"},
				help = true,
				description = "specify the VRF privatekey",
				order = 4)
		private String vrfKey;
	}
}


