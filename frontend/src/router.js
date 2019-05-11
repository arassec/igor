import Vue from 'vue'
import Router from 'vue-router'
import AppStatus from './views/app-status'
import ServiceEditor from './views/service-editor'
import JobEditor from './views/job-editor'

Vue.use(Router)

export default new Router({
    routes: [
        {
            path: '/',
            name: 'app-status',
            component: AppStatus
        },
        {
            path: '/service-editor/:serviceId?',
            name: 'service-editor',
            component: ServiceEditor,
            props: true
        },
        {
            path: '/job-editor/:jobId?',
            name: 'job-editor',
            component: JobEditor,
            props: true
        }

    ]
})
