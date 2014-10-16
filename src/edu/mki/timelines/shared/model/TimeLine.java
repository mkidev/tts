package edu.mki.timelines.shared.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeMap;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.datepicker.client.CalendarUtil;

@SuppressWarnings("deprecation")
public class TimeLine {
	private String word;
	private TreeMap<Date, WordAtDateData> frequencies = new TreeMap<>();
	private TreeMap<Date, WordAtDateData> monthly = new TreeMap<>();
	private TreeMap<Date, WordAtDateData> weekly = new TreeMap<>();
	private TreeMap<Date, WordAtDateData> yearly = new TreeMap<>();
	private TreeMap<Date, WordAtDateData> movingAverage = new TreeMap<>();
	private TreeMap<Date, WordAtDateData> rankList = new TreeMap<>();

	public TimeLine(String word) {
		this.word = word;
	}

	public TimeLine(String word, ArrayList<Date> dates) {
		this.word = word;
		for (Date date : dates) {
			frequencies.put(date, new WordAtDateData(0F));
		}
	}

	public TreeMap<Date, WordAtDateData> getFrequencies() {
		return frequencies;
	}

	public String getWord() {
		return word;
	}

	public TreeMap<Date, WordAtDateData> getDaily() {
		return frequencies;
	}

	/**
	 * 
	 * 
	 * @return Treemap mit monatlichen Frequenzen
	 */
	public TreeMap<Date, WordAtDateData> getMonthly() {
		if (monthly.size() == 0) {
			TreeMap<Date, WordAtDateData> result = new TreeMap<>();
			Date start = frequencies.firstKey();
			int lastMonth = start.getMonth();
			float freq = 0F;
			boolean firstRun = true;
			boolean isPeak = false;
			String comment = "";
			float dayCount = 0;
			for (Date date : frequencies.keySet()) {
				if (lastMonth != date.getMonth()) {
					Date tmp = CalendarUtil.copyDate(date);
					// CalendarUtil.addMonthsToDate(tmp, -1);
					lastMonth = date.getMonth();
					result.put(tmp, new WordAtDateData(freq / dayCount, isPeak,
							comment));
					freq = 0;
					dayCount = 0;
					isPeak = false;
				}
				if (!isPeak) {
					isPeak = frequencies.get(date).isPeak();
					comment = frequencies.get(date).getPeakComment();

				}
				freq += frequencies.get(date).getFreq();
				dayCount++;
			}
			monthly = result;
			return result;
		} else
			return monthly;
	}

	public TreeMap<Date, WordAtDateData> getWeekly() {
		if (weekly.size() == 0) {
			TreeMap<Date, WordAtDateData> result = new TreeMap<>();
			float freq = 0F;
			int daycount = 0;
			String comment = "";

			boolean isPeak = false;
			for (Date date : frequencies.keySet()) {
				freq += frequencies.get(date).getFreq();
				if (!isPeak) {
					isPeak = frequencies.get(date).isPeak();
					comment = frequencies.get(date).getPeakComment();

				}
				if (daycount == 6) {
					result.put(date, new WordAtDateData(freq / daycount,
							isPeak, comment));
					freq = 0;
					daycount = 0;
					isPeak = false;
				} else {
					daycount++;
				}
			}
			weekly = result;
			return result;
		} else
			return weekly;
	}

	public TreeMap<Date, WordAtDateData> getYearly() {
		if (yearly.size() == 0) {
			TreeMap<Date, WordAtDateData> result = new TreeMap<>();
			float freq = 0F;
			int lastYear = frequencies.firstKey().getYear();
			String comment = "";
			boolean firstRun = true;
			boolean isPeak = false;
			int dayCount = 0;
			for (Date date : frequencies.keySet()) {
				if (lastYear != date.getYear()) {
					lastYear = date.getYear();
					result.put(date, new WordAtDateData(freq / dayCount,
							isPeak, comment));
					freq = 0;
					isPeak = false;
					dayCount = 0;
				}

				freq += frequencies.get(date).getFreq();
				dayCount++;
				if (!isPeak) {
					isPeak = frequencies.get(date).isPeak();
					comment = frequencies.get(date).getPeakComment();
				}
			}
			yearly = result;
			return result;
		} else
			return yearly;
	}

	public TreeMap<Date, WordAtDateData> getMovingAverage() {
		if (movingAverage.size() == 0) {
			movingAverage = new TreeMap<>();
			Queue<Float> window = new LinkedList<>();
			Float sum = 0F;
			int period = 31;
			for (Date date : getFrequencies().keySet()) {
				Float num = getFrequencies().get(date).getFreq();
				sum += num;
				window.add(num);
				if (window.size() > period) {
					sum -= window.remove();
				}
				movingAverage.put(date, new WordAtDateData(
						(sum / window.size())));
			}
			return movingAverage;
		} else
			return movingAverage;
	}

	public TreeMap<Date, WordAtDateData> getTest() {
		if (movingAverage.size() == 0) {
			double[][] rawData = new double[getFrequencies().size()][2];
			movingAverage = new TreeMap<>();
			int x = 0;
			int c = 0;
			for (Date date : getFrequencies().keySet()) {
				Float num = getFrequencies().get(date).getFreq();
				rawData[x][0] = num;
				rawData[x][1] = x;
				x++;

				c++;
			}
			double[] coef = linear_equation(rawData, 2);
			x = 0;
			Date lastDate = new Date();
			for (Date date : getFrequencies().keySet()) {
				movingAverage.put(date,
						new WordAtDateData((float) getValue(coef, x)));
				x++;
				lastDate = date;
			}
			Date date = CalendarUtil.copyDate(lastDate);
			for (int i = x; i < x + 5; i++) {
				CalendarUtil.addDaysToDate(date, 1);
				movingAverage.put(CalendarUtil.copyDate(date),
						new WordAtDateData((float) getValue(coef, i)));
			}
			return movingAverage;
		} else
			return movingAverage;
	}

	public TreeMap<Date, WordAtDateData> getLinearRegression() {
		if (movingAverage.size() == 0) {
			movingAverage = new TreeMap<>();
			Float newVal = 0F;
			int t = 0;
			Float alpha = 0.0F;
			Float beta = 0.0F;
			Float sumx = 0F;
			Float sumy = 0F;

			for (Date date : getFrequencies().keySet()) {
				Float num = getFrequencies().get(date).getFreq();
				sumx += t;
				sumy += num;
				t += 1;
			}
			t = 0;
			double xbar = sumx / getFrequencies().size();
			double ybar = sumy / getFrequencies().size();
			double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
			for (Date date : getFrequencies().keySet()) {
				Float num = getFrequencies().get(date).getFreq();
				xxbar += (t - xbar) * (t - xbar);
				yybar += (num - ybar) * (num - ybar);
				xybar += (t - xbar) * (num - ybar);
				t += 1;
			}
			beta = (float) (xybar / xxbar);
			alpha = (float) (ybar - alpha * xbar);
			GWT.log("f(x) = " + alpha + " + " + beta + "x");
			t = 0;
			for (Date date : getFrequencies().keySet()) {
				newVal = (float) (alpha + beta * Math.pow(t, 1));
				movingAverage.put(date, new WordAtDateData(newVal));
				t += 1;
			}

			return movingAverage;
		} else
			return movingAverage;
	}

	static double rsquared = 0;

	// Apply least squares to raw data to determine the coefficients for
	// an n-order equation: y = a0*X^0 + a1*X^1 + ... + an*X^n.
	// Returns the coefficients for the solved equation, given a number
	// of y and x data points. The rawData input is given in the form of
	// {{y0, x0}, {y1, x1},...,{yn, xn}}. The coefficients returned by
	// the regression are {a0, a1,...,an} which corresponds to
	// {X^0, X^1,...,X^n}. The number of coefficients returned is the
	// requested equation order (norder) plus 1.
	static double[] linear_equation(double rawData[][], int norder) {
		double a[][] = new double[norder + 1][norder + 1];
		double b[] = new double[norder + 1];
		double term[] = new double[norder + 1];
		double ysquare = 0;

		// step through each raw data entries
		for (int i = 0; i < rawData.length; i++) {

			// sum the y values
			b[0] += rawData[i][0];
			ysquare += rawData[i][0] * rawData[i][0];

			// sum the x power values
			double xpower = 1;
			for (int j = 0; j < norder + 1; j++) {
				term[j] = xpower;
				a[0][j] += xpower;
				xpower = xpower * rawData[i][1];
			}

			// now set up the rest of rows in the matrix - multiplying each row
			// by each term
			for (int j = 1; j < norder + 1; j++) {
				b[j] += rawData[i][0] * term[j];
				for (int k = 0; k < b.length; k++) {
					a[j][k] += term[j] * term[k];
				}
			}
		}

		// solve for the coefficients
		double coef[] = gauss(a, b);

		// calculate the r-squared statistic
		double ss = 0;
		double yaverage = b[0] / rawData.length;
		for (int i = 0; i < norder + 1; i++) {
			double xaverage = a[0][i] / rawData.length;
			ss += coef[i] * (b[i] - (rawData.length * xaverage * yaverage));
		}
		rsquared = ss / (ysquare - (rawData.length * yaverage * yaverage));

		// solve the simultaneous equations via gauss
		return coef;
	}

	// it's been so long since I wrote this, that I don't recall the math
	// logic behind it. IIRC, it's just a standard gaussian technique for
	// solving simultaneous equations of the form: |A| = |B| * |C| where we
	// know the values of |A| and |B|, and we are solving for the coefficients
	// in |C|
	static double[] gauss(double ax[][], double bx[]) {
		double a[][] = new double[ax.length][ax[0].length];
		double b[] = new double[bx.length];
		double pivot;
		double mult;
		double top;
		int n = b.length;
		double coef[] = new double[n];

		// copy over the array values - inplace solution changes values
		for (int i = 0; i < ax.length; i++) {
			for (int j = 0; j < ax[i].length; j++) {
				a[i][j] = ax[i][j];
			}
			b[i] = bx[i];
		}

		for (int j = 0; j < (n - 1); j++) {
			pivot = a[j][j];
			for (int i = j + 1; i < n; i++) {
				mult = a[i][j] / pivot;
				for (int k = j + 1; k < n; k++) {
					a[i][k] = a[i][k] - mult * a[j][k];
				}
				b[i] = b[i] - mult * b[j];
			}
		}

		coef[n - 1] = b[n - 1] / a[n - 1][n - 1];
		for (int i = n - 2; i >= 0; i--) {
			top = b[i];
			for (int k = i + 1; k < n; k++) {
				top = top - a[i][k] * coef[k];
			}
			coef[i] = top / a[i][i];
		}
		return coef;
	}

	static double getValue(double coef[], int x) {
		double result = 0;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < coef.length; i++) {
			result += coef[i] * Math.pow(x, i);
		}
		return result;
	}

	// simple routine to print the equation for inspection
	static String print_equation(double coef[]) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < coef.length; i++) {
			if (i == 0) {
				sb.append("Y = ");
			} else {
				sb.append(" + ");
			}
			sb.append(coef[i] + "*X^" + i);
		}
		sb.append("      [r^2 = " + rsquared + "]");
		return sb.toString();
	}
}
