package view;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import adapter.XYSeriesAdapter;
import controller.HRVController;
import files.HRVIBIFileReader;
import hrv.HRVParameter;
import hrv.RRData;
import hrv.calc.frequency.psd.PowerSpectrum;
import units.TimeUnitConverter.TimeUnit;

public class HRVViewerView extends JFrame {
		
	//FileMenu
	private JMenu fileMenu; 
	private JMenuItem selectFileMenuItem;
	
	JFreeChart powerPlotChart;
	JTable statisticsTable;
	JTable frequencyTable;
	
	private HRVController controller = new HRVController();
	
	public HRVViewerView() {
		setVisible(true);	
		initializeMenuBar();
		setContentPane(createContentPane());	
		setSize(800, 500);
        pack();
	}
	
	private JPanel createContentPane() {
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		
		contentPanel.setOpaque(true);
		RRData rrData = controller.generateSinusRRData();
		PowerSpectrum ps = controller.getPowerSpectrum(rrData);
		contentPanel.add(initializeMainChart(createXYSeriesCollection(ps)));
		
		JPanel hrvParamsPanel = new JPanel(new GridLayout(1, 2));
		
		String[][] data = new String[][] { { , }};
		String[] columnNames = new String[] { "Parameter", "Value" };
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		statisticsTable = new JTable(model);
		hrvParamsPanel.add(statisticsTable);

		String[][] data2 = new String[][] { { , }};
		String[] columnNames2 = new String[] { "Parameter", "Value" };
		DefaultTableModel modelFrequencyTable = new DefaultTableModel(data2, columnNames2);
		frequencyTable = new JTable(modelFrequencyTable);
		hrvParamsPanel.add(frequencyTable);
		
		contentPanel.add(hrvParamsPanel);
		
		return contentPanel;
	}
	

	
	private XYDataset createXYSeriesCollection(PowerSpectrum ps) {
		XYSeriesAdapter adapter = new XYSeriesAdapter();
		XYSeries powerSpectrum = adapter.adapt(ps, "Power Spectrum");		
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(powerSpectrum);
        return dataset;
	}
	
	private ChartPanel initializeMainChart(XYDataset data) {
		
        powerPlotChart = ChartFactory.createXYLineChart(
            "PowerSpectrum", "Frequency", "Power",
            data, PlotOrientation.VERTICAL, false, true, false);
                
        XYPlot plot = (XYPlot)powerPlotChart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, Color.BLUE);
        plot.setRenderer(renderer);
        
        ChartPanel cp = new ChartPanel(powerPlotChart) {

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(800, 500);
            }
        };
        
        cp.setMouseWheelEnabled(true);
        return cp;
	}
		
	private void initializeMenuBar() {
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		//File Menu
		fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		
		//Select File Menu Item
		selectFileMenuItem = new JMenuItem("Select File");
		selectFileMenuItem.addActionListener(new MenuListener());
		fileMenu.add(selectFileMenuItem);
	}
		
	private class MenuListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			if(arg0.getSource() == selectFileMenuItem) {

				JFileChooser fileChooser = new JFileChooser();
				int returnVal = fileChooser.showOpenDialog(HRVViewerView.this);
				
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					
					HRVIBIFileReader fileReader = new HRVIBIFileReader();
					try {
						RRData data = fileReader.readIBIFile(selectedFile.getAbsolutePath(), TimeUnit.MILLISECOND);
						data.changeTimeAxisUnit(TimeUnit.SECOND);
						data.changeValuesAxisUnit(TimeUnit.SECOND);
						
						onRRDataUpdate(data);
					} catch (IOException e) {
						e.printStackTrace();
					}	
				}	
			}
		}		
		
		private void onRRDataUpdate(RRData newData) {
			updateStatisticsTable(newData);
			
			PowerSpectrum ps = controller.getPowerSpectrum(newData);
			updateFrequencyTable(ps);
			updateMainChart(createXYSeriesCollection(ps));
		}
		
		private void updateStatisticsTable(RRData data) {
			List<HRVParameter> params = controller.getAllStatisticsParameters(data);
						
			DefaultTableModel model = (DefaultTableModel) statisticsTable.getModel();
			model.setRowCount(0);
			
			DecimalFormat df = new DecimalFormat("#.####");
			
			for(HRVParameter param : params) {
				model.addRow(new Object[]{param.getName(), df.format(param.getValue()) + " " + param.getUnit() });
			}
		}
		
		private void updateFrequencyTable(PowerSpectrum ps) {
			List<HRVParameter> params = controller.getAllFrequencyParameters(ps);
			
			DefaultTableModel model = (DefaultTableModel) frequencyTable.getModel();
			model.setRowCount(0);
			
			DecimalFormat df = new DecimalFormat("#.####");
			
			for(HRVParameter param : params) {
				model.addRow(new Object[]{param.getName(), df.format(param.getValue()) + " " + param.getUnit() });
			}
		}
		
		private void updateMainChart(XYDataset xyDataset) {
	        powerPlotChart.getXYPlot().setDataset(xyDataset);
		}
	}
}
