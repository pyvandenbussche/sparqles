package sparqles.paper.objects;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AMonth {
	private Date date=null;
	private int zeroFive = 0;
	private int fiveSeventyfive = 0;
	private int seventyfiveNintyfive = 0;
	private int nintyfiveNintynine = 0;
	private int nintynineHundred = 0;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
	private static DecimalFormat df = new DecimalFormat("0.####",new DecimalFormatSymbols(Locale.US));
	
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public int getZeroFive() {
		return zeroFive;
	}
	public void setZeroFive(int zeroFive) {
		this.zeroFive = zeroFive;
	}
	public int getFiveSeventyfive() {
		return fiveSeventyfive;
	}
	public void setFiveSeventyfive(int fiveSeventyfive) {
		this.fiveSeventyfive = fiveSeventyfive;
	}
	public int getSeventyfiveNintyfive() {
		return seventyfiveNintyfive;
	}
	public void setSeventyfiveNintyfive(int seventyfiveNintyfive) {
		this.seventyfiveNintyfive = seventyfiveNintyfive;
	}
	public int getNintyfiveNintynine() {
		return nintyfiveNintynine;
	}
	public void setNintyfiveNintynine(int nintyfiveNintynine) {
		this.nintyfiveNintynine = nintyfiveNintynine;
	}
	public int getNintynineHundred() {
		return nintynineHundred;
	}
	public void setNintynineHundred(int nintynineHundred) {
		this.nintynineHundred = nintynineHundred;
	}
	
	
}
