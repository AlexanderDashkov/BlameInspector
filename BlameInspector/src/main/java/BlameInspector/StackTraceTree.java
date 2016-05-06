package blameinspector;

import com.jmolly.stacktraceparser.NFrame;

import java.util.ArrayList;
import java.util.List;

public class StackTraceTree {

    private Node root;
    private String projectName;
    private int size;

    public StackTraceTree(final String projectName) {
        root = new Node();
        this.projectName = projectName;
        size = 0;
    }

    public int size() {
        return size;
    }


    /*
    * @param stackTrace stackTrace of ticket to be added to the tree
    * @param ticketNumber number of ticket  to be added to the tree
    * @return true, if exactly such StackTrace hasn't mentioned yet,
    * false otherwise
    */
    public ArrayList<Integer> addTicket(final ArrayList<TraceInfo> stackTrace, final int ticketNumber) {
        List<TraceInfo> frames = stackTrace;
        Node currentNode = root;
        Node prevNode = root;
        for (int i = 0; i < frames.size(); i++) {
            prevNode = currentNode;
            TraceInfo traceInfo = frames.get(i);
            NFrame frame = frames.get(i).getFrame();
            if (i + 1 == frames.size()) {
                currentNode.addDuplicate(ticketNumber);
                size++;
                return currentNode.getDuplicates();
            }
            if (currentNode.isFinal) {
                if (i + 1 < frames.size()) {
                    currentNode.addChild(new Node(frame, traceInfo.getFileName(), traceInfo.getLineNumber()));
                } else {
                    currentNode.addDuplicate(ticketNumber);
                    size++;
                    return currentNode.getDuplicates();
                }
            }
            for (Node child : currentNode.getChildren()) {
                if (child.isSimilar(frame, traceInfo)) {
                    currentNode = child;
                    break;
                }
            }
            if (prevNode == currentNode) {
                currentNode.addChild(new Node(frame, traceInfo.getFileName(), traceInfo.getLineNumber()));
                currentNode = currentNode.children.get(currentNode.children.size() - 1);
            }
        }
        return prevNode.getDuplicates();
    }

    public ArrayList<ArrayList<Integer>> getDuplicates() {
        return getDuplicates(root);
    }

    private ArrayList<ArrayList<Integer>> getDuplicates(Node currentRoot) {
        ArrayList<ArrayList<Integer>> result = new ArrayList<>();
        for (Node child : currentRoot.children) {
            if (child.isFinal) {
                if (child.getDuplicates().size() != 1) {
                    result.add(child.getDuplicates());
                }
            } else {
                result.addAll(getDuplicates(child));
            }
        }
        return result;
    }

    public void storeTree() {
    }

    public void readTree() {
    }


    private static class Node {
        private boolean isFinal;
        private NFrame frame;
        private int lineNumber;
        private String fileName;
        private ArrayList<Integer> duplicates;
        private ArrayList<Node> children;

        private void init() {
            duplicates = new ArrayList<>();
            children = new ArrayList<>();
            isFinal = true;
        }

        public Node(){
            init();
            this.frame = null;
        }

        public Node(final NFrame frame, final String fileName,
                    final int lineNumber) {
            init();
            this.frame = frame;
            this.fileName = fileName;
            this.lineNumber = lineNumber;
        }

        public Node(final NFrame frame, final int ticket) {
            init();
            this.frame = frame;
            this.duplicates.add(ticket);
        }

        public void addChild(final Node child) {
            isFinal = false;
            children.add(child);
        }

        public void addDuplicate(final int ticketNumber) {
            duplicates.add(ticketNumber);
        }

        public boolean isFinal() {
            return isFinal;
        }

        public boolean isSimilar(final NFrame ticketFrame,final TraceInfo traceInfo) {
            if (ticketFrame == null && frame == null) return true;
            return traceInfo.getLineNumber() == lineNumber &&
                    traceInfo.getFileName().toLowerCase().equals(fileName.toLowerCase());
//            boolean isMethodSimilar = ticketFrame.getMethodName().equals(frame.getMethodName());
//            boolean isClassSimilar = ticketFrame.getClassName().equals(frame.getClassName());
//            boolean isLocSimilar = ticketFrame.getLocation().equals(frame.getLocation());
//            return isMethodSimilar && isClassSimilar && isLocSimilar;
        }

        public ArrayList<Integer> getDuplicates() {
            return duplicates;
        }

        public ArrayList<Node> getChildren() {
            return children;
        }
    }


}

