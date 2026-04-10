package cs321.btree;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

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
        int n; // number of keys currently stored
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

    // Constructor 1: filename only
    public BTree(String fileName) {
        this.degree = 2;
        this.fileName = fileName;
        this.size = 0;
        this.numberOfNodes = 1;
        this.height = 0;
        this.root = new BTreeNode(true);
    }

    // Constructor 2: degree + filename
    public BTree(int degree, String fileName) {
        this.degree = degree;
        this.fileName = fileName;
        this.size = 0;
        this.numberOfNodes = 1;
        this.height = 0;
        this.root = new BTreeNode(true);
    }

    // Optional constructor
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

    private void splitChild(BTreeNode parent, int childIndex) {
        BTreeNode fullChild = parent.children[childIndex];
        BTreeNode newChild = new BTreeNode(fullChild.leaf);

        // newChild gets t-1 keys
        newChild.n = degree - 1;

        // copy last t-1 keys from fullChild to newChild
        for (int j = 0; j < degree - 1; j++) {
            newChild.keys[j] = fullChild.keys[j + degree];
        }

        // if not leaf, copy last t children
        if (!fullChild.leaf) {
            for (int j = 0; j < degree; j++) {
                newChild.children[j] = fullChild.children[j + degree];
            }
        }

        // reduce fullChild key count
        fullChild.n = degree - 1;

        // shift parent's children right
        for (int j = parent.n; j >= childIndex + 1; j--) {
            parent.children[j + 1] = parent.children[j];
        }
        parent.children[childIndex + 1] = newChild;

        // shift parent's keys right
        for (int j = parent.n - 1; j >= childIndex; j--) {
            parent.keys[j + 1] = parent.keys[j];
        }

        // move median key up to parent
        parent.keys[childIndex] = fullChild.keys[degree - 1];
        parent.n++;

        numberOfNodes++;
    }

    @Override
    public TreeObject search(String key) throws IOException {
        return search(root, key);
    }

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

    public String[] getSortedKeyArray() {
        List<String> keys = new ArrayList<>();
        collectKeysInOrder(root, keys);
        return keys.toArray(new String[0]);
    }

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