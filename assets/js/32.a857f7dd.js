(window.webpackJsonp=window.webpackJsonp||[]).push([[32],{354:function(t,e,a){"use strict";a.r(e);var l=a(13),i=Object(l.a)({},(function(){var t=this,e=t._self._c;return e("ContentSlotsDistributor",{attrs:{"slot-key":t.$parent.slotKey}},[e("h1",{attrs:{id:"cron-trigger"}},[e("a",{staticClass:"header-anchor",attrs:{href:"#cron-trigger"}},[t._v("#")]),t._v(" CRON Trigger")]),t._v(" "),e("h2",{attrs:{id:"description"}},[e("a",{staticClass:"header-anchor",attrs:{href:"#description"}},[t._v("#")]),t._v(" Description")]),t._v(" "),e("p",[t._v("The CRON trigger runs a job periodically according to its cron expression. Just like the UNIX cron daemon does.")]),t._v(" "),e("p",[t._v("A CRON expression consists of six fields:\n"),e("code",[t._v("second, minute, hour, day, month, weekday")])]),t._v(" "),e("p",[t._v("Month and weekday names can be given as the first three letters of the English names.")]),t._v(" "),e("p",[t._v("The following special characters can be used in a CRON expression:")]),t._v(" "),e("table",[e("thead",[e("tr",[e("th",{staticStyle:{"text-align":"left"}},[t._v("character")]),t._v(" "),e("th",{staticStyle:{"text-align":"left"}},[t._v("means")]),t._v(" "),e("th",{staticStyle:{"text-align":"left"}},[t._v("explanation")]),t._v(" "),e("th",{staticStyle:{"text-align":"left"}},[t._v("example")])])]),t._v(" "),e("tbody",[e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("*")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("all")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("the event should happen for every time unit")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v('"*" in the \'minute\' field means "for every minute"')])]),t._v(" "),e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("?")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("any")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("denotes in the 'day-of-month' and 'day-of-week' fields the arbitrary value")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("\"?\" in the 'day-of-week' field indicates that the event should occur no matter the actual week day")])]),t._v(" "),e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("-")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("range")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("determines a value range")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v('"20-23" in the \'minute\' field means "run at minute 20, 21, 22, 23"')])]),t._v(" "),e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v(",")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("values")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("specify multiple values")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v('"2,5,11" in the \'hour\' field means "run at hour 2, 5 and 11"')])]),t._v(" "),e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("/")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("incremental")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("specify incremental values")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v('"5/15" in the \'seconds\' field means "the seconds 5, 20, 35 and 50"')])])])]),t._v(" "),e("h2",{attrs:{id:"examples"}},[e("a",{staticClass:"header-anchor",attrs:{href:"#examples"}},[t._v("#")]),t._v(" Examples")]),t._v(" "),e("table",[e("thead",[e("tr",[e("th",{staticStyle:{"text-align":"left"}},[t._v("CRON expression")]),t._v(" "),e("th",{staticStyle:{"text-align":"left"}},[t._v("Description")])])]),t._v(" "),e("tbody",[e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("0 0 * * * *")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("The top of every hour of every day.")])]),t._v(" "),e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("*/30 * * * * *")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("Every thirty seconds.")])]),t._v(" "),e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("0 */15 * * * *")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("Once every fifteen minutes.")])]),t._v(" "),e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("0 0 8,10 * * *")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("8 and 10o'clock of every day.")])]),t._v(" "),e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("0 0/30 8-10 * * *")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("8:00,8:30,9:00,9:30and 10o'clock every day.")])]),t._v(" "),e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("0 0 9-17 * * MON-FRI")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("On the hour nine-to-five weekdays.")])]),t._v(" "),e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("0 0 0 25 12 ?")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("Every Christmas Day at midnight.")])])])]),t._v(" "),e("h2",{attrs:{id:"parameters"}},[e("a",{staticClass:"header-anchor",attrs:{href:"#parameters"}},[t._v("#")]),t._v(" Parameters")]),t._v(" "),e("p",[t._v("The component can be configured by the following parameters:")]),t._v(" "),e("table",[e("thead",[e("tr",[e("th",{staticStyle:{"text-align":"left"}},[t._v("Parameter")]),t._v(" "),e("th",{staticStyle:{"text-align":"left"}},[t._v("Description")])])]),t._v(" "),e("tbody",[e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("Cron Expression")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("The CRON expression that is used to trigger the job.")])])])])])}),[],!1,null,null,null);e.default=i.exports}}]);