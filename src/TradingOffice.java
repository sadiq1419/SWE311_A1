import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;


// class to safe  dividend records
class divRecord {
	private Double div;
	private Date date;
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

	public divRecord(Date date, Double div) {
		this.div = div;
		this.date = date;
	}

	public Double getDiv() {
		return div;
	}

	public Date getDate() {
		return date;
	}

	@Override
	public String toString() {
		return String.format("Date: %s Dividend: %f", this.dateFormatter.format(this.date), this.div);
	}
}

//class to safe normal records
class record {
	private Date date;
	private double open;
	private double high;
	private double low;
	private double close;
	private double adjClose;
	private double volume;
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

	public record(Date date, double open, double high, double low, double close, double adjClose, double volume)
			throws ParseException {

		this.date = date;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.adjClose = adjClose;
		this.volume = volume;
	}

	public Date getDate() {
		return this.date;
	}

	public double getOpen() {
		return this.open;
	}

	public double getHigh() {
		return this.high;
	}

	public double getLow() {
		return this.low;
	}

	public double getClose() {
		return this.close;
	}

	public double getAdjClose() {
		return this.adjClose;
	}

	public double getVolume() {
		return this.volume;
	}

	@Override
	public String toString() {
		return String.format("Date: %s Open: %f High: %f Low: %f Close: %f Adj-Close: %f Volume: %f",
				this.dateFormatter.format(this.date), this.open, this.high, this.low, this.close, this.adjClose,
				this.volume);
	}
}

//class to encapsulate all the needed calculation 
class dataFrame {
	private ArrayList<record> records;
	private ArrayList<divRecord> divRecords;
	private Date firstDate;
	private Date lastDate;
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	Calendar cal = Calendar.getInstance();

	public dataFrame(String datafile, String dicDataFile) {
		this.records = new ArrayList<record>();
		this.divRecords = new ArrayList<divRecord>();
		try {

			// create csvReader object
			CSVReader csvReader = new CSVReaderBuilder(new FileReader(datafile)).withSkipLines(1).build();
			String[] nextRecord;

			// we are going to read data line by line
			while ((nextRecord = csvReader.readNext()) != null) {
				// set each attribute to a var
				String date = nextRecord[0];
				double open = Double.parseDouble(nextRecord[1]);
				double high = Double.parseDouble(nextRecord[2]);
				double low = Double.parseDouble(nextRecord[3]);
				double close = Double.parseDouble(nextRecord[4]);
				double adjClose = Double.parseDouble(nextRecord[5]);
				double volume = Double.parseDouble(nextRecord[6]);
				// create a record and added to ArrayList
				records.add(new record(this.dateFormatter.parse(date), open, high, low, close, adjClose, volume));
			}
			// find the start and end date within our data
			this.firstDate = records.get(0).getDate();
			this.lastDate = records.get(records.size() - 1).getDate();

			// make csvReader object for dividend data
			csvReader = new CSVReaderBuilder(new FileReader(dicDataFile)).withSkipLines(1).build();
			String[] nextDiv;

			// we are going to read data line by line
			while ((nextDiv = csvReader.readNext()) != null) {
				String date = nextDiv[0];
				double div = Double.parseDouble(nextDiv[1]);

				divRecords.add(new divRecord(this.dateFormatter.parse(date), div));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public record getRecord(Date date) {
		for (record r : this.records) {
			if (r.getDate().equals(date)) {
				return r;
			}
		}
		return null;
	}

	public record getRecord(String date) throws ParseException {
		return getRecord(this.dateFormatter.parse(date));
	}

// SMA and EMA calculation functions
	public double SMA(Date start, int windSize) {
		record startRec = getRecord(start);
		int startRecIndex = this.records.indexOf(startRec);
		double runningSum = 0;

		// window size range checking
		if (startRec == null) {
			System.out.println("Invalid record");
			return -1;
		} else if (start.before(firstDate) || start.after(lastDate) || startRecIndex + windSize > this.records.size()) {
			System.out.println("invalid range");
			return -1;
		} else if (windSize == 1) {
			return getRecord(start).getClose();
		} else {
			for (int i = 0; i < windSize; i++) {
				runningSum += this.records.get(startRecIndex + i).getClose();
			}
		}

		return runningSum / windSize;
	}

	public double EMA(Date start, int windSize) {
		record startRec = getRecord(start);
		int startRecIndex = this.records.indexOf(startRec);
		double EMA = -1;

		if (startRec == null) {
			System.out.println("Invalid record");
			return -1;
		} else if (start.before(firstDate) || start.after(lastDate) || startRecIndex + windSize > this.records.size()) {
			System.out.println("invalid range");
			return -1;
		} else if (windSize == 1) {
			return getRecord(start).getClose();
		} else {
			double prevEMA = SMA(start, windSize);

			for (int j = 0; j < windSize; j++) {
				double a = 2 / ((double) windSize + 1);
				double currentRecPrice = this.records.get(startRecIndex + j).getClose();
				EMA = a * currentRecPrice + (1 - a) * prevEMA;
				prevEMA = EMA;
			}
			return EMA;
		}
	}

// overloading 
	public double SMA(String start, int windSize) throws ParseException {
		return SMA(this.dateFormatter.parse(start), windSize);
	}

	public double EMA(String start, int windSize) throws ParseException {
		return EMA(this.dateFormatter.parse(start), windSize);
	}

// functions for dividend per share
	public double getAnnualDiv(Date date) {
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		double annualDiv = 0;

		for (divRecord r : this.divRecords) {
			cal.setTime(r.getDate());
			if (year == cal.get(Calendar.YEAR)) {
				annualDiv += r.getDiv();
			}
		}

		return annualDiv;
	}

	public double getAnnualDiv(String date) throws ParseException {
		return getAnnualDiv(this.dateFormatter.parse(date));
	}

	public double getDivSharePrice(Date date) {
		record r = getRecord(date);
		return getAnnualDiv(date) / r.getVolume();
	}

	public double getDivSharePrice(String date) throws ParseException {
		return getDivSharePrice(this.dateFormatter.parse(date));
	}
}

public class TradingOffice {
	public static void main(String[] args) throws ParseException {
		dataFrame df = new dataFrame("AAPL.csv", "AAPL-Dividends.csv");
	
		System.out.println(df.SMA("1980-12-12", 30));
		System.out.println(df.EMA("1980-12-12", 25));
		System.out.println(df.getAnnualDiv("1991-02-15"));
		System.out.println(df.getDivSharePrice("1991-02-15"));
	}
}
