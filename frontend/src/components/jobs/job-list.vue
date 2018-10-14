<template>
    <core-container>

        <spacer-item/>

        <core-content>
            <list-header :addButtonTarget="'job-editor'" :addButtonText="'Add Job'" :filter="filter">
                <p slot="title">Available Jobs</p>
                <p slot="feedback">
                    <feedback-box v-if="feedback.length > 0" :text="feedback" :alert="!feedbackOk"/>
                </p>
            </list-header>

            <job-list-entry v-for="job in filteredJobs"
                            v-bind:key="job.id"
                            v-bind:id="job.id"
                            v-bind:name="job.name"
                            v-on:job-deleted="loadJobs()"/>
        </core-content>

        <spacer-item/>

    </core-container>
</template>

<script>
import SpacerItem from '../common/spacer-item'
import ListHeader from '../common/list-header'
import CorePanel from '../common/core-panel'
import JobListEntry from './job-list-entry'
import CoreContainer from '../common/core-container'
import CoreContent from '../common/core-content'
import FeedbackBox from '../common/feedback-box'

export default {
  name: 'job-list',
  components: {CoreContent, CoreContainer, FeedbackBox, CorePanel, JobListEntry, ListHeader, SpacerItem},
  props: ['saveResult'],
  data: function () {
    return {
      jobs: [],
      feedback: '',
      feedbackOk: true,
      filterText: ''
    }
  },
  methods: {
    loadJobs: function () {
      this.feedback = ''
      this.feedbackOk = true
      let component = this
      this.$http.get('/api/job/id').then(function (response) {
        for (let i = component.jobs.length; i > 0; i--) {
          component.jobs.pop()
        }
        Array.from(response.data).forEach(function (item) {
          component.jobs.push(item)
        })
        component.jobs.sort((a, b) => a.localeCompare(b))
      }).catch(function (error) {
        component.feedback = error
        component.feedbackOk = false
      })
    },
    filter: function (filterTextFromListHeader) {
      this.filterText = filterTextFromListHeader
    }
  },
  computed: {
    filteredJobs: function () {
      let component = this
      return this.jobs.filter(function (job) {
        return job.name.includes(component.filterText)
      })
    }
  },
  mounted () {
    this.loadJobs()
  }
}
</script>

<style scoped>

</style>
