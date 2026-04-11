package cs321.btree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class BTree implements BTreeInterface {

    private int METADATA_SIZE = Long.BYTES; //offset from root node
    private long nextDiskAddress = METADATA_SIZE;
    private FileChannel file;
    private ByteBuffer buffer;
    private int nodeSize;
    private File fileObj;

    private long rootAddress = METADATA_SIZE;

    private final int degree;
    private long size;
    private long numberOfNodes;
    private int height;
    private String fileName;

    private BTreeNode root;

    /**
     * Internal B-Tree node class.
     */
private class BTreeNode {
    int n;
    boolean leaf;
    TreeObject[] keys;

    long[] children;   
    long address;      

    BTreeNode(boolean leaf, boolean isNew) {
        this.leaf = leaf;
        this.n = 0;

        this.keys = new TreeObject[2 * degree - 1];
        //this.children = new BTreeNode[2 * degree];
        this.children = new long[2 * degree]; //proposed change
        //TODO change all references to children to work with type long instead of type BTreeNode
        /* This way the child pointers can be stored on disk without loading
        everything into memory all at once */

        for (int i = 0; i < children.length; i++) {
            children[i] = 0;
        }

        if (isNew) {
            this.address = nextDiskAddress;
            nextDiskAddress += nodeSize;
        }
    }
}

    public BTree(String fileName) {
        this(2, fileName);
    }

    public BTree(int degree, String fileName) {
        this.degree = degree;
        this.fileName = fileName;
        this.size = 0;
        this.numberOfNodes = 1;
        this.height = 0;
        this.root = new BTreeNode(true, true);

        File fileObj = new File(fileName);

		try {
			if (!fileObj.exists()) {
				fileObj.createNewFile();
				RandomAccessFile dataFile = new RandomAccessFile(fileName,
						"rw");
				file = dataFile.getChannel();
				writeMetaData();
			} else {
				RandomAccessFile dataFile = new RandomAccessFile(fileName,
						"rw");
				file = dataFile.getChannel();
				readMetaData();
				root = diskRead(rootAddress);
			}
		} catch (FileNotFoundException e) {
			System.err.println(e);
		} catch (IOException e) {
            System.err.println(e);
        }
    }

    public BTree(int degree) throws IOException {
        this.degree = degree;
        this.fileName = null;
        this.size = 0;
        this.numberOfNodes = 1;
        this.height = 0;
        this.root = new BTreeNode(true, true);
    }

    /**
     * Reads in the metadata from the file
     * 
     * @throws IOException
     */
    public void readMetaData() throws IOException {
        file.position(0);

		ByteBuffer tmpbuffer = ByteBuffer.allocateDirect(METADATA_SIZE);

		tmpbuffer.clear();
		file.read(tmpbuffer);

		tmpbuffer.flip();
		rootAddress = tmpbuffer.getLong();
    }

    /**
     * Writes the metadata to the file
     * 
     * @throws IOException
     */
    public void writeMetaData() throws IOException {
        file.position(0);

		ByteBuffer tmpbuffer = ByteBuffer.allocateDirect(METADATA_SIZE);

		tmpbuffer.clear();
		tmpbuffer.putLong(rootAddress);

		tmpbuffer.flip();
		file.write(tmpbuffer);
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public int getDegree() {
        return degree;
    }

    @Override
    public long getNumberOfNodes() {
        return numberOfNodes;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void insert(TreeObject obj) throws IOException {
        TreeObject existing = search(obj.getKey());
        if (existing != null) {
            existing.incCount();
            return;
        }

        if (root.n == 2 * degree - 1) {
            BTreeNode newRoot = new BTreeNode(false, true);
            newRoot.children[0] = root.address; //proposed change
            //newRoot.children[0] = root;
            splitChild(newRoot, 0);
            root = newRoot;
            numberOfNodes++;
            height++;
        }

        insertNonFull(root, obj);
        size++;
    }

    /**
     * Helper method to insert a key into a node that is guaranteed to be non-full.
     * @param node the node to insert into
     * @param obj the TreeObject to insert
     */
    private void insertNonFull(BTreeNode node, TreeObject obj) {
        int i = node.n - 1;

        if (node.leaf) {
            while (i >= 0 && obj.compareTo(node.keys[i]) < 0) {
                node.keys[i + 1] = node.keys[i];
                i--;
            }
            node.keys[i + 1] = obj;
            node.n++;
        } else {
            while (i >= 0 && obj.compareTo(node.keys[i]) < 0) {
                i--;
            }
            i++;

            BTreeNode child = diskRead(node.children[i])

            if (child.n == 2 * degree - 1) {
                splitChild(node, i);

                if (obj.compareTo(node.keys[i]) > 0) {
                    i++;
                }
            }

            insertNonFull(child, obj);
        }
    }

    /**
     * Splits the child node at childIndex of the given parent node.
     * @param parent the node whose child is being split
     * @param childIndex the index of the child to split
     */
    private void splitChild(BTreeNode parent, int childIndex) {

        //BTreeNode fullChild = parent.children[childIndex];
        //BTreeNode newChild = new BTreeNode(fullChild.leaf);

        newChild.n = degree - 1;

        for (int j = 0; j < degree - 1; j++) {
            newChild.keys[j] = fullChild.keys[j + degree];
        }

        if (!fullChild.leaf) {
            for (int j = 0; j < degree; j++) {
                newChild.children[j] = fullChild.children[j + degree];
            }
        }

        fullChild.n = degree - 1;

        for (int j = parent.n; j >= childIndex + 1; j--) {
            parent.children[j + 1] = parent.children[j];
        }
        parent.children[childIndex + 1] = newChild;

        for (int j = parent.n - 1; j >= childIndex; j--) {
            parent.keys[j + 1] = parent.keys[j];
        }

        parent.keys[childIndex] = fullChild.keys[degree - 1];
        parent.n++;

        numberOfNodes++;
    }

    @Override
    public TreeObject search(String key) throws IOException {
        return search(root, key);
    }

    /**
     * Helper method to recursively search for a key in the B-Tree starting from a given node.
     * @param node current node being searched
     * @param key the key to search for
     * @return the TreeObject if found, or null if not found
     */
    private TreeObject search(BTreeNode node, String key) {
        int i = 0;

        while (i < node.n && key.compareTo(node.keys[i].getKey()) > 0) {
            i++;
        }

        if (i < node.n && key.equals(node.keys[i].getKey())) {
            return node.keys[i];
        }

        if (node.leaf) {
            return null;
        }

        return search(node.children[i], key);
    }

    @Override
    public void dumpToFile(PrintWriter out) throws IOException {
        dumpToFile(root, out);
    }

    /**
     * Helper method to perform in-order traversal and write keys to file.
     * @param node current node being traversed
     * @param out PrintWriter to write keys to
     */
    private void dumpToFile(BTreeNode node, PrintWriter out) {
        for (int i = 0; i < node.n; i++) {
            if (!node.leaf) {
                dumpToFile(node.children[i], out);
            }
            out.println(node.keys[i].toString());
        }

        if (!node.leaf) {
            dumpToFile(node.children[node.n], out);
        }
    }

    @Override
    public void dumpToDatabase(String dbName, String tableName) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(String key) {
        // not implemented
    }

    /**
     * Returns an array of all keys in the B-Tree, sorted in ascending order.
     * @return String[] of sorted keys
     */
    public String[] getSortedKeyArray() {
        List<String> keys = new ArrayList<>();
        collectKeysInOrder(root, keys);
        return keys.toArray(new String[0]);
    }

    /**
     * Helper method to perform in-order traversal and collect keys in sorted order.
     * @param node current node being traversed
     * @param keys list to collect keys into
     */
    private void collectKeysInOrder(BTreeNode node, List<String> keys) {
        for (int i = 0; i < node.n; i++) {
            if (!node.leaf) {
                collectKeysInOrder(node.children[i], keys);
            }
            keys.add(node.keys[i].getKey());
        }

        if (!node.leaf) {
            collectKeysInOrder(node.children[node.n], keys);
        }
    }

    // ------------------------
    // Disk Write / Read
    // ------------------------

    /**
     * 
     * @param diskAddress
     * 
     * @return the Node object
     * @throws IOException
     */
    private BTreeNode diskRead(long diskAddress) throws IOException {
        //FIXME finish diskRead
        if (diskAddress == 0)
            return null;

        file.position(diskAddress);
        buffer.clear();
        file.read(buffer);
        buffer.flip();

        BTreeNode node = new BTreeNode(false, false);

        node.n = buffer.getInt();
        node.leaf = buffer.get() == 1;

        for (int i = 0; i < 2 * degree - 1; i++) {
            long value = buffer.getLong();
            long count = buffer.getLong();
        }

        //FIXME finish diskRead

        long value = buffer.getLong();
        long frequency = buffer.getLong();

        return null;
    }

    /**
     * 
     */
    private void diskWrite(BTreeNode x) throws IOException {
        //FIXME finish diskWrite
        file.position(x.address);
        buffer.clear();

        buffer.putInt(x.n);
        buffer.put((byte)(x.leaf ? 1 : 0));

        for (int i = 0; i < 2 * degree - 1; i++) {
            if (i < x.n) {
                buffer.putLong(x.keys[i].getCount());
            } else {
                buffer.putLong(0);
                buffer.putLong(0);
            }
        }

        for (int i = 0; i < 2 * degree; i++) {
            buffer.putLong(x.children[i]);
        }

        buffer.flip();
        file.write(buffer);
    }

    /**
     * Helper method to insert a TreeObject into the B-Tree without checking for duplicates.
     * @param obj the TreeObject to insert
     */
    private void insertLoaded(TreeObject obj) {
        if (root.n == 2 * degree - 1) {
            BTreeNode newRoot = new BTreeNode(false);
            newRoot.children[0] = root;
            splitChild(newRoot, 0);
            root = newRoot;
            numberOfNodes++;
            height++;
        }

        insertNonFull(root, obj);
        size++;
    }

    /**
     * Helper method to perform in-order traversal and collect TreeObjects in sorted order.
     * @param node current node being traversed
     * @param objects list to collect TreeObjects into
     */
    private void collectObjectsInOrder(BTreeNode node, List<TreeObject> objects) {
        for (int i = 0; i < node.n; i++) {
            if (!node.leaf) {
                collectObjectsInOrder(node.children[i], objects);
            }
            objects.add(node.keys[i]);
        }

        if (!node.leaf) {
            collectObjectsInOrder(node.children[node.n], objects);
        }
    }
}