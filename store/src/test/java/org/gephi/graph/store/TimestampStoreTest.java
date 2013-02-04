package org.gephi.graph.store;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author mbastian
 */
public class TimestampStoreTest {

    @Test
    public void testEmpty() {
        TimestampStore store = new TimestampStore(null);

        Assert.assertEquals(store.size(), 0);
    }

    @Test
    public void testAddTimestamp() {
        TimestampStore store = new TimestampStore(null);

        int pos = store.addTimestamp(1.0);
        Assert.assertEquals(pos, 0);
        int pos2 = store.addTimestamp(2.0);
        Assert.assertEquals(pos2, 1);

        Assert.assertEquals(store.size(), 2);
        Assert.assertEquals(pos, store.getTimestampIndex(1.0));
        Assert.assertEquals(pos2, store.getTimestampIndex(2.0));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddInfinityTimestamp() {
        TimestampStore store = new TimestampStore(null);
        store.addTimestamp(Double.POSITIVE_INFINITY);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testAddNaNTimestamp() {
        TimestampStore store = new TimestampStore(null);
        store.addTimestamp(Double.NaN);
    }

    @Test
    public void testGetTimestampIndex() {
        TimestampStore store = new TimestampStore(null);

        int pos = store.getTimestampIndex(1.0);
        Assert.assertEquals(pos, 0);
        Assert.assertEquals(store.size(), 1);
    }

    @Test
    public void testContains() {
        TimestampStore store = new TimestampStore(null);

        store.addTimestamp(1.0);
        Assert.assertTrue(store.contains(1.0));
        Assert.assertFalse(store.contains(2.0));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testContainsNaN() {
        TimestampStore store = new TimestampStore(null);
        store.contains(Double.NaN);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testContainsInfinity() {
        TimestampStore store = new TimestampStore(null);
        store.contains(Double.POSITIVE_INFINITY);
    }

    @Test
    public void testRemoveTimestamp() {
        TimestampStore store = new TimestampStore(null);

        store.addTimestamp(1.0);
        store.removeTimestamp(1.0);

        Assert.assertEquals(store.size(), 0);
        Assert.assertFalse(store.contains(1.0));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoveNaN() {
        TimestampStore store = new TimestampStore(null);
        store.removeTimestamp(Double.NaN);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRemoveInfinity() {
        TimestampStore store = new TimestampStore(null);
        store.removeTimestamp(Double.POSITIVE_INFINITY);
    }

    @Test
    public void testGarbage() {
        TimestampStore store = new TimestampStore(null);

        store.addTimestamp(1.0);
        int pos = store.addTimestamp(2.0);
        store.addTimestamp(3.0);
        store.removeTimestamp(2.0);

        Assert.assertEquals(1, store.garbageQueue.size());
        Assert.assertEquals(pos, store.garbageQueue.firstInt());
        Assert.assertEquals(2, store.size());

        int pos2 = store.addTimestamp(6.0);

        Assert.assertEquals(pos, pos2);
        Assert.assertTrue(store.garbageQueue.isEmpty());
        Assert.assertEquals(3, store.size());
    }

    @Test
    public void testClear() {
        TimestampStore store = new TimestampStore(null);
        store.clear();

        store.addTimestamp(1.0);

        store.clear();

        Assert.assertEquals(0, store.size());
    }

    @Test
    public void testGetMin() {
        TimestampStore store = new TimestampStore(null);
        Assert.assertEquals(store.getMin(), Double.NEGATIVE_INFINITY);

        store.addTimestamp(1.0);
        Assert.assertEquals(store.getMin(), 1.0);

        store.addTimestamp(2.0);
        Assert.assertEquals(store.getMin(), 1.0);

        store.removeTimestamp(1.0);
        Assert.assertEquals(store.getMin(), 2.0);
    }

    @Test
    public void testGetMax() {
        TimestampStore store = new TimestampStore(null);
        Assert.assertEquals(store.getMax(), Double.POSITIVE_INFINITY);

        store.addTimestamp(1.0);
        Assert.assertEquals(store.getMax(), 1.0);

        store.addTimestamp(2.0);
        Assert.assertEquals(store.getMax(), 2.0);

        store.removeTimestamp(2.0);
        Assert.assertEquals(store.getMax(), 1.0);
    }

    @Test
    public void testAddElement() {
        TimestampStore store = new TimestampStore(null);
        int index = store.getTimestampIndex(1.0);

        NodeImpl nodeImpl = new NodeImpl(0);

        store.addElement(index, nodeImpl);

        Assert.assertEquals(getArrayFromIterable(store.getNodes(1.0))[0], nodeImpl);
    }

    @Test
    public void testRemoveElement() {
        TimestampStore store = new TimestampStore(null);
        int index = store.getTimestampIndex(1.0);

        NodeImpl nodeImpl = new NodeImpl(0);

        store.addElement(index, nodeImpl);
        store.removeElement(index, nodeImpl);

        Assert.assertEquals(getArrayFromIterable(store.getNodes(1.0)).length, 0);
    }

    @Test
    public void testGetElements() {
        TimestampStore store = new TimestampStore(null);

        int index1 = store.getTimestampIndex(1.0);
        int index2 = store.getTimestampIndex(2.0);
        int index3 = store.getTimestampIndex(3.0);

        NodeImpl n0 = new NodeImpl(0);
        NodeImpl n1 = new NodeImpl(1);
        NodeImpl n2 = new NodeImpl(2);
        NodeImpl n3 = new NodeImpl(3);

        store.addElement(index1, n0);
        store.addElement(index1, n1);
        store.addElement(index2, n2);
        store.addElement(index3, n3);

        ObjectSet r1 = new ObjectOpenHashSet(getArrayFromIterable(store.getNodes(1.0, 1.0)));
        Assert.assertTrue(r1.contains(n0));
        Assert.assertTrue(r1.contains(n1));
        Assert.assertEquals(r1.size(), 2);

        ObjectSet r2 = new ObjectOpenHashSet(getArrayFromIterable(store.getNodes(-1, 1.9)));
        Assert.assertTrue(r2.contains(n0));
        Assert.assertTrue(r2.contains(n1));
        Assert.assertEquals(r2.size(), 2);

        ObjectSet r3 = new ObjectOpenHashSet(getArrayFromIterable(store.getNodes(-1, 2.0)));
        Assert.assertTrue(r3.contains(n0));
        Assert.assertTrue(r3.contains(n1));
        Assert.assertTrue(r3.contains(n2));
        Assert.assertEquals(r3.size(), 3);

        ObjectSet r4 = new ObjectOpenHashSet(getArrayFromIterable(store.getNodes(2.0, 2.0)));
        Assert.assertTrue(r4.contains(n2));
        Assert.assertEquals(r4.size(), 1);

        ObjectSet r5 = new ObjectOpenHashSet(getArrayFromIterable(store.getNodes(2.0, 3.5)));
        Assert.assertTrue(r5.contains(n2));
        Assert.assertTrue(r5.contains(n3));
        Assert.assertEquals(r5.size(), 2);
    }

    @Test
    public void testHasNodesEdgesEmpty() {
        TimestampStore store = new TimestampStore(null);
        Assert.assertFalse(store.hasNodes());
        Assert.assertFalse(store.hasEdges());
    }

    @Test
    public void testHasNodes() {
        TimestampStore store = new TimestampStore(null);
        int index = store.getTimestampIndex(1.0);
        int index2 = store.getTimestampIndex(2.0);
        Assert.assertFalse(store.hasNodes());

        NodeImpl nodeImpl = new NodeImpl(0);

        store.addElement(index, nodeImpl);
        store.addElement(index2, nodeImpl);
        Assert.assertTrue(store.hasNodes());

        store.removeElement(index, nodeImpl);
        Assert.assertTrue(store.hasNodes());
        store.removeElement(index2, nodeImpl);
        Assert.assertFalse(store.hasNodes());
    }

    @Test
    public void testHasNodesClear() {
        TimestampStore store = new TimestampStore(null);
        int index = store.getTimestampIndex(1.0);

        NodeImpl nodeImpl = new NodeImpl(0);

        store.addElement(index, nodeImpl);
        store.clear();
        Assert.assertFalse(store.hasNodes());
    }

    @Test
    public void testIndexNode() {
        GraphStore graphStore = new GraphStore();
        TimestampStore store = graphStore.timestampStore;

        NodeImpl nodeImpl = (NodeImpl)graphStore.factory.newNode(0);
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        Assert.assertEquals(store.size(), 2);

        store.index(nodeImpl);
        
        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.getNodes(1.0, 2.0)));
        Assert.assertTrue(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 1);
    }
    
    @Test
    public void testIndexNodeAdd() {
        GraphStore graphStore = new GraphStore();
        TimestampStore store = graphStore.timestampStore;

        NodeImpl nodeImpl = (NodeImpl)graphStore.factory.newNode(0);
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        Assert.assertEquals(store.size(), 2);

        graphStore.addNode(nodeImpl);
        
        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.getNodes(1.0, 2.0)));
        Assert.assertTrue(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 1);
    }
    
    @Test
    public void testClearNode() {
        GraphStore graphStore = new GraphStore();
        TimestampStore store = graphStore.timestampStore;

        NodeImpl nodeImpl = (NodeImpl)graphStore.factory.newNode(0);
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        Assert.assertEquals(store.size(), 2);

        store.index(nodeImpl);
        store.clear(nodeImpl);
        
        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.getNodes(1.0, 2.0)));
        Assert.assertFalse(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 0);
    }
    
    @Test
    public void testClearRemove() {
        GraphStore graphStore = new GraphStore();
        TimestampStore store = graphStore.timestampStore;

        NodeImpl nodeImpl = (NodeImpl)graphStore.factory.newNode(0);
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        Assert.assertEquals(store.size(), 2);

        graphStore.addNode(nodeImpl);
        graphStore.removeNode(nodeImpl);
        
        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.getNodes(1.0, 2.0)));
        Assert.assertFalse(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 0);
    }
    
    @Test
    public void testAddAfterAdd() {
        GraphStore graphStore = new GraphStore();
        TimestampStore store = graphStore.timestampStore;

        NodeImpl nodeImpl = (NodeImpl)graphStore.factory.newNode(0);
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        graphStore.addNode(nodeImpl);
        nodeImpl.addTimestamp(3.0);
        
        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.getNodes(3.0, 3.0)));
        Assert.assertTrue(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 1);
    }
    
    @Test
    public void testRemoveAfterAdd() {
        GraphStore graphStore = new GraphStore();
        TimestampStore store = graphStore.timestampStore;

        NodeImpl nodeImpl = (NodeImpl)graphStore.factory.newNode(0);
        nodeImpl.addTimestamp(1.0);
        nodeImpl.addTimestamp(2.0);

        graphStore.addNode(nodeImpl);
        nodeImpl.removeTimestamp(1.0);
        
        ObjectSet r = new ObjectOpenHashSet(getArrayFromIterable(store.getNodes(1.0, 1.0)));
        Assert.assertFalse(r.contains(nodeImpl));
        Assert.assertEquals(r.size(), 0);
        
        ObjectSet r2 = new ObjectOpenHashSet(getArrayFromIterable(store.getNodes(2.0, 2.0)));
        Assert.assertTrue(r2.contains(nodeImpl));
        Assert.assertEquals(r2.size(), 1);
    }

    //UTILITY
    private <T> Object[] getArrayFromIterable(Iterable<T> iterable) {
        List<T> list = new ArrayList<T>();
        for (T t : iterable) {
            list.add(t);
        }
        return list.toArray();
    }
}