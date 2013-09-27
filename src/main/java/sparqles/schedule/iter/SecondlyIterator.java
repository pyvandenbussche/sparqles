package sparqles.schedule.iter;

import java.util.Calendar;
import java.util.Date;


public class SecondlyIterator implements ScheduleIterator {

	private final int second;
	private final Calendar calendar = Calendar.getInstance();

	public SecondlyIterator(int second) {
		this(second, new Date());
	}

	public SecondlyIterator(int second, Date date) {
		this.second = second;
		calendar.setTime(date);
		calendar.set(Calendar.SECOND, second);
		calendar.set(Calendar.MILLISECOND, 0);
		if (!calendar.getTime().before(date)) {
			calendar.add(Calendar.SECOND, -second);
		}
	}

	public Date next() {
		calendar.add(Calendar.SECOND, second);
		return calendar.getTime();
	}

}