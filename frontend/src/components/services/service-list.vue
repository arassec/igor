<template>
    <core-content>
        <list-header :addButtonTarget="'service-editor'" :addButtonText="'Add Service'" :filter="filter"
                     :filter-key="'service-list-filter'">
            <p slot="title">
                <font-awesome-icon icon="cogs"/>
                Available Services
            </p>
        </list-header>

        <div v-if="servicesPage">
            <list-entry
                    v-for="service in servicesPage.items" :key="service.id" v-on:clicked="editService(service.id)">
                <list-name slot="left" :class="!service.used ? 'inactive' : ''">
                    {{ formatName(service.name) }}
                </list-name>
                <p slot="right" :class="!service.used ? 'inactive' : ''">
                    <input-button v-on:clicked="duplicateService(service.id)" icon="clone" class="button-margin-right"/>
                    <input-button v-on:clicked="openDeleteServiceDialog(service.id, service.name)" icon="trash-alt"/>
                </p>
            </list-entry>

            <list-pager :page="servicesPage" v-if="servicesPage && servicesPage.totalPages > 1" :dark="true"
                        v-on:first="loadServices(0)"
                        v-on:previous="loadServices(servicesPage.number - 1)"
                        v-on:next="loadServices(servicesPage.number + 1)"
                        v-on:last="loadServices(servicesPage.totalPages -1)"/>
        </div>

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
  import ListPager from "../common/list-pager";

  export default {
    name: 'service-list',
    components: {
      ListPager,
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
        servicesPage: {
          number: 0,
          size: 12,
          totalPages: 0,
          items: []
        },
        filterText: '',
        showDeleteDialog: false,
        selectedServiceId: null,
        selectedServiceName: null
      }
    },
    methods: {
      loadServices: async function (page) {
        if (this.servicesPage) {
          this.servicesPage = await IgorBackend.getData('/api/service?pageNumber=' + page + "&pageSize=" +
              this.servicesPage.size + "&nameFilter=" + this.filterText);
        }
      },
      filter: function (filterTextFromListHeader) {
        this.filterText = filterTextFromListHeader
        this.loadServices(0)
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
          this.loadServices(0)
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
        return FormatUtils.shorten(name, 36)
      }
    },
    mounted() {
      if (this.$root.$data.store.getValue('service-list-filter')) {
        this.filterText = this.$root.$data.store.getValue('service-list-filter')
      }
      this.loadServices(0)
      this.$root.$on('reload-services', () => {
        this.loadServices(0);
      });
    }
  }
</script>

<style scoped>

</style>
