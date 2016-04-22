package blameinspector;

import com.jmolly.stacktraceparser.NFrame;
import com.jmolly.stacktraceparser.NStackTrace;

import java.util.ArrayList;
import java.util.List;

public class StackTraceTree {

    private Node root;
    private String projectName;

    public StackTraceTree(final String projectName){
        root = new Node(null);
        this.projectName = projectName;
    }

    /*
    * @param ticketNumber number of ticket to be searched in tree
    * @return Node which contains this ticket StackTrace final frame
    */
    public Node getTicket(final int ticketNumber){
       Node currentNode = root;
       while (currentNode.tickets.size() > 1){
           for (Node child: currentNode.getChildren()){
               if (child.getTickets().contains(ticketNumber)){
                   currentNode = child;
               }
           }
       }
       return currentNode;
    }

    /*
    * @param stackTrace stackTrace of ticket to be added to the tree
    * @param ticketNumber number of ticket  to be added to the tree
    * @return true, if exactly such StackTrace hasn't mentioned yet,
    * false otherwise
    */
    public boolean addTicket(final NStackTrace stackTrace, final int ticketNumber){
        List<NFrame> frames = stackTrace.getTrace().getFrames();
        Node currentNode = root;
        for (int i = 0; i < frames.size(); i++) {
            NFrame frame = frames.get(i);
            currentNode.addTicket(ticketNumber);
            if (currentNode.isFinal){
                if (i < frames.size()){
                    currentNode.addChild(new Node(frame));
                }else{
                    currentNode.addTicket(ticketNumber);
                    return false;
                }
            }
            for (Node child : currentNode.getChildren()) {
                if (child.isSimilar(frame)) {
                    currentNode = child;
                    break;
                }
            }
        }
        return true;
    }

    public ArrayList<Integer> getDuplicates(){
        return null;
    }

    public void storeTree(){}
    public void readTree(){}


    private static class Node{
        private boolean isFinal;
        private NFrame frame;
        private ArrayList<Integer> tickets;
        private ArrayList<Node> children;

        private void init(){
            tickets = new ArrayList<>();
            children = new ArrayList<>();
        }

        public Node(final NFrame frame){
            init();
            this.isFinal = true;
            this.frame = frame;
        }

        public Node(final NFrame frame, final int ticket){
            init();
            this.isFinal = false;
            this.frame = frame;
            this.tickets.add(ticket);
        }

        public void addChild(final Node child){
            isFinal = false;
            children.add(child);
        }

        public void addTicket(final int ticketNumber){
            tickets.add(ticketNumber);
        }

        public boolean isFinal() {
            return isFinal;
        }

        public boolean isSimilar(final NFrame ticketFrame){
            boolean isMethodSimilar = ticketFrame.getMethodName().equals(frame.getMethodName());
            boolean isClassSimilar = ticketFrame.getClassName().equals(frame.getClassName());
            boolean isLocSimilar = ticketFrame.getLocation().equals(frame.getLocation());
            return isMethodSimilar && isClassSimilar && isLocSimilar;
        }

        public ArrayList<Integer> getTickets() {
            return tickets;
        }

        public ArrayList<Node> getChildren() {
            return children;
        }
    }


}

