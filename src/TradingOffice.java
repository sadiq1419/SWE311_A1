import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.commons.lang3.ObjectUtils;

class record{
	private Date date;
	private double open;
	private double high;
	private double low;
	private double close;
	private double adjClose;
	private double volume;
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
	public record(Date date, double open, double high, double low, double close, double adjClose, double volume) throws ParseException {
		
		this.date = date;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.adjClose = adjClose;
		this.volume = volume;
	}
	
	public Date getDate() {
		return date;
	}

	public double getOpen() {
		return open;
	}

	public double getHigh() {
		return high;
	}

	public double getLow() {
		return low;
	}

	public double getClose() {
		return close;
	}

	public double getAdjClose() {
		return adjClose;
	}

	public double getVolume() {
		return volume;
	}

	@Override
	public String toString() {
		return String.format("Date: %s Open: %f High: %f Low: %f Close: %f Adj-Close: %f Volume: %f",formatter.format(this.getDate()),this.getOpen(),this.getHigh(),this.getLow(),this.getClose(),this.getAdjClose(),this.getVolume());
	}
}

class dataFrame {
	private ArrayList<record> records;
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
	Calendar cal = Calendar.getInstance();
	public dataFrame(String datafile) {
		this.records = new ArrayList<record>();
		try {
			 
	        // create csvReader object 
	        CSVReader csvReader = new CSVReaderBuilder(new FileReader(datafile)).withSkipLines(1).build();
	        String[] nextRecord;
	        
	        // we are going to read data line by line
	        while ((nextRecord = csvReader.readNext()) != null) {
	        	String date = nextRecord[0];
	    		double open = Double.parseDouble(nextRecord[1]);
	    		double high = Double.parseDouble(nextRecord[2]);
	    		double low = Double.parseDouble(nextRecord[3]);
	    		double close = Double.parseDouble(nextRecord[4]);
	    		double adjClose = Double.parseDouble(nextRecord[5]);
	    		double volume = Double.parseDouble(nextRecord[6]);
	    		records.add(new record(formatter.parse(date),open,high,low,close,adjClose,volume));
 	        }
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public ArrayList<record> getRecordList() {
		return records;
	}
	
	public record getRecord(Date date) {
		for (record r : this.records) {
			if(r.getDate().equals(date)) {
				return r;
			}
		}
		return null;
	}
	
	public record getRecord(String date) throws ParseException {
		return getRecord(formatter.parse(date));
	}
	

	private double SMA(Date start, Date end) {
		Date LastDate = records.get(records.size()-1).getDate();
		Date FirstDate = records.get(0).getDate();
		
		record startRec = getRecord(start);
		record endRec = getRecord(end);
		int startRecIndex = this.records.indexOf(startRec);
		int endRecIndex = this.records.indexOf(endRec);
		
		int recNum = endRecIndex-startRecIndex+1;
		double runningSum = 0;

		if(startRec == null || endRec == null){
			return -1;
		}else if(start.before(FirstDate) || end.after(LastDate) || end.before(start) || start.after(end)) {
			return -1;
		}else if(start.equals(end)){
			return getRecord(start).getClose();
		}else {
			for(int i = startRecIndex; i <= endRecIndex; i++) {

				runningSum += this.records.get(i).getClose();
			}
		}
		
		return runningSum/recNum;
	} 
	
	public double SMA(String start, String end) throws ParseException {	
		return SMA(formatter.parse(start),formatter.parse(end));	
	}
}

public class TradingOffice {
	static dataFrame df = new dataFrame("AAPL.csv");
	public static void main(String[] args) throws ParseException {
		System.out.println(df.SMA("1980-12-12", "1981-12-15"));
	}
}
