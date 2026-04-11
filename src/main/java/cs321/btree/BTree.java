package cs321.btree;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


/** A B-Tree implementation that supports insertion, searching, 
 * and dumping to file. The delete operation and database 
 * dumping are not implemented in this version. 
 * The B-Tree is designed to store TreeObjects, which contain a key and a count.
 * The B-Tree maintains properties such as size, number of nodes, and height.
 * @author Jacob Smith, Jonah Elliott
 */
public class BTree implements BTreeInterface {

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
        BTreeNode[] children;

        BTreeNode(boolean leaf) {
            this.leaf = leaf;
            this.n = 0;
            this.keys = new TreeObject[2 * degree - 1];
            this.children = new BTreeNode[2 * degree];
        }
    }

    /**
     * Constructor for BTree with specified file name. Initializes an empty B-Tree with the given file name and a default degree of 2.
     * @param fileName the name of the file to dump to.
     */
    public BTree(String fileName) {
        this.degree = 2;
        this.fileName = fileName;
        this.size = 0;
        this.numberOfNodes = 1;
        this.height = 0;
        this.root = new BTreeNode(true);
    }
    
    /**
     * Constructor for BTree with specified degree and file name. Initializes an empty B-Tree with the given degree and file name.
     * @param degree the minimum degree of the B-Tree
     * @param fileName the name of the file to dump to
     */
    public BTree(int degree, String fileName) {
        this.degree = degree;
        this.fileName = fileName;
        this.size = 0;
        this.numberOfNodes = 1;
        this.height = 0;
        this.root = new BTreeNode(true);
    }

    /**
     * Constructor for BTree with specified degree. Initializes an empty B-Tree with the given degree.
     * @param degree the minimum degree of the B-Tree
     */
    public BTree(int degree) {
        this.degree = degree;
        this.fileName = null;
        this.size = 0;
        this.numberOfNodes = 1;
        this.height = 0;
        this.root = new BTreeNode(true);
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

            if (node.children[i].n == 2 * degree - 1) {
                splitChild(node, i);

                if (obj.compareTo(node.keys[i]) > 0) {
                    i++;
                }
            }

            insertNonFull(node.children[i], obj);
        }
    }

    /**
     * Splits the child node at childIndex of the given parent node.
     * @param parent the node whose child is being split
     * @param childIndex the index of the child to split
     */
    private void splitChild(BTreeNode parent, int childIndex) {
        BTreeNode fullChild = parent.children[childIndex];
        BTreeNode newChild = new BTreeNode(fullChild.leaf);

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
        // not implemented
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

    /**
     * Writes the B-Tree to disk in a binary format. 
     * The format includes the degree, size, number 
     * of nodes, height, and all TreeObjects in sorted order.
     * @throws IOException if an I/O error occurs while writing to the file
     */
    public void diskWrite() throws IOException {
        if (fileName == null) {
            return;
        }

        List<TreeObject> objects = new ArrayList<>();
        collectObjectsInOrder(root, objects);

        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName))) {
            out.writeInt(degree);
            out.writeLong(size);
            out.writeLong(numberOfNodes);
            out.writeInt(height);

            out.writeInt(objects.size());
            for (TreeObject obj : objects) {
                out.writeUTF(obj.getKey());
                out.writeLong(obj.getCount());
            }
        }
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