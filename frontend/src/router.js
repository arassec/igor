import Vue from 'vue'
import Router from 'vue-router'
import AppStatus from './views/app-status'
import ServiceList from './views/service-list'
import ServiceEditor from './views/service-editor'
import JobList from './views/job-list'
import JobEditor from './views/job-editor'

Vue.use(Router)

export default new Router({
    routes: [
        {
            path: '/',
            name: 'home',
            component: AppStatus
        },
        {
            path: '/services:saveResult?',
            name: 'services',
            component: ServiceList,
            props: true
        },
        {
            path: '/service-editor/:serviceId?',
            name: 'service-editor',
            component: ServiceEditor,
            props: true
        },
        {
            path: '/jobs',
            name: 'jobs',
            component: JobList,
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
