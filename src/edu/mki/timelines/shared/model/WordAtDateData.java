package edu.mki.timelines.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class WordAtDateData implements IsSerializable {

	private Float freq = 0F;
	private boolean peak = false;
	private String peakComment = "";
	private long dateLong = 0L;
	private int rank = 0;

	public WordAtDateData() {

	}

	public WordAtDateData(Float freq, Boolean peak, String peakComment) {
		this.setFreq(freq);
		this.setPeak(peak);
		this.setPeakComment(peakComment);
	}

	public WordAtDateData(Float freq, Boolean peak) {
		this.setFreq(freq);
		this.setPeak(peak);
		this.setPeakComment("");
	}

	public WordAtDateData(Float freq, long dateLong) {
		this.setFreq(freq);
		this.setPeak(false);
		this.setPeakComment("");
		this.dateLong = dateLong;
	}

	public WordAtDateData(Float freq) {
		this.setFreq(freq);
		this.setPeak(false);
		this.setPeakComment("");
	}

	public Float getFreq() {
		return freq;
	}

	public void setFreq(Float freq) {
		this.freq = freq;
	}

	public boolean isPeak() {
		return peak;
	}

	public void setPeak(boolean peak) {
		this.peak = peak;
	}

	public String getPeakComment() {
		return peakComment;
	}

	public void setPeakComment(String peakComment) {
		this.peakComment = peakComment;
	}

	public long getDateLong() {
		return dateLong;
	}

	public void setDateLong(long dateLong) {
		this.dateLong = dateLong;
	}

	public void setRank(int rint) {
		this.rank = rint;
	}

	public int getRank() {
		return rank;
	}
}
