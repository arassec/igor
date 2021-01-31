// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'

import {library} from '@fortawesome/fontawesome-svg-core'
import {FontAwesomeIcon, FontAwesomeLayers} from '@fortawesome/vue-fontawesome'
import {fas} from '@fortawesome/free-solid-svg-icons'
import {far} from '@fortawesome/free-regular-svg-icons'

import "animate.css/source/animate.css";

library.add(fas)
library.add(far)

Vue.component('font-awesome-icon', FontAwesomeIcon)
Vue.component('font-awesome-layers', FontAwesomeLayers)

Vue.config.productionTip = false

window.axios = require('axios')
window.axios.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest'
Vue.prototype.$http = window.axios

export const store = {
  debug: true,
  feedback: {
    timeout: undefined,
    message: '',
    alert: false
  },
  wip: {
    timeout: undefined,
    message: '',
    cancelCallback: undefined
  },
  connectorData: {
    connectorConfiguration: null
  },
  jobData: {
    jobConfiguration: null,
    selectionKey: null,
    parameterIndex: null,
    connectorCategoryCandidates: null,
    connectorParameter: null
  },
  valueStore: {},
  routeChanged: false,
  setFeedback (message, alert) {
    this.feedback.message = message
    this.feedback.alert = alert
    this.routeChanged = false
    let component = this
    if (this.feedback.timeout !== undefined) {
      clearTimeout(this.feedback.timeout)
    }
    if (this.feedback.alert) {
      this.feedback.timeout = setTimeout(function () {
        component.clearFeedback()
      }, 15000)
    } else {
      this.feedback.timeout = setTimeout(function () {
        component.clearFeedback()
      }, 5000)
    }
  },
  getFeedback () {
    return this.feedback
  },
  clearFeedback () {
    this.feedback.message = ''
    this.feedback.alert = false
    this.routeChanged = false
  },
  setWip (message, cancelCallback) {
    let component = this
    this.wip.cancelCallback = cancelCallback;
    this.wip.timeout = setTimeout(function () {
      component.wip.message = message
    }, 250)
  },
  getWip () {
    return this.wip
  },
  clearWip () {
    if (this.wip.timeout !== undefined) {
      clearTimeout(this.wip.timeout);
    }
    this.wip.cancelCallback = undefined;
    this.wip.message = ''
  },
  setConnectorData (connectorConfiguration) {
    this.connectorData.connectorConfiguration = connectorConfiguration
  },
  getConnectorData () {
    return this.connectorData
  },
  clearConnectorData () {
    this.connectorData.connectorConfiguration = null
  },
  setJobData (jobConfiguration, selectionKey, parameterIndex, connectorCategoryCandidates) {
    this.jobData.jobConfiguration = jobConfiguration
    this.jobData.selectionKey = selectionKey
    this.jobData.parameterIndex = parameterIndex
    this.jobData.connectorCategoryCandidates = connectorCategoryCandidates
  },
  getJobData () {
    return this.jobData
  },
  clearJobData () {
    this.jobData.jobConfiguration = null
    this.jobData.selectionKey = null
    this.jobData.parameterIndex = null
    this.jobData.connectorCategoryCandidates = null;
    this.jobData.connectorParameter = null
  },
  setValue (key, value) {
    this.valueStore[key] = value
  },
  getValue (key) {
    return this.valueStore[key]
  },
  clearValue (key) {
    delete this.valueStore[key]
  }
}

/* eslint-disable no-new */
new Vue({
  el: '#app',
  data: {
    store: store
  },
  router,
  components: {App},
  template: '<App/>'
})

router.beforeEach((to, from, next) => {
  // First route change keeps the message save so it can be displayed on the route's target site.
  if (!store.routeChanged) {
    store.routeChanged = true
  } else {
    store.clearFeedback()
  }
  next()
})
