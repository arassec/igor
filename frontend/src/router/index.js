import Vue from 'vue'
import Router from 'vue-router'
import Home from '../components/home/app-status'
import ServiceList from '../components/services/service-list'
import ServiceEditor from '../components/services/service-editor'
import JobList from '../components/jobs/job-list'
import JobEditor from '../components/jobs/job-editor'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'home',
      component: Home
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
      component: JobList
    },
    {
      path: '/job-editor/:id?',
      name: 'job-editor',
      component: JobEditor,
      props: true
    }

  ]
})
