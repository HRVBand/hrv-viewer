package view;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

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
import controller.HRVDataController.RRDataChangedEvent;
import controller.HRVDataController.RRDataChangedListener;
import fasades.HRVCalculateFacade;
import hrv.RRData;
import hrv.calc.psd.PowerSpectrum;

public class HRVDataView extends JPanel implements RRDataChangedListener {

	JFreeChart rrDataChart;
	
	private HRVCalculateFacade controller = new HRVCalculateFacade();
	
	public HRVDataView() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBorder(BorderFactory.createTitledBorder("Data"));

		this.setOpaque(true);
		RRData rrData = controller.generateSinusRRData();
		
		this.add(initializeMainChart(createXYSeriesCollection(rrData)));
	}
	
	private XYDataset createXYSeriesCollection(RRData data) {
		XYSeriesAdapter adapter = new XYSeriesAdapter();
		XYSeries xySeries = adapter.adapt(data, "Power Spectrum");
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(xySeries);
		return dataset;
	}
	
	private void updateMainChart(XYDataset xyDataset) {
		rrDataChart.getXYPlot().setDataset(xyDataset);
	}
	
	private ChartPanel initializeMainChart(XYDataset data) {

		rrDataChart = ChartFactory.createXYLineChart("RR-Data", "Time", "Interval", data,
				PlotOrientation.VERTICAL, false, true, false);

		XYPlot plot = (XYPlot) rrDataChart.getPlot();
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
		renderer.setSeriesPaint(0, Color.BLUE);
		plot.setRenderer(renderer);

		ChartPanel cp = new ChartPanel(rrDataChart) {

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(800, 400);
			}
		};

		cp.setMouseWheelEnabled(false);
		return cp;
	}

	@Override
	public void rrDataChanged(RRDataChangedEvent e) {
		updateMainChart(createXYSeriesCollection(e.getRRData()));
	}
}
