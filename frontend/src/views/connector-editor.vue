<template>
    <core-container>

        <side-menu>
            <p slot="title">Connector Configuration</p>
            <layout-row slot="header">
                <p slot="left">
                    <input-button v-on:clicked="cancelConfiguration()" icon="arrow-left" class="margin-right"/>
                    <input-button v-on:clicked="testConfiguration()" icon="plug" class="margin-right"/>
                    <input-button v-on:clicked="saveConfiguration()" icon="save"/>
                </p>
            </layout-row>
            <core-panel slot="footer">
                <p
                        v-if="referencingJobsPage && referencingJobsPage.items && referencingJobsPage.items.length > 0">
                    <label class="list-label">Used by the following jobs:</label>
                    <feedback-box v-for="(referencingJob, index) in referencingJobsPage.items" :key="index"
                                  class="list-entry"
                                  :clickable="true"
                                  v-on:feedback-clicked="editJob(referencingJob.key)">
                        <div slot="left" class="width-restricted truncate">{{referencingJob.value}}</div>
                    </feedback-box>
                    <list-pager :page="referencingJobsPage" v-if="referencingJobsPage.totalPages > 1"
                                v-on:first="loadReferencingJobs(0)"
                                v-on:previous="loadReferencingJobs(referencingJobsPage.number - 1)"
                                v-on:next="loadReferencingJobs(referencingJobsPage.number + 1)"
                                v-on:last="loadReferencingJobs(referencingJobsPage.totalPages -1)"/>
                </p>
                <label v-if="!referencingJobsPage || !referencingJobsPage.items || referencingJobsPage.items.length
             === 0">No jobs are using this connector.</label>
            </core-panel>
        </side-menu>

        <core-content>
            <core-panel>
                <layout-row>
                    <h1 slot="left" class="truncate width-restricted">
                        <font-awesome-icon icon="link"/>
                        {{ connectorConfiguration.name.length > 0 ? connectorConfiguration.name : 'Unnamed Connector' }}
                    </h1>
                    <icon-button slot="right" icon="question" v-on:clicked="openDocumentation('connector')"/>
                </layout-row>
                <div class="table">
                    <div class="tr">
                        <div class="td"><label for="connector-name">Name</label></div>
                        <div class="td">
                            <input-validated id="connector-name" :type="'text'" v-model="connectorConfiguration.name"
                                             :parent-id="connectorConfiguration.id" :property-id="'name'"
                                             :validation-errors="validationErrors"/>
                        </div>
                    </div>
                </div>
            </core-panel>

            <core-panel>
                <layout-row>
                    <h2 slot="left">Connector</h2>
                    <icon-button slot="right" icon="question" v-show="hasDocumentation(connectorConfiguration.type.key)"
                                 v-on:clicked="openDocumentation(connectorConfiguration.type.key)"/>
                </layout-row>
                <div class="table">
                    <div class="tr">
                        <div class="td"><label for="category-input">Category</label></div>
                        <div class="td">
                            <select id="category-input" v-model="connectorConfiguration.category"
                                    v-on:change="loadTypesOfCategory(connectorConfiguration.category, true).then(() => {
                                        loadParametersOfType(connectorConfiguration.type.key)})"
                                    :disabled="!newConnector">
                                <option v-for="category in connectorCategories" v-bind:value="category"
                                        v-bind:key="category.key">
                                    {{category.value}}
                                </option>
                            </select>
                        </div>
                    </div>
                    <div class="tr">
                        <div class="td"><label for="type-input">Type</label></div>
                        <div class="td">
                            <select id="type-input" v-model="connectorConfiguration.type"
                                    v-on:change="loadParametersOfType(connectorConfiguration.type.key)"
                                    :disabled="!newConnector">
                                <option v-for="type in connectorTypes" v-bind:value="type" v-bind:key="type.key">
                                    {{type.value}}
                                </option>
                            </select>
                        </div>
                    </div>
                </div>
            </core-panel>

            <core-panel>
                <h2>Connector Parameters</h2>
                <parameter-editor v-if="Object.keys(connectorConfiguration.parameters).length > 0"
                                  :parent-id="connectorConfiguration.id"
                                  :validation-errors="validationErrors"
                                  :parameters="connectorConfiguration.parameters" ref="parameterEditor"/>
                <p v-if="Object.keys(connectorConfiguration.parameters).length === 0">
                    This connector has no parameters to configure.
                </p>
            </core-panel>

            <modal-dialog v-if="showUnsavedValuesExistDialog" @close="showUnsavedValuesExistDialog = false">
                <h1 slot="header">Unsaved configuration</h1>
                <p slot="body">There are unsaved configuration changes.<br/><br/>Do you really want to leave?</p>
                <div slot="footer">
                    <layout-row>
                        <input-button slot="left" v-on:clicked="showUnsavedValuesExistDialog = false" icon="times"/>
                        <input-button slot="right" v-on:clicked="nextRoute()" icon="check"/>
                    </layout-row>
                </div>
            </modal-dialog>

        </core-content>

        <modal-dialog v-if="showTestDetails" @close="showTestDetails = false"
                      v-on:cancel="showTestDetails = false">
            <layout-row slot="header">
                <h1 slot="left">Testing Failed!</h1>
                <input-button slot="right" icon="times" v-on:clicked="showTestDetails = false"/>
            </layout-row>
            <div slot="body" class="paragraph">
                Connection to the service failed!
                <pre><code>{{ testError }}</code></pre>
            </div>
        </modal-dialog>

        <documentation-container :documentation="documentation" v-show="showDocumentation"
                                 v-on:close="showDocumentation = false"/>

        <background-icon right="true" icon-one="link"/>

    </core-container>
</template>

<script>
    import ParameterEditor from '../components/common/parameter-editor'
    import InputButton from '../components/common/input-button'
    import CorePanel from '../components/common/core-panel'
    import CoreContainer from '../components/common/core-container'
    import CoreContent from '../components/common/core-content'
    import LayoutRow from '../components/common/layout-row'
    import SideMenu from '../components/common/side-menu'
    import FormatUtils from '../utils/utils.js'
    import IgorBackend from '../utils/igor-backend.js'
    import BackgroundIcon from "../components/common/background-icon";
    import ModalDialog from "../components/common/modal-dialog";
    import FeedbackBox from "../components/common/feedback-box";
    import ListPager from "../components/common/list-pager";
    import IconButton from "../components/common/icon-button";
    import DocumentationContainer from "../components/common/documentation-container";
    import Utils from "../utils/utils";
    import InputValidated from "../components/common/input-validated";

    export default {
        name: 'connector-editor',
        components: {
            InputValidated,
            DocumentationContainer,
            IconButton,
            ListPager,
            FeedbackBox,
            ModalDialog,
            BackgroundIcon,
            SideMenu,
            LayoutRow,
            CoreContent,
            CoreContainer,
            CorePanel,
            InputButton,
            ParameterEditor
        },
        props: ['connectorId'],
        data: function () {
            return {
                newConnector: true,
                connectorCategories: [],
                connectorTypes: [],
                loadParameters: true,
                originalConnectorConfiguration: null,
                validationErrors: {},
                connectorConfiguration: {
                    id: Utils.uuidv4(),
                    name: '',
                    category: {},
                    type: {},
                    parameters: {}
                },
                referencingJobsPage: {
                    number: 0,
                    size: 10,
                    totalPages: 0,
                    items: []
                },
                showUnsavedValuesExistDialog: false,
                nextRoute: null,
                showDocumentation: false,
                documentation: null,
                showTestDetails: false,
                testError: null
            }
        },
        methods: {
            loadConnector: async function (id) {
                await IgorBackend.getData('/api/connector/' + id).then((connectorConfiguration) => {
                    this.connectorConfiguration = connectorConfiguration;
                    this.connectorCategories.push(this.connectorConfiguration.category);
                    this.connectorTypes.push(this.connectorConfiguration.type);
                    this.newConnector = false
                })
            },
            loadCategories: async function () {
                await IgorBackend.getData('/api/category/connector').then((categories) => {
                    for (let i = this.connectorCategories.length; i > 0; i--) {
                        this.connectorCategories.pop()
                    }
                    let component = this;
                    Array.from(categories).forEach(function (item) {
                        component.connectorCategories.push(item)
                    });
                    if (!('key' in this.connectorConfiguration.category)) {
                        this.connectorConfiguration.category = this.connectorCategories[0]
                    }
                })
            },
            loadTypesOfCategory: async function (category, selectFirst) {
                // If the category contains an array with name 'typeCandidates' the allowed types are pre-selected, because
                // the connector is created from within a job configuration:
                if (category.hasOwnProperty('typeCandidates')) {
                    for (let i = this.connectorTypes.length; i > 0; i--) {
                        this.connectorTypes.pop()
                    }
                    let component = this;
                    Array.from(category.typeCandidates).forEach(function (item) {
                        component.connectorTypes.push(item)
                    });
                    if (selectFirst) {
                        this.connectorConfiguration.type = this.connectorTypes[0]
                    }
                } else {
                    // No restrictions, simply load all types of the given category:
                    await IgorBackend.getData('/api/type/' + category.key).then((types) => {
                        for (let i = this.connectorTypes.length; i > 0; i--) {
                            this.connectorTypes.pop()
                        }
                        let component = this;
                        Array.from(types).forEach(function (item) {
                            component.connectorTypes.push(item)
                        });
                        if (selectFirst) {
                            this.connectorConfiguration.type = this.connectorTypes[0]
                        }
                    })
                }
            },
            loadParametersOfType: async function (typeKey) {
                if (this.hasDocumentation(typeKey)) {
                    await this.switchDocumentation(typeKey);
                } else {
                    this.showDocumentation = false
                }
                IgorBackend.getData('/api/parameters/connector/' + typeKey).then((parameters) => {
                    this.connectorConfiguration.parameters = parameters;
                    this.validationErrors = {};
                })
            },
            loadReferencingJobs: async function (page) {
                if (this.connectorConfiguration && this.connectorConfiguration.id) {
                    this.referencingJobsPage = await IgorBackend.getData('/api/connector/' + this.connectorConfiguration.id +
                        '/job-references?pageNumber=' + page + '&pageSize=' + this.referencingJobsPage.size)
                }
            },
            testConfiguration: async function () {
                await IgorBackend.postData('/api/connector/test', this.connectorConfiguration, 'Testing connector', 'Test OK.', 'Testing Failed!').then((result) => {
                    this.validationErrors = {};
                    if (result.status === 400) {
                        this.validationErrors = result.data;
                    } else if (result.status === 424 && 'generalError' in result.data) {
                        this.testError = result.data['generalError'];
                        this.showTestDetails = true;
                    }
                })
            },
            saveConfiguration: async function () {
                IgorBackend.postData('/api/connector', this.connectorConfiguration, 'Saving connector',
                    'Connector \'' + FormatUtils.formatNameForSnackbar(this.connectorConfiguration.name) + '\' saved.',
                    'Saving failed!').then((result) => {
                    if (result.status === 400) {
                        this.validationErrors = result.data;
                    } else {
                        this.newConnector = false
                        this.connectorConfiguration = result.data;
                        this.originalConnectorConfiguration = JSON.stringify(this.connectorConfiguration);
                        let jobData = this.$root.$data.store.getJobData();
                        if (jobData.jobConfiguration != null) {
                            jobData.connectorParameter = {
                                name: this.connectorConfiguration.name,
                                id: this.connectorConfiguration.id
                            }
                        }
                    }
                })
            },
            cancelConfiguration: function () {
                let jobData = this.$root.$data.store.getJobData();
                if (jobData.jobConfiguration != null) {
                    this.$router.push({name: 'job-editor'})
                } else {
                    this.$router.push({name: 'connector-overview'})
                }
            },
            editJob: function (jobId) {
                this.$router.push({name: 'job-editor', params: {jobId: jobId}})
            },
            hasDocumentation: function (typeId) {
                for (let i = 0; i < this.connectorTypes.length; i++) {
                    if (this.connectorTypes[i].key === typeId) {
                        return this.connectorTypes[i].documentationAvailable;
                    }
                }
                return false;
            },
            openDocumentation: async function (key) {
                this.documentation = await IgorBackend.getData('/api/doc/' + key);
                this.showDocumentation = true;
                this.testResults = null;
            },
            switchDocumentation: async function (key) {
                if (this.showDocumentation) {
                    this.documentation = await IgorBackend.getData('/api/doc/' + key);
                    this.testResults = null;
                }
            }
        },
        mounted() {
            let connectorData = this.$root.$data.store.getConnectorData();
            // Connector duplication: don't load type parameters because they are provided by the root connector configuration
            if (connectorData.connectorConfiguration != null) {
                this.connectorConfiguration = connectorData.connectorConfiguration;
                this.loadCategories().then(() => {
                    this.loadTypesOfCategory(this.connectorConfiguration.category, false)
                });
                this.originalConnectorConfiguration = JSON.stringify(connectorData.connectorConfiguration)
            } else {
                // Load a connector configuration from the backend
                if (this.connectorId != null) {
                    this.loadConnector(this.connectorId).then(() => {
                        this.originalConnectorConfiguration = JSON.stringify(this.connectorConfiguration);
                        this.loadReferencingJobs(0)
                    })
                } else {
                    // Create a new connector from within a job configuration. The categories and types are fixed since the job
                    // requires a certain set of connectors.
                    let jobData = this.$root.$data.store.getJobData();
                    if (jobData.connectorCategoryCandidates != null) {
                        for (let i = this.connectorCategories.length; i > 0; i--) {
                            this.connectorCategories.pop()
                        }
                        let component = this;
                        Array.from(jobData.connectorCategoryCandidates).forEach(function (item) {
                            component.connectorCategories.push(item)
                        });
                        this.connectorConfiguration.category = this.connectorCategories[0];
                        component.loadTypesOfCategory(component.connectorConfiguration.category, true).then(() => {
                            component.loadParametersOfType(component.connectorConfiguration.type.key).then(() => {
                                component.originalConnectorConfiguration = JSON.stringify(component.connectorConfiguration)
                            })
                        })
                    } else {
                        // Create a new connector without restrictions:
                        let component = this;
                        this.loadCategories().then(() => {
                            component.loadTypesOfCategory(component.connectorConfiguration.category, true).then(() => {
                                component.loadParametersOfType(component.connectorConfiguration.type.key).then(() => {
                                    component.originalConnectorConfiguration = JSON.stringify(component.connectorConfiguration)
                                })
                            })
                        })
                    }
                }
            }
        },
        beforeRouteLeave(to, from, next) {
            if (this.originalConnectorConfiguration) {
                let newConnectorConfiguration = JSON.stringify(this.connectorConfiguration);
                if (this.originalConnectorConfiguration !== newConnectorConfiguration) {
                    this.nextRoute = next;
                    this.showUnsavedValuesExistDialog = true;
                    return
                }
            }
            next();
        }
    }
</script>

<style scoped>

    .width-restricted {
        max-width: 500px;
    }

    .list-label {
        margin-bottom: 5px;
        display: inline-block;
    }

    .panel .validation-error {
        background-color: var(--color-alert);
    }

    ::placeholder { /* Chrome, Firefox, Opera, Safari 10.1+ */
        color: var(--color-font);
        opacity: 1; /* Firefox */
    }

    pre {
        padding: 10px;
        max-height: calc(100vh / 2);
        height: auto;
        overflow: auto;
        word-break: normal !important;
        word-wrap: normal !important;
        white-space: pre !important;
        background-color: var(--color-alert)
    }

</style>