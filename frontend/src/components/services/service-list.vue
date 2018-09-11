<template>
    <core-container>

        <spacer-item/>

        <core-content>
            <list-header :addButtonTarget="'service-editor'" :addButtonText="'Add Service'" :filter="filter">
                <p slot="title">Available Services</p>
            </list-header>

            <service-list-entry v-for="service in filteredServices"
                                v-bind:key="service.id"
                                v-bind:id="service.id"
                                v-bind:name="service.name"
                                @actionPerformed="setActionFeedback"/>

            <feedback-snackbar :show="actionFeedback.length > 0" :feedbackOk="actionFeedbackOk"
                               @timedOut="resetActionFeedback()">
                {{actionFeedback}}
            </feedback-snackbar>

            <feedback-snackbar :show="feedback.length > 0" :feedbackOk="feedbackOk"
                               @timedOut="resetFeedback()">
                {{feedback}}
            </feedback-snackbar>

        </core-content>

        <spacer-item/>

    </core-container>
</template>

<script>
import SpacerItem from '../common/spacer-item'
import ListHeader from '../common/list-header'
import ServiceListEntry from './service-list-entry'
import CorePanel from '../common/core-panel'
import InfoBox from '../common/info-box'
import AlertBox from '../common/alert-box'
import CoreContainer from '../common/core-container'
import CoreContent from '../common/core-content'
import FeedbackSnackbar from '../common/feedback-snackbar'

export default {
  name: 'service-list',
  components: {
    FeedbackSnackbar,
    CoreContent,
    CoreContainer,
    AlertBox,
    InfoBox,
    CorePanel,
    ServiceListEntry,
    ListHeader,
    SpacerItem},
  props: ['saveResult'],
  data: function () {
    return {
      services: [],
      feedback: '',
      feedbackOk: true,
      showFeedback: false,
      filterText: '',
      actionFeedback: '',
      actionFeedbackOk: true
    }
  },
  methods: {
    loadServices: function () {
      this.feedback = ''
      this.feedbackOk = true
      let component = this
      this.$http.get('/api/service').then(function (response) {
        for (let i = component.services.length; i > 0; i--) {
          component.services.pop()
        }
        Array.from(response.data).forEach(function (item) {
          component.services.push(item)
        })
        component.services.sort((a, b) => a.name.localeCompare(b.name))
      }).catch(function (error) {
        console.log(error)
        component.feedback = 'Services could not be loaded!'
        component.feedbackOk = false
      })
    },
    filter: function (filterTextFromListHeader) {
      this.filterText = filterTextFromListHeader
    },
    setActionFeedback: function (newFeedback, newFeedbackOk) {
      this.actionFeedback = newFeedback
      this.actionFeedbackOk = newFeedbackOk
      if (newFeedbackOk) {
        this.loadServices()
      }
    },
    resetActionFeedback: function () {
      this.actionFeedback = ''
      this.actionFeedbackOk = true
    },
    resetFeedback: function () {
      this.feedback = ''
      this.feedbackOk = true
    }
  },
  computed: {
    filteredServices: function () {
      let component = this
      return this.services.filter(function (service) {
        return service.name.includes(component.filterText)
      })
    }
  },
  mounted () {
    this.loadServices()
  }
}
</script>

<style scoped>

</style>
