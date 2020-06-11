# CRON Trigger

## Description
The CRON trigger runs a job periodically according to its cron expression. 
Just like the UNIX cron daemon does.

## Parameters
The following parameters can be configured for cron trigger:

Parameter | Description
---|:---|
CRON expression | A CRON expression

A CRON expression consists of six fields:
```
second, minute, hour, day, month, weekday
```
Month and weekday names can be given as the first three letters of the English names.

The following special characters can be used in a CRON expression:

character | means | explanation | example
---|:---|:---|:---|
* | all | the event should happen for every time unit | "*" in the 'minute' field means "for every minute" 
? | any | denotes in the 'day-of-month' and 'day-of-week' fields the arbitrary value | "?" in the 'day-of-week' field indicates that the event should occur no matter the actual week day
- | range | determines a value range | "20-23" in the 'minute' field means "run at minute 20, 21, 22, 23"
, | values | specify multiple values | "2,5,11" in the 'hour' field means "run at hour 2, 5 and 11"
/ | incremental | specify incremental values | "5/15" in the 'seconds' field means "the seconds 5, 20, 35 and 50"

## Examples

CRON expression | Description
---|:---|
0 0 * * * * | The top of every hour of every day.
*/30 * * * * * | Every thirty seconds.
0 */15 * * * * | Once every fifteen minutes.
0 0 8,10 * * * | 8 and 10 o'clock of every day.
0 0/30 8-10 * * * | 8:00, 8:30, 9:00, 9:30 and 10 o'clock every day.
0 0 9-17 * * MON-FRI | On the hour nine-to-five weekdays.
0 0 0 25 12 ? | Every Christmas Day at midnight.