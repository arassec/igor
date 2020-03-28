<template>
    <core-container class="wrap">

        <action-bar>
            <div slot="left" class="action-bar-container">
                <input-filter :filter-key="'service-name-filter'" :filter="filterServiceName" :label="'Name filter:'"/>
            </div>
            <div slot="right">
                <router-link :to="'service-editor'">
                    <input-button icon="plus" label="Add service" class="button-margin-right"/>
                </router-link>
                <input-button icon="file-upload" label="Import service"/>
            </div>
        </action-bar>

        <div class="tiles-container">
            <div class="tiles">
                <div v-for="service of servicesPage.items" :key="service.id">
                    <overview-tile v-on:clicked="editService(service.id)" :active="service.used">
                        <div slot="title">{{service.name}}</div>
                        <layout-row slot="menu">
                            <div slot="left">
                                <input-button icon="trash-alt" v-on:clicked="openDeleteServiceDialog(service.id, service.name)"
                                              class="button-margin-right"/>
                                <input-button icon="file-download" v-on:clicked="openExportDialog(service.id, service.name)"
                                              class="button-margin-right"/>
                                <input-button icon="clone" v-on:clicked="duplicateService(service.id)"
                                              class="button-margin-right"/>
                            </div>
                        </layout-row>
                    </overview-tile>
                </div>
            </div>

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

    </core-container>
</template>

<script>
    import CoreContainer from "../components/common/core-container";
    import ActionBar from "../components/common/action-bar";
    import InputFilter from "../components/common/input-filter";
    import InputButton from "../components/common/input-button";
    import IgorBackend from "../utils/igor-backend";
    import OverviewTile from "../components/common/overview-tile";
    import DeleteServiceDialog from "../components/services/delete-service-dialog";
    import ModalDialog from "../components/common/modal-dialog";
    import LayoutRow from "../components/common/layout-row";
    import FormatUtils from "../utils/format-utils";
    import ListPager from "../components/common/list-pager";

    export default {
        name: "service-overview",
        components: {
            ListPager,
            LayoutRow,
            ModalDialog, DeleteServiceDialog, OverviewTile, InputButton, InputFilter, ActionBar, CoreContainer},
        data: function () {
            return {
                servicesPage: {
                    number: 0,
                    size: 20,
                    totalPages: 0,
                    items: []
                },
                nameFilter: '',
                showDeleteDialog: false,
                showExportDialog: false,
                selectedServiceId: null,
                selectedServiceName: null
            }
        },
        methods: {
            loadServices: async function (page) {
                if (page === undefined) {
                    page = this.servicesPage.number;
                }
                if (this.servicesPage) {
                    this.servicesPage = await IgorBackend.getData('/api/service?pageNumber=' + page + "&pageSize=" +
                        this.servicesPage.size + "&nameFilter=" + this.nameFilter);
                }
            },
            filterServiceName: function (filterSelectionFromSelector) {
                this.nameFilter = filterSelectionFromSelector;
                this.loadServices(0)
            },
            editService: function (serviceId) {
                this.$router.push({name: 'service-editor', params: {serviceId: serviceId}})
            },
            openDeleteServiceDialog: function (serviceId, serviceName) {
                this.selectedServiceId = serviceId;
                this.selectedServiceName = serviceName;
                this.showDeleteDialog = true
            },
            openExportDialog: function (serviceId, serviceName) {
                this.selectedServiceId = serviceId;
                this.selectedServiceName = serviceName;
                this.showExportDialog = true
            },
            deleteService: function (deleteAffectedJobs) {
                this.showDeleteDialog = false;
                IgorBackend.deleteData('/api/service/' + this.selectedServiceId + '?deleteAffectedJobs=' + deleteAffectedJobs, 'Deleting service',
                    'Service \'' + FormatUtils.formatNameForSnackbar(this.selectedServiceName) + '\' has been deleted.',
                    'Service \'' + FormatUtils.formatNameForSnackbar(this.selectedServiceName) + '\' could not be deleted!').then(() => {
                    this.loadServices(0);
                    if (deleteAffectedJobs) {
                        this.$root.$emit('reload-jobs')
                    }
                })
            },
            exportService: function () {
                this.showExportDialog = false;
                IgorBackend.getResponse('/api/transfer/service/' + this.selectedServiceId).then((response) => {
                    const url = window.URL.createObjectURL(new Blob([JSON.stringify(response.data)]));
                    const link = document.createElement('a');
                    link.href = url;
                    let fileName = response.headers['content-disposition'].split("filename=")[1];
                    link.setAttribute('download', fileName);
                    document.body.appendChild(link);
                    link.click();
                });
            },
            duplicateService: async function (id) {
                let serviceConfiguration = await IgorBackend.getData('/api/service/' + id);
                serviceConfiguration.name = 'Copy of ' + serviceConfiguration.name;
                delete serviceConfiguration.id;
                this.$root.$data.store.setServiceData(serviceConfiguration);
                this.$router.push({name: 'service-editor'})
            }
        },
        mounted() {
            this.$root.$data.store.clearServiceData();
            if (this.$root.$data.store.getValue('service-name-filter')) {
                this.nameFilter = this.$root.$data.store.getValue('service-name-filter')
            }
            this.loadServices(0)
        }

    }
</script>

<style scoped>

    .wrap {
        flex-wrap: wrap;
    }

    .tiles-container {
        display: flex;
        flex-direction: column;
        width: 100%;
    }

    .tiles {
        display: flex;
        flex-wrap: wrap;
        width: 100%;
    }

    .action-bar-container {
        display: flex;
        flex-direction: row;
    }

</style>