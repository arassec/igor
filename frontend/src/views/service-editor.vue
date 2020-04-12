<template>
    <core-container>

        <side-menu>
            <p slot="title">Service Configuration</p>
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
             === 0">No jobs are using this service.</label>
            </core-panel>
        </side-menu>

        <core-content>
            <core-panel>
                <h1 class="truncate width-restricted">
                    <font-awesome-icon icon="cogs"/>
                    {{ serviceConfiguration.name.length > 0 ? serviceConfiguration.name : 'Unnamed Service' }}
                </h1>
                <table>
                    <tr>
                        <td><label>Name</label></td>
                        <td><input type="text" autocomplete="off"
                                   v-model="serviceConfiguration.name"
                                   :class="nameValidationError.length > 0 ? 'validation-error' : ''"
                                   :placeholder="nameValidationError.length > 0 ? nameValidationError : ''"/>
                        </td>
                    </tr>
                    <tr>
                        <td><label for="category-input">Category</label></td>
                        <td>
                            <select id="category-input" v-model="serviceConfiguration.category"
                                    v-on:change="loadTypesOfCategory(serviceConfiguration.category, true).then(() => {
                                        loadParametersOfType(serviceConfiguration.type.key)})" :disabled="!newService">
                                <option v-for="category in serviceCategories" v-bind:value="category"
                                        v-bind:key="category.key">
                                    {{category.value}}
                                </option>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td><label for="type-input">Type</label></td>
                        <td>
                            <select id="type-input" v-model="serviceConfiguration.type"
                                    v-on:change="loadParametersOfType(serviceConfiguration.type.key)"
                                    :disabled="!newService">
                                <option v-for="type in serviceTypes" v-bind:value="type" v-bind:key="type.key">
                                    {{type.value}}
                                </option>
                            </select>
                        </td>
                    </tr>
                </table>
            </core-panel>

            <core-panel>
                <h2>Service Parameters</h2>
                <parameter-editor v-if="Object.keys(serviceConfiguration.parameters).length > 0"
                                  :parameters="serviceConfiguration.parameters" ref="parameterEditor"/>
                <p v-if="Object.keys(serviceConfiguration.parameters).length === 0">
                    This service has no parameters to configure.
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

        <background-icon right="true" icon-one="cogs"/>

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
    import {store} from "../main";

    export default {
        name: 'service-editor',
        components: {
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
        props: ['serviceId'],
        data: function () {
            return {
                newService: true,
                serviceCategories: [],
                serviceTypes: [],
                nameValidationError: '',
                loadParameters: true,
                originalServiceConfiguration: null,
                serviceConfiguration: {
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
                nextRoute: null
            }
        },
        methods: {
            loadService: async function (id) {
                await IgorBackend.getData('/api/service/' + id).then((serviceConfiguration) => {
                    this.serviceConfiguration = serviceConfiguration;
                    this.serviceCategories.push(this.serviceConfiguration.category);
                    this.serviceTypes.push(this.serviceConfiguration.type);
                    this.newService = false
                })
            },
            loadCategories: async function () {
                await IgorBackend.getData('/api/category/service').then((categories) => {
                    for (let i = this.serviceCategories.length; i > 0; i--) {
                        this.serviceCategories.pop()
                    }
                    let component = this;
                    Array.from(categories).forEach(function (item) {
                        component.serviceCategories.push(item)
                    });
                    if (!('key' in this.serviceConfiguration.category)) {
                        this.serviceConfiguration.category = this.serviceCategories[0]
                    }
                })
            },
            loadTypesOfCategory: async function (category, selectFirst) {
                // If the category contains an array with name 'typeCandidates' the allowed types are pre-selected, because
                // the service is created from within a job configuration:
                if (category.hasOwnProperty('typeCandidates')) {
                    for (let i = this.serviceTypes.length; i > 0; i--) {
                        this.serviceTypes.pop()
                    }
                    let component = this;
                    Array.from(category.typeCandidates).forEach(function (item) {
                        component.serviceTypes.push(item)
                    });
                    if (selectFirst) {
                        this.serviceConfiguration.type = this.serviceTypes[0]
                    }
                } else {
                    // No restrictions, simply load all types of the given category:
                    await IgorBackend.getData('/api/type/' + category.key).then((types) => {
                        for (let i = this.serviceTypes.length; i > 0; i--) {
                            this.serviceTypes.pop()
                        }
                        let component = this;
                        Array.from(types).forEach(function (item) {
                            component.serviceTypes.push(item)
                        });
                        if (selectFirst) {
                            this.serviceConfiguration.type = this.serviceTypes[0]
                        }
                    })
                }
            },
            loadParametersOfType: async function (typeKey) {
                await IgorBackend.getData('/api/parameters/service/' + typeKey).then((parameters) => {
                    this.serviceConfiguration.parameters = parameters
                })
            },
            loadReferencingJobs: async function (page) {
                if (this.serviceConfiguration && this.serviceConfiguration.id) {
                    this.referencingJobsPage = await IgorBackend.getData('/api/service/' + this.serviceConfiguration.id +
                        '/job-references?pageNumber=' + page + '&pageSize=' + this.referencingJobsPage.size)
                }
            },
            testConfiguration: async function () {
                if (!(await this.validateInput())) {
                    return
                }
                await IgorBackend.postData('/api/service/test', this.serviceConfiguration, 'Testing service', 'Test OK.', 'Testing Failed!')
            },
            saveConfiguration: async function () {
                if (!(await this.validateInput())) {
                    return
                }
                if (this.newService) {
                    IgorBackend.postData('/api/service', this.serviceConfiguration, 'Saving service',
                        'Service \'' + FormatUtils.formatNameForSnackbar(this.serviceConfiguration.name) + '\' saved.',
                        'Saving failed!').then((result) => {
                        if (result === 'NAME_ALREADY_EXISTS_ERROR') {
                            this.nameValidationError = 'nameAlreadyExists'
                        } else {
                            this.serviceConfiguration = result;
                            this.newService = false;
                            this.originalServiceConfiguration = JSON.stringify(this.serviceConfiguration);
                            this.$router.push({
                                name: 'service-editor',
                                params: {serviceId: this.serviceConfiguration.id}
                            });
                            let jobData = this.$root.$data.store.getJobData();
                            if (jobData.jobConfiguration != null) {
                                jobData.serviceParameter = {
                                    name: this.serviceConfiguration.name,
                                    id: this.serviceConfiguration.id
                                }
                            }
                        }
                    })
                } else {
                    IgorBackend.putData('/api/service', this.serviceConfiguration, 'Saving service',
                        'Service \'' + FormatUtils.formatNameForSnackbar(this.serviceConfiguration.name) + '\' updated.',
                        'Saving failed!').then(() => {
                        this.originalServiceConfiguration = JSON.stringify(this.serviceConfiguration)
                    })
                }
            },
            cancelConfiguration: function () {
                let jobData = this.$root.$data.store.getJobData();
                if (jobData.jobConfiguration != null) {
                    this.$router.push({name: 'job-editor'})
                } else {
                    this.$router.push({name: 'service-overview'})
                }
            },
            validateInput: async function () {
                this.nameValidationError = '';

                if (this.serviceConfiguration.name == null || this.serviceConfiguration.name === '') {
                    this.nameValidationError = 'Name must be set'
                } else {
                    let nameAlreadyExists = await IgorBackend.getData('/api/service/check/'
                        + btoa(this.serviceConfiguration.name) + '/' + (this.serviceConfiguration.id === undefined ? -1 : this.serviceConfiguration.id));
                    if (nameAlreadyExists === true) {
                        store.setFeedback('A service with this name already exists!', true)
                        this.nameValidationError = 'nameAlreadyExists'
                    }
                }

                let parameterValidationResult = true;
                if (typeof this.$refs.parameterEditor !== 'undefined') {
                    parameterValidationResult = this.$refs.parameterEditor.validateInput()
                }

                return ((this.nameValidationError.length === 0) && parameterValidationResult)
            },
            editJob: function (jobId) {
                this.$router.push({name: 'job-editor', params: {jobId: jobId}})
            },
        },
        mounted() {
            let serviceData = this.$root.$data.store.getServiceData();
            // Service duplication: don't load type parameters because they are provided by the root service configuration
            if (serviceData.serviceConfiguration != null) {
                this.serviceConfiguration = serviceData.serviceConfiguration;
                this.loadCategories().then(() => {
                    this.loadTypesOfCategory(this.serviceConfiguration.category, false)
                });
                this.originalServiceConfiguration = JSON.stringify(serviceData.serviceConfiguration)
            } else {
                // Load a service configuration from the backend
                if (this.serviceId != null) {
                    this.loadService(this.serviceId).then(() => {
                        this.originalServiceConfiguration = JSON.stringify(this.serviceConfiguration);
                        this.loadReferencingJobs(0)
                    })
                } else {
                    // Create a new service from within a job configuration. The categories and types are fixed since the job
                    // requires a certain set of services.
                    let jobData = this.$root.$data.store.getJobData();
                    if (jobData.serviceCategoryCandidates != null) {
                        for (let i = this.serviceCategories.length; i > 0; i--) {
                            this.serviceCategories.pop()
                        }
                        let component = this;
                        Array.from(jobData.serviceCategoryCandidates).forEach(function (item) {
                            component.serviceCategories.push(item)
                        });
                        this.serviceConfiguration.category = this.serviceCategories[0];
                        component.loadTypesOfCategory(component.serviceConfiguration.category, true).then(() => {
                            component.loadParametersOfType(component.serviceConfiguration.type.key).then(() => {
                                component.originalServiceConfiguration = JSON.stringify(component.serviceConfiguration)
                            })
                        })
                    } else {
                        // Create a new service without restrictions:
                        let component = this;
                        this.loadCategories().then(() => {
                            component.loadTypesOfCategory(component.serviceConfiguration.category, true).then(() => {
                                component.loadParametersOfType(component.serviceConfiguration.type.key).then(() => {
                                    component.originalServiceConfiguration = JSON.stringify(component.serviceConfiguration)
                                })
                            })
                        })
                    }
                }
            }
        },
        beforeRouteLeave(to, from, next) {
            if (this.originalServiceConfiguration) {
                let newServiceConfiguration = JSON.stringify(this.serviceConfiguration);
                if (this.originalServiceConfiguration !== newServiceConfiguration) {
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

</style>
