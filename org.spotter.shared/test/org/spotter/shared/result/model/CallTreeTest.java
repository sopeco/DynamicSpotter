package org.spotter.shared.result.model;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class CallTreeTest {

	@Test
	public void testCallTreeCreation() {
		CallTree<String> tree = new CallTree<>();

		Assert.assertNotNull(tree.getRootNode());
		Assert.assertNull(tree.getRootNode().getData());

		CallNode<String> rootNode = new CallNode<String>("testRoot");
		tree.setRootNode(rootNode);
		Assert.assertEquals(1, tree.toList().size());
		Assert.assertEquals(rootNode, tree.toList().get(0));
		Assert.assertEquals(rootNode.getData(), tree.getRootNode().getData());
		tree.toString();
	}

	@Test
	public void testCallNodes() {
		CallTree<String> tree = new CallTree<>("testRoot");
		CallNode<String> rootNode = tree.getRootNode();
		Assert.assertNotNull(rootNode);

		CallNode<String> child_1 = new CallNode<String>("child_1");
		CallNode<String> child_2 = new CallNode<String>("child_2");
		List<CallNode<String>> childList = new ArrayList<>();
		childList.add(child_1);
		childList.add(child_2);
		rootNode.addChild(child_1);
		rootNode.insertChildAt(0, child_2);
		Assert.assertEquals(2, rootNode.getNumberOfChildren());
		Assert.assertEquals(child_2, rootNode.getChildren().get(0));
		Assert.assertEquals(child_1, rootNode.getChildren().get(1));
		rootNode.removeChildAt(0);
		Assert.assertEquals(1, rootNode.getNumberOfChildren());
		rootNode.removeChildAt(0);
		Assert.assertEquals(0, rootNode.getNumberOfChildren());
		rootNode.setData("testRoot2");
		Assert.assertEquals("testRoot2", rootNode.getData());

		rootNode.setChildren(childList);
		Assert.assertEquals(2, rootNode.getNumberOfChildren());
		Assert.assertEquals(child_1, rootNode.getChildren().get(0));
		Assert.assertEquals(child_2, rootNode.getChildren().get(1));

		Assert.assertEquals(rootNode, tree.toList().get(0));
		Assert.assertEquals(child_1, tree.toList().get(1));
		Assert.assertEquals(child_2, tree.toList().get(2));
	}
}
