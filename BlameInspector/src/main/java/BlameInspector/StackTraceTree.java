package BlameInspector;

import com.jmolly.stacktraceparser.NFrame;
import com.jmolly.stacktraceparser.NStackTrace;

import java.util.ArrayList;

public class StackTraceTree {

    private Node root;

    public StackTraceTree(){
        root = new Node(null);
    }

    public boolean addTicket(final NStackTrace stackTrace, final int ticketNumber){
        return false;
    }


    private class Node{
        private boolean isFinal;
        private NFrame frame;
        private ArrayList<Integer> tickets;
        private ArrayList<Node> children;

        public Node(final NFrame frame){
            this.isFinal = false;
            this.frame = frame;
        }

        public Node(final NFrame frame, final int ticket){
            this.isFinal = true;
            this.frame = frame;
            this.tickets.add(ticket);
        }

        public void addChild(Node child){
            children.add(child);
        }

        public boolean isFinal() {
            return isFinal;
        }

        public ArrayList<Integer> getTickets() {
            return tickets;
        }

        public ArrayList<Node> getChildren() {
            return children;
        }
    }


}

