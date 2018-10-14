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
                                @actionPerformed="loadServices()"/>

            <feedback-snackbar/>

        </core-content>

        <spacer-item/>

    </core-container>
</template>

<script>
import SpacerItem from '../common/spacer-item'
import ListHeader from '../common/list-header'
import ServiceListEntry from './service-list-entry'
import CorePanel from '../common/core-panel'
import CoreContainer from '../common/core-container'
import CoreContent from '../common/core-content'
import FeedbackSnackbar from '../common/feedback-snackbar'

export default {
  name: 'service-list',
  components: {
    FeedbackSnackbar,
    CoreContent,
    CoreContainer,
    CorePanel,
    ServiceListEntry,
    ListHeader,
    SpacerItem},
  props: ['saveResult'],
  data: function () {
    return {
      services: [],
      filterText: ''
    }
  },
  methods: {
    loadServices: function () {
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
        component.$root.$data.store.setFeedback('Services could not be loaded!', true)
      })
    },
    filter: function (filterTextFromListHeader) {
      this.filterText = filterTextFromListHeader
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
