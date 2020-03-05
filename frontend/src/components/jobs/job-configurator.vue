<template>
    <div class="sticky max-width" v-if="jobConfiguration">
        <core-panel>
            <h1 class="truncate">
                <font-awesome-icon icon="toolbox"/>
                {{ jobConfiguration.name.length > 0 ? jobConfiguration.name : 'Unnamed Job' }}
            </h1>
            <table>
                <tr>
                    <td><label>Active</label></td>
                    <td>
                        <font-awesome-icon :icon="jobConfiguration.active ? 'check-square' : 'square'"
                                           v-on:click="jobConfiguration.active = !jobConfiguration.active"/>
                    </td>
                </tr>
                <tr>
                    <td><label>Name*</label></td>
                    <td>
                        <input type="text" autocomplete="off" v-model="jobConfiguration.name"
                               :class="nameValidationError.length > 0 ? 'validation-error' : ''"
                               :placeholder="nameValidationError.length > 0 ? nameValidationError : ''"/>
                    </td>
                </tr>
                <tr v-show="showAdvancedParameters">
                    <td><label for="description-input">Description</label></td>
                    <td>
                        <input id="description-input" type="text" autocomplete="off"
                               v-model="jobConfiguration.description"/>
                    </td>
                </tr>
                <tr v-show="showAdvancedParameters">
                    <td><label for="numexechistory-input">Execution-History Limit</label></td>
                    <td>
                        <input id="numexechistory-input" type="text" autocomplete="off"
                               v-model.number="jobConfiguration.executionHistoryLimit"/>
                    </td>
                </tr>
                <tr v-show="showAdvancedParameters">
                    <td><label for="faulttolerant-input">Fault tolerant</label></td>
                    <td>
                        <font-awesome-icon id="faulttolerant-input" :icon="jobConfiguration.faultTolerant ? 'check-square' : 'square'"
                                           v-on:click="jobConfiguration.faultTolerant = !jobConfiguration.faultTolerant"/>
                    </td>
                </tr>
                <tr>
                    <td>
                        <font-awesome-icon class="arrow"
                                           v-bind:icon="showAdvancedParameters ? 'chevron-up' : 'chevron-down'"
                                           v-on:click="showAdvancedParameters = !showAdvancedParameters"/>
                    </td>
                </tr>
            </table>
        </core-panel>

        <core-panel>
            <h2>Trigger</h2>
            <table>
                <tr>
                    <td><label>Category</label></td>
                    <td>
                        <select v-model="jobConfiguration.trigger.category"
                                v-on:change="loadTypesOfCategory(jobConfiguration.trigger.category.key)">
                            <option v-for="triggerCategory in triggerCategories" v-bind:value="triggerCategory"
                                    v-bind:key="triggerCategory.key">
                                {{triggerCategory.value}}
                            </option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td><label>Type</label></td>
                    <td>
                        <select v-model="jobConfiguration.trigger.type"
                                v-on:change="loadParametersOfType(jobConfiguration.trigger.type.key)">
                            <option v-for="triggerType in triggerTypes" v-bind:value="triggerType"
                                    v-bind:key="triggerType.key">
                                {{triggerType.value}}
                            </option>
                        </select>
                    </td>
                </tr>
            </table>
        </core-panel>

        <core-panel v-if="jobConfiguration.trigger.parameters.length">
            <h2>Trigger Parameters</h2>
            <parameter-editor v-bind:parameters="jobConfiguration.trigger.parameters" ref="parameterEditor"
                              v-on:create-service="createService"/>
        </core-panel>

    </div>
</template>

<script>
    import CorePanel from '../common/core-panel'
    import ParameterEditor from '../common/parameter-editor'
    import IgorBackend from '../../utils/igor-backend.js'

    export default {
        name: 'job-configurator',
        components: {ParameterEditor, CorePanel},
        props: ['jobConfiguration'],
        data: function () {
            return {
                nameValidationError: '',
                showAdvancedParameters: false,
                triggerCategories: [],
                triggerTypes: []
            }
        },
        methods: {
            loadCategories: async function () {
                await IgorBackend.getData('/api/category/trigger').then((categories) => {
                    for (let i = this.triggerCategories.length; i > 0; i--) {
                        this.triggerCategories.pop()
                    }
                    let component = this
                    Array.from(categories).forEach(function (item) {
                        component.triggerCategories.push(item)
                    })
                    if (this.jobConfiguration.trigger.category == null) {
                        this.jobConfiguration.trigger.category = this.triggerCategories[0]
                    }
                })
            },
            loadTypesOfCategory: async function (categoryKey) {
                await IgorBackend.getData('/api/type/' + categoryKey).then((types) => {
                    for (let i = this.triggerTypes.length; i > 0; i--) {
                        this.triggerTypes.pop()
                    }
                    let component = this
                    Array.from(types).forEach(function (item) {
                        component.triggerTypes.push(item)
                    })
                    if (this.jobConfiguration.trigger.type == null) {
                        this.jobConfiguration.trigger.type = component.triggerTypes[0]
                    }
                })
            },
            loadParametersOfType: async function (typeKey) {
                await IgorBackend.getData('/api/parameters/trigger/' + typeKey).then((parameters) => {
                    this.jobConfiguration.trigger.parameters = parameters
                })
            },
            validateInput: async function () {
                this.nameValidationError = '';

                if (this.jobConfiguration.name == null || this.jobConfiguration.name === '') {
                    this.nameValidationError = 'Name must be set'
                } else {
                    let nameAlreadyExists = await IgorBackend.getData('/api/job/check/'
                        + btoa(this.jobConfiguration.name) + '/' + (this.jobConfiguration.id === undefined ? -1 : this.jobConfiguration.id))
                    if (nameAlreadyExists === true) {
                        this.nameValidationError = 'nameAlreadyExists'
                    }
                }

                let parameterValidationResult = true
                if (typeof this.$refs.parameterEditor !== 'undefined') {
                    parameterValidationResult = this.$refs.parameterEditor.validateInput()
                }

                return ((this.nameValidationError.length === 0) && parameterValidationResult)
            },
            setNameValidationError: function (errorMessage) {
                this.nameValidationError = errorMessage
            },
            createService: function (parameterIndex, serviceCategoryCandidates) {
                this.$emit('create-service', this.taskKey, parameterIndex, serviceCategoryCandidates)
            }
        },
        mounted() {
            let loadParameters = (this.jobConfiguration.trigger.type === null)
            this.loadCategories().then(() => {
                this.loadTypesOfCategory(this.jobConfiguration.trigger.category.key).then(() => {
                    if (loadParameters) {
                        this.loadParametersOfType(this.jobConfiguration.trigger.type.key).then(() => {
                            this.$emit('update-original-job-configuration')
                        })
                    }
                })
            })
        }
    }
</script>

<style scoped>

    .arrow:hover {
        cursor: pointer;
    }

    .panel .validation-error {
        background-color: var(--alert-background-color);
    }

    ::placeholder { /* Chrome, Firefox, Opera, Safari 10.1+ */
        color: var(--main-background-color);
        opacity: 1; /* Firefox */
    }

</style>
