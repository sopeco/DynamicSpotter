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
 * Represents a tree storing objects of generic type T. The number of children
 * that a particular node may have is not restricted. This tree can be
 * serialized.
 */
public class CallTree<T extends Serializable> implements Serializable {

	private static final long serialVersionUID = -7234896331135502491L;

	private CallNode<T> root;

	/**
	 * Create a new tree with an empty root node.
	 */
	public CallTree() {
		this.root = new CallNode<>();
	}

	/**
	 * Create a new tree with a root node containing the given data.
	 */
	public CallTree(T data) {
		this.root = new CallNode<>(data);
	}

	/**
	 * Return the root node of the tree.
	 * 
	 * @return the root element.
	 */
	public CallNode<T> getRootNode() {
		return this.root;
	}

	/**
	 * Set the root node for the tree.
	 * 
	 * @param rootNode
	 *            the root node to set.
	 */
	public void setRootNode(CallNode<T> rootNode) {
		this.root = rootNode;
	}

	/**
	 * Returns the tree as a list of CallNode objects. The elements are inserted
	 * into the list using a preorder traversal of the tree.
	 * 
	 * @return a List<CallNode>.
	 */
	public List<CallNode<T>> toList() {
		List<CallNode<T>> list = new ArrayList<CallNode<T>>();
		walk(root, list);
		return list;
	}

	@Override
	public String toString() {
		return toList().toString();
	}

	// Traverses the tree in preorder
	private void walk(CallNode<T> element, List<CallNode<T>> list) {
		list.add(element);
		for (CallNode<T> child : element.getChildren()) {
			walk(child, list);
		}
	}

}
