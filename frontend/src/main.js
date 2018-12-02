// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'

import {library} from '@fortawesome/fontawesome-svg-core'
import {FontAwesomeIcon, FontAwesomeLayers } from '@fortawesome/vue-fontawesome'
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
    timeout: undefined,
    feedback: {
        message: '',
        alert: false
    },
    routeChanged: false,
    setFeedback (message, alert) {
        if (this.debug) console.log('setFeedback triggered with', message)
        this.feedback.message = message
        this.feedback.alert = alert
        this.routeChanged = false
        let component = this
        if (this.timeout !== undefined) {
            clearTimeout(this.timeout)
        }
        this.timeout = setTimeout(function () {
            component.clearFeedback()
        }, 5000)
    },
    getFeedback () {
        if (this.debug) console.log('getFeedback triggered')
        return this.feedback
    },
    clearFeedback () {
        if (this.debug) console.log('clearFeedback triggered')
        this.feedback.message = ''
        this.feedback.alert = false
        this.routeChanged = false
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
