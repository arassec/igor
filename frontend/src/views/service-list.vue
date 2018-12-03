<template>
  <core-container class="min-width">

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

    </core-content>

    <spacer-item/>

    <background-icon left="true" icon-one="cogs"/>

  </core-container>
</template>

<script>
import SpacerItem from '../components/common/spacer-item'
import ListHeader from '../components/common/list-header'
import ServiceListEntry from '../components/services/service-list-entry'
import CoreContainer from '../components/common/core-container'
import CoreContent from '../components/common/core-content'
import BackgroundIcon from '../components/common/background-icon'

export default {
  name: 'service-list',
  components: {
    BackgroundIcon,
    CoreContent,
    CoreContainer,
    ServiceListEntry,
    ListHeader,
    SpacerItem
  },
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

  .min-width {
    --content-width: 800px;
  }

  .background-icon {
    color: var(--nav-background-color);
    opacity: 0.3;
    position: fixed;
    bottom: -20px;
    left: -20px;
    z-index: -1;
  }

</style>
