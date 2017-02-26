package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import controller.HRVDataController;
import hrv.RRData;
import hrv.files.HRVIBIFileReader;
import units.TimeUnitConverter.TimeUnit;

public class HRVViewerView extends JFrame {

	// FileMenu
	private JMenu fileMenu;
	private JMenuItem selectFileMenuItem;

	HRVDataController dataController = new HRVDataController();
	
	HRVCalculationResultsView calculationResultsView = new HRVCalculationResultsView();
	HRVDataView dataView = new HRVDataView();
	
	public HRVViewerView() {
		setVisible(true);
		initializeMenuBar();
		setContentPane(createContentPane());
		setSize(800, 500);
		pack();
	}

	public JPanel createContentPane() {
		dataController.addRRDataChangedListener(dataView);
		dataController.addRRDataChangedListener(calculationResultsView);		
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.add(dataView);
		contentPanel.add(calculationResultsView);
		
		return contentPanel;
	}

	private void initializeMenuBar() {

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		// File Menu
		fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		// Select File Menu Item
		selectFileMenuItem = new JMenuItem("Select File");
		selectFileMenuItem.addActionListener(new MenuListener());
		fileMenu.add(selectFileMenuItem);
	}

	private class MenuListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {

			if (arg0.getSource() == selectFileMenuItem) {

				JFileChooser fileChooser = new JFileChooser();
				int returnVal = fileChooser.showOpenDialog(HRVViewerView.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();

					HRVIBIFileReader fileReader = new HRVIBIFileReader();
					try {
						RRData data = fileReader.readIBIFile(selectedFile.getAbsolutePath(), TimeUnit.SECOND);
						data.changeTimeAxisUnit(TimeUnit.SECOND);
						data.changeValuesAxisUnit(TimeUnit.SECOND);

						dataController.rrDataChanged(data);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}		
	}
}
