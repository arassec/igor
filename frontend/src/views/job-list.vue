<template>
    <core-container class="min-width">

        <spacer-item/>

        <core-content>
            <list-header :addButtonTarget="'job-editor'" :addButtonText="'Add Job'" :filter="filter"
                    :filter-key="'job-list-filter'">
                <p slot="title">Available Jobs</p>
            </list-header>

            <job-list-entry v-for="job in filteredJobs"
                            v-bind:key="job.id"
                            v-bind:id="job.id"
                            v-bind:name="job.name"
                            v-bind:active="job.active"
                            v-on:job-deleted="loadJobs()"/>
        </core-content>

        <spacer-item/>

        <background-icon right="true" icon-one="toolbox"/>

    </core-container>
</template>

<script>
import SpacerItem from '../components/common/spacer-item'
import ListHeader from '../components/common/list-header'
import JobListEntry from '../components/jobs/job-list-entry'
import CoreContainer from '../components/common/core-container'
import CoreContent from '../components/common/core-content'
import BackgroundIcon from '../components/common/background-icon'

export default {
  name: 'job-list',
  components: {BackgroundIcon, CoreContent, CoreContainer, JobListEntry, ListHeader, SpacerItem},
  data: function () {
    return {
      jobs: [],
      filterText: ''
    }
  },
  methods: {
    loadJobs: function () {
      let component = this
      this.$http.get('/api/job').then(function (response) {
        for (let i = component.jobs.length; i > 0; i--) {
          component.jobs.pop()
        }
        Array.from(response.data).forEach(function (item) {
          component.jobs.push(item)
        })
        component.jobs.sort((a, b) => a.name.localeCompare(b.name))
      }).catch(function (error) {
        component.$root.$data.store.setFeedback('Jobs could not be loaded (' + error + ')', true)
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
        return job.name.toLowerCase().includes(component.filterText.toLowerCase())
      })
    }
  },
  mounted () {
    this.loadJobs()
    this.$root.$data.store.clearServiceData()
    this.$root.$data.store.clearJobData()
  }
}
</script>

<style scoped>

    .min-width {
        --content-width: 800px;
    }

    .background-icon {
        color: var(--nav-background-color);
        opacity: 0.3;
        position: fixed;
        bottom: -20px;
        right: -20px;
        z-index: -1;
    }

    .background-icon-left {
        color: var(--nav-background-color);
        opacity: 0.3;
        position: fixed;
        bottom: -20px;
        right: 420px;
        z-index: -1;
    }

</style>
