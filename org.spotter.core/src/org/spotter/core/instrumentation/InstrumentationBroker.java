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
import java.util.concurrent.Semaphore;

import org.aim.api.exceptions.InstrumentationException;
import org.aim.api.instrumentation.description.InstrumentationDescription;
import org.lpe.common.extension.IExtension;
import org.lpe.common.util.system.LpeSystemUtils;

/**
 * The instrumentation broker manages the distribution of instrumentation
 * commands.
 * 
 * @author Alexander Wert
 * 
 */
public final class InstrumentationBroker implements ISpotterInstrumentation {

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

	private final List<ISpotterInstrumentation> instrumentationControllers;

	/**
	 * Constructor.
	 * 
	 * @param instrumentationControllers
	 *            instrumentation controllers to manage
	 */
	private InstrumentationBroker() {
		this.instrumentationControllers = new ArrayList<ISpotterInstrumentation>();

	}

	/**
	 * sets a collection of controllers.
	 * 
	 * @param instrumentationControllers
	 *            controllers
	 */
	public void setControllers(Collection<ISpotterInstrumentation> instrumentationControllers) {
		this.instrumentationControllers.clear();
		this.instrumentationControllers.addAll(instrumentationControllers);
	}

	@Override
	public void initialize() throws InstrumentationException {
		try {
			final Semaphore semaphore = new Semaphore(instrumentationControllers.size(), true);

			for (ISpotterInstrumentation instController : instrumentationControllers) {
				semaphore.acquire();
				LpeSystemUtils.submitTask(new InitializeTask(semaphore, instController));
			}

			// wait for termination of all instrumentation tasks
			semaphore.acquire(instrumentationControllers.size());
		} catch (InterruptedException e) {
			throw new InstrumentationException(e);
		}

	}

	@Override
	public void instrument(InstrumentationDescription description) throws InstrumentationException {
		try {
			if (description == null) {
				throw new InstrumentationException("Instrumentation description must not be null!");
			}
			final Semaphore semaphore = new Semaphore(instrumentationControllers.size(), true);

			for (ISpotterInstrumentation instController : instrumentationControllers) {
				semaphore.acquire();
				LpeSystemUtils.submitTask(new InstrumentTask(semaphore, instController, description));
			}

			// wait for termination of all instrumentation tasks
			semaphore.acquire(instrumentationControllers.size());
		} catch (InterruptedException e) {
			throw new InstrumentationException(e);
		}

	}

	@Override
	public void uninstrument() throws InstrumentationException {
		try {
			final Semaphore semaphore = new Semaphore(instrumentationControllers.size(), true);

			for (ISpotterInstrumentation instController : instrumentationControllers) {
				semaphore.acquire();
				LpeSystemUtils.submitTask(new UninstrumentTask(semaphore, instController));
			}

			// wait for termination of all instrumentation tasks
			semaphore.acquire(instrumentationControllers.size());
		} catch (InterruptedException e) {
			throw new InstrumentationException(e);
		}

	}

	@Override
	public Properties getProperties() {
		Properties props = new Properties();
		for (ISpotterInstrumentation instController : instrumentationControllers) {
			props.putAll(instController.getProperties());
		}
		return props;
	}

	@Override
	public IExtension<?> getProvider() {
		return null;
	}

	private abstract class Task implements Runnable {

		private Semaphore semaphore;

		public Task(Semaphore semaphore) {
			this.semaphore = semaphore;
		}

		@Override
		public void run() {
			try {

				executeTask();
				semaphore.release();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}

		protected abstract void executeTask() throws InstrumentationException;

	}

	private class InstrumentTask extends Task {
		ISpotterInstrumentation instController;
		InstrumentationDescription description;

		public InstrumentTask(Semaphore semaphore, ISpotterInstrumentation instController,
				InstrumentationDescription description) {
			super(semaphore);
			this.instController = instController;
			this.description = description;
		}

		@Override
		protected void executeTask() throws InstrumentationException {

			String csListIncludes = instController.getProperties().getProperty(
					ISpotterInstrumentation.INSTRUMENTATION_INCLUDES);
			csListIncludes = (csListIncludes == null || csListIncludes.isEmpty()) ? null : csListIncludes;
			if (csListIncludes != null) {
				String[] includesArr = csListIncludes.split(",");
				for (String inc : includesArr) {
					description.getGlobalRestrictions().getInclusions().add(inc);
				}
			}
			String csListExcludes = instController.getProperties().getProperty(
					ISpotterInstrumentation.INSTRUMENTATION_EXCLUDES);
			csListExcludes = (csListExcludes == null || csListExcludes.isEmpty()) ? null : csListExcludes;

			if (csListExcludes != null) {
				String[] excludesArr = csListExcludes.split(",");
				for (String exc : excludesArr) {
					description.getGlobalRestrictions().getExclusions().add(exc);
				}
			}
			instController.instrument(description);

		}
	}

	private class UninstrumentTask extends Task {
		ISpotterInstrumentation instController;

		public UninstrumentTask(Semaphore semaphore, ISpotterInstrumentation instController) {
			super(semaphore);
			this.instController = instController;
		}

		@Override
		protected void executeTask() throws InstrumentationException {
			instController.uninstrument();

		}
	}

	private class InitializeTask extends Task {
		private ISpotterInstrumentation instController;

		public InitializeTask(Semaphore semaphore, ISpotterInstrumentation instController) {
			super(semaphore);
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
	public <T extends ISpotterInstrumentation> List<T> getInstrumentationControllers(Class<T> type) {
		List<T> result = new ArrayList<>();

		for (ISpotterInstrumentation controller : instrumentationControllers) {
			if (type.isAssignableFrom(controller.getClass())) {
				result.add((T) controller);
			}
		}

		return result;
	}

}
