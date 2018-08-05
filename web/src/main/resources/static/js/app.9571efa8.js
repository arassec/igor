(function(e){function t(t){for(var a,s,o=t[0],c=t[1],l=t[2],v=0,d=[];v<o.length;v++)s=o[v],r[s]&&d.push(r[s][0]),r[s]=0;for(a in c)Object.prototype.hasOwnProperty.call(c,a)&&(e[a]=c[a]);u&&u(t);while(d.length)d.shift()();return n.push.apply(n,l||[]),i()}function i(){for(var e,t=0;t<n.length;t++){for(var i=n[t],a=!0,o=1;o<i.length;o++){var c=i[o];0!==r[c]&&(a=!1)}a&&(n.splice(t--,1),e=s(s.s=i[0]))}return e}var a={},r={1:0},n=[];function s(t){if(a[t])return a[t].exports;var i=a[t]={i:t,l:!1,exports:{}};return e[t].call(i.exports,i,i.exports,s),i.l=!0,i.exports}s.m=e,s.c=a,s.d=function(e,t,i){s.o(e,t)||Object.defineProperty(e,t,{enumerable:!0,get:i})},s.r=function(e){"undefined"!==typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(e,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(e,"__esModule",{value:!0})},s.t=function(e,t){if(1&t&&(e=s(e)),8&t)return e;if(4&t&&"object"===typeof e&&e&&e.__esModule)return e;var i=Object.create(null);if(s.r(i),Object.defineProperty(i,"default",{enumerable:!0,value:e}),2&t&&"string"!=typeof e)for(var a in e)s.d(i,a,function(t){return e[t]}.bind(null,a));return i},s.n=function(e){var t=e&&e.__esModule?function(){return e["default"]}:function(){return e};return s.d(t,"a",t),t},s.o=function(e,t){return Object.prototype.hasOwnProperty.call(e,t)},s.p="/";var o=window["webpackJsonp"]=window["webpackJsonp"]||[],c=o.push.bind(o);o.push=t,o=o.slice();for(var l=0;l<o.length;l++)t(o[l]);var u=c;n.push([8,0]),i()})({"/nnb":function(e,t,i){},"14z5":function(e,t,i){},8:function(e,t,i){e.exports=i("Vtdi")},AqL9:function(e,t,i){},EDI0:function(e,t,i){},LFrd:function(e,t,i){"use strict";var a=i("lTdO"),r=i.n(a);r.a},Nem0:function(e,t,i){},O75A:function(e,t,i){"use strict";var a=i("AqL9"),r=i.n(a);r.a},Vtdi:function(e,t,i){"use strict";i.r(t);i("VRzm");var a=i("Kw5r"),r=function(){var e=this,t=e.$createElement,i=e._self._c||t;return i("div",{attrs:{id:"app"}},[i("Navigation")],1)},n=[],s=function(){var e=this,t=e.$createElement,i=e._self._c||t;return i("div",[i("div",{staticClass:"navigation"},[i("ul",[i("li",{class:[e.isActiveTab("services")?e.activeClass:"",e.tabClass]},[i("a",{on:{click:function(t){e.setActiveTab("services")}}},[e._v("Services")])]),i("li",{class:[e.isActiveTab("jobs")?e.activeClass:"",e.tabClass]},[i("a",{on:{click:function(t){e.setActiveTab("jobs")}}},[e._v("Jobs")])])])]),i("div",{staticClass:"center"},[e.isActiveTab("services")?i("ServiceList"):e._e()],1),i("div",{staticClass:"center"},[e.isActiveTab("jobs")?i("JobList"):e._e()],1)])},o=[],c=function(){var e=this,t=e.$createElement,i=e._self._c||t;return i("div",{staticClass:"panel"},[i("div",{staticClass:"header-container overflow"},[i("h1",[e._v("Available Services")]),i("button",{staticClass:"button right",on:{click:function(t){e.showNewService=!e.showNewService}}},[0==e.showNewService?i("font-awesome-icon",{attrs:{icon:"plus"}}):e._e(),1==e.showNewService?i("font-awesome-icon",{attrs:{icon:"minus"}}):e._e()],1)]),e.showNewService?i("NewService",{on:{save:e.loadExistingServices}}):e._e(),e._l(e.services,function(t,a){return i("ServiceListEntry",{key:t,attrs:{id:t,index:a},on:{"service-deleted":function(t){e.loadExistingServices()}}})})],2)},l=[],u=(i("Vd3H"),i("HEwt"),i("rGqo"),function(){var e=this,t=e.$createElement,i=e._self._c||t;return i("div",{staticClass:"input"},[e._m(0),i("table",[i("tr",[e._m(1),i("td",[i("input",{directives:[{name:"model",rawName:"v-model",value:e.serviceConfiguration.id,expression:"serviceConfiguration.id"}],attrs:{id:"id-input",type:"text",autocomplete:"off"},domProps:{value:e.serviceConfiguration.id},on:{input:function(t){t.target.composing||e.$set(e.serviceConfiguration,"id",t.target.value)}}})]),i("td",[!e.validationOk&&e.idValidationError.length>0?i("div",{staticClass:"validationError"},[e._v(e._s(e.idValidationError))]):e._e()])]),i("tr",[e._m(2),i("td",[i("select",{directives:[{name:"model",rawName:"v-model",value:e.selectedCategory,expression:"selectedCategory"}],attrs:{id:"category-input"},on:{change:[function(t){var i=Array.prototype.filter.call(t.target.options,function(e){return e.selected}).map(function(e){var t="_value"in e?e._value:e.value;return t});e.selectedCategory=t.target.multiple?i:i[0]},function(t){e.loadServiceTypes(e.selectedCategory)}]}},e._l(e.serviceCategories,function(t){return i("option",{key:t.type,domProps:{value:t.type}},[e._v("\n                        "+e._s(t.label)+"\n                    ")])}))]),i("td")]),i("tr",[e._m(3),i("td",[i("select",{directives:[{name:"model",rawName:"v-model",value:e.selectedType,expression:"selectedType"}],attrs:{id:"type-input"},on:{change:[function(t){var i=Array.prototype.filter.call(t.target.options,function(e){return e.selected}).map(function(e){var t="_value"in e?e._value:e.value;return t});e.selectedType=t.target.multiple?i:i[0]},function(t){e.loadTypeParameters(e.selectedType)}]}},e._l(e.serviceTypes,function(t){return i("option",{key:t.type,domProps:{value:t.type}},[e._v("\n                        "+e._s(t.label)+"\n                    ")])}))]),i("td")]),e._l(e.parameters,function(t,a){return[i("tr",{key:t.name},[i("td",[t.optional?i("label",[e._v(e._s(e.formatString(t.name)))]):e._e(),t.optional?e._e():i("label",[e._v(e._s(e.formatString(t.name))+"*")])]),"int"==t.type?i("td",[i("input",{directives:[{name:"model",rawName:"v-model.number",value:e.parameterValues[a],expression:"parameterValues[index]",modifiers:{number:!0}}],attrs:{type:t.secured?"password":"text",autocomplete:"off"},domProps:{value:e.parameterValues[a]},on:{input:function(t){t.target.composing||e.$set(e.parameterValues,a,e._n(t.target.value))},blur:function(t){e.$forceUpdate()}}})]):e._e(),"int"!=t.type?i("td",[i("input",{directives:[{name:"model",rawName:"v-model.trim",value:e.parameterValues[a],expression:"parameterValues[index]",modifiers:{trim:!0}}],attrs:{type:t.secured?"password":"text",autocomplete:"off"},domProps:{value:e.parameterValues[a]},on:{input:function(t){t.target.composing||e.$set(e.parameterValues,a,t.target.value.trim())},blur:function(t){e.$forceUpdate()}}})]):e._e(),i("td",[!e.validationOk&&e.parameterValidationErrors[a].length>0?i("div",{staticClass:"validationError"},[e._v("\n                        "+e._s(e.parameterValidationErrors[a])+"\n                    ")]):e._e()])])]})],2),e.testInProgress?i("div",{staticClass:"feedbackbox"},[i("font-awesome-icon",{staticClass:"fa-spin",attrs:{icon:"spinner"}}),i("label",{staticClass:"margin-left"},[e._v("Testing...")])],1):e._e(),e.testResult.length>0?i("div",{class:e.testOk?"feedbackbox":"alertbox"},[e._v("\n        "+e._s(e.testResult)+"\n    ")]):e._e(),!e.saveOk&&e.saveResult.length>0?i("div",{staticClass:"alertbox"},[e._v("\n        "+e._s(e.saveResult)+"\n    ")]):e._e(),i("button",{staticClass:"button right margin-left",on:{click:function(t){e.saveConfiguration()}}},[i("font-awesome-icon",{attrs:{icon:"save"}})],1),i("button",{staticClass:"button right",on:{click:function(t){e.testConfiguration()}}},[i("font-awesome-icon",{attrs:{icon:"plug"}})],1)])}),v=[function(){var e=this,t=e.$createElement,i=e._self._c||t;return i("div",{staticClass:"overflow"},[i("h2",{staticClass:"left"},[e._v("New Service")])])},function(){var e=this,t=e.$createElement,i=e._self._c||t;return i("td",[i("label",{attrs:{for:"id-input"}},[e._v("Name")])])},function(){var e=this,t=e.$createElement,i=e._self._c||t;return i("td",[i("label",{attrs:{for:"category-input"}},[e._v("Category")])])},function(){var e=this,t=e.$createElement,i=e._self._c||t;return i("td",[i("label",{attrs:{for:"type-input"}},[e._v("Type")])])}],d=(i("pIFo"),i("f3/d"),{name:"NewService",data:function(){return{testResult:"",testInProgress:!1,testOk:!0,saveResult:"",saveOk:!0,serviceCategories:[],selectedCategory:"",serviceTypes:[],selectedType:"",parameters:[],parameterValues:[],idValidationError:"",parameterValidationErrors:[],validationOk:!0,serviceConfiguration:{id:"",type:"",parameters:{}}}},methods:{loadServiceCategories:function(){var e=this;this.$http.get("/api/service/category").then(function(t){for(var i=e.serviceCategories.length;i>0;i--)e.serviceCategories.pop();Array.from(t.data).forEach(function(t,i){e.serviceCategories.push(t)}),e.selectedCategory=e.serviceCategories[0].type,e.loadServiceTypes(e.selectedCategory)}).catch(function(e){console.log(e)})},loadServiceTypes:function(e){var t=this;this.$http.get("/api/service/category/"+e).then(function(e){for(var i=t.serviceTypes.length;i>0;i--)t.serviceTypes.pop();Array.from(e.data).forEach(function(e,i){t.serviceTypes.push(e)}),t.selectedType=t.serviceTypes[0].type,t.loadTypeParameters(t.selectedType)}).catch(function(e){console.log(e)})},loadTypeParameters:function(e){var t=this;this.$http.get("/api/service/type/"+e).then(function(e){for(var i=t.parameterValidationErrors.length;i>0;i--)t.parameterValidationErrors.pop();for(var a=t.parameters.length;a>0;a--)t.parameters.pop();Array.from(e.data).forEach(function(e,i){t.parameters.push(e),t.parameterValidationErrors.push("")})}).catch(function(e){console.log(e)})},testConfiguration:function(){if(this.validateInputs(),this.validationOk){var e=this;e.testInProgress=!0,e.serviceConfiguration.type=e.selectedType,this.parameters.forEach(function(t,i){e.serviceConfiguration.parameters[t.name]=e.parameterValues[i]}),this.$http.post("/api/service/test",this.serviceConfiguration).then(function(t){e.testResult=t.data,e.testOk=!0,e.testInProgress=!1}).catch(function(t){e.testResult=t.response.data,e.testOk=!1,e.testInProgress=!1})}},saveConfiguration:function(){if(this.validateInputs(),console.log(this.validationOk),this.validationOk){var e=this;e.serviceConfiguration.type=e.selectedType,this.parameters.forEach(function(t,i){e.serviceConfiguration.parameters[t.name]=e.parameterValues[i]}),this.$http.post("/api/service",this.serviceConfiguration).then(function(t){e.$emit("save")}).catch(function(t){e.saveResult=t.response.data,e.saveOk=!1})}},formatString:function(e){return e=e.replace(/\.?([A-Z])/g,function(e,t){return" "+t.toLowerCase()}).replace(/^_/,""),e.charAt(0).toUpperCase()+e.slice(1)},validateInputs:function(){this.testResult="",this.saveResult="",this.validationOk=!0,this.idValidationError="";for(var e=this.parameterValidationErrors.length;e>0;e--)this.parameterValidationErrors[e]="";null!=this.serviceConfiguration.id&&""!==this.serviceConfiguration.id||(this.idValidationError="ID must be set",this.validationOk=!1),(this.serviceConfiguration.id.indexOf(":")>-1||this.serviceConfiguration.id.indexOf("/")>-1)&&(this.idValidationError="Invalid ID",this.validationOk=!1);var t=this;this.parameters.forEach(function(e,i){e.optional||null!=t.parameterValues[i]&&""!==t.parameterValues[i]||(t.parameterValidationErrors[i]="Value required",t.validationOk=!1)})}},mounted:function(){this.loadServiceCategories()}}),p=d,f=(i("i1lt"),i("KHd+")),m=Object(f["a"])(p,u,v,!1,null,"0314da6b",null),h=m.exports,g=function(){var e=this,t=e.$createElement,i=e._self._c||t;return i("div",{staticClass:"service-entry"},[i("div",{staticClass:"service-entry-header"},[i("div",{staticClass:"left service-entry-id"},[e._v("\n            "+e._s(e.id)+"\n        ")]),i("div",[i("button",{staticClass:"service-entry-button button right margin-left",on:{click:function(t){e.showDelete=!e.showDelete}}},[i("font-awesome-icon",{attrs:{icon:"trash-alt"}})],1),i("button",{staticClass:"service-entry-button button right",on:{click:function(t){e.editService(e.id)}}},[i("font-awesome-icon",{attrs:{icon:"cog"}})],1)])]),e.showDelete?i("ModalDialog",{on:{cancel:function(t){e.showDelete=!1},ok:function(t){e.deleteService(e.id)}}},[i("p",{attrs:{slot:"header"},slot:"header"},[e._v("Delete Service?")]),i("p",{attrs:{slot:"body"},slot:"body"},[e._v("Do you really want to delete service '"+e._s(e.id)+"'?")])]):e._e(),e.showEditService?i("div",{staticClass:"service-entry-config"},[i("table",[i("tr",[e._m(0),i("td",[e._v(e._s(e.serviceDescription.serviceCategory.label))]),i("td")]),i("tr",[e._m(1),i("td",[e._v(e._s(e.serviceDescription.serviceType.label))]),i("td")]),e._l(e.parameters,function(t,a){return[i("tr",{key:t.name},[i("td",[t.optional?i("label",[e._v(e._s(e.formatString(t.name)))]):e._e(),t.optional?e._e():i("label",[e._v(e._s(e.formatString(t.name))+"*")])]),"int"==t.type?i("td",[i("input",{directives:[{name:"model",rawName:"v-model.number",value:e.parameterValues[a],expression:"parameterValues[index]",modifiers:{number:!0}}],attrs:{type:t.secured?"password":"text",autocomplete:"off"},domProps:{value:e.parameterValues[a]},on:{input:function(t){t.target.composing||e.$set(e.parameterValues,a,e._n(t.target.value))},blur:function(t){e.$forceUpdate()}}})]):e._e(),"int"!=t.type?i("td",[i("input",{directives:[{name:"model",rawName:"v-model.trim",value:e.parameterValues[a],expression:"parameterValues[index]",modifiers:{trim:!0}}],attrs:{type:t.secured?"password":"text",autocomplete:"off"},domProps:{value:e.parameterValues[a]},on:{input:function(t){t.target.composing||e.$set(e.parameterValues,a,t.target.value.trim())},blur:function(t){e.$forceUpdate()}}})]):e._e(),i("td",[!e.validationOk&&e.parameterValidationErrors[a].length>0?i("div",{staticClass:"validationError"},[e._v("\n                            "+e._s(e.parameterValidationErrors[a])+"\n                        ")]):e._e()])])]})],2),e.testInProgress?i("div",{staticClass:"feedbackbox"},[i("font-awesome-icon",{staticClass:"fa-spin",attrs:{icon:"spinner"}}),i("label",{staticClass:"margin-left"},[e._v("Testing...")])],1):e._e(),e.testResult.length>0?i("div",{class:e.testOk?"feedbackbox":"alertbox"},[e._v("\n            "+e._s(e.testResult)+"\n        ")]):e._e(),e.saveResult.length>0?i("div",{class:e.saveOk?"feedbackbox":"alertbox"},[e._v("\n            "+e._s(e.saveResult)+"\n        ")]):e._e(),i("button",{staticClass:"button right margin-left",on:{click:function(t){e.saveConfiguration()}}},[i("font-awesome-icon",{attrs:{icon:"save"}})],1),i("button",{staticClass:"button right",on:{click:function(t){e.testConfiguration()}}},[i("font-awesome-icon",{attrs:{icon:"plug"}})],1)]):e._e()],1)},b=[function(){var e=this,t=e.$createElement,i=e._self._c||t;return i("td",[i("label",[e._v("Category")])])},function(){var e=this,t=e.$createElement,i=e._self._c||t;return i("td",[i("label",[e._v("Type")])])}],_=function(){var e=this,t=e.$createElement,i=e._self._c||t;return i("transition",{attrs:{name:"modal"}},[i("div",{staticClass:"modal-mask"},[i("div",{staticClass:"modal-wrapper"},[i("div",{staticClass:"modal-container center"},[i("div",{staticClass:"modal-header"},[i("h1",[e._t("header",[e._v("\n                            default header\n                        ")])],2)]),i("div",{staticClass:"modal-body"},[e._t("body",[e._v("\n                        default body\n                    ")])],2),i("div",{staticClass:"modal-footer"},[e._t("footer",[i("button",{staticClass:"button left",on:{click:function(t){e.$emit("cancel")}}},[i("font-awesome-icon",{attrs:{icon:"times"}})],1),i("button",{staticClass:"button right",on:{click:function(t){e.$emit("ok")}}},[i("font-awesome-icon",{attrs:{icon:"check"}})],1)])],2)])])])])},y=[],C={name:"ModalDialog"},w=C,E=(i("hwqO"),Object(f["a"])(w,_,y,!1,null,"f9ef0e08",null)),k=E.exports,V={name:"ServiceListEntry",components:{ModalDialog:k},props:["id","type"],data:function(){return{showDelete:!1,showEditService:!1,testResult:"",testInProgress:!1,testOk:!0,saveResult:"",saveOk:!0,parameters:[],parameterValues:[],parameterValidationErrors:[],validationOk:!0,serviceDescription:{},serviceConfiguration:{id:"",type:"",parameters:{}}}},methods:{editService:function(e){this.testResult="",this.saveResult="";var t=this;this.$http.get("/api/service/"+encodeURIComponent(e)).then(function(e){t.serviceDescription=e.data,t.loadTypeParameters(e.data.serviceType.type),t.showEditService=!t.showEditService}).catch(function(e){console.log(e)})},deleteService:function(e){this.showDelete=!1;var t=this;this.$http.delete("/api/service/"+encodeURIComponent(e)).then(function(e){t.$emit("service-deleted")}).catch(function(e){console.log(e)})},loadTypeParameters:function(e){var t=this;this.$http.get("/api/service/type/"+e).then(function(e){for(var i=t.parameters.length;i>0;i--)t.parameters.pop();Array.from(e.data).forEach(function(e,i){t.parameters.push(e)}),t.fillTypeParameters()}).catch(function(e){console.log(e)})},fillTypeParameters:function(){var e=this;this.parameters.forEach(function(t,i){null!=e.serviceDescription.parameters[t.name]&&(e.parameterValues[i]=e.serviceDescription.parameters[t.name])})},testConfiguration:function(){if(this.validateInputs(),this.validationOk){var e=this;e.testInProgress=!0,e.serviceConfiguration.id=e.serviceDescription.id,e.serviceConfiguration.type=e.serviceDescription.serviceType.type,this.parameters.forEach(function(t,i){e.serviceConfiguration.parameters[t.name]=e.parameterValues[i]}),this.$http.post("/api/service/test",this.serviceConfiguration).then(function(t){e.testResult=t.data,e.testOk=!0,e.testInProgress=!1}).catch(function(t){e.testResult=t.response.data,e.testOk=!1,e.testInProgress=!1})}},saveConfiguration:function(){if(this.validateInputs(),this.validationOk){var e=this;e.serviceConfiguration.id=e.serviceDescription.id,e.serviceConfiguration.type=e.serviceDescription.serviceType.type,this.parameters.forEach(function(t,i){e.serviceConfiguration.parameters[t.name]=e.parameterValues[i]}),this.$http.post("/api/service",this.serviceConfiguration).then(function(t){e.saveResult="Saved...",e.saveOk=!0,e.showEditService=!e.showEditService}).catch(function(t){e.saveResult=t.response.data,e.saveOk=!1})}},formatString:function(e){return e=e.replace(/\.?([A-Z])/g,function(e,t){return" "+t.toLowerCase()}).replace(/^_/,""),e.charAt(0).toUpperCase()+e.slice(1)},validateInputs:function(){this.testResult="",this.saveResult="",this.validationOk=!0;for(var e=this.parameterValidationErrors.length;e>0;e--)this.parameterValidationErrors[e]="";var t=this;this.parameters.forEach(function(e,i){e.optional||null!=t.parameterValues[i]&&""!==t.parameterValues[i]||(t.parameterValidationErrors[i]="Value required",t.validationOk=!1)})}}},O=V,S=(i("LFrd"),Object(f["a"])(O,g,b,!1,null,"da928eba",null)),x=S.exports,T={name:"ServiceList",components:{ServiceListEntry:x,NewService:h},data:function(){return{services:[],showNewService:!1}},methods:{loadExistingServices:function(){var e=this;this.$http.get("/api/service/ids").then(function(t){for(var i=e.services.length;i>0;i--)e.services.pop();Array.from(t.data).forEach(function(t,i){e.services.push(t)}),e.services.sort(function(e,t){return e.localeCompare(t)}),e.showNewService=!1}).catch(function(e){console.log(e)})}},mounted:function(){this.loadExistingServices()}},$=T,j=(i("O75A"),Object(f["a"])($,c,l,!1,null,"12ec5469",null)),N=j.exports,R=function(){var e=this,t=e.$createElement,i=e._self._c||t;return i("div",{staticClass:"panel"},[i("div",{staticClass:"header-container overflow"},[i("h1",[e._v("Available Jobs")]),i("button",{staticClass:"button right",on:{click:function(t){e.showNewJob=!e.showNewJob}}},[0==e.showNewJob?i("font-awesome-icon",{attrs:{icon:"plus"}}):e._e(),1==e.showNewJob?i("font-awesome-icon",{attrs:{icon:"minus"}}):e._e()],1)]),e._l(e.services,function(t,a){return i("JobListEntry",{key:t,attrs:{id:t,index:a},on:{"job-deleted":function(t){e.loadJobs()}}})})],2)},P=[],I=function(){var e=this,t=e.$createElement,i=e._self._c||t;return i("div",{staticClass:"job-entry"},[i("div",{staticClass:"job-entry-header"},[i("div",{staticClass:"left job-entry-id"},[e._v("\n            "+e._s(e.id)+"\n        ")]),i("div",[i("button",{staticClass:"job-entry-button button right margin-left",on:{click:function(t){e.deleteJob(e.id)}}},[i("font-awesome-icon",{attrs:{icon:"trash-alt"}})],1),i("button",{staticClass:"job-entry-button button right",on:{click:function(t){e.editJob(e.id)}}},[i("font-awesome-icon",{attrs:{icon:"cog"}})],1)])])])},A=[],D={name:"JobListEntry",props:["id"],data:function(){return{}},editService:function(e){},deleteService:function(e){var t=this;this.$http.delete("/api/job/"+encodeURIComponent(e)).then(function(e){t.$emit("job-deleted")}).catch(function(e){console.log(e)})}},L=D,J=(i("qIVe"),Object(f["a"])(L,I,A,!1,null,"e5ac0168",null)),q=J.exports,U={name:"JobList",components:{JobListEntry:q},data:function(){return{jobs:[],showNewJob:!1}},methods:{loadJobs:function(){var e=this;this.$http.get("/api/job/ids").then(function(t){for(var i=e.services.length;i>0;i--)e.jobs.pop();Array.from(t.data).forEach(function(t,i){e.jobs.push(t)}),e.jobs.sort(function(e,t){return e.localeCompare(t)}),e.showNewJob=!1}).catch(function(e){console.log(e)})}},mounted:function(){this.loadJobs()}},M=U,H=(i("lLU4"),Object(f["a"])(M,R,P,!1,null,"3b3355e2",null)),W=H.exports,F={name:"Navigation",components:{JobList:W,ServiceList:N},data:function(){return{choice:"services",activeClass:"active",tabClass:"tab"}},methods:{setActiveTab:function(e){this.choice=e},isActiveTab:function(e){return this.choice===e}}},Y=F,Z=(i("YrrW"),Object(f["a"])(Y,s,o,!1,null,"330f7de5",null)),z=Z.exports,G={name:"app",components:{Navigation:z}},K=G,X=(i("ZL7j"),Object(f["a"])(K,r,n,!1,null,null,null)),B=X.exports,Q=i("7O5W"),ee=i("elWB"),te=i("wHSu");Q["library"].add(te["a"]),a["a"].component("font-awesome-icon",ee["FontAwesomeIcon"]),a["a"].config.productionTip=!0,window.axios=i("vDqi"),window.axios.defaults.headers.common["X-Requested-With"]="XMLHttpRequest",a["a"].prototype.$http=window.axios,new a["a"]({render:function(e){return e(B)}}).$mount("#app")},YGL9:function(e,t,i){},YrrW:function(e,t,i){"use strict";var a=i("/nnb"),r=i.n(a);r.a},ZL7j:function(e,t,i){"use strict";var a=i("EDI0"),r=i.n(a);r.a},hTNV:function(e,t,i){},hwqO:function(e,t,i){"use strict";var a=i("14z5"),r=i.n(a);r.a},i1lt:function(e,t,i){"use strict";var a=i("hTNV"),r=i.n(a);r.a},lLU4:function(e,t,i){"use strict";var a=i("YGL9"),r=i.n(a);r.a},lTdO:function(e,t,i){},qIVe:function(e,t,i){"use strict";var a=i("Nem0"),r=i.n(a);r.a}});
//# sourceMappingURL=app.9571efa8.js.map