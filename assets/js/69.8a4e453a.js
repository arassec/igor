(window.webpackJsonp=window.webpackJsonp||[]).push([[69],{388:function(t,e,a){"use strict";a.r(e);var s=a(13),i=Object(s.a)({},(function(){var t=this,e=t._self._c;return e("ContentSlotsDistributor",{attrs:{"slot-key":t.$parent.slotKey}},[e("h1",{attrs:{id:"rabbitmq-message-trigger"}},[e("a",{staticClass:"header-anchor",attrs:{href:"#rabbitmq-message-trigger"}},[t._v("#")]),t._v(" RabbitMQ Message Trigger")]),t._v(" "),e("h2",{attrs:{id:"description"}},[e("a",{staticClass:"header-anchor",attrs:{href:"#description"}},[t._v("#")]),t._v(" Description")]),t._v(" "),e("p",[t._v("A trigger that fires on incoming messages on a RabbitMQ server.")]),t._v(" "),e("p",[t._v("This message trigger is an event based trigger that processes an incoming message as data item as soon as it is received.")]),t._v(" "),e("h2",{attrs:{id:"limitations-and-caveats"}},[e("a",{staticClass:"header-anchor",attrs:{href:"#limitations-and-caveats"}},[t._v("#")]),t._v(" Limitations and Caveats")]),t._v(" "),e("p",[t._v("Not all actions are available for event-triggered jobs. E.g. sorting by timestamp requires all data items, that should be sorted, to be known to the action. Since event-triggered jobs process a continuous stream of incoming events, there is no fixed number of data items to sort.")]),t._v(" "),e("p",[t._v("During simulated job executions the trigger will receive (and probably consume) messages provided by the message connector. Thus RabbitMQ messages will be retrieved "),e("strong",[t._v("and acknowledged")]),t._v(" during simulated job executions!")]),t._v(" "),e("h2",{attrs:{id:"parameters"}},[e("a",{staticClass:"header-anchor",attrs:{href:"#parameters"}},[t._v("#")]),t._v(" Parameters")]),t._v(" "),e("p",[t._v("The component can be configured by the following parameters:")]),t._v(" "),e("table",[e("thead",[e("tr",[e("th",{staticStyle:{"text-align":"left"}},[t._v("Parameter")]),t._v(" "),e("th",{staticStyle:{"text-align":"left"}},[t._v("Description")])])]),t._v(" "),e("tbody",[e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("Rabbit Mq Connector")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("A RabbitMQ message connector that provides the incoming messages.")])]),t._v(" "),e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("Queue Name")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("The RabbitMQ queue containing the messages.")])])])])])}),[],!1,null,null,null);e.default=i.exports}}]);