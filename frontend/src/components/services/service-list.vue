<template>
    <core-content overflow-hidden="true">
        <list-header :addButtonTarget="'service-editor'" :addButtonText="'Add Service'" :filter="filter"
                     :filter-key="'service-list-filter'" v-on:import="importService()">
            <p slot="title">
                <font-awesome-icon icon="cogs"/>
                Available Services
            </p>
        </list-header>

        <div v-if="servicesPage">
            <list-entry
                    v-for="service in servicesPage.items" :key="service.id" v-on:clicked="editService(service.id)">
                <list-name slot="left" :class="!service.used ? 'inactive' : ''">
                    {{ service.name }}
                </list-name>
                <p slot="right" :class="!service.used ? 'inactive' : ''">
                    <input-button v-on:clicked="openExportDialog(service.id, service.name)"
                                  class="button-margin-right button-margin-left"
                                  icon="file-download"/>
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

        <modal-dialog v-if="showExportDialog" @close="showExportDialog = false" v-on:cancel="showExportDialog = false">
            <h1 slot="header">Export service?</h1>
            <div slot="body">
                <div class="paragraph">
                    Export service
                    <div class="truncate highlight">{{selectedServiceName}}</div>
                    to file?
                </div>
                <div class="paragraph alert">
                    WARNING: the created file will contain passwords and other sensitive information in cleartext!
                </div>
            </div>
            <layout-row slot="footer">
                <input-button slot="left" v-on:clicked="showExportDialog = false" icon="times"/>
                <input-button slot="right" v-on:clicked="exportService()" icon="check"/>
            </layout-row>
        </modal-dialog>

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
    import ModalDialog from "../common/modal-dialog";
    import LayoutRow from "../common/layout-row";

    export default {
        name: 'service-list',
        components: {
            LayoutRow,
            ModalDialog,
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
                showExportDialog: false,
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
            openExportDialog: function (serviceId, serviceName) {
                this.selectedServiceId = serviceId
                this.selectedServiceName = serviceName
                this.showExportDialog = true
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
            exportService: function () {
                this.showExportDialog = false;
                IgorBackend.getResponse('/api/transfer/service/' + this.selectedServiceId).then((response) => {
                    const url = window.URL.createObjectURL(new Blob([JSON.stringify(response.data)]))
                    const link = document.createElement('a');
                    link.href = url;
                    let fileName = response.headers['content-disposition'].split("filename=")[1];
                    link.setAttribute('download', fileName);
                    document.body.appendChild(link);
                    link.click();
                });
            },
            duplicateService: async function (id) {
                let serviceConfiguration = await IgorBackend.getData('/api/service/' + id)
                serviceConfiguration.name = 'Copy of ' + serviceConfiguration.name
                delete serviceConfiguration.id
                this.$root.$data.store.setServiceData(serviceConfiguration)
                this.$router.push({name: 'service-editor'})
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
