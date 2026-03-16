package org.ohdsi.webapi.arachne.scheduler.api.v1.converter;

import static com.cronutils.model.field.expression.FieldExpressionFactory.always;
import static com.cronutils.model.field.expression.FieldExpressionFactory.every;
import static com.cronutils.model.field.expression.FieldExpressionFactory.on;
import static com.cronutils.model.field.expression.FieldExpressionFactory.questionMark;

import com.cronutils.builder.CronBuilder;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.FieldExpressionFactory;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.IntegerFieldValue;
import org.ohdsi.webapi.arachne.commons.converter.BaseConvertionServiceAwareConverter;
import org.ohdsi.webapi.arachne.scheduler.api.v1.dto.ArachneJobDTO;
import org.ohdsi.webapi.arachne.scheduler.model.ArachneJob;
import org.ohdsi.webapi.arachne.scheduler.model.JobExecutingType;
import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class BaseArachneJobDTOToArachneJobConverter<S extends ArachneJobDTO, T extends ArachneJob> extends BaseConvertionServiceAwareConverter<S, T> {

    protected final CronDefinition cronDefinition;

    protected BaseArachneJobDTOToArachneJobConverter(CronDefinition cronDefinition) {

        this.cronDefinition = cronDefinition;
    }

    @Override
    protected final void convert(final S source, T target) {
        final Date startDate = source.getStartDate();
        final JobExecutingType frequency = source.getFrequency();
        final List<DayOfWeek> weekDays = source.getWeekDays();
        final String cron = createCron(startDate, frequency, weekDays);
        target.setCron(cron);
        target.setId(source.getId());
        target.setEnabled(source.isEnabled());
        target.setFrequency(frequency);
        target.setRecurringTimes(Optional.ofNullable(source.getRecurringTimes()).orElse(0));
        target.setRecurringUntilDate(source.getRecurringUntilDate());
        target.setStartDate(source.getStartDate());
        target.setWeekDays(weekDays);
        convertJob(source, target);
    }

    protected abstract void convertJob(final S source, T target);

    protected String createCron(Date startDate, JobExecutingType frequency, List<DayOfWeek> weekDays) {

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        final int second = calendar.get(Calendar.SECOND);
        final int minute = calendar.get(Calendar.MINUTE);
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int month = calendar.get(Calendar.MONTH) + 1;

        Cron cron;
        switch (frequency) {
            case ONCE:
                cron = CronBuilder.cron(cronDefinition)
                        .withDoM(on(day))
                        .withMonth(on(month))
                        .withDoW(questionMark())
                        .withHour(on(hour))
                        .withMinute(on(minute))
                        .withSecond(on(second))
                        .instance();
                break;
            case HOURLY:
                cron = CronBuilder.cron(cronDefinition)
                        .withDoM(always())
                        .withMonth(always())
                        .withDoW(questionMark())
                        .withHour(always())
                        .withMinute(on(minute))
                        .withSecond(on(second))
                        .instance();
                break;
            case DAILY: {
                cron = CronBuilder.cron(cronDefinition)
                        .withDoM(every(1))
                        .withMonth(every(1))
                        .withDoW(questionMark())
                        .withHour(on(hour))
                        .withMinute(on(minute))
                        .withSecond(on(second))
                        .instance();
                break;
            }
            case WEEKLY: {
                if (weekDays == null || weekDays.isEmpty()) {
                    final String message = String.format("Execution period %s must have at least 1 day of execute", frequency);
                    throw new IllegalArgumentException(message);
                }
                final List<FieldExpression> collect = weekDays.stream().map(
                        dayOfWeek -> (FieldExpression) new On(new IntegerFieldValue(dayOfWeek.getValue()))).collect(Collectors.toList());
                cron = CronBuilder.cron(cronDefinition)
                        .withDoM(questionMark())
                        .withMonth(always())
                        .withDoW(FieldExpressionFactory.and(collect))
                        .withHour(on(hour))
                        .withMinute(on(minute))
                        .withSecond(on(second))
                        .instance();
                break;
            }
            case MONTHLY: {
                cron = CronBuilder.cron(cronDefinition)
                        .withDoM(on(day))
                        .withMonth(always())
                        .withDoW(questionMark())
                        .withHour(on(hour))
                        .withMinute(on(minute))
                        .withSecond(on(second))
                        .instance();
                break;
            }
            case YEARLY: {
                cron = CronBuilder.cron(cronDefinition)
                        .withDoM(on(day))
                        .withMonth(on(month))
                        .withDoW(questionMark())
                        .withHour(on(hour))
                        .withMinute(on(minute))
                        .withSecond(on(second))
                        .instance();
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported period: " + frequency);
            }
        }
        return cron.asString();
    }
}
