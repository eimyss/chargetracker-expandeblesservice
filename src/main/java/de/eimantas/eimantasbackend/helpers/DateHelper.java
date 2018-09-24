package de.eimantas.eimantasbackend.helpers;

import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

public class DateHelper {


    private static org.slf4j.Logger logger = LoggerFactory.getLogger(DateHelper.class);


    //testing can be tricky...‚
    public static LocalDateTime getEndOfWeek(int weekNumber) {

        LocalDateTime desiredDateNext = LocalDateTime.now()
                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, weekNumber+1)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        return desiredDateNext.minusDays(1);
    }

    // I just hope it works
    public static LocalDateTime getBeginOfWeek(int weekNumber) {

        LocalDateTime desiredDate = LocalDateTime.now()
                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, weekNumber)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        return desiredDate;
    }


    /**
     * the months before are based on todays date
     * @param monthsbefore
     * @param date to check
     * @return
     */
    public static boolean isInMonth(int monthsbefore, Instant date) {

        // OMG So innefient
        LocalDate checkDate = LocalDateTime.ofInstant(date, ZoneOffset.UTC).toLocalDate();

        LocalDate today= LocalDate.now();
        LocalDate ago = today.minus(Period.ofMonths(monthsbefore));

        LocalDate start = ago.withDayOfMonth(1);
        LocalDate end = ago.withDayOfMonth(ago.lengthOfMonth());

        return start.isBefore(checkDate) && end.isAfter(checkDate);

    }

    public static String getMonthNameForAgo(int monthsBefore) {
        LocalDate today= LocalDate.now();
        LocalDate ago = today.minus(Period.ofMonths(monthsBefore));
        Instant instant = ago.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        Date res = Date.from(instant);
        return  new SimpleDateFormat("MMM").format(res);
    }
}
