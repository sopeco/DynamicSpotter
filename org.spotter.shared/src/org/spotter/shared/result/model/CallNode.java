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
package org.spotter.shared.result.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node of the CallTree<T> class. It can store any data of type T.
 */
public class CallNode<T extends Serializable> implements Serializable {

	private static final long serialVersionUID = -681028928605289333L;

	public T data;
	public List<CallNode<T>> children;

	/**
	 * Create an empty node.
	 */
	public CallNode() {
	}

	/**
	 * Create a node with the given data.
	 * 
	 * @param data
	 *            the data of this node
	 */
	public CallNode(T data) {
		this.data = data;
	}

	/**
	 * Return the children of this node. In case the node does not contain any
	 * children an empty list is returned.
	 * 
	 * @return the children of this node or an empty list if there are no
	 *         children
	 */
	public List<CallNode<T>> getChildren() {
		if (this.children == null) {
			return new ArrayList<CallNode<T>>();
		}
		return this.children;
	}

	/**
	 * Sets the children of this node.
	 * 
	 * @param children
	 *            the List<CallNode<T>> to set.
	 */
	public void setChildren(List<CallNode<T>> children) {
		this.children = children;
	}

	/**
	 * Returns the number of immediate children of this node.
	 * 
	 * @return the number of immediate children.
	 */
	public int getNumberOfChildren() {
		if (children == null) {
			return 0;
		}
		return children.size();
	}

	/**
	 * Adds a child to the list of children for this node.
	 * 
	 * @param child
	 *            a CallNode<T> object to add.
	 */
	public void addChild(CallNode<T> child) {
		if (children == null) {
			children = new ArrayList<CallNode<T>>();
		}
		children.add(child);
	}

	/**
	 * Inserts a node at the specified position in the children list. An index
	 * equal to the size of the current list is allowed and will add the node at
	 * the end of the list. Indices greater than the size of the list will throw
	 * an exception.
	 * 
	 * @param index
	 *            the position to insert at.
	 * @param child
	 *            the CallNode<T> object to insert.
	 * @throws IndexOutOfBoundsException
	 *             if the index does not exist.
	 */
	public void insertChildAt(int index, CallNode<T> child)
			throws IndexOutOfBoundsException {
		if (index == getNumberOfChildren()) {
			// this is an append
			addChild(child);
			return;
		} else {
			// will throw IndexOutOfBoundsException if negative or greater than
			// current size
			children.get(index);
			// otherwise index valid
			children.add(index, child);
		}
	}

	/**
	 * Remove the child at index <code>index</code>.
	 * 
	 * @param index
	 *            the index of the element to delete.
	 * @throws IndexOutOfBoundsException
	 *             if element does not exist
	 */
	public void removeChildAt(int index) throws IndexOutOfBoundsException {
		children.remove(index);
	}

	/**
	 * @return the data of this node
	 */
	public T getData() {
		return this.data;
	}

	/**
	 * Sets the data of this node.
	 * 
	 * @param data
	 *            the data to set.
	 */
	public void setData(T data) {
		this.data = data;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{" + getData() + ",[");
		int i = 0;
		for (CallNode<T> child : getChildren()) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(child.getData());
			i++;
		}
		sb.append("]}");
		return sb.toString();
	}

}
