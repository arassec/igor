<template>
    <div class="sticky max-width" v-if="jobConfiguration">

        <core-panel>
            <layout-row>
                <h1 slot="left" class="truncate">
                    <font-awesome-icon icon="toolbox" class="margin-right"/>
                    {{ jobConfiguration.name.length > 0 ? jobConfiguration.name : 'Job' }}
                </h1>
                <icon-button slot="right" icon="question" v-on:clicked="$emit('open-documentation', 'job')"/>
            </layout-row>
            <div class="table">
                <div class="tr">
                    <div class="td"><label>Active</label></div>
                    <div class="td align-left">
                        <font-awesome-icon :icon="jobConfiguration.active ? 'check-square' : 'square'"
                                           v-on:click="jobConfiguration.active = !jobConfiguration.active"/>
                    </div>
                </div>
                <div class="tr">
                    <div class="td"><label for="name">Name*</label></div>
                    <div class="td">
                        <input-validated id="name" :type="'text'" v-model="jobConfiguration.name"
                            :parent-id="jobConfiguration.id" :property-id="'name'" :validation-errors="validationErrors"/>
                    </div>
                </div>
                <div class="tr" v-bind:style="!showAdvancedParameters ? 'visibility: collapse' : ''">
                    <div class="td"><label for="description-input">Description</label></div>
                    <div class="td">
                        <input id="description-input" type="text" autocomplete="off"
                               v-model="jobConfiguration.description"/>
                    </div>
                </div>
                <div class="tr" v-bind:style="!showAdvancedParameters ? 'visibility: collapse' : ''">
                    <div class="td"><label for="numexechistory-input">History Limit</label></div>
                    <div class="td">
                        <input id="numexechistory-input" type="text" autocomplete="off"
                               v-model.number="jobConfiguration.historyLimit"/>
                    </div>
                </div>
                <div class="tr" v-bind:style="!showAdvancedParameters ? 'visibility: collapse' : ''">
                    <div class="td"><label for="faulttolerant-input">Fault tolerant</label></div>
                    <div class="td align-left">
                        <font-awesome-icon id="faulttolerant-input"
                                           :icon="jobConfiguration.faultTolerant ? 'check-square' : 'square'"
                                           v-on:click="jobConfiguration.faultTolerant = !jobConfiguration.faultTolerant"/>
                    </div>
                </div>
                <div class="tr">
                    <div class="td align-left">
                        <font-awesome-icon class="arrow"
                                           v-bind:icon="showAdvancedParameters ? 'chevron-up' : 'chevron-down'"
                                           v-on:click="showAdvancedParameters = !showAdvancedParameters"/>
                    </div>
                </div>
            </div>
        </core-panel>

        <core-panel>
            <layout-row>
                <h2 slot="left">Trigger</h2>
                <icon-button slot="right" icon="question" v-show="hasDocumentation(jobConfiguration.trigger.type)"
                             v-on:clicked="$emit('open-documentation', jobConfiguration.trigger.type.key)"/>
            </layout-row>
            <div class="table">
                <div class="tr">
                    <div class="td"><label for="category">Category</label></div>
                    <div class="td">
                        <select id="category" v-model="jobConfiguration.trigger.category"
                                v-on:change="loadTypesOfCategory(jobConfiguration.trigger.category.key)">
                            <option v-for="triggerCategory in triggerCategories" v-bind:value="triggerCategory"
                                    v-bind:key="triggerCategory.key">
                                {{triggerCategory.value}}
                            </option>
                        </select>
                    </div>
                </div>
                <div class="tr">
                    <div class="td"><label for="type">Type</label></div>
                    <div class="td">
                        <select id="type" v-model="jobConfiguration.trigger.type"
                                v-on:change="loadParametersOfType(jobConfiguration.trigger.type.key)">
                            <option v-for="triggerType in triggerTypes" v-bind:value="triggerType"
                                    v-bind:key="triggerType.key">
                                {{triggerType.value}}
                            </option>
                        </select>
                    </div>
                </div>
            </div>
        </core-panel>

        <core-panel v-if="jobConfiguration.trigger.parameters.length">
            <h2>Trigger Configuration</h2>
            <parameter-editor
                    :parent-id="jobConfiguration.trigger.id"
                    :validation-errors="validationErrors"
                    :parameters="jobConfiguration.trigger.parameters"
                    v-on:create-connector="createConnector"/>
        </core-panel>

    </div>
</template>

<script>
    import CorePanel from '../common/core-panel'
    import ParameterEditor from '../common/parameter-editor'
    import IgorBackend from '../../utils/igor-backend.js'
    import IconButton from "../common/icon-button";
    import LayoutRow from "../common/layout-row";
    import InputValidated from "../common/input-validated";

    export default {
        name: 'job-configurator',
        components: {InputValidated, LayoutRow, IconButton, ParameterEditor, CorePanel},
        props: ['jobConfiguration', 'validationErrors'],
        data: function () {
            return {
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
                    let component = this;
                    Array.from(categories).forEach(function (item) {
                        component.triggerCategories.push(item)
                    });
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
                    let component = this;
                    Array.from(types).forEach(function (item) {
                        component.triggerTypes.push(item)
                    });
                    if (this.jobConfiguration.trigger.type == null) {
                        this.jobConfiguration.trigger.type = component.triggerTypes[0]
                    }
                })
            },
            loadParametersOfType: async function (typeKey) {
                if (this.hasDocumentation(typeKey)) {
                    this.$emit('switch-documentation', typeKey);
                } else {
                    this.$emit('close-documentation');
                }
                await IgorBackend.getData('/api/parameters/trigger/' + typeKey).then((parameters) => {
                    this.jobConfiguration.trigger.parameters = parameters
                })
            },
            createConnector: function (parameterIndex, connectorCategoryCandidates) {
                this.$emit('create-connector', this.taskKey, parameterIndex, connectorCategoryCandidates)
            },
            hasDocumentation: function (type) {
                if (type == null) {
                    return false;
                }
                for (let i = 0; i < this.triggerTypes.length; i++) {
                    if (this.triggerTypes[i].key === type.key) {
                        return this.triggerTypes[i].documentationAvailable;
                    }
                }
                return false;
            }
        },
        mounted() {
            let loadParameters = (this.jobConfiguration.trigger.type === null);
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

</style>
