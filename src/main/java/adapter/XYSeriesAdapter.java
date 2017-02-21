package adapter;

import org.jfree.data.xy.XYSeries;

import hrv.calc.frequency.psd.PowerSpectrum;

public class XYSeriesAdapter {

	public XYSeries adapt(PowerSpectrum ps, String seriesName) {
		XYSeries powerSpectrum = new XYSeries(seriesName);
		double[] frequencies = ps.getFrequency();
		double[] power = ps.getPower();
		
		for(int i = 0; i < ps.getFrequency().length; i++) {
			powerSpectrum.add(frequencies[i], power[i]);
		}
		
		return powerSpectrum;
	}
}
