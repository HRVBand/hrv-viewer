package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import adapter.XYSeriesAdapter;
import common.ArrayUtils;
import fasades.HRVCalculateFacade;
import hrv.RRData;
import hrv.calc.parameter.HRVParameter;
import hrv.calc.psd.PowerSpectrum;

public class HRVCalculationResultsView extends JPanel {

	JFreeChart powerPlotChart;
	JTable statisticsTable;
	JTable frequencyTable;	
	
	private HRVCalculateFacade controller = new HRVCalculateFacade();
	
	public HRVCalculationResultsView() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBorder(BorderFactory.createTitledBorder("Results"));
		
		this.setOpaque(true);
		RRData rrData = controller.generateSinusRRData();
		PowerSpectrum ps = controller.getPowerSpectrum(rrData);
		this.add(initializeMainChart(createXYSeriesCollection(ps)));

		JPanel hrvParamsPanel = new JPanel(new GridLayout(1, 2));

		//Create statistics parameter panel
		JPanel hrvStatisticsParamsPanel = new JPanel();
		hrvStatisticsParamsPanel.setLayout(new BoxLayout(hrvStatisticsParamsPanel, BoxLayout.Y_AXIS));
		hrvStatisticsParamsPanel.setBorder(BorderFactory.createTitledBorder("Statistics Parameters"));
		
		String[][] data = new String[][] { {,} };
		String[] columnNames = new String[] { "Parameter", "Value" };
		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		statisticsTable = new JTable(model);
		hrvStatisticsParamsPanel.add(statisticsTable);
		hrvParamsPanel.add(hrvStatisticsParamsPanel);

		//Create frequency parameter panel
		JPanel hrvFrequencyParamsPanel = new JPanel();
		hrvFrequencyParamsPanel.setLayout(new BoxLayout(hrvFrequencyParamsPanel, BoxLayout.Y_AXIS));
		hrvFrequencyParamsPanel.setBorder(BorderFactory.createTitledBorder("Frequency Parameters"));

		String[][] data2 = new String[][] { {,} };
		String[] columnNames2 = new String[] { "Parameter", "Value" };
		DefaultTableModel modelFrequencyTable = new DefaultTableModel(data2, columnNames2);
		frequencyTable = new JTable(modelFrequencyTable);
		hrvFrequencyParamsPanel.add(frequencyTable);
		hrvParamsPanel.add(hrvFrequencyParamsPanel);

		this.add(hrvParamsPanel);
	}	

	private XYDataset createXYSeriesCollection(PowerSpectrum ps) {
		XYSeriesAdapter adapter = new XYSeriesAdapter();
		XYSeries xySeries = adapter.adapt(ps, "Power Spectrum");
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(xySeries);
		return dataset;
	}	

	private ChartPanel initializeMainChart(XYDataset data) {

		powerPlotChart = ChartFactory.createXYLineChart("PowerSpectrum", "Frequency", "Power", data,
				PlotOrientation.VERTICAL, false, true, false);

		XYPlot plot = (XYPlot) powerPlotChart.getPlot();
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
		renderer.setSeriesPaint(0, Color.BLUE);
		plot.setRenderer(renderer);

		ChartPanel cp = new ChartPanel(powerPlotChart) {

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(800, 400);
			}
		};

		cp.setMouseWheelEnabled(false);
		return cp;
	}
	
	protected void onRRDataUpdate(RRData newData) {
		updateStatisticsTable(newData);

		PowerSpectrum ps = controller.getPowerSpectrum(newData);
		updateFrequencyTable(ps);
		
		PowerSpectrum psCut = cutPowerSpectrum(ps, 0.5);
	
		
		updateMainChart(createXYSeriesCollection(psCut));
	}
	
	private void updateStatisticsTable(RRData data) {
		List<HRVParameter> params = controller.getAllStatisticsParameters(data);

		DefaultTableModel model = (DefaultTableModel) statisticsTable.getModel();
		model.setRowCount(0);

		DecimalFormat df = new DecimalFormat("#.####");

		for (HRVParameter param : params) {
			model.addRow(new Object[] { param.getName(), df.format(param.getValue()) + " " + param.getUnit() });
		}
	}

	private void updateFrequencyTable(PowerSpectrum ps) {
		List<HRVParameter> params = controller.getAllFrequencyParameters(ps);

		DefaultTableModel model = (DefaultTableModel) frequencyTable.getModel();
		model.setRowCount(0);

		DecimalFormat df = new DecimalFormat("#.####");

		for (HRVParameter param : params) {
			model.addRow(new Object[] { param.getName(), df.format(param.getValue()) + " " + param.getUnit() });
		}
	}

	private void updateMainChart(XYDataset xyDataset) {
		powerPlotChart.getXYPlot().setDataset(xyDataset);
	}
	
	private PowerSpectrum cutPowerSpectrum(PowerSpectrum ps, double atFrequency) {

		double[] oldPower = ps.getPower();
		double[] oldFrequency = ps.getFrequency();

		List<Double> newPower = new ArrayList<>();
		List<Double> newFrequency = new ArrayList<>();

		for (int i = 0; i < oldFrequency.length; i++) {
			if (oldFrequency[i] >= atFrequency) {
				break;
			}

			newPower.add(oldPower[i]);
			newFrequency.add(oldFrequency[i]);
		}

		return new PowerSpectrum(ArrayUtils.toPrimitive(newPower, 0.0),
				ArrayUtils.toPrimitive(newFrequency, 0.0));
	}
}
