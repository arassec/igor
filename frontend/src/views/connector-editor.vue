<template>
    <core-container>
        <side-menu>
            <template v-slot:title>
                <font-awesome-icon icon="link" class="margin-right fa-fw" />
                Connector Configuration
            </template>
            <template v-slot:header>
                <layout-row>
                    <template v-slot:left>
                        <input-button
                            v-on:clicked="returnToJobConfiguration()"
                            icon="arrow-left"
                            class="margin-right"
                            :disabled="!inJobConfiguration"
                            data-e2e="connector-editor-return-to-job"
                        />
                        <input-button v-on:clicked="saveConfiguration()" icon="save" data-e2e="connector-editor-save" />
                    </template>
                    <template v-slot:right>
                        <input-button v-on:clicked="testConfiguration()" icon="plug" data-e2e="connector-editor-test" />
                    </template>
                </layout-row>
            </template>
            <template v-slot:footer>
                <core-panel class="spacer-top">
                    <template
                        v-if="referencingJobsPage && referencingJobsPage.items && referencingJobsPage.items.length > 0"
                    >
                        <label class="list-label">Used by the following jobs:</label>
                        <feedback-box
                            v-for="(referencingJob, index) in referencingJobsPage.items"
                            :key="index"
                            class="list-entry"
                            :clickable="true"
                            v-on:feedback-clicked="editJob(referencingJob.key)"
                        >
                            <template v-slot:left>
                                <div class="max-width truncate">
                                    {{ referencingJob.value }}
                                </div>
                            </template>
                        </feedback-box>
                        <list-pager
                            :page="referencingJobsPage"
                            v-if="referencingJobsPage.totalPages > 1"
                            v-on:first="loadReferencingJobs(0)"
                            v-on:previous="loadReferencingJobs(referencingJobsPage.number - 1)"
                            v-on:next="loadReferencingJobs(referencingJobsPage.number + 1)"
                            v-on:last="loadReferencingJobs(referencingJobsPage.totalPages - 1)"
                        />
                    </template>
                    <label
                        v-if="
                            !referencingJobsPage || !referencingJobsPage.items || referencingJobsPage.items.length === 0
                        "
                        >No jobs are using this connector.</label
                    >
                </core-panel>
            </template>
        </side-menu>

        <core-content class="configurator fixed-width">
            <div class="max-width" data-e2e="connector-configurator">
                <core-panel>
                    <layout-row>
                        <template v-slot:left>
                            <h1 class="truncate max-width" data-e2e="connector-name-heading">
                                <font-awesome-icon icon="link" />
                                {{
                                    connectorConfiguration.name.length > 0
                                        ? connectorConfiguration.name
                                        : "Unnamed Connector"
                                }}
                            </h1>
                        </template>
                        <template v-slot:right>
                            <icon-button
                                icon="question"
                                v-on:clicked="openDocumentation('connector')"
                                data-e2e="main-help-button"
                            />
                        </template>
                    </layout-row>
                    <div class="table">
                        <div class="tr">
                            <div class="td">
                                <label for="connector-name">Name</label>
                            </div>
                            <div class="td">
                                <input-validated
                                    id="connector-name"
                                    :type="'text'"
                                    :model-value="connectorConfiguration.name"
                                    :parent-id="connectorConfiguration.id"
                                    :property-id="'name'"
                                    :validation-errors="validationErrors"
                                    @input="connectorConfiguration.name = $event"
                                    data-e2e="connector-name-input"
                                />
                            </div>
                        </div>
                    </div>
                </core-panel>

                <core-panel class="spacer-top">
                    <layout-row>
                        <template v-slot:left>
                            <h2>Connector</h2>
                        </template>
                        <template v-slot:right>
                            <icon-button
                                icon="question"
                                v-show="hasDocumentation(connectorConfiguration.type.key)"
                                v-on:clicked="openDocumentation(connectorConfiguration.type.key)"
                                data-e2e="secondary-help-button"
                            />
                        </template>
                    </layout-row>
                    <div class="table">
                        <div class="tr">
                            <div class="td">
                                <label for="category-input">Category</label>
                            </div>
                            <div class="td">
                                <select
                                    id="category-input"
                                    v-model="connectorConfiguration.category"
                                    v-on:change="
                                        loadTypesOfCategory(connectorConfiguration.category, true).then(() => {
                                            loadParametersOfType(connectorConfiguration.type.key);
                                        })
                                    "
                                    :disabled="!newConnector"
                                    data-e2e="category-selector"
                                >
                                    <option
                                        v-for="category in connectorCategories"
                                        v-bind:value="category"
                                        v-bind:key="category.key"
                                    >
                                        {{ category.value }}
                                    </option>
                                </select>
                            </div>
                        </div>
                        <div class="tr">
                            <div class="td">
                                <label for="type-input">Type</label>
                            </div>
                            <div class="td">
                                <select
                                    id="type-input"
                                    v-model="connectorConfiguration.type"
                                    v-on:change="loadParametersOfType(connectorConfiguration.type.key)"
                                    :disabled="!newConnector"
                                    data-e2e="type-selector"
                                >
                                    <option v-for="type in connectorTypes" v-bind:value="type" v-bind:key="type.key">
                                        {{ type.value }}
                                    </option>
                                </select>
                            </div>
                        </div>
                    </div>
                </core-panel>

                <core-panel class="spacer-top">
                    <h2>Connector Parameters</h2>
                    <parameter-editor
                        v-if="Object.keys(connectorConfiguration.parameters).length > 0"
                        :parent-id="connectorConfiguration.id"
                        :validation-errors="validationErrors"
                        :parameters="connectorConfiguration.parameters"
                        ref="parameterEditor"
                    />
                    <p v-if="Object.keys(connectorConfiguration.parameters).length === 0">
                        This connector has no parameters to configure.
                    </p>
                </core-panel>
            </div>
        </core-content>

        <transition
            v-on:after-leave="blendInDocumentation"
            name="animate-css-transition"
            enter-active-class="animated slideInRight"
            leave-active-class="animated slideOutRight"
        >
            <connectiontest-result-container
                :test-error="testError"
                v-if="showTestDetails"
                v-on:close="closeTestDetails"
            />
        </transition>

        <transition
            v-on:after-leave="blendInTestDetails"
            name="animate-css-transition"
            enter-active-class="animated slideInRight"
            leave-active-class="animated slideOutRight"
        >
            <documentation-container
                :documentation="documentation"
                v-show="showDocumentation"
                v-on:close="closeDocumentation"
            />
        </transition>

        <modal-dialog v-if="showUnsavedValuesExistDialog" @close="showUnsavedValuesExistDialog = false">
            <template v-slot:header>
                <h1>Unsaved configuration</h1>
            </template>
            <template v-slot:body>
                <p>There are unsaved configuration changes.<br /><br />Do you really want to leave?</p>
            </template>
            <template v-slot:footer>
                <layout-row>
                    <template v-slot:left>
                        <input-button v-on:clicked="showUnsavedValuesExistDialog = false" icon="times" />
                    </template>
                    <template v-slot:right>
                        <input-button v-on:clicked="nextRoute()" icon="check" />
                    </template>
                </layout-row>
            </template>
        </modal-dialog>

        <background-icon icon="network-wired" />
    </core-container>
</template>

<script>
import ParameterEditor from "@/components/common/parameter-editor.vue";
import InputButton from "@/components/common/input-button.vue";
import CorePanel from "@/components/common/core-panel.vue";
import CoreContainer from "@/components/common/core-container.vue";
import CoreContent from "@/components/common/core-content.vue";
import LayoutRow from "@/components/common/layout-row.vue";
import SideMenu from "@/components/common/side-menu.vue";
import IgorBackend from "@/utils/igor-backend.js";
import BackgroundIcon from "@/components/common/background-icon.vue";
import ModalDialog from "@/components/common/modal-dialog.vue";
import FeedbackBox from "@/components/common/feedback-box.vue";
import ListPager from "@/components/common/list-pager.vue";
import IconButton from "@/components/common/icon-button.vue";
import DocumentationContainer from "@/components/common/documentation-container.vue";
import Utils from "@/utils/utils";
import InputValidated from "@/components/common/input-validated.vue";
import ConnectiontestResultContainer from "@/components/connectors/connectiontest-result-container.vue";
import { useJobDataStore } from "@/stores/jobdata";
import { useConnectorDataStore } from "@/stores/connectordata";

export default {
    name: "connector-editor",
    components: {
        CoreContainer,
        ConnectiontestResultContainer,
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
        CorePanel,
        InputButton,
        ParameterEditor,
    },
    props: ["connectorId"],
    data: function () {
        return {
            newConnector: true,
            connectorCategories: [],
            connectorTypes: [],
            connectorCategoryCandidates: [],
            loadParameters: true,
            originalConnectorConfiguration: null,
            validationErrors: {},
            connectorConfiguration: {
                id: Utils.uuidv4(),
                name: "",
                category: {},
                type: {},
                parameters: {},
            },
            referencingJobsPage: {
                number: 0,
                size: 10,
                totalPages: 0,
                items: [],
            },
            showUnsavedValuesExistDialog: false,
            nextRoute: null,
            shouldShowDocumentation: false,
            showDocumentation: false,
            documentation: null,
            shouldShowTestDetails: false,
            showTestDetails: false,
            testError: null,
        };
    },
    computed: {
        inJobConfiguration: function () {
            return useJobDataStore().getJobData().jobConfiguration != null;
        },
    },
    methods: {
        blendInTestDetails: function () {
            if (this.shouldShowTestDetails) {
                this.showTestDetails = true;
            }
        },
        blendInDocumentation: function () {
            if (this.shouldShowDocumentation) {
                this.showDocumentation = true;
            }
        },
        loadConnector: async function (id) {
            await IgorBackend.getData("/api/connector/" + id)
                .then((connectorConfiguration) => {
                    this.connectorConfiguration = connectorConfiguration;
                    this.connectorCategories.push(this.connectorConfiguration.category);
                    this.connectorTypes.push(this.connectorConfiguration.type);
                    this.newConnector = false;
                })
                .catch((error) => {
                    console.error("Error during backend request: " + error);
                });
        },
        loadCategories: async function () {
            await IgorBackend.getData("/api/category/connector")
                .then((categories) => {
                    for (let i = this.connectorCategories.length; i > 0; i--) {
                        this.connectorCategories.pop();
                    }

                    let component = this;
                    Array.from(categories).forEach(function (category) {
                        if (component.connectorCategoryCandidates.length > 0) {
                            for (const element of component.connectorCategoryCandidates) {
                                if (category.key === element.key) {
                                    component.connectorCategories.push(category);
                                }
                            }
                        } else {
                            component.connectorCategories.push(category);
                        }
                    });

                    if (!("key" in this.connectorConfiguration.category)) {
                        this.connectorConfiguration.category = this.connectorCategories[0];
                    }
                })
                .catch((error) => {
                    console.error("Error during backend request: " + error);
                });
        },
        loadTypesOfCategory: async function (category, selectFirst) {
            await IgorBackend.getData("/api/type/connector/" + category.key)
                .then((types) => {
                    for (let i = this.connectorTypes.length; i > 0; i--) {
                        this.connectorTypes.pop();
                    }
                    let component = this;

                    let connectorTypeCandidates = [];
                    if (this.connectorCategoryCandidates.length > 0) {
                        for (const element of this.connectorCategoryCandidates) {
                            if (category.key === element.key) {
                                connectorTypeCandidates = element.typeCandidates;
                                break;
                            }
                        }
                    }

                    Array.from(types).forEach(function (type) {
                        if (connectorTypeCandidates.length > 0) {
                            for (const element of connectorTypeCandidates) {
                                if (type.key === element.key) {
                                    component.connectorTypes.push(type);
                                }
                            }
                        } else {
                            component.connectorTypes.push(type);
                        }
                    });

                    if (selectFirst) {
                        this.connectorConfiguration.type = this.connectorTypes[0];
                    }
                })
                .catch((error) => {
                    console.error("Error during backend request: " + error);
                });
        },
        loadParametersOfType: async function (typeKey) {
            if (this.hasDocumentation(typeKey)) {
                await this.switchDocumentation(typeKey);
            } else {
                this.showDocumentation = false;
            }
            await IgorBackend.getData("/api/parameters/connector/" + typeKey)
                .then((parameters) => {
                    this.connectorConfiguration.parameters = parameters;
                    this.validationErrors = {};
                })
                .catch((error) => {
                    console.error("Error during backend request: " + error);
                });
        },
        loadReferencingJobs: async function (page) {
            if (this.connectorConfiguration && this.connectorConfiguration.id) {
                this.referencingJobsPage = await IgorBackend.getData(
                    "/api/connector/" +
                        this.connectorConfiguration.id +
                        "/job-references?pageNumber=" +
                        page +
                        "&pageSize=" +
                        this.referencingJobsPage.size
                ).catch((error) => {
                    console.error("Error during backend request: " + error);
                });
            }
        },
        testConfiguration: async function () {
            this.showDocumentation = false;
            this.showTestDetails = false;
            this.testError = null;
            await IgorBackend.postData(
                "/api/connector/test",
                this.connectorConfiguration,
                "Testing connection",
                "Connection OK.",
                "Connection Failed!"
            )
                .then((result) => {
                    this.validationErrors = {};
                    if (result.status === 400) {
                        this.validationErrors = result.data;
                    } else if (result.status === 424 && "generalError" in result.data) {
                        this.testError = result.data["generalError"];
                    }
                    this.showTestDetails = true;
                })
                .catch((error) => {
                    console.error("Error during backend request: " + error);
                });
        },
        saveConfiguration: async function () {
            IgorBackend.postData(
                "/api/connector",
                this.connectorConfiguration,
                "Saving connector",
                "Connector '" + Utils.formatNameForSnackbar(this.connectorConfiguration.name) + "' saved.",
                "Saving failed!"
            )
                .then((result) => {
                    this.validationErrors = {};
                    if (result.status === 400) {
                        this.validationErrors = result.data;
                    } else {
                        this.newConnector = false;
                        this.connectorConfiguration = result.data;
                        this.originalConnectorConfiguration = JSON.stringify(this.connectorConfiguration);
                        let jobData = useJobDataStore().getJobData();
                        if (jobData.jobConfiguration != null) {
                            jobData.connectorParameter = {
                                name: this.connectorConfiguration.name,
                                id: this.connectorConfiguration.id,
                            };
                        }
                    }
                })
                .catch((error) => {
                    console.error("Error during backend request: " + error);
                });
        },
        returnToJobConfiguration: function () {
            let jobData = useJobDataStore().getJobData();
            if (jobData.jobConfiguration != null) {
                this.$router.push({ name: "job-editor" });
            } else {
                this.$router.push({ name: "connector-overview" });
            }
        },
        editJob: function (jobId) {
            this.$router.push({ name: "job-editor", params: { jobId: jobId } });
        },
        hasDocumentation: function (typeId) {
            for (const element of this.connectorTypes) {
                if (element.key === typeId) {
                    return element.documentationAvailable;
                }
            }
            return false;
        },
        openDocumentation: async function (key) {
            this.documentation = await IgorBackend.getData("/api/doc/" + key).catch((error) => {
                console.error("Error during backend request: " + error);
            });
            if (this.showTestDetails) {
                this.showTestDetails = false;
                this.shouldShowDocumentation = true;
            } else {
                this.showDocumentation = true;
            }
        },
        closeDocumentation: function () {
            if (this.testError) {
                this.shouldShowTestDetails = true;
                this.shouldShowDocumentation = false;
                this.showDocumentation = false;
            } else {
                this.showDocumentation = false;
                this.shouldShowDocumentation = false;
            }
        },
        switchDocumentation: async function (key) {
            if (this.showDocumentation) {
                this.documentation = await IgorBackend.getData("/api/doc/" + key).catch((error) => {
                    console.error("Error during backend request: " + error);
                });
            }
        },
        closeTestDetails: function () {
            this.testError = null;
            this.shouldShowTestDetails = false;
            this.showTestDetails = false;
        },
    },
    mounted() {
        let connectorData = useConnectorDataStore().getConnectorData();
        // Connector duplication: don't load type parameters because they are provided by the root connector configuration
        if (connectorData.connectorConfiguration != null) {
            this.connectorConfiguration = connectorData.connectorConfiguration;
            this.loadCategories().then(() => {
                this.loadTypesOfCategory(this.connectorConfiguration.category, false);
            });
            this.originalConnectorConfiguration = JSON.stringify(connectorData.connectorConfiguration);
        } else {
            // Load a connector configuration from the backend
            if (this.connectorId != null && this.connectorId !== "") {
                this.loadConnector(this.connectorId).then(() => {
                    this.originalConnectorConfiguration = JSON.stringify(this.connectorConfiguration);
                    this.loadReferencingJobs(0);
                });
            } else {
                // Create a new connector. The categories and types may be fixed if the connector is created from
                // within a job configuration..
                this.connectorCategoryCandidates = [];
                let jobData = useJobDataStore().getJobData();
                if (jobData.connectorCategoryCandidates != null) {
                    this.connectorCategoryCandidates = jobData.connectorCategoryCandidates;
                }
                let component = this;
                this.loadCategories().then(() => {
                    component.loadTypesOfCategory(component.connectorConfiguration.category, true).then(() => {
                        component.loadParametersOfType(component.connectorConfiguration.type.key).then(() => {
                            component.originalConnectorConfiguration = JSON.stringify(component.connectorConfiguration);
                        });
                    });
                });
            }
        }
    },
    beforeRouteLeave(to, from, next) {
        if (this.originalConnectorConfiguration) {
            let newConnectorConfiguration = JSON.stringify(this.connectorConfiguration);
            if (this.originalConnectorConfiguration !== newConnectorConfiguration) {
                this.nextRoute = next;
                this.showUnsavedValuesExistDialog = true;
                return;
            }
        }
        next();
    },
};
</script>

<style scoped>
.configurator {
    flex-grow: 2;
}

.fixed-width {
    max-width: 35em;
    min-width: 35em;
}

.list-label {
    margin-bottom: 5px;
    display: inline-block;
}

.panel .validation-error {
    background-color: var(--color-alert);
}

::placeholder {
    /* Chrome, Firefox, Opera, Safari 10.1+ */
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
    background-color: var(--color-alert);
}

/* animate.css animation speed */
.animated {
    -webkit-animation-duration: var(--animate-css-duration);
    animation-duration: var(--animate-css-duration);
    -webkit-animation-fill-mode: both;
    animation-fill-mode: both;
}
</style>
