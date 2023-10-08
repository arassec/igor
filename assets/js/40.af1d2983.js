(window.webpackJsonp=window.webpackJsonp||[]).push([[40],{362:function(t,e,a){"use strict";a.r(e);var n=a(13),s=Object(n.a)({},(function(){var t=this,e=t._self._c;return e("ContentSlotsDistributor",{attrs:{"slot-key":t.$parent.slotKey}},[e("h1",{attrs:{id:"http-request-action"}},[e("a",{staticClass:"header-anchor",attrs:{href:"#http-request-action"}},[t._v("#")]),t._v(" 'HTTP Request' Action")]),t._v(" "),e("h2",{attrs:{id:"description"}},[e("a",{staticClass:"header-anchor",attrs:{href:"#description"}},[t._v("#")]),t._v(" Description")]),t._v(" "),e("p",[t._v("This action issues HTTP requests against web servers.")]),t._v(" "),e("p",[t._v("The action adds the results of the request to the data item under the 'webResponse' key.")]),t._v(" "),e("p",[t._v("A data item processed by this action could look like this:")]),t._v(" "),e("div",{staticClass:"language- extra-class"},[e("pre",{pre:!0,attrs:{class:"language-text"}},[e("code",[t._v('{\n  "data": {},\n  "meta": {\n    "jobId": "69c52202-4bc6-4753-acfe-b24870a75e90",\n    "simulation": true,\n    "timestamp": 1601302794228\n  },\n  "webResponse": {\n    "headers": {\n      ":status": [\n        "200"\n      ],\n      "access-control-allow-credentials": [\n        "true"\n      ],\n      "access-control-allow-origin": [\n        "*"\n      ],\n      "content-length": [\n        "451"\n      ],\n      "content-type": [\n        "application/json"\n      ],\n      "date": [\n        "Mon, 28 Sep 2020 14:19:52 GMT"\n      ],\n      "server": [\n        "gunicorn/19.9.0"\n      ]\n    },\n    "body": {\n      "args": {},\n      "json": {\n        "jobId": "69c52202-4bc6-4753-acfe-b24870a75e90"\n      },\n      "url": "https://httpbin.org/post"\n    }\n  }\n}\n')])])]),e("h2",{attrs:{id:"parameters"}},[e("a",{staticClass:"header-anchor",attrs:{href:"#parameters"}},[t._v("#")]),t._v(" Parameters")]),t._v(" "),e("p",[t._v("The component can be configured by the following parameters:")]),t._v(" "),e("table",[e("thead",[e("tr",[e("th",{staticStyle:{"text-align":"left"}},[t._v("Parameter")]),t._v(" "),e("th",{staticStyle:{"text-align":"left"}},[t._v("Description")])])]),t._v(" "),e("tbody",[e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("Http Connector")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("An 'HTTP Connector' that is configured to execute the request.")])]),t._v(" "),e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("Url")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("The HTTP(S) URL for the request.")])]),t._v(" "),e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("Method")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("The HTTP method. Can be one of 'GET', 'HEAD', 'POST', 'PUT', 'DELETE', 'CONNECT', 'OPTIONS', 'TRACE' or 'PATCH'.")])]),t._v(" "),e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("Headers")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("The HTTP headers to use for the request. Headers must be entered as 'key: value'-pairs, with each header in a separate line. Mustache templates are "),e("strong",[t._v("not")]),t._v(" supported in headers.")])]),t._v(" "),e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("Body")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("The HTTP body to use for the request.")])]),t._v(" "),e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("Ignore Errors")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("If checked, igor will ignore the HTTP result code and treat every response as HTTP 200 ('OK').")])]),t._v(" "),e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("Simulation Safe")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("If checked, HTTP POST, PUT and DELETE will not be executed by the action during simulated job executions.")])]),t._v(" "),e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("Username")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("A username for HTTP Basic Authentication. If set together with a password, a HTTP Authorization header with the encoded password will be added to every request.")])]),t._v(" "),e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("Password")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("A password for HTTP Basic Authentication.")])]),t._v(" "),e("tr",[e("td",{staticStyle:{"text-align":"left"}},[t._v("Target Key")]),t._v(" "),e("td",{staticStyle:{"text-align":"left"}},[t._v("The target key to put the web response in the data item.")])])])])])}),[],!1,null,null,null);e.default=s.exports}}]);