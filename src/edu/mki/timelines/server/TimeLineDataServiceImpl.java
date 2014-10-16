package edu.mki.timelines.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.clustering.MultiKMeansPlusPlusClusterer;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.gwt.dev.util.collect.HashMap;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.mki.timelines.client.TimeLineDataService;
import edu.mki.timelines.shared.model.WordAtDateData;

public class TimeLineDataServiceImpl extends RemoteServiceServlet implements
		TimeLineDataService {
	private String dbHost = "woclu3.informatik.uni-leipzig.de";
	private String dbPort = "3306";
	private String dbName = "mkis_bachelor";
	private String dbUsername = "mkisilowski";
	private String dbPassword = "D,hdn1-Rd?";
	private String freqTableName = "frequencies";
	private Connection dbCon;
	private String wordsTableName = "words";
	private String datesTableName = "dates";
	private ArrayList<Date> dates = null;

	public Connection connectToDB() {
		try {
			String url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName;
			Class.forName("com.mysql.jdbc.Driver");
			return DriverManager.getConnection(url, dbUsername, dbPassword);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Connection connectToDB(String dbName) {
		try {
			String url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName;
			Class.forName("com.mysql.jdbc.Driver");
			return DriverManager.getConnection(url, dbUsername, dbPassword);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

	}

	public ArrayList<String> getWords() {
		ArrayList<String> result = new ArrayList<>();
		Statement stmt;
		// String query = "SELECT DISTINCT wort FROM wfneu";
		String query = "SELECT DISTINCT word FROM " + wordsTableName;
		ResultSet rs;
		try {
			Connection dbCon = connectToDB();
			if (dbCon != null) {
				stmt = dbCon.createStatement();
				rs = stmt.executeQuery(query);
				while (rs.next()) {
					result.add(new String((byte[]) rs.getObject(1)));
				}
				dbCon.close();
				return result;
			} else {
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public ArrayList<Date> getDates() {
		ArrayList<Date> result = new ArrayList<>();
		Statement stmt;
		String query = "SELECT DISTINCT date FROM " + datesTableName;
		ResultSet rs;
		try {
			Connection dbCon = connectToDB();
			if (dbCon != null) {
				stmt = dbCon.createStatement();
				rs = stmt.executeQuery(query);
				while (rs.next()) {
					result.add(rs.getDate(1));
				}
				dbCon.close();
				dates = result;
				return result;
			} else {
				return result;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return result;
		}
	}

	@Override
	public TreeMap<Date, WordAtDateData> getTimeLine(String word) {
		PreparedStatement stmt;
		String query = "select frequency,date from " + freqTableName
				+ " where word=? order by date desc;";
		ResultSet rs;
		TreeMap<Date, WordAtDateData> result = new TreeMap<>();
		try {
			for (Date date : dates) {
				// result.put(date, new WordAtDateData(0F));
			}
			Connection dbCon = connectToDB();
			if (dbCon != null) {
				stmt = dbCon.prepareStatement(query);
				stmt.setBytes(1, word.getBytes());
				rs = stmt.executeQuery();
				while (rs.next()) {
					result.put(rs.getDate(2),
							new WordAtDateData(rs.getFloat(1)));
					// result.get(rs.getDate(2)).setFreq(rs.getFloat(1));
				}
				dbCon.close();
				// return getPeakTimeLine(result);
				return result;
			} else {
				return result;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return result;
		}
	}

	public TreeMap<Date, WordAtDateData> getPeakTimeLine(
			TreeMap<Date, WordAtDateData> data) {
		TreeMap<Date, WordAtDateData> result = new TreeMap<>();
		DescriptiveStatistics mainstat = new DescriptiveStatistics();
		for (Date date : data.keySet()) {
			result.put(
					date,
					new WordAtDateData(data.get(date).getFreq(), date.getTime()));
			mainstat.addValue((double) data.get(date).getFreq());
		}
		double maxVal = mainstat.getMax();
		mainstat.clear();
		for (Date date : data.keySet()) {
			data.get(date).setFreq((float) (data.get(date).getFreq() / maxVal));
			mainstat.addValue((double) data.get(date).getFreq());
		}
		mainstat.clear();
		mainstat.setWindowSize(364);
		for (Date date : data.keySet()) {
			float diff;
			if (mainstat.getMean() == 0) {
				diff = 0F;
			} else {
				diff = (float) data.get(date).getFreq()
						/ (float) mainstat.getMean();
				// * (float) mainstat.getVariance();
			}
			result.get(date).setFreq((float) Math.rint(diff));
			mainstat.addValue(data.get(date).getFreq());
		}
		return result;
	}

	@Override
	public TreeMap<Date, WordAtDateData> getTimeLineFourier(String word) {
		PreparedStatement stmt;
		String query = "select frequency,date from " + freqTableName
				+ " where word=? order by date desc;";
		ResultSet rs;
		TreeMap<Date, WordAtDateData> result = new TreeMap<>();
		for (Date date : dates) {
			result.put(date, new WordAtDateData(0F));
		}
		try {
			Connection dbCon = connectToDB();
			if (dbCon != null) {
				stmt = dbCon.prepareStatement(query);
				stmt.setBytes(1, word.getBytes());
				rs = stmt.executeQuery();
				while (rs.next()) {
					// result.put(rs.getDate(2),
					// new WordAtDateData(rs.getFloat(1)));
					result.get(rs.getDate(2)).setFreq(rs.getFloat(1));

				}
				dbCon.close();
				return transformMA(result, word);
			} else {
				return result;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return result;
		}
	}

	private TreeMap<Date, WordAtDateData> transformMA(
			TreeMap<Date, WordAtDateData> data, String word) {
		TreeMap<Date, WordAtDateData> result = new TreeMap<>();
		DescriptiveStatistics mainstat = new DescriptiveStatistics();
		for (Date date : data.keySet()) {
			result.put(
					date,
					new WordAtDateData(data.get(date).getFreq(), date.getTime()));
			mainstat.addValue((double) data.get(date).getFreq());
		}
		double maxVal = mainstat.getMax();
		mainstat.clear();
		for (Date date : data.keySet()) {
			data.get(date).setFreq((float) (data.get(date).getFreq() / maxVal));
			mainstat.addValue((double) data.get(date).getFreq());
		}
		System.out.println("popvar: " + word + " : "
				+ mainstat.getPopulationVariance());
		System.out.println("var   : " + word + " : " + mainstat.getVariance());
		System.out.println("stdev : " + word + " : "
				+ mainstat.getStandardDeviation());
		System.out.println();
		mainstat.clear();
		mainstat.setWindowSize(364);
		for (Date date : data.keySet()) {
			float diff;
			if (mainstat.getMean() == 0) {
				diff = 0F;
			} else {
				diff = (float) data.get(date).getFreq()
						/ (float) mainstat.getMean();
				// * (float) mainstat.getVariance();
			}
			result.get(date).setFreq((float) Math.rint(diff));
			mainstat.addValue(data.get(date).getFreq());
		}
		return result;
	}

	private TreeMap<Date, WordAtDateData> transformFourier(
			TreeMap<Date, WordAtDateData> data, String word) {
		TreeMap<Date, WordAtDateData> result = data;
		double[] freqs;
		Complex[] transfResult;
		int n = 1;
		while (Math.pow(2, n) < result.keySet().size()) {
			n++;
		}
		if (result.keySet().size() != (int) Math.pow(2, n)) {
			freqs = new double[(int) Math.pow(2, n)];
		} else {
			freqs = new double[result.keySet().size()];
		}
		int k = (int) Math.pow(2, n) - result.keySet().size();
		while (k != 0) {
			freqs[result.keySet().size() - 1 + k] = 0;
			k--;
		}
		int i = 0;
		for (Date date : result.keySet()) {
			freqs[i] = result.get(date).getFreq();
			i++;
		}
		FastFourierTransformer fft = new FastFourierTransformer(
				DftNormalization.STANDARD);
		transfResult = fft.transform(freqs, TransformType.FORWARD);
		i = 0;

		transfResult = fft.transform(transfResult, TransformType.INVERSE);
		for (Date date : result.keySet()) {
			result.get(date).setFreq((float) transfResult[i].abs());
		}
		return result;
	}

	private TreeMap<Date, WordAtDateData> addPeaksToTimeLineML(
			TreeMap<Date, WordAtDateData> data) {
		TreeMap<Date, WordAtDateData> result = data;
		List<WordWrapper> clusterInput = new ArrayList<WordWrapper>();
		double x = 0;
		double gYM = 0;
		double tmp = 0;
		DescriptiveStatistics stats = new DescriptiveStatistics();
		int c = 0;
		for (WordAtDateData wadd : data.values()) {
			c++;
			tmp += wadd.getFreq();
			if (c == 180) {
				tmp = tmp / c;
				if (tmp > gYM)
					gYM = tmp;
				tmp = 0;
				c = 0;
			}
			stats.addValue(wadd.getFreq());
		}
		tmp = tmp / c;
		if (tmp > gYM)
			gYM = tmp;
		double median = gYM * 1.4;
		double mean = stats.getMean();
		for (Date date : data.keySet()) {
			data.get(date).setDateLong(date.getTime());
			// TODO change back
			data.get(date).setFreq(
					data.get(date).getFreq() / (float) stats.getMax());
			clusterInput.add(new WordWrapper(data.get(date), x));
			x++;
		}
		int clusterCount = data.size() / 10;
		KMeansPlusPlusClusterer<WordWrapper> clusterer = new KMeansPlusPlusClusterer<>(
				clusterCount, 10000);
		MultiKMeansPlusPlusClusterer<WordWrapper> multiclusterer = new MultiKMeansPlusPlusClusterer<>(
				clusterer, 10);
		List<CentroidCluster<WordWrapper>> clusterResults = multiclusterer
				.cluster(clusterInput);
		for (int i = 0; i < clusterResults.size(); i++) {
			if (median < clusterResults.get(i).getCenter().getPoint()[1]
					&& mean < clusterResults.get(i).getCenter().getPoint()[1]) {

				for (WordWrapper wordWrapper : clusterResults.get(i)
						.getPoints()) {
					if (median < wordWrapper.getWord().getFreq()) {
						result.get(
								new Date(wordWrapper.getWord().getDateLong()))
								.setPeak(true);
						result.get(
								new Date(wordWrapper.getWord().getDateLong()))
								.setPeakComment(
										"Cluster"
												+ i
												+ " s= "
												+ clusterResults.get(i)
														.getPoints().size());
					}

				}
				System.out.println();
			}
		}
		System.out.println("Estimated clusters(peaks) " + clusterCount);
		return result;
	}

	private int getEstimatedPeakCount(TreeMap<Date, WordAtDateData> data,
			double median) {
		int result = 0;

		DescriptiveStatistics stats = new DescriptiveStatistics();
		int postWindowSize = 15;
		float postAverage = 0F;
		int preWindowSize = 15;
		float preAverage = 0F;
		Date[] dates = new Date[data.size()];
		data.keySet().toArray(dates);
		float freq = 0F;
		float average = (float) median;
		float lambda = 0F;
		for (int i = 0; i < dates.length; i++) {
			freq = data.get(dates[i]).getFreq();
			stats.addValue(freq);
			lambda = (float) stats.getMean();
			if (i < preWindowSize) {
				postAverage = getAverage(i, i + postWindowSize * 2, data);
				if (postAverage > average)
					if (freq > postAverage && freq > lambda) {
						result++;
					}
			} else if (i > dates.length - postWindowSize) {
				preAverage = getAverage(i - preWindowSize * 2, i + 1, data);
				if (preAverage > average)
					if (freq > preAverage && freq > lambda) {
						result++;
					}
			} else {
				preAverage = getAverage(i - preWindowSize, i + 1, data);
				postAverage = getAverage(i, i + postWindowSize, data);
				if (preAverage > average || postAverage > average)
					if ((freq > data.get(dates[i - 1]).getFreq() && freq > data
							.get(dates[i + 1]).getFreq()) && freq > lambda) {
						result++;
					}
			}
		}
		return result;
	}

	private TreeMap<Date, WordAtDateData> addPeaksToTimeLine(
			TreeMap<Date, WordAtDateData> data) {
		TreeMap<Date, WordAtDateData> result = data;
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (WordAtDateData wadd : data.values()) {
			stats.addValue(wadd.getFreq());
		}
		double mean = stats.getMean();
		double std = stats.getStandardDeviation();
		double variance = stats.getVariance();
		double skewness = stats.getSkewness();
		double median = stats.getPercentile(90);
		double[] sorted_vals = stats.getSortedValues();
		int postWindowSize = 5;
		float postAverage = 0F;
		int preWindowSize = 5;
		float preAverage = 0F;
		Date[] dates = new Date[result.size()];
		result.keySet().toArray(dates);
		System.out.println("Some data");
		System.out.println("mean : " + mean);
		System.out.println("std : " + std);
		System.out.println("variance : " + variance);
		System.out.println("skewness : " + skewness);
		System.out.println("median : " + median);
		System.out
				.println("min/max : " + stats.getMin() + "/" + stats.getMax());

		float freq = 0F;
		float average = (float) median;
		float lambda = 0F;
		stats = new DescriptiveStatistics(300);
		for (int i = 0; i < dates.length; i++) {
			freq = result.get(dates[i]).getFreq();
			stats.addValue(freq);
			lambda = (float) stats.getMean();
			if (i < preWindowSize) {
				postAverage = getAverage(i, i + postWindowSize, data);
				if (postAverage > average)
					if (freq > postAverage && freq > lambda) {
						result.get(dates[i]).setPeak(true);
					}
			} else if (i > dates.length - postWindowSize) {
				preAverage = getAverage(i - preWindowSize, i + 1, data);
				if (preAverage > average)
					if (freq > preAverage && freq > lambda) {
						result.get(dates[i]).setPeak(true);
					}
			} else {
				preAverage = getAverage(i - preWindowSize, i + 1, data);
				postAverage = getAverage(i, i + postWindowSize, data);
				if (preAverage > average || postAverage > average)
					if ((freq > postAverage && freq > preAverage)
							&& freq > lambda) {
						result.get(dates[i]).setPeak(true);
						result.get(dates[i]).setPeakComment("MAX");
					} else if (freq > postAverage) {
						result.get(dates[i]).setPeak(true);
						result.get(dates[i]).setPeakComment("END");
					} else if (freq > preAverage) {
						result.get(dates[i]).setPeak(true);
						result.get(dates[i]).setPeakComment("START");
					}
			}
		}
		return result;
	}

	private float getAverage(int startPos, int endPos,
			TreeMap<Date, WordAtDateData> data) {
		float result = 0F;
		Date[] dates = new Date[data.size()];
		data.keySet().toArray(dates);

		for (int i = startPos; i < endPos; i++) {
			result += data.get(dates[i]).getFreq();
		}
		result = result / (endPos - startPos);
		return result;
	}

	/**
	 * select distinct wort from wfneu where datum between -START- AND -ENDE-
	 * order by frequenz desc limit 50;
	 */

	@Override
	public Map<String, Integer> getTopWordsAtDate(Date startDate, Date endDate) {
		Map<String, Integer> result = new HashMap<>();
		LocalDate sDate = LocalDate.fromDateFields(startDate);
		LocalDate eDate = LocalDate.fromDateFields(endDate);
		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");
		String startDateString = sDate.toString(dtf);
		String endDateString = eDate.toString(dtf);
		String query = "select r.word, r.rank from ranks as r, frequencies as f where r.date not like '2010-10-13' and r.date between '"
				+ startDateString
				+ "' AND '"
				+ endDateString
				+ "'and r.word=f.word and r.date=f.date order by r.rank desc, f.frequency desc limit 20";
		System.out.println(query);
		try {
			Connection dbCon = connectToDB();
			if (dbCon != null) {
				Statement stmt = dbCon.prepareStatement(query);
				ResultSet rs = stmt.executeQuery(query);
				while (rs.next()) {
					result.put(new String((byte[]) rs.getObject(1)),
							rs.getInt(2));
				}
				dbCon.close();
				return sortByValue(result, 0);
			} else {
				return result;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return result;
		}
	}

	public ArrayList<String[]> getSentences(String dbName, String wort) {
		Statement stmt;
		String query = "select s.beispiel,q.name_lang from saetze as s join inv_liste as i join wortliste as w join quelle as q on s.quelle=q.quelle and i.wort_nr = w.wort_nr and i.bsp_nr = s.bsp_nr and w.wort_alph = \'"
				+ wort + "\';";
		ResultSet rs;
		ArrayList<String[]> result = new ArrayList<String[]>();
		Connection dbCon;
		try {
			if ((dbCon = connectToDB(dbName)) != null) {
				stmt = dbCon.prepareStatement(query);
				rs = stmt.executeQuery(query);
				while (rs.next()) {
					result.add(new String[] { rs.getString(1), rs.getString(2) });
				}
				dbCon.close();
				return result;
			} else {
				throw new SQLException("Can't connect to db");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private ArrayList<Integer> getWordIds(String dbName, String wort) {
		Statement stmt;
		String query = "select wort_nr from wortliste where wort_alph like '"
				+ wort + "'";
		ResultSet rs;
		ArrayList<Integer> result = new ArrayList<Integer>();
		Connection dbCon;
		try {
			if ((dbCon = connectToDB(dbName)) != null) {
				stmt = dbCon.prepareStatement(query);
				rs = stmt.executeQuery(query);
				while (rs.next()) {
					result.add(rs.getInt(1));
				}
				dbCon.close();
				return result;
			} else {
				throw new SQLException("Can't connect to db");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Map<String, Integer> getKollokSimWords(String dbName, String wort) {
		Statement stmt;
		String whereWortNrString = "where ks.wort_nr1=";
		ArrayList<Integer> wordIDList = getWordIds(dbName, wort);
		Connection dbCon;
		for (int i = 0; i < wordIDList.size(); i++) {
			if (i == 0) {
				whereWortNrString += wordIDList.get(i).toString();
			} else {
				whereWortNrString += " OR ks.wort_nr1=" + wordIDList.get(i);
			}
		}
		String query = "select wl.wort_alph,ks.signifikanz "
				+ "from kollok_sig as ks join wortliste as wl "
				+ "on(wl.wort_nr=ks.wort_nr2) " + whereWortNrString
				+ " order by signifikanz desc limit 15;";
		Map<String, Integer> result = new HashMap<>();
		ResultSet rs;
		System.out.println(wordIDList.size() + " listsize " + query);
		try {
			if ((dbCon = connectToDB(dbName)) != null) {
				stmt = dbCon.prepareStatement(query);
				rs = stmt.executeQuery(query);
				while (rs.next()) {
					result.put(rs.getString(1), rs.getInt(2));
				}
				dbCon.close();
				return sortByValue(result, 0);
			} else {
				throw new SQLException("Can't connect to db");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(
			Map<K, V> map, int size) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue()) * -1;
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			if (size != 0)
				if (result.size() >= size)
					break;
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

}
