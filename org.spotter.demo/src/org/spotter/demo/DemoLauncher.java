/**
 * Copyright 2014 SAP AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spotter.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.lpe.common.util.web.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spotter.demo.app.ServerLauncher;

/**
 * Launches the Demo appplication and sampler.
 * 
 * @author Alexander Wert
 * 
 */
public final class DemoLauncher {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerLauncher.class);
	private static final String DEFAULT_PORT = "8089";

	private static final String DEMO_APP_JAR_KEY = "org.spotter.demo.demoAppJar";
	private static final String SPOTTER_CONF_PATH_KEY = "org.spotter.demo.spotterConf";
	private static final String SAMPLER_CONF_PATH_KEY = "org.spotter.demo.samplerConf";
	private static final String INSTRUMENTATION_AGENT_JAR_KEY = "org.spotter.demo.instrumentationAgentJar";
	private static final String RESOURCE_MONITORING_JAR_KEY = "org.spotter.demo.samplerJar";
	private static final String DEMO_APP_OUT_KEY = "org.spotter.demo.demoAppOut";
	private static final String SAMPLER_OUT_KEY = "org.spotter.demo.samplerOut";
	private static final String SPOTTER_OUT_KEY = "org.spotter.demo.spotterOut";

	private static String demoAppJar = null;
	private static String spotterConfPath = null;
	private static String samplerConfPath = null;
	private static String instrumentationAgentJar = null;
	private static String resourceMonitoringJar = null;
	private static String demoAppOut = null;
	private static String samplerOut = null;
	private static String spotterOut = null;

	private DemoLauncher() {
	}

	/**
	 * 
	 * @param args
	 *            prog args
	 * @throws IOException
	 *             if fails
	 * @throws InterruptedException
	 *             if fails
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		if (args == null || args.length < 1) {
			LOGGER.error("Demo App requires at least one parameter:");
			LOGGER.error("1st argument: start / shutdown");

			System.exit(0);
		}

		String mode = args[0];

		String port = DEFAULT_PORT;

		if (mode.equalsIgnoreCase("start")) {
			if (args == null || args.length < 2) {
				LOGGER.error("Demo App requires at least two parameters when started with the mode 'start':");
				LOGGER.error("1st argument: start");
				LOGGER.error("2nd argument: path to demo configuration file");
				System.exit(0);
			}

			loadProperties(args[1]);

			LOGGER.info("Starting Demo Appplication ...");
			Process demoAppProcess = startDemoApp();

//			LOGGER.info("Starting Sampler ...");
//			Process samplerProcess = startSampler();

			demoAppProcess.waitFor();
			LOGGER.info("Demo Application terminated!");

//			samplerProcess.destroy();
//			LOGGER.info("Sampler terminated!");

		} else if (mode.equalsIgnoreCase("shutdown")) {
			LOGGER.info("Trying to shut down...");
			WebServer.triggerServerShutdown(Integer.parseInt(port), "");
		} else {
			LOGGER.error("Invalid value for 1st argument! Valid values are: start / shutdown");
		}
	}

	private static void loadProperties(String demoConfFile) {
		try {
			File confFile = new File(demoConfFile);
			Properties props = new Properties();

			props.load(new FileReader(confFile));

			demoAppOut = props.getProperty(DEMO_APP_OUT_KEY);
			if (demoAppOut == null) {
				throw new RuntimeException("Demo App output not specified!");
			}

			samplerOut = props.getProperty(SAMPLER_OUT_KEY);
			if (samplerOut == null) {
				throw new RuntimeException("Sampler output not specified!");
			}

			spotterOut = props.getProperty(SPOTTER_OUT_KEY);
			if (spotterOut == null) {
				throw new RuntimeException("Spotter output not specified!");
			}

			demoAppJar = props.getProperty(DEMO_APP_JAR_KEY);
			if (demoAppJar == null) {
				throw new RuntimeException("Demo App JAR not specified!");
			}

			spotterConfPath = props.getProperty(SPOTTER_CONF_PATH_KEY);
			if (spotterConfPath == null) {
				throw new RuntimeException("Spotter configuration file not specified!");
			}

			samplerConfPath = props.getProperty(SAMPLER_CONF_PATH_KEY);
			if (samplerConfPath == null) {
				throw new RuntimeException("Sampler configuration file not specified!");
			}

			instrumentationAgentJar = props.getProperty(INSTRUMENTATION_AGENT_JAR_KEY);
			if (instrumentationAgentJar == null) {
				throw new RuntimeException("Instrumentation agent JAR not specified!");
			}

			resourceMonitoringJar = props.getProperty(RESOURCE_MONITORING_JAR_KEY);
			if (resourceMonitoringJar == null) {
				throw new RuntimeException("Resource monitoring JAR not specified!");
			}

		} catch (Exception e) {
			throw new RuntimeException("Demo configuration is not valid!");
		}
	}

	private static Process startDemoApp() throws IOException {

		String javaAgentPath = instrumentationAgentJar;
		if (javaAgentPath.contains(" ")) {
			javaAgentPath = "\"" + javaAgentPath + "\"";
		}
		File jarFile = new File(demoAppJar);
		if (!jarFile.exists()) {
			LOGGER.error("jar file {} does not exist!", jarFile.getAbsolutePath());
			System.exit(0);
		}
		String pathToJar = jarFile.getAbsolutePath().contains(" ") ? "\"" + jarFile.getAbsolutePath() + "\"" : jarFile
				.getAbsolutePath();

		String command = "java -jar" + " -javaagent:" + javaAgentPath + "=port=8888 -Xms2g -Xmx2g "
				+ pathToJar + " " + DEFAULT_PORT;
		System.out.println(command);
		return executeCommand(command, demoAppOut);

	}

	private static Process startSampler() throws IOException {

		File jarFile = new File(resourceMonitoringJar);
		if (!jarFile.exists()) {
			LOGGER.error("jar file {} does not exist!", jarFile.getAbsolutePath());
			System.exit(0);
		}
		String pathToJar = jarFile.getAbsolutePath().contains(" ") ? "\"" + jarFile.getAbsolutePath() + "\"" : jarFile
				.getAbsolutePath();

		String samplerConf = samplerConfPath.contains(" ") ? "\"" + samplerConfPath + "\"" : samplerConfPath;

		String command = "java -jar " + pathToJar + " start " + samplerConf;
		return executeCommand(command, samplerOut);

	}

	private static Process executeCommand(String command, String output) throws IOException {
		final Process process = Runtime.getRuntime().exec(command);
		final FileWriter fWriter = new FileWriter(output);
		final FileWriter fWriterError = new FileWriter(output + ".err");
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					BufferedReader bReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
					String line = bReader.readLine();
					while (line != null) {
						fWriterError.write(line);
						fWriterError.write(System.getProperty("line.separator"));
						fWriterError.flush();
						line = bReader.readLine();
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					if (fWriterError != null) {
						try {
							fWriterError.close();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}

			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {

					BufferedReader bReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line = bReader.readLine();
					while (line != null) {
						fWriter.write(line);
						fWriter.write(System.getProperty("line.separator"));
						fWriter.flush();
						line = bReader.readLine();
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					if (fWriter != null) {
						try {
							fWriter.close();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}

			}
		}).start();
		return process;
	}
}
