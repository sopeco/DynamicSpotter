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
package org.spotter.detection.highmem;

import java.util.ArrayList;
import java.util.List;

public class AggTrace {
	private static final String LOOP_STR = "LOOP";
	private static final long PER_CENT = 100;
	private List<AggTrace> subTraces;
	private AggTrace parent;
	private String methodName;
	private long totalMemoryFootprint;
	private boolean loop;
	private int loopCount;

	/**
	 * Constructor.
	 */
	public AggTrace() {
		setParent(null);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            parent method
	 */
	public AggTrace(AggTrace parent) {
		setParent(parent);
	}

	/**
	 * Constructor.
	 * 
	 * @param methodName
	 *            name of the current method
	 */
	public AggTrace(String methodName) {
		setParent(null);
		setMethodName(methodName);
	}

	/**
	 * Constructor.
	 * 
	 * @param methodName
	 *            name of the current method
	 * @param parent
	 *            parent method
	 */
	public AggTrace(AggTrace parent, String methodName) {
		setParent(parent);
		setMethodName(methodName);
	}

	/**
	 * @return the subTraces
	 */
	public List<AggTrace> getSubTraces() {
		if (subTraces == null) {
			subTraces = new ArrayList<>();
		}
		return subTraces;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @param methodName
	 *            the methodName to set
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * @return the totalMemoryFootprint [bytes]
	 */
	public long getTotalMemoryFootprint() {
		return totalMemoryFootprint;
	}

	/**
	 * @param totalMemoryFootprint
	 *            the totalMemoryFootprint to set [bytes]
	 */
	public void setTotalMemoryFootprint(long totalMemoryFootprint) {
		this.totalMemoryFootprint = totalMemoryFootprint;
	}

	/**
	 * 
	 * @return the footprint of this method excluding the footprints of child
	 *         methods
	 */
	public long getOwnFootprint() {
		long footprint = getTotalMemoryFootprint();
		for (AggTrace child : getSubTraces()) {
			footprint -= child.getTotalMemoryFootprint();
		}
		return footprint;
	}

	/**
	 * @return the parent
	 */
	public AggTrace getParent() {
		return parent;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(AggTrace parent) {
		this.parent = parent;
		if (parent != null && !parent.getSubTraces().contains(this)) {
			parent.getSubTraces().add(this);
		}
	}

	/**
	 * @return the loop
	 */
	public boolean isLoop() {
		return loop;
	}

	/**
	 * @param loop
	 *            the loop to set
	 */
	public void setLoop(boolean loop) {
		this.loop = loop;
	}

	/**
	 * @return the loopCount
	 */
	public int getLoopCount() {
		return loopCount;
	}

	/**
	 * @param loopCount
	 *            the loopCount to set
	 */
	public void setLoopCount(int loopCount) {
		this.loopCount = loopCount;
	}

	@Override
	public String toString() {
		int depth = 0;
		AggTrace parent = getParent();
		AggTrace root = this;
		while (parent != null) {
			depth++;
			root = parent;
			parent = parent.getParent();
		}
		StringBuilder indention = new StringBuilder();
		for (int i = 0; i < depth; i++) {
			indention.append("   ");
		}

		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(indention.toString());
		strBuilder.append(getMethodName());
		if (isLoop()) {
			strBuilder.append(" [");
			strBuilder.append(getLoopCount());
			strBuilder.append("]");
		}
		strBuilder.append("   ");
		strBuilder.append(getTotalMemoryFootprint());
		strBuilder.append("   ");
		strBuilder.append(getOwnFootprint());
		strBuilder.append("   ");
		if (root.getTotalMemoryFootprint() != 0) {
			strBuilder.append(getOwnFootprint() * PER_CENT / root.getTotalMemoryFootprint());
		} else {
			strBuilder.append("NA");
		}

		strBuilder.append("\n");
		for (AggTrace child : getSubTraces()) {
			strBuilder.append(child.toString());
		}

		return strBuilder.toString();
	}

	/**
	 * 
	 * @param other
	 *            trace to compare with
	 * @return returns true if other trace represents the same operation
	 *         sequence
	 */
	public boolean similarTrace(Trace other) {
		if (!getMethodName().equals(other.getMethodName())) {
			return false;
		}

		for (int i = 0; i < getSubTraces().size(); i++) {
			if (!getSubTraces().get(i).similarTrace(other.getSubTraces().get(i))) {
				return false;
			}
		}

		return true;
	}

	public static AggTrace fromTrace(Trace rootTrace) {
		if (rootTrace == null) {
			return null;
		}
		if (rootTrace.getParent() != null) {
			throw new IllegalArgumentException(
					"Cannot convert trace which is not a root (parent not null) into an aggregated trace.");
		}

		return fromTrace(rootTrace, null);

	}

	private static AggTrace fromTrace(Trace trace, AggTrace parentAggTrace) {
		AggTrace aggRootTrace = new AggTrace(parentAggTrace, trace.getMethodName());
		aggRootTrace.setTotalMemoryFootprint(trace.getTotalMemoryFootprint());

//		for (Trace child : trace.getSubTraces()) {
//
//			aggRootTrace.getSubTraces().add(fromTrace(child, aggRootTrace));
//		}

		int i = 0;
		while (i < trace.getSubTraces().size() - 1) {

			int currentTraceHash = trace.getSubTraces().get(i).hashCode();
			int candidateSequenceStartIx = i;
			int candidateSequenceEndIx = i;
			int j = i + 1;
			int nextTraceHash = trace.getSubTraces().get(j).hashCode();
			while (currentTraceHash != nextTraceHash && j < trace.getSubTraces().size() - 1) {
				j++;
				nextTraceHash = trace.getSubTraces().get(j).hashCode();
			}
			AggTrace loopTrace = null;
			if (currentTraceHash == nextTraceHash && (j - 1) + (j - i) < trace.getSubTraces().size()) {

				candidateSequenceEndIx = j - 1;
				int sequenceLength = j - i;
				int sequenceHash = calculateSequenceHash(trace.getSubTraces(), candidateSequenceStartIx,
						candidateSequenceEndIx);

				int nextSequenceStart = candidateSequenceEndIx + 1;
				int nextSequenceEnd = candidateSequenceEndIx + sequenceLength;
				int nextSequenceHash = 0;
				int loopCount = 1;

				long totalMemoryUsage = 0;
				sequenceLoop: while (nextSequenceEnd < trace.getSubTraces().size()) {

					nextSequenceHash = calculateSequenceHash(trace.getSubTraces(), nextSequenceStart,
							nextSequenceEnd);
					if (nextSequenceHash == sequenceHash) {

						if (loopTrace == null) {
							loopTrace = new AggTrace(aggRootTrace, LOOP_STR);
							loopTrace.setLoop(true);
							for (int ix = candidateSequenceStartIx; ix <= candidateSequenceEndIx; ix++) {
								fromTrace(trace.getSubTraces().get(ix), loopTrace);
								totalMemoryUsage += trace.getSubTraces().get(ix).getTotalMemoryFootprint();
							}
						}

						for (int ix = nextSequenceStart; ix <= nextSequenceEnd; ix++) {
							totalMemoryUsage += trace.getSubTraces().get(ix).getTotalMemoryFootprint();
						}

						loopCount++;
						loopTrace.setLoopCount(loopCount);
						loopTrace.setTotalMemoryFootprint(totalMemoryUsage / sequenceLength);

						i = nextSequenceEnd;

						nextSequenceStart += sequenceLength;
						nextSequenceEnd += sequenceLength;

					} else {
						break sequenceLoop;
					}

				}

			}
			if (loopTrace == null) {
				fromTrace(trace.getSubTraces().get(i), aggRootTrace);
			}
			i++;
		}

		if (i < trace.getSubTraces().size()) {
			fromTrace(trace.getSubTraces().get(i), aggRootTrace);
		}

		return aggRootTrace;
	}

	private static int calculateSequenceHash(List<Trace> childList, int candidateSequenceStartIx,
			int candidateSequenceEndIx) {
		int sequenceHash = 0;
		int counter = 1;
		for (int x = candidateSequenceStartIx; x <= candidateSequenceEndIx; x++) {
			sequenceHash += counter * childList.get(x).hashCode();
			counter++;
		}
		return sequenceHash;
	}

}
