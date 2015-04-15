package cs269.project;

import java.util.Comparator;
import cs269.project.Node;

public class NodeRule implements Comparator<Node> {

	@Override
	public int compare(Node o1, Node o2) {
		if (o1.fval() < o2.fval()) {
			return -1;
		}
		else if (o1.fval() > o2.fval()) {
			return 1;
		}
		return 0;
	}

	
	
}
