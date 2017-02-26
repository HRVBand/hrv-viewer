import javax.swing.WindowConstants;

import view.HRVViewerView;

public class MainClass {

	public static void main(String[] args) {
		
		HRVViewerView view = new HRVViewerView();
		view.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

}
