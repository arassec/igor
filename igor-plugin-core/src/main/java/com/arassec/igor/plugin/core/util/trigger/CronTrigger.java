package com.arassec.igor.plugin.core.util.trigger;

import com.arassec.igor.application.annotation.IgorComponent;
import com.arassec.igor.core.model.annotation.IgorParam;
import com.arassec.igor.core.model.job.misc.ParameterSubtype;
import com.arassec.igor.core.model.trigger.BaseTrigger;
import com.arassec.igor.core.model.trigger.ScheduledTrigger;
import com.arassec.igor.plugin.core.CoreCategory;
import com.arassec.igor.plugin.core.validation.ValidCronExpression;
import jakarta.validation.constraints.NotEmpty;
import lombok.Setter;


/**
 * <h2>CRON Trigger</h2>
 *
 * <h3>Description</h3>
 * The CRON trigger runs a job periodically according to its cron expression. Just like the UNIX cron daemon does.<br>
 *
 * A CRON expression consists of six fields: <pre>second, minute, hour, day, month, weekday</pre><br>
 *
 * Month and weekday names can be given as the first three letters of the English names.<br>
 *
 * The following special characters can be used in a CRON expression:<br>
 * <table>
 *     <caption>CRON special characters</caption>
 *     <tr>
 *         <th>character</th>
 *         <th>means</th>
 *         <th>explanation</th>
 *         <th>example</th>
 *     </tr>
 *     <tr>
 *         <td>*</td>
 *         <td>all</td>
 *         <td>the event should happen for every time unit</td>
 *         <td>"*" in the 'minute' field means "for every minute"</td>
 *     </tr>
 *     <tr>
 *         <td>?</td>
 *         <td>any</td>
 *         <td>denotes in the 'day-of-month' and 'day-of-week' fields the arbitrary value</td>
 *         <td>"?" in the 'day-of-week' field indicates that the event should occur no matter the actual week day</td>
 *     </tr>
 *     <tr>
 *         <td>-</td>
 *         <td>range</td>
 *         <td>determines a value range</td>
 *         <td>"20-23" in the 'minute' field means "run at minute 20, 21, 22, 23"</td>
 *     </tr>
 *     <tr>
 *         <td>,</td>
 *         <td>values</td>
 *         <td>specify multiple values</td>
 *         <td>"2,5,11" in the 'hour' field means "run at hour 2, 5 and 11"</td>
 *     </tr>
 *     <tr>
 *         <td>/</td>
 *         <td>incremental</td>
 *         <td>specify incremental values</td>
 *         <td>"5/15" in the 'seconds' field means "the seconds 5, 20, 35 and 50"</td>
 *     </tr>
 * </table>
 *
 * <h3>Examples</h3>
 * <table>
 *     <caption>CRON examples</caption>
 *     <tr>
 *         <th>CRON expression</th>
 *         <th>Description</th>
 *     </tr>
 *     <tr>
 *         <td>0 0 * * * *</td>
 *         <td>The top of every hour of every day.</td>
 *     </tr>
 *     <tr>
 *         <td>*&#47;30 * * * * *</td>
 *         <td>Every thirty seconds.</td>
 *     </tr>
 *     <tr>
 *         <td>0 *&#47;15 * * * *</td>
 *         <td>Once every fifteen minutes.</td>
 *     </tr>
 *     <tr>
 *         <td>0 0 8,10 * * *</td>
 *         <td>8 and 10o'clock of every day.</td>
 *     </tr>
 *     <tr>
 *         <td>0 0/30 8-10 * * *</td>
 *         <td>8:00,8:30,9:00,9:30and 10o'clock every day.</td>
 *     </tr>
 *     <tr>
 *         <td>0 0 9-17 * * MON-FRI</td>
 *         <td>On the hour nine-to-five weekdays.</td>
 *     </tr>
 *     <tr>
 *         <td>0 0 0 25 12 ?</td>
 *         <td>Every Christmas Day at midnight.</td>
 *     </tr>
 * </table>
 */
@IgorComponent(typeId = "cron-trigger", categoryId = CoreCategory.UTIL)
public class CronTrigger extends BaseTrigger implements ScheduledTrigger {

    /**
     * The CRON expression that is used to trigger the job.
     */
    @Setter
    @NotEmpty
    @ValidCronExpression
    @IgorParam(subtype = ParameterSubtype.CRON)
    protected String cronExpression;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCronExpression() {
        return cronExpression;
    }

}
