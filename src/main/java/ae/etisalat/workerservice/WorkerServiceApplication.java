package ae.etisalat.workerservice;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.ZeebeClientBuilder;
import io.zeebe.client.api.clients.JobClient;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.subscription.JobHandler;
import io.zeebe.client.api.subscription.JobWorker;

@SpringBootApplication
public class WorkerServiceApplication {


	static Logger LOGGER = Logger.getLogger(WorkerServiceApplication.class.getName());
	
	public static void main(String[] args) {
	
		final String jobType = "operation-add";
		
		final ZeebeClientBuilder builder = ZeebeClient.newClientBuilder().brokerContactPoint("10.56.16.227:26500");

		try (ZeebeClient client = builder.build()) {

			LOGGER.info("Job Name :: " + client.getConfiguration().getDefaultJobWorkerName());

			final JobWorker workerRegistration = client.newWorker().jobType(jobType).handler(new ExampleJobHandler1())
					.timeout(Duration.ofSeconds(10)).open();
			
			LOGGER.info("Job worker opened and receiving jobs.");

			// run until System.in receives exit command
			///waitUntilSystemInput("exit");
			
			try {
			      new CountDownLatch(1).await();
			    } catch (InterruptedException e) {
			}
			
		}
	}

	private static class ExampleJobHandler1 implements JobHandler {

		@Override
		public void handle(final JobClient client, final ActivatedJob job) {
			LOGGER.info("######################### OPERATION: ADD");

			final Map<String, Object> inputVariables = job.getVariablesAsMap();
			final Map<String, Object> outputVariables = new HashMap<>();

			Integer input1 = (Integer) inputVariables.get("input1");
			LOGGER.info("######################### INPUT1: " + input1);

			Integer input2 = (Integer) inputVariables.get("input2"); // To create Active Instances just change the
																		// input2 to input3
			LOGGER.info("######################### INPUT2: " + input2);

			Integer result = Integer.sum(input1, input2);

			LOGGER.info("######################### RESULT: " + result);

			outputVariables.put("result", result);

			LOGGER.info("*************************************** output variable sent ");
			client.newCompleteCommand(job.getKey()).variables(outputVariables).send().join();
		}
	}

	private static void waitUntilSystemInput(final String exitCode) {
		try (Scanner scanner = new Scanner(System.in)) {
			while (scanner.hasNextLine()) {
				final String nextLine = scanner.nextLine();
				if (nextLine.contains(exitCode)) {
					return;
				}
			}
		}
	}
}

