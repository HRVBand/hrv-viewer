package adapter;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import hrv.RRData;
import hrv.calc.psd.PowerSpectrum;
import hrv.calc.psd.PowerSpectrumUnivariateFunctionAdapter;

public class XYSeriesAdapter {

	public XYSeries adapt(PowerSpectrum ps, String seriesName) {
		XYSeries xySeries = new XYSeries(seriesName);
		double[] frequencies = ps.getFrequency();
		double[] power = ps.getPower();
		
		for(int i = 0; i < ps.getFrequency().length; i++) {
			xySeries.add(frequencies[i], power[i]);
		}
		
		return xySeries;
	}
	
	public XYSeries adapt(RRData data, String seriesName) {
		XYSeries xySeries = new XYSeries(seriesName);
		double[] x = data.getTimeAxis();
		double[] y = data.getValueAxis();
		
		for(int i = 0; i < x.length; i++) {
			xySeries.add(x[i], y[i]);
		}
		
		return xySeries;
	}
	
	public XYSeries adapt(PowerSpectrumUnivariateFunctionAdapter adapter, String seriesName) {
		XYSeries xySeries = new XYSeries(seriesName);
		
		for(double iteratorCount = 0.0; iteratorCount < 0.49; iteratorCount += 0.001) {
			xySeries.add(iteratorCount, adapter.value(iteratorCount));
		}
		
		return xySeries;
	}
}
