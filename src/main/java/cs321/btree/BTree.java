package cs321.btree;

import java.io.IOException;
import java.io.PrintWriter;

public class BTree implements BTreeInterface {

    private int degree;
    private long size;              // number of UNIQUE keys in tree
    private long numberOfNodes;
    private int height;             // root-only tree has height 0
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


    private void splitChild(BTreeNode parent, int childIndex) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'dumpToFile'");
    }

    @Override
    public void dumpToFile(PrintWriter out) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'dumpToFile'");
    }

    @Override
    public void dumpToDatabase(String dbName, String tableName) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'dumpToDatabase'");
    }

    @Override
    public TreeObject search(String key) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'search'");
    }

    @Override
    public void delete(String key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
}