(window.webpackJsonp=window.webpackJsonp||[]).push([[26],{348:function(t,e,a){"use strict";a.r(e);var i=a(13),o=Object(i.a)({},(function(){var t=this,e=t._self._c;return e("ContentSlotsDistributor",{attrs:{"slot-key":t.$parent.slotKey}},[e("h1",{attrs:{id:"job"}},[e("a",{staticClass:"header-anchor",attrs:{href:"#job"}},[t._v("#")]),t._v(" Job")]),t._v(" "),e("h2",{attrs:{id:"description"}},[e("a",{staticClass:"header-anchor",attrs:{href:"#description"}},[t._v("#")]),t._v(" Description")]),t._v(" "),e("p",[t._v("Jobs are the core elements of igor. They contain all configurations required to fulfill a specific... job.")]),t._v(" "),e("p",[t._v("A "),e("strong",[t._v("Trigger")]),t._v(" determines when the job should run. It activates job execution e.g. based on a daily schedule or on\nreceived events. The trigger creates an initial data item, which is passed to the job's first action.")]),t._v(" "),e("p",[t._v('Each data item is then handed over to the following action and so on. Think about the "Java Stream API" to get an idea\nof the process.')]),t._v(" "),e("p",[e("strong",[t._v("Actions")]),t._v(" implement the job's logic and operate on the data items of the data stream.")]),t._v(" "),e("h2",{attrs:{id:"parameters"}},[e("a",{staticClass:"header-anchor",attrs:{href:"#parameters"}},[t._v("#")]),t._v(" Parameters")]),t._v(" "),e("p",[t._v("The following parameters can be configured for every job.")]),t._v(" "),e("table",[e("thead",[e("tr",[e("th",[t._v("Parameter")]),t._v(" "),e("th",{staticStyle:{"text-align":"left"}},[t._v("Description")])])]),t._v(" "),e("tbody",[e("tr",[e("td",[t._v("Active")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("Active jobs are triggered by the configured trigger, inactive jobs not.")])]),t._v(" "),e("tr",[e("td",[t._v("Name")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("The name of the job.")])]),t._v(" "),e("tr",[e("td",[t._v("Description")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("An optional description of the job.")])]),t._v(" "),e("tr",[e("td",[t._v("History Limit")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("Number of job executions that are kept by igor. If the limit is reached, old successful executions will be removed.")])]),t._v(" "),e("tr",[e("td",[t._v("Simulation Limit")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("Maximum number of data items that is processed by actions during a job simulation.")])]),t._v(" "),e("tr",[e("td",[t._v("Fault tolerant")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("If checked, the job will be triggered even if the last job execution failed. A successful execution will mark all previous, failed executions as 'Resolved'. If unchecked, the job will not be triggered if the last job execution failed.")])]),t._v(" "),e("tr",[e("td",[t._v("Num Threads")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("The number of threads the job uses per action to process data items. Note that some actions may require execution in a single thread, which is then automatically handled by the job.")])])])]),t._v(" "),e("p",[t._v("Additional configuration parameters might be available, depending on the trigger's type.")])])}),[],!1,null,null,null);e.default=o.exports}}]);