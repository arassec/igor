<template>
    <core-container class="wrap">

        <action-bar>
            <div slot="left" class="action-bar-container">
                <input-filter :filter-key="'connector-name-filter'" :filter="filterConnectorName"
                              :label="'Name filter:'"/>
            </div>
            <div slot="right">
                <router-link :to="'connector-editor'">
                    <input-button icon="plus" label="Add connector" class="margin-right"/>
                </router-link>
                <input-button icon="file-upload" label="Import connector"/>
            </div>
        </action-bar>

        <div class="tiles-container">
            <div class="tiles">
                <div v-for="connector of connectorsPage.items" :key="connector.id">
                    <overview-tile v-on:clicked="editConnector(connector.id)" :active="connector.used">
                        <div slot="title">{{connector.name}}</div>
                        <layout-row slot="menu">
                            <div slot="left">
                                <input-button icon="trash"
                                              v-on:clicked="openDeleteConnectorDialog(connector.id, connector.name)"
                                              class="margin-right"/>
                                <input-button icon="file-download"
                                              v-on:clicked="openExportDialog(connector.id, connector.name)"
                                              class="margin-right"/>
                                <input-button icon="clone" v-on:clicked="duplicateConnector(connector.id)"
                                              class="margin-right"/>
                            </div>
                        </layout-row>
                    </overview-tile>
                </div>
            </div>

            <list-pager :page="connectorsPage" v-if="connectorsPage && connectorsPage.totalPages > 1" :dark="true"
                        v-on:first="loadConnectors(0)"
                        v-on:previous="loadConnectors(connectorsPage.number - 1)"
                        v-on:next="loadConnectors(connectorsPage.number + 1)"
                        v-on:last="loadConnectors(connectorsPage.totalPages -1)"/>
        </div>

        <delete-connector-dialog v-if="showDeleteDialog"
                                 v-bind:connector-id="selectedConnectorId"
                                 v-bind:connector-name="selectedConnectorName"
                                 v-on:cancel="showDeleteDialog = false"
                                 v-on:delete-plus="deleteConnector(true)"
                                 v-on:delete="deleteConnector(false)"/>

        <modal-dialog v-if="showExportDialog" @close="showExportDialog = false" v-on:cancel="showExportDialog = false">
            <h1 slot="header">Export connector?</h1>
            <div slot="body">
                <div class="paragraph">
                    Export connector
                    <div class="truncate highlight">{{selectedConnectorName}}</div>
                    to file?
                </div>
                <div class="paragraph alert">
                    WARNING: the created file will contain passwords and other sensitive information in cleartext!
                </div>
            </div>
            <layout-row slot="footer">
                <input-button slot="left" v-on:clicked="showExportDialog = false" icon="times"/>
                <input-button slot="right" v-on:clicked="exportConnector()" icon="check"/>
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
    import DeleteConnectorDialog from "../components/connectors/delete-connector-dialog";
    import ModalDialog from "../components/common/modal-dialog";
    import LayoutRow from "../components/common/layout-row";
    import FormatUtils from "../utils/utils";
    import ListPager from "../components/common/list-pager";

    export default {
        name: "connector-overview",
        components: {
            DeleteConnectorDialog,
            ListPager,
            LayoutRow,
            ModalDialog,
            OverviewTile,
            InputButton,
            InputFilter,
            ActionBar,
            CoreContainer
        },
        data: function () {
            return {
                connectorsPage: {
                    number: 0,
                    size: 20,
                    totalPages: 0,
                    items: []
                },
                nameFilter: '',
                showDeleteDialog: false,
                showExportDialog: false,
                selectedConnectorId: null,
                selectedConnectorName: null
            }
        },
        methods: {
            loadConnectors: async function (page) {
                if (page === undefined) {
                    page = this.connectorsPage.number;
                }
                if (this.connectorsPage) {
                    this.connectorsPage = await IgorBackend.getData('/api/connector?pageNumber=' + page + "&pageSize=" +
                        this.connectorsPage.size + "&nameFilter=" + this.nameFilter);
                }
            },
            filterConnectorName: function (filterSelectionFromSelector) {
                this.nameFilter = filterSelectionFromSelector;
                this.loadConnectors(0)
            },
            editConnector: function (connectorId) {
                this.$router.push({name: 'connector-editor', params: {connectorId: connectorId}})
            },
            openDeleteConnectorDialog: function (connectorId, connectorName) {
                this.selectedConnectorId = connectorId;
                this.selectedConnectorName = connectorName;
                this.showDeleteDialog = true
            },
            openExportDialog: function (connectorId, connectorName) {
                this.selectedConnectorId = connectorId;
                this.selectedConnectorName = connectorName;
                this.showExportDialog = true
            },
            deleteConnector: function (deleteAffectedJobs) {
                this.showDeleteDialog = false;
                IgorBackend.deleteData('/api/connector/' + this.selectedConnectorId + '?deleteAffectedJobs=' + deleteAffectedJobs, 'Deleting connector',
                    'Connector \'' + FormatUtils.formatNameForSnackbar(this.selectedConnectorName) + '\' has been deleted.',
                    'Connector \'' + FormatUtils.formatNameForSnackbar(this.selectedConnectorName) + '\' could not be deleted!').then(() => {
                    this.loadConnectors(0);
                    if (deleteAffectedJobs) {
                        this.$root.$emit('reload-jobs')
                    }
                })
            },
            exportConnector: function () {
                this.showExportDialog = false;
                IgorBackend.getResponse('/api/transfer/connector/' + this.selectedConnectorId).then((response) => {
                    const url = window.URL.createObjectURL(new Blob([JSON.stringify(response.data)]));
                    const link = document.createElement('a');
                    link.href = url;
                    let fileName = response.headers['content-disposition'].split("filename=")[1];
                    link.setAttribute('download', fileName);
                    document.body.appendChild(link);
                    link.click();
                });
            },
            duplicateConnector: async function (id) {
                let connectorConfiguration = await IgorBackend.getData('/api/connector/' + id);
                connectorConfiguration.name = 'Copy of ' + connectorConfiguration.name;
                delete connectorConfiguration.id;
                this.$root.$data.store.setConnectorData(connectorConfiguration);
                this.$router.push({name: 'connector-editor'})
            }
        },
        mounted() {
            this.$root.$data.store.clearConnectorData();
            if (this.$root.$data.store.getValue('connector-name-filter')) {
                this.nameFilter = this.$root.$data.store.getValue('connector-name-filter')
            }
            this.loadConnectors(0)
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