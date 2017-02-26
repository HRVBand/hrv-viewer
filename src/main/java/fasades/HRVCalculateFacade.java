package fasades;

import java.util.ArrayList;
import java.util.List;

import hrv.RRData;
import hrv.calc.manipulator.HRVCutToPowerTwoDataManipulator;
import hrv.calc.manipulator.HRVMultiDataManipulator;
import hrv.calc.manipulator.HRVSplineInterpolator;
import hrv.calc.manipulator.HRVSubstractMeanManipulator;
import hrv.calc.parameter.BaevskyCalculator;
import hrv.calc.parameter.HFnuCalculator;
import hrv.calc.parameter.HRVParameter;
import hrv.calc.parameter.LFnuCalculator;
import hrv.calc.parameter.NN50Calculator;
import hrv.calc.parameter.PNN50Calculator;
import hrv.calc.parameter.RMSSDCalculator;
import hrv.calc.parameter.SD1Calculator;
import hrv.calc.parameter.SD1SD2Calculator;
import hrv.calc.parameter.SD2Calculator;
import hrv.calc.parameter.SDSDCalculator;
import hrv.calc.psd.PowerSpectrum;
import hrv.calc.psd.PowerSpectrumIntegralCalculator;
import hrv.calc.psd.StandardPowerSpectralDensityEstimator;
import units.TimeUnitConverter.TimeUnit;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

public class HRVCalculateFacade {

	public PowerSpectrum getPowerSpectrum(RRData data) {
		HRVMultiDataManipulator mani = new HRVMultiDataManipulator();
		mani.addManipulator(new HRVSplineInterpolator(4));
		mani.addManipulator(new HRVCutToPowerTwoDataManipulator());
		mani.addManipulator(new HRVSubstractMeanManipulator());
		RRData manipulatedData = mani.manipulate(data);
		
		StandardPowerSpectralDensityEstimator estimator = new StandardPowerSpectralDensityEstimator();
		return estimator.calculateEstimate(manipulatedData);
	}
	
	public List<HRVParameter> getAllFrequencyParameters(PowerSpectrum ps) {
		List<HRVParameter> parameters = new ArrayList<>();
		
		double lflowerBound = 0.04;
		double lfupperBound = 0.15;
		double hflowerBound = 0.15;
		double hfupperBound = 0.4;
		
		PowerSpectrumIntegralCalculator calcLF = new PowerSpectrumIntegralCalculator(lflowerBound, lfupperBound);
		parameters.add(new HRVParameter("LF", calcLF.process(ps) * 1000000, "ms * ms"));
		
		PowerSpectrumIntegralCalculator calcHF = new PowerSpectrumIntegralCalculator(hflowerBound, hfupperBound);
		parameters.add(new HRVParameter("HF", calcHF.process(ps) * 1000000, "ms * ms"));

		LFnuCalculator calcLFnu = new LFnuCalculator(lflowerBound, lfupperBound, hflowerBound, hfupperBound);
		parameters.add(new HRVParameter("LF n.u.", calcLFnu.process(ps), "n.u."));
		
		HFnuCalculator calcHFnu = new HFnuCalculator(lflowerBound, lfupperBound, hflowerBound, hfupperBound);
		parameters.add(new HRVParameter("HF n.u.", calcHFnu.process(ps), "n.u."));
		
		return parameters;
	}
	
	public List<HRVParameter> getAllStatisticsParameters(RRData data) {
		List<HRVParameter> parameters = new ArrayList<>();
		
		Mean m = new Mean();
		parameters.add(new HRVParameter("Mean", m.evaluate(data.getValueAxis()), data.getValueAxisUnit().toString()));
		
		StandardDeviation d = new StandardDeviation();
		parameters.add(new HRVParameter("Standard Deviation", d.evaluate(data.getValueAxis()), "non"));
		
		BaevskyCalculator baevskyCalc = new BaevskyCalculator();
		parameters.add(baevskyCalc.process(data));
		
		NN50Calculator nn50Calc = new NN50Calculator();
		parameters.add(nn50Calc.process(data));

		PNN50Calculator pnn50Calc = new PNN50Calculator();
		parameters.add(pnn50Calc.process(data));

		RMSSDCalculator rmssdCalc = new RMSSDCalculator();
		parameters.add(rmssdCalc.process(data));

		SD1Calculator sd1Calc = new SD1Calculator();
		parameters.add(sd1Calc.process(data));

		SD2Calculator sd2Calc = new SD2Calculator();
		parameters.add(sd2Calc.process(data));

		SD1SD2Calculator sd1sd2Calc = new SD1SD2Calculator();
		parameters.add(sd1sd2Calc.process(data));

		SDSDCalculator sdsdCalc = new SDSDCalculator();
		parameters.add(sdsdCalc.process(data));
		return parameters;
	}
		
	public RRData generateSinusRRData() {
		double sinHz = 1; //Frequency of the sin function
		int sampleFrequency = 8; //Sample Frequency in Hz
		double xLength = 2; //Length of the data.
		double[] sinY = generateSinArray(xLength, sampleFrequency, sinHz);
		
		//Generate X-Axis
		double[] sinX = new double[sinY.length];
		for(int i = 0; i < sinX.length; i++) {
			sinX[i] = i * (1.0 / sampleFrequency);
		}
		
		return new RRData(sinX, TimeUnit.SECOND, sinY, TimeUnit.SECOND);
	}
	
	private double[] generateSinArray(double xMax, int sampleFrequency, double sinHz) {
		double[] sin = new double[(int)xMax * sampleFrequency];
		
		for(int i = 0; i < sin.length; i++) {
			sin[i] = Math.sin(2 * Math.PI * sinHz * i * (1.0 / sampleFrequency));
		}
		
		return sin;
	}
}
