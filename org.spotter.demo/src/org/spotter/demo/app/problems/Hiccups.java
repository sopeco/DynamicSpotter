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
package org.spotter.demo.app.problems;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Emulates a hiccup problem.
 * 
 * @author C5170547
 * 
 */
public final class Hiccups {
	private static final int CORE_TIME_TO_SLEEP = 200;
	private static final int HICCUP_DURATION = 100;
	private static final int HICCUP_INTERARRIVAL_TIME = 10 * 1000;
	private static final int LARGE_NUMBER = 10000;
	private static final int GARBAGE_CLEANER_TIMEOUT = 200;
	private static final int NUM_SMALL_GARBAGE = 5;

	private static final int FIB_NUM_1 = 3;
	private static final int FIB_NUM_2 = 4;
	private static final int FIB_NUM_3 = 6;

	private static Hiccups instance;
	private long startTime = -1;
	private long nextHiccup = 0;
	private Random random;
	private BlockingQueue<GarbageObjectA> garbageQueue;

	/**
	 * 
	 * @return singleton instance
	 */
	public static Hiccups getInstnace() {
		if (instance == null) {
			instance = new Hiccups();
		}
		return instance;
	}

	private Hiccups() {
		random = new Random(System.currentTimeMillis());
		garbageQueue = new LinkedBlockingQueue<>();

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(HICCUP_INTERARRIVAL_TIME);
						for (int i = 0; i < LARGE_NUMBER; i++) {
							Object obj = garbageQueue.poll(GARBAGE_CLEANER_TIMEOUT, TimeUnit.MILLISECONDS);
							if (obj == null) {
								break;
							}
						}

					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}

			}
		}).start();
	}

	/**
	 * emulates hiccups.
	 */
	public void hiccup() {
		if (startTime < 0) {
			startTime = System.currentTimeMillis();
			synchronized (this) {
				nextHiccup = startTime + HICCUP_INTERARRIVAL_TIME;
			}

		}

		long currentTime = System.currentTimeMillis();
		if (currentTime < nextHiccup) {
			try {

				Thread.sleep(CORE_TIME_TO_SLEEP + (long) (random.nextDouble() * CORE_TIME_TO_SLEEP));
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		} else {
			synchronized (this) {
				try {
					Thread.sleep(CORE_TIME_TO_SLEEP / 2L + (long) (random.nextDouble() * CORE_TIME_TO_SLEEP));
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

				if (currentTime > nextHiccup + HICCUP_DURATION) {
					nextHiccup = currentTime + HICCUP_INTERARRIVAL_TIME;
				}
			}

		}
	}

	/**
	 * Emulate hiccups by garbage collection.
	 */
	public void garbageHiccup() {
		List<String> aList = new ArrayList<>();
		aList.add(String.valueOf(System.currentTimeMillis()));
		for (int i = 0; i < 10; i++) {
			thirdMethod();
		}

		anotherMethod();

		someMethod();
	}

	/**
	 * Emulate hiccups by garbage collection.
	 */
	public void garbageHiccupWithNoise() {
		if (random.nextInt(2000) < 10) {

			long sleepTime = 1500 + random.nextInt(1500);
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} else {
			List<String> aList = new ArrayList<>();
			aList.add(String.valueOf(System.currentTimeMillis()));
			for (int i = 0; i < 10; i++) {
				thirdMethod();
			}

			anotherMethod();

			someMethod();
		}

	}

	private void someMethod() {
		String a = "aStringalskdaksdiasjdhiausdhiauhsdasd";
		String b = "aStringalskdaksdiasjdhiausdhiauhsdasd";
		String c = a + b;
		List<String> atrList = new ArrayList<>();
		atrList.add(a);
		atrList.add(b);
		atrList.add(c);
	}

	private void anotherMethodB() {
		String a = "aStringalskdaksdiasdhiausdhiauhsdasd";
		String b = "aStringalskdaksdiaasfhiausdhiauhsdasd";
		String c = a + b;
		List<String> atrList = new ArrayList<>();
		atrList.add(a);
		atrList.add(b);
		atrList.add(c);
		guiltyMethod();
	}

	private void thirdMethod() {
		String a = "aStringalskdaksdiasdhiausdhiadshsdasd";
		String b = "aStringskdaksdiaasfhiausdhiauhsdasd";
		String c = a + b;
		List<String> atrList = new ArrayList<>();
		atrList.add(a);
		atrList.add(b);
		atrList.add(c);
		List<Byte> bytes = new ArrayList<>();
		for (int i = 0; i < 1024; i++) {
			bytes.add(new Integer(i).byteValue());
		}
		fourthMethod();
	}

	private void fourthMethod() {
		String a = "aStringalskdaksdiasdhiausdhiadshsdasd";
		String b = "aStringskdaksdiaasfhiausdhiauhsdasd";
		String c = a + b;
		List<String> atrList = new ArrayList<>();
		atrList.add(a);
		atrList.add(b);
		atrList.add(c);
	}

	private void guiltyMethod() {

		thirdMethod();
		garbageQueue.offer(new GarbageObjectA());
		someMethod();

	}

	private void anotherMethod() {
		anotherMethodB();

	}
}
