package common.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ScheduleUtil {

    public static int timeToIndex(String timeStr) {
        // split timeStr to hour and minute
        String[] parts = timeStr.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        // calculate index
        if (minute == 0) {
            return hour * 2;
        }
        else if (minute < 30) {
            return hour * 2 + 1;
        }
        else{
            return hour * 2 + 2;
        }
    }

    public static boolean checkTimeSlot(String timeSlots, String checkTime) {
        // get index of checkTime
        int index = timeToIndex(checkTime);
        // return the time slot at the index
        return timeSlots.charAt(index) == '1';
    }


    public static boolean judgeNowFromScheduleInDay(String schedule) {
        // get current time
        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return checkTimeSlot(schedule,now.format(formatter));

    }

    public static boolean judgeNowFromScheduleInWeek(List<String> schedules) {
        //get current day of week
        LocalDate now = LocalDate.now();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        //get index of current day of week
        int index = dayOfWeek.getValue()-1;

        return judgeNowFromScheduleInDay(schedules.get(index));

    }

}
