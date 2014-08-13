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
package org.spotter.core.instrumentation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.description.InstrumentationDescription;
import org.lpe.common.extension.IExtension;
import org.lpe.common.util.system.LpeSystemUtils;

/**
 * The instrumentation broker manages the distribution of instrumentation
 * commands.
 * 
 * @author Alexander Wert
 * 
 */
public final class InstrumentationBroker implements IInstrumentationAdapter {

	private static InstrumentationBroker instance;

	/**
	 * 
	 * @return singleton instance
	 */
	public static synchronized InstrumentationBroker getInstance() {
		if (instance == null) {
			instance = new InstrumentationBroker();
		}
		return instance;
	}

	private final List<IInstrumentationAdapter> instrumentationControllers;

	/**
	 * Constructor.
	 * 
	 * @param instrumentationControllers
	 *            instrumentation controllers to manage
	 */
	private InstrumentationBroker() {
		this.instrumentationControllers = new ArrayList<IInstrumentationAdapter>();

	}

	/**
	 * sets a collection of controllers.
	 * 
	 * @param instrumentationControllers
	 *            controllers
	 */
	public void setControllers(Collection<IInstrumentationAdapter> instrumentationControllers) {
		this.instrumentationControllers.clear();
		this.instrumentationControllers.addAll(instrumentationControllers);
	}

	@Override
	public void initialize() throws InstrumentationException {
		try {
			List<Future<?>> tasks = new ArrayList<>();

			for (IInstrumentationAdapter instController : instrumentationControllers) {
				tasks.add(LpeSystemUtils.submitTask(new InitializeTask(instController)));
			}
			// wait for termination of all initialization tasks
			for (Future<?> task : tasks) {
				task.get();
			}
		} catch (InterruptedException | ExecutionException e) {
			throw new InstrumentationException(e);
		}

	}

	@Override
	public void instrument(InstrumentationDescription description) throws InstrumentationException {
		try {
			if (description == null) {
				throw new InstrumentationException("Instrumentation description must not be null!");
			}
			List<Future<?>> tasks = new ArrayList<>();
			for (IInstrumentationAdapter instController : instrumentationControllers) {

				tasks.add(LpeSystemUtils.submitTask(new InstrumentTask(instController, description)));
			}
			// wait for termination of all instrumentation tasks
			for (Future<?> task : tasks) {
				task.get();
			}
		} catch (InterruptedException | ExecutionException e) {
			throw new InstrumentationException(e);
		}

	}

	@Override
	public void uninstrument() throws InstrumentationException {
		try {
			List<Future<?>> tasks = new ArrayList<>();
			for (IInstrumentationAdapter instController : instrumentationControllers) {

				tasks.add(LpeSystemUtils.submitTask(new UninstrumentTask(instController)));
			}

			// wait for termination of all uninstrumentation tasks
			for (Future<?> task : tasks) {
				task.get();
			}
		} catch (InterruptedException | ExecutionException e) {
			throw new InstrumentationException(e);
		}

	}

	@Override
	public Properties getProperties() {
		Properties props = new Properties();
		for (IInstrumentationAdapter instController : instrumentationControllers) {
			props.putAll(instController.getProperties());
		}
		return props;
	}

	@Override
	public IExtension<?> getProvider() {
		return null;
	}

	private abstract class Task implements Runnable {

		@Override
		public void run() {
			try {

				executeTask();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}

		protected abstract void executeTask() throws InstrumentationException;

	}

	private class InstrumentTask extends Task {
		IInstrumentationAdapter instController;
		InstrumentationDescription description;

		public InstrumentTask(IInstrumentationAdapter instController, InstrumentationDescription description)
				throws InterruptedException {

			this.instController = instController;
			this.description = description;
		}

		@Override
		protected void executeTask() throws InstrumentationException {

			String csListIncludes = instController.getProperties().getProperty(
					IInstrumentationAdapter.INSTRUMENTATION_INCLUDES);
			csListIncludes = (csListIncludes == null || csListIncludes.isEmpty()) ? null : csListIncludes;
			if (csListIncludes != null) {
				String[] includesArr = csListIncludes.split(",");
				for (String inc : includesArr) {
					description.getGlobalRestriction().getPackageIncludes().add(inc);
				}
			}
			String csListExcludes = instController.getProperties().getProperty(
					IInstrumentationAdapter.INSTRUMENTATION_EXCLUDES);
			csListExcludes = (csListExcludes == null || csListExcludes.isEmpty()) ? null : csListExcludes;

			if (csListExcludes != null) {
				String[] excludesArr = csListExcludes.split(",");
				for (String exc : excludesArr) {
					description.getGlobalRestriction().getPackageExcludes().add(exc);
				}
			}
			instController.instrument(description);

		}
	}

	private class UninstrumentTask extends Task {
		IInstrumentationAdapter instController;

		public UninstrumentTask(IInstrumentationAdapter instController) throws InterruptedException {
			this.instController = instController;
		}

		@Override
		protected void executeTask() throws InstrumentationException {
			instController.uninstrument();

		}
	}

	private class InitializeTask extends Task {
		private IInstrumentationAdapter instController;

		public InitializeTask(IInstrumentationAdapter instController) throws InterruptedException {
			this.instController = instController;
		}

		@Override
		protected void executeTask() throws InstrumentationException {
			instController.initialize();

		}
	}

	@Override
	public String getName() {
		return "Instrumentation Broker";
	}

	@Override
	public String getPort() {
		return "NA";
	}

	@Override
	public String getHost() {
		return "localhost";
	}

	@Override
	public void setProperties(Properties properties) {
		// nothing to do
	}

	/**
	 * Returns a list of instrumentation controllers of the given type.
	 * 
	 * @param type
	 *            type of interest
	 * @return list of instrumentation controllers of the given type
	 * @param <T>
	 *            Class type of the controllers
	 */
	@SuppressWarnings("unchecked")
	public <T extends IInstrumentationAdapter> List<T> getInstrumentationControllers(Class<T> type) {
		List<T> result = new ArrayList<>();

		for (IInstrumentationAdapter controller : instrumentationControllers) {
			if (type.isAssignableFrom(controller.getClass())) {
				result.add((T) controller);
			}
		}

		return result;
	}

}
