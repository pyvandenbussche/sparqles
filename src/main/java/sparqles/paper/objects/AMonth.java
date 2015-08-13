package sparqles.paper.objects;

import java.util.Date;

public class AMonth {
	private Date date=null;
	private int zeroFive = 0;
	private int fiveSeventyfive = 0;
	private int seventyfiveNintyfive = 0;
	private int nintyfiveNintynine = 0;
	private int nintynineHundred = 0;
	
	
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
	
	public void addEndpoint(long availTests, long unavailTests){
		//prevent from division by 0
		if(availTests+unavailTests == 0) zeroFive++;
		else{
			double perc = ((double)availTests/(availTests+unavailTests))*100;
			if(perc<=5)zeroFive++;
			else if(perc>5 && perc<=75)fiveSeventyfive++;
			else if(perc>75 && perc<=95)seventyfiveNintyfive++;
			else if(perc>95 && perc<=99)nintyfiveNintynine++;
			else if(perc>99)nintynineHundred++;
		}
	}
}
