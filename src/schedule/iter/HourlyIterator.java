package schedule.iter;

import java.util.Calendar;
import java.util.Date;


public class HourlyIterator implements ScheduleIterator {
    private final int minute, second;
    private final Calendar calendar = Calendar.getInstance();

    public HourlyIterator(int minute, int second) {
        this( minute, second, new Date());
    }

    public HourlyIterator(int minute, int second, Date date) {
        this.minute = minute;
        this.second = second;
        calendar.setTime(date);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        if (!calendar.getTime().before(date)) {
            calendar.add(Calendar.HOUR, -1);
        }
    }

    public Date next() {
        calendar.add(Calendar.HOUR, 1);
        return calendar.getTime();
    }

}