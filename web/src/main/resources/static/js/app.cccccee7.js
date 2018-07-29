(function(e){function t(t){for(var s,n,o=t[0],c=t[1],u=t[2],p=0,v=[];p<o.length;p++)n=o[p],a[n]&&v.push(a[n][0]),a[n]=0;for(s in c)Object.prototype.hasOwnProperty.call(c,s)&&(e[s]=c[s]);l&&l(t);while(v.length)v.shift()();return i.push.apply(i,u||[]),r()}function r(){for(var e,t=0;t<i.length;t++){for(var r=i[t],s=!0,o=1;o<r.length;o++){var c=r[o];0!==a[c]&&(s=!1)}s&&(i.splice(t--,1),e=n(n.s=r[0]))}return e}var s={},a={1:0},i=[];function n(t){if(s[t])return s[t].exports;var r=s[t]={i:t,l:!1,exports:{}};return e[t].call(r.exports,r,r.exports,n),r.l=!0,r.exports}n.m=e,n.c=s,n.d=function(e,t,r){n.o(e,t)||Object.defineProperty(e,t,{enumerable:!0,get:r})},n.r=function(e){"undefined"!==typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(e,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(e,"__esModule",{value:!0})},n.t=function(e,t){if(1&t&&(e=n(e)),8&t)return e;if(4&t&&"object"===typeof e&&e&&e.__esModule)return e;var r=Object.create(null);if(n.r(r),Object.defineProperty(r,"default",{enumerable:!0,value:e}),2&t&&"string"!=typeof e)for(var s in e)n.d(r,s,function(t){return e[t]}.bind(null,s));return r},n.n=function(e){var t=e&&e.__esModule?function(){return e["default"]}:function(){return e};return n.d(t,"a",t),t},n.o=function(e,t){return Object.prototype.hasOwnProperty.call(e,t)},n.p="/";var o=window["webpackJsonp"]=window["webpackJsonp"]||[],c=o.push.bind(o);o.push=t,o=o.slice();for(var u=0;u<o.length;u++)t(o[u]);var l=c;i.push([5,0]),r()})({5:function(e,t,r){e.exports=r("Vtdi")},"6F+2":function(e,t,r){},AkSE:function(e,t,r){},EDI0:function(e,t,r){},FiPi:function(e,t,r){"use strict";var s=r("M6GP"),a=r.n(s);a.a},M6GP:function(e,t,r){},Vtdi:function(e,t,r){"use strict";r.r(t);r("VRzm");var s=r("Kw5r"),a=function(){var e=this,t=e.$createElement,r=e._self._c||t;return r("div",{attrs:{id:"app"}},[r("Navigation")],1)},i=[],n=function(){var e=this,t=e.$createElement,r=e._self._c||t;return r("div",[r("div",{staticClass:"navigation"},[r("ul",[r("li",{class:[e.isActiveTab("services")?e.activeClass:"",e.tabClass]},[r("a",{on:{click:function(t){e.setActiveTab("services")}}},[e._v("Services")])]),r("li",{class:[e.isActiveTab("jobs")?e.activeClass:"",e.tabClass]},[r("a",{on:{click:function(t){e.setActiveTab("jobs")}}},[e._v("Jobs")])])])]),r("div",{staticClass:"center"},[e.isActiveTab("services")?r("ServiceList"):e._e()],1)])},o=[],c=function(){var e=this,t=e.$createElement,r=e._self._c||t;return r("div",{staticClass:"panel"},[r("div",{staticClass:"header-container overflow"},[r("h1",[e._v("Available Services")]),r("button",{staticClass:"button right",on:{click:function(t){e.showNewService=!e.showNewService}}},[0==e.showNewService?r("font-awesome-icon",{attrs:{icon:"plus"}}):e._e(),1==e.showNewService?r("font-awesome-icon",{attrs:{icon:"minus"}}):e._e()],1)]),r("transition",{attrs:{name:"fade"}},[e.showNewService?r("NewService",{on:{save:e.loadExistingServices}}):e._e()],1),e._l(e.services,function(e,t){return r("ServiceListEntry",{key:e,attrs:{id:e,index:t}})})],2)},u=[],l=(r("Vd3H"),r("HEwt"),r("rGqo"),function(){var e=this,t=e.$createElement,r=e._self._c||t;return r("div",{staticClass:"input"},[e._m(0),r("table",[r("tr",[e._m(1),r("td",[r("input",{directives:[{name:"model",rawName:"v-model",value:e.serviceConfiguration.id,expression:"serviceConfiguration.id"}],attrs:{id:"id-input",type:"text",autocomplete:"off"},domProps:{value:e.serviceConfiguration.id},on:{input:function(t){t.target.composing||e.$set(e.serviceConfiguration,"id",t.target.value)}}})])]),r("tr",[e._m(2),r("td",[r("select",{directives:[{name:"model",rawName:"v-model",value:e.selectedCategory,expression:"selectedCategory"}],attrs:{id:"category-input"},on:{change:[function(t){var r=Array.prototype.filter.call(t.target.options,function(e){return e.selected}).map(function(e){var t="_value"in e?e._value:e.value;return t});e.selectedCategory=t.target.multiple?r:r[0]},function(t){e.loadServiceTypes(e.selectedCategory)}]}},e._l(e.serviceCategories,function(t){return r("option",{key:t.type,domProps:{value:t.type}},[e._v("\n                        "+e._s(t.label)+"\n                    ")])}))])]),r("tr",[e._m(3),r("td",[r("select",{directives:[{name:"model",rawName:"v-model",value:e.selectedType,expression:"selectedType"}],attrs:{id:"type-input"},on:{change:[function(t){var r=Array.prototype.filter.call(t.target.options,function(e){return e.selected}).map(function(e){var t="_value"in e?e._value:e.value;return t});e.selectedType=t.target.multiple?r:r[0]},function(t){e.loadTypeParameters(e.selectedType)}]}},e._l(e.serviceTypes,function(t){return r("option",{key:t.type,domProps:{value:t.type}},[e._v("\n                        "+e._s(t.label)+"\n                    ")])}))])]),e._l(e.parameters,function(t,s){return[r("tr",{key:t.name},[r("td",[t.optional?r("label",[e._v(e._s(e.formatString(t.name)))]):e._e(),t.optional?e._e():r("label",[e._v(e._s(e.formatString(t.name))+"*")])]),"int"==t.type?r("td",[r("input",{directives:[{name:"model",rawName:"v-model.number",value:e.parameterValues[s],expression:"parameterValues[index]",modifiers:{number:!0}}],attrs:{type:t.secured?"password":"text",autocomplete:"off"},domProps:{value:e.parameterValues[s]},on:{input:function(t){t.target.composing||e.$set(e.parameterValues,s,e._n(t.target.value))},blur:function(t){e.$forceUpdate()}}})]):e._e(),"int"!=t.type?r("td",[r("input",{directives:[{name:"model",rawName:"v-model.trim",value:e.parameterValues[s],expression:"parameterValues[index]",modifiers:{trim:!0}}],attrs:{type:t.secured?"password":"text",autocomplete:"off"},domProps:{value:e.parameterValues[s]},on:{input:function(t){t.target.composing||e.$set(e.parameterValues,s,t.target.value.trim())},blur:function(t){e.$forceUpdate()}}})]):e._e()])]})],2),e.showTestResult&&e.testOk?r("div",{staticClass:"feedbackbox"},[e._v("\n        "+e._s(e.testResult)+"\n    ")]):e._e(),e.showTestResult&&!e.testOk?r("div",{staticClass:"alertbox"},[e._v("\n        "+e._s(e.testResult)+"\n    ")]):e._e(),e.showSaveResult&&!e.saveOk?r("div",{staticClass:"alertbox"},[e._v("\n        "+e._s(e.saveResult)+"\n    ")]):e._e(),r("button",{staticClass:"button right margin-left",on:{click:function(t){e.saveConfiguration()}}},[r("font-awesome-icon",{attrs:{icon:"save"}})],1),r("button",{staticClass:"button right",on:{click:function(t){e.testConfiguration()}}},[r("font-awesome-icon",{attrs:{icon:"plug"}})],1)])}),p=[function(){var e=this,t=e.$createElement,r=e._self._c||t;return r("div",{staticClass:"overflow"},[r("h2",{staticClass:"left"},[e._v("New Service")])])},function(){var e=this,t=e.$createElement,r=e._self._c||t;return r("td",[r("label",{attrs:{for:"id-input"}},[e._v("Name")])])},function(){var e=this,t=e.$createElement,r=e._self._c||t;return r("td",[r("label",{attrs:{for:"category-input"}},[e._v("Category")])])},function(){var e=this,t=e.$createElement,r=e._self._c||t;return r("td",[r("label",{attrs:{for:"type-input"}},[e._v("Type")])])}],v=(r("pIFo"),r("f3/d"),{name:"NewService",data:function(){return{showTestResult:!1,testResult:"",testOk:!0,showSaveResult:!1,saveResult:"",saveOk:!0,serviceCategories:[],selectedCategory:"",serviceTypes:[],selectedType:"",parameters:[],parameterValues:[],serviceConfiguration:{id:"",type:"",parameters:{}}}},methods:{loadServiceCategories:function(){var e=this;this.$http.get(this.$igorUrl+"/api/service/category").then(function(t){for(var r=e.serviceCategories.length;r>0;r--)e.serviceCategories.pop();Array.from(t.data).forEach(function(t,r){e.serviceCategories.push(t)}),e.selectedCategory=e.serviceCategories[0].type,e.loadServiceTypes(e.selectedCategory)}).catch(function(e){console.log(e)})},loadServiceTypes:function(e){var t=this;this.$http.get(this.$igorUrl+"/api/service/category/"+e).then(function(e){for(var r=t.serviceTypes.length;r>0;r--)t.serviceTypes.pop();Array.from(e.data).forEach(function(e,r){t.serviceTypes.push(e)}),t.selectedType=t.serviceTypes[0].type,t.loadTypeParameters(t.selectedType)}).catch(function(e){console.log(e)})},loadTypeParameters:function(e){var t=this;this.$http.get(this.$igorUrl+"/api/service/type/"+e).then(function(e){for(var r=t.parameters.length;r>0;r--)t.parameters.pop();Array.from(e.data).forEach(function(e,r){t.parameters.push(e)})}).catch(function(e){console.log(e)})},testConfiguration:function(){var e=this;e.serviceConfiguration.type=e.selectedType,this.parameters.forEach(function(t,r){e.serviceConfiguration.parameters[t.name]=e.parameterValues[r]}),this.$http.post(this.$igorUrl+"/api/service/test",this.serviceConfiguration).then(function(t){e.testResult=t.data,e.showTestResult=!0,e.testOk=!0}).catch(function(t){e.testResult=t.response.data,e.showTestResult=!0,e.testOk=!1})},saveConfiguration:function(){var e=this;e.serviceConfiguration.type=e.selectedType,this.parameters.forEach(function(t,r){e.serviceConfiguration.parameters[t.name]=e.parameterValues[r]}),this.$http.post(this.$igorUrl+"/api/service",this.serviceConfiguration).then(function(t){e.$emit("save")}).catch(function(t){e.saveResult=t.response.data,e.showSaveResult=!0,e.saveOk=!1})},formatString:function(e){return e=e.replace(/\.?([A-Z])/g,function(e,t){return" "+t.toLowerCase()}).replace(/^_/,""),e.charAt(0).toUpperCase()+e.slice(1)}},mounted:function(){this.loadServiceCategories()}}),f=v,d=(r("XEz+"),r("KHd+")),m=Object(d["a"])(f,l,p,!1,null,"4fea00b0",null),h=m.exports,g=function(){var e=this,t=e.$createElement,r=e._self._c||t;return r("div",{staticClass:"service-entry"},[r("div",{staticClass:"service-entry-header"},[r("div",{staticClass:"left service-entry-id"},[e._v("\n            "+e._s(e.id)+"\n        ")]),r("div",[r("button",{staticClass:"service-entry-button button right margin-left",on:{click:function(t){e.deleteService(e.id)}}},[r("font-awesome-icon",{attrs:{icon:"trash-alt"}})],1),r("button",{staticClass:"service-entry-button button right",on:{click:function(t){e.editService(e.id)}}},[r("font-awesome-icon",{attrs:{icon:"cog"}})],1)])]),r("transition",{attrs:{name:"fade"}},[e.showEditService?r("div",{staticClass:"service-entry-config"},[r("table",[r("tr",[r("td",[r("label",[e._v("Type")])]),r("td",[e._v(e._s(e.selectedType))])]),e._l(e.parameters,function(t,s){return[r("tr",{key:t.name},[r("td",[t.optional?r("label",[e._v(e._s(e.formatString(t.name)))]):e._e(),t.optional?e._e():r("label",[e._v(e._s(e.formatString(t.name))+"*")])]),"int"==t.type?r("td",[r("input",{directives:[{name:"model",rawName:"v-model.number",value:e.parameterValues[s],expression:"parameterValues[index]",modifiers:{number:!0}}],attrs:{type:t.secured?"password":"text",autocomplete:"off"},domProps:{value:e.parameterValues[s]},on:{input:function(t){t.target.composing||e.$set(e.parameterValues,s,e._n(t.target.value))},blur:function(t){e.$forceUpdate()}}})]):e._e(),"int"!=t.type?r("td",[r("input",{directives:[{name:"model",rawName:"v-model.trim",value:e.parameterValues[s],expression:"parameterValues[index]",modifiers:{trim:!0}}],attrs:{type:t.secured?"password":"text",autocomplete:"off"},domProps:{value:e.parameterValues[s]},on:{input:function(t){t.target.composing||e.$set(e.parameterValues,s,t.target.value.trim())},blur:function(t){e.$forceUpdate()}}})]):e._e()])]})],2),e.showTestResult&&e.testOk?r("div",{staticClass:"feedbackbox"},[e._v("\n                "+e._s(e.testResult)+"\n            ")]):e._e(),e.showTestResult&&!e.testOk?r("div",{staticClass:"alertbox"},[e._v("\n                "+e._s(e.testResult)+"\n            ")]):e._e(),e.showSaveResult&&e.saveOk?r("div",{staticClass:"feedbackbox"},[e._v("\n                "+e._s(e.saveResult)+"\n            ")]):e._e(),e.showSaveResult&&!e.saveOk?r("div",{staticClass:"alertbox"},[e._v("\n                "+e._s(e.saveResult)+"\n            ")]):e._e(),r("button",{staticClass:"button right margin-left",on:{click:function(t){e.saveConfiguration()}}},[r("font-awesome-icon",{attrs:{icon:"save"}})],1),r("button",{staticClass:"button right",on:{click:function(t){e.testConfiguration()}}},[r("font-awesome-icon",{attrs:{icon:"plug"}})],1)]):e._e()])],1)},y=[],_={name:"ServiceListEntry",props:["id"],data:function(){return{showEditService:!1,showTestResult:!1,testResult:"",testOk:!0,showSaveResult:!1,saveResult:"",saveOk:!0,selectedType:"",parameters:[],parameterValues:[],serviceConfiguration:{id:"",type:"",parameters:{}}}},methods:{editService:function(e){var t=this;this.$http.get(this.$igorUrl+"/api/service/"+e).then(function(e){t.serviceConfiguration=e.data,t.selectedType=t.serviceConfiguration.type,t.loadTypeParameters(t.selectedType)}).catch(function(e){console.log(e)}),this.showEditService=!this.showEditService},deleteService:function(e){var t=this;this.$http.delete(this.$igorUrl+"/api/service/"+e).then(function(e){t.loadExistingServices()}).catch(function(e){console.log(e)})},loadTypeParameters:function(e){var t=this;this.$http.get(this.$igorUrl+"/api/service/type/"+e).then(function(e){for(var r=t.parameters.length;r>0;r--)t.parameters.pop();Array.from(e.data).forEach(function(e,r){t.parameters.push(e)}),t.fillTypeParameters()}).catch(function(e){console.log(e)})},fillTypeParameters:function(){var e=this;this.parameters.forEach(function(t,r){null!=e.serviceConfiguration.parameters[t.name]&&(e.parameterValues[r]=e.serviceConfiguration.parameters[t.name])})},testConfiguration:function(){var e=this;e.serviceConfiguration.type=e.selectedType,this.parameters.forEach(function(t,r){e.serviceConfiguration.parameters[t.name]=e.parameterValues[r]}),this.$http.post(this.$igorUrl+"/api/service/test",this.serviceConfiguration).then(function(t){e.testResult=t.data,e.showTestResult=!0,e.testOk=!0}).catch(function(t){e.testResult=t.response.data,e.showTestResult=!0,e.testOk=!1})},saveConfiguration:function(){var e=this;e.serviceConfiguration.type=e.selectedType,this.parameters.forEach(function(t,r){e.serviceConfiguration.parameters[t.name]=e.parameterValues[r]}),this.$http.post(this.$igorUrl+"/api/service",this.serviceConfiguration).then(function(t){e.saveResult="Saved...",e.showSaveResult=!0,e.saveOk=!0}).catch(function(t){e.saveResult=t.response.data,e.showSaveResult=!0,e.saveOk=!1})},formatString:function(e){return e=e.replace(/\.?([A-Z])/g,function(e,t){return" "+t.toLowerCase()}).replace(/^_/,""),e.charAt(0).toUpperCase()+e.slice(1)}}},b=_,w=(r("t35x"),Object(d["a"])(b,g,y,!1,null,"6d18cbec",null)),C=w.exports,S={name:"ServiceList",components:{ServiceListEntry:C,NewService:h},data:function(){return{services:[],showNewService:!1}},methods:{loadExistingServices:function(){var e=this;this.$http.get(this.$igorUrl+"/api/service/ids").then(function(t){for(var r=e.services.length;r>0;r--)e.services.pop();Array.from(t.data).forEach(function(t,r){e.services.push(t)}),e.services.sort(function(e,t){return e.localeCompare(t)}),e.showNewService=!1}).catch(function(e){console.log(e)})}},mounted:function(){this.loadExistingServices()}},T=S,x=(r("lKrq"),Object(d["a"])(T,c,u,!1,null,"f19f30c0",null)),$=x.exports,k={name:"Navigation",components:{ServiceList:$},data:function(){return{choice:"services",activeClass:"active",tabClass:"tab"}},methods:{setActiveTab:function(e){this.choice=e},isActiveTab:function(e){return this.choice===e}}},R=k,E=(r("FiPi"),Object(d["a"])(R,n,o,!1,null,"54928230",null)),O=E.exports,V={name:"app",components:{Navigation:O}},P=V,A=(r("ZL7j"),Object(d["a"])(P,a,i,!1,null,null,null)),N=A.exports,j=r("7O5W"),U=r("elWB"),L=r("wHSu");j["library"].add(L["a"]),s["a"].component("font-awesome-icon",U["FontAwesomeIcon"]),s["a"].config.productionTip=!0,window.axios=r("vDqi"),window.axios.defaults.headers.common["X-Requested-With"]="XMLHttpRequest",s["a"].prototype.$http=window.axios;var M=!0;s["a"].prototype.$igorUrl=M?"":"http://localhost:8080",new s["a"]({render:function(e){return e(N)}}).$mount("#app")},"XEz+":function(e,t,r){"use strict";var s=r("6F+2"),a=r.n(s);a.a},ZL7j:function(e,t,r){"use strict";var s=r("EDI0"),a=r.n(s);a.a},lKrq:function(e,t,r){"use strict";var s=r("AkSE"),a=r.n(s);a.a},t35x:function(e,t,r){"use strict";var s=r("zjhx"),a=r.n(s);a.a},zjhx:function(e,t,r){}});
//# sourceMappingURL=app.cccccee7.js.map