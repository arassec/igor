// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'

import {library} from '@fortawesome/fontawesome-svg-core'
import {FontAwesomeIcon, FontAwesomeLayers} from '@fortawesome/vue-fontawesome'
import {fas} from '@fortawesome/free-solid-svg-icons'
import {far} from '@fortawesome/free-regular-svg-icons'

library.add(fas)
library.add(far)

Vue.component('font-awesome-icon', FontAwesomeIcon)
Vue.component('font-awesome-layers', FontAwesomeLayers)

Vue.config.productionTip = false

window.axios = require('axios')
window.axios.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest'
Vue.prototype.$http = window.axios

const store = {
  debug: true,
  feedback: {
    timeout: undefined,
    message: '',
    alert: false
  },
  wip: {
    timeout: undefined,
    message: ''
  },
  jobData: {
    jobConfiguration: null,
    selectionKey: null,
    parameterIndex: null,
    serviceCategory: null,
    serviceParameter: null
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
  setWip (message) {
    let component = this
    this.wip.timeout = setTimeout(function () {
      component.wip.message = message
    }, 250)
  },
  getWip () {
    return this.wip
  },
  clearWip () {
    if (this.wip.timeout !== undefined) {
      clearTimeout(this.wip.timeout)
    }
    this.wip.message = ''
  },
  setJobData (jobConfiguration, selectionKey, parameterIndex, serviceCategory) {
    this.jobData.jobConfiguration = jobConfiguration
    this.jobData.selectionKey = selectionKey
    this.jobData.parameterIndex = parameterIndex
    this.jobData.serviceCategory = serviceCategory
  },
  getJobData () {
    return this.jobData
  },
  clearJobData () {
    this.jobData.jobConfiguration = null
    this.jobData.selectionKey = null
    this.jobData.parameterIndex = null
    this.jobData.serviceParameter = null
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
