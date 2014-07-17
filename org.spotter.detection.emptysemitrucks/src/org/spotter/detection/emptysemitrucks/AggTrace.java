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
package org.spotter.detection.emptysemitrucks;

import java.util.ArrayList;
import java.util.List;

public class AggTrace {
	public static final String LOOP_STR = "LOOP";
	private static final long PER_CENT = 100;
	private List<AggTrace> subTraces;
	private AggTrace parent;
	private String methodName;
	private boolean loop;
	private int loopCount;
	private boolean sendMethod;
	private long overhead;
	private long payload;
	
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

	

	public String getPathToParentString(){
		String result = "";
		AggTrace currentTrace = this;
		int depth = -1;
		while(currentTrace != null){
			depth++;
			currentTrace = currentTrace.getParent();
		}
		
		currentTrace = this;
		int currentDepth = depth;
		while(currentTrace != null){
			String indention = "";
			for(int i = 1; i <= currentDepth; i++){
				indention += "   ";
			}
			result += indention + currentTrace.getMethodName();
			if(currentTrace.isLoop()){
				result += " ["+currentTrace.getLoopCount()+"]";
			}
			result += "\n";
			currentTrace = currentTrace.getParent();
			currentDepth--;
		}
		return result;
		
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
		} else if(isSendMethod()){
			strBuilder.append(" ***SENT: ");
			strBuilder.append(getOverhead());
			strBuilder.append(" | ");
			strBuilder.append(getPayload());
			strBuilder.append(" Bytes");
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
		if(trace.isSendMethod()){
			aggRootTrace.setSendMethod(true);
			aggRootTrace.setPayload(trace.getPayload());
			aggRootTrace.setOverhead(trace.getOverhead());
		}

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

				sequenceLoop: while (nextSequenceEnd < trace.getSubTraces().size()) {

					nextSequenceHash = calculateSequenceHash(trace.getSubTraces(), nextSequenceStart,
							nextSequenceEnd);
					if (nextSequenceHash == sequenceHash) {

						if (loopTrace == null) {
							loopTrace = new AggTrace(aggRootTrace, LOOP_STR);
							loopTrace.setLoop(true);
							for (int ix = candidateSequenceStartIx; ix <= candidateSequenceEndIx; ix++) {
								fromTrace(trace.getSubTraces().get(ix), loopTrace);
							}
						}

					

						loopCount++;
						loopTrace.setLoopCount(loopCount);
				
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

	/**
	 * @return the sendMethod
	 */
	public boolean isSendMethod() {
		return sendMethod;
	}

	/**
	 * @param sendMethod the sendMethod to set
	 */
	public void setSendMethod(boolean sendMethod) {
		this.sendMethod = sendMethod;
	}

	/**
	 * @return the overhead
	 */
	public long getOverhead() {
		return overhead;
	}

	/**
	 * @param overhead the overhead to set
	 */
	public void setOverhead(long overhead) {
		this.overhead = overhead;
	}

	/**
	 * @return the payload
	 */
	public long getPayload() {
		return payload;
	}

	/**
	 * @param payload the payload to set
	 */
	public void setPayload(long payload) {
		this.payload = payload;
	}

}
