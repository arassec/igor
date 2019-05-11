<template>
    <core-content>
        <list-header :addButtonTarget="'service-editor'" :addButtonText="'Add Service'" :filter="filter"
                     :filter-key="'service-list-filter'">
            <p slot="title">
                <font-awesome-icon icon="cogs"/>
                Available Services
            </p>
        </list-header>

        <list-entry v-for="service in filteredServices" :key="service.id" v-on:clicked="editService(service.id)">
            <list-name slot="left" :class="!service.used ? 'inactive' : ''">
                {{ formatName(service.name) }}
            </list-name>
            <p slot="right" :class="!service.used ? 'inactive' : ''">
                <input-button v-on:clicked="duplicateService(service.id)" icon="clone" class="button-margin-right"/>
                <input-button v-on:clicked="openDeleteServiceDialog(service.id, service.name)" icon="trash-alt"/>
            </p>
        </list-entry>

        <delete-service-dialog v-if="showDeleteDialog"
                               v-bind:service-id="selectedServiceId"
                               v-bind:service-name="selectedServiceName"
                               v-on:cancel="showDeleteDialog = false"
                               v-on:delete-plus="deleteService(true)"
                               v-on:delete="deleteService(false)"/>

    </core-content>
</template>

<script>
  import ListHeader from '../common/list-header'
  import CoreContent from '../common/core-content'
  import IgorBackend from '../../utils/igor-backend.js'
  import FormatUtils from '../../utils/format-utils.js'
  import ListEntry from "../common/list-entry";
  import ListName from "../common/list-name";
  import InputButton from "../common/input-button";
  import DeleteServiceDialog from "./delete-service-dialog";

  export default {
    name: 'service-list',
    components: {
      DeleteServiceDialog,
      InputButton,
      ListName,
      ListEntry,
      CoreContent,
      ListHeader
    },
    props: ['saveResult'],
    data: function () {
      return {
        services: [],
        filterText: '',
        showDeleteDialog: false,
        selectedServiceId: null,
        selectedServiceName: null
      }
    },
    methods: {
      loadServices: function () {
        IgorBackend.getData('/api/service').then((result) => {
          for (let i = this.services.length; i > 0; i--) {
            this.services.pop()
          }
          let component = this
          Array.from(result).forEach(function (item) {
            component.services.push(item)
          })
          this.services.sort((a, b) => a.name.localeCompare(b.name))
        })
      },
      filter: function (filterTextFromListHeader) {
        this.filterText = filterTextFromListHeader
      },
      editService: function (id) {
        this.$router.push({name: 'service-editor', params: {serviceId: id}})
      },
      openDeleteServiceDialog: function (serviceId, serviceName) {
        this.selectedServiceId = serviceId
        this.selectedServiceName = serviceName
        this.showDeleteDialog = true
      },
      deleteService: function (deleteAffectedJobs) {
        this.showDeleteDialog = false
        IgorBackend.deleteData('/api/service/' + this.selectedServiceId + '?deleteAffectedJobs=' + deleteAffectedJobs, 'Deleting service',
            'Service \'' + FormatUtils.formatNameForSnackbar(this.selectedServiceName) + '\' has been deleted.',
            'Service \'' + FormatUtils.formatNameForSnackbar(this.selectedServiceName) + '\' could not be deleted!').then(() => {
          this.loadServices()
          if (deleteAffectedJobs) {
            this.$root.$emit('reload-jobs')
          }
        })
      },
      duplicateService: async function (id) {
        let serviceConfiguration = await IgorBackend.getData('/api/service/' + id)
        serviceConfiguration.name = 'Copy of ' + serviceConfiguration.name
        delete serviceConfiguration.id
        this.$root.$data.store.setServiceData(serviceConfiguration)
        this.$router.push({name: 'service-editor'})
      },
      formatName: function (name) {
        return FormatUtils.formatNameForListEntry(name, 36)
      }
    },
    computed: {
      filteredServices: function () {
        let component = this
        return this.services.filter(function (service) {
          return service.name.toLowerCase().includes(component.filterText.toLowerCase())
        })
      }
    },
    mounted() {
      this.loadServices()
      this.$root.$on('reload-services', () => {
        this.loadServices();
      });
    }
  }
</script>

<style scoped>

</style>
