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
                                         :parent-id="jobConfiguration.id" :property-id="'name'"
                                         :validation-errors="validationErrors"/>
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
                <icon-button slot="right" icon="question"
                             v-show="hasTriggerDocumentation(jobConfiguration.trigger.type.key)"
                             v-on:clicked="$emit('open-documentation', jobConfiguration.trigger.type.key)"/>
            </layout-row>
            <div class="table margin-bottom">
                <div class="tr">
                    <div class="td"><label for="category">Category</label></div>
                    <div class="td">
                        <select id="category" v-model="jobConfiguration.trigger.category"
                                v-on:change="loadTriggerTypesOfCategory(jobConfiguration.trigger.category.key, true).then(() => {
                                        loadTriggerParametersOfType(jobConfiguration.trigger.type.key)})">
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
                                v-on:change="loadTriggerParametersOfType(jobConfiguration.trigger.type.key)">
                            <option v-for="triggerType in triggerTypes" v-bind:value="triggerType"
                                    v-bind:key="triggerType.key">
                                {{triggerType.value}}
                            </option>
                        </select>
                    </div>
                </div>
            </div>

            <div v-if="jobConfiguration.trigger.parameters.length">
                <parameter-editor
                        :parent-id="jobConfiguration.trigger.id"
                        :validation-errors="validationErrors"
                        :parameters="jobConfiguration.trigger.parameters"
                        v-on:create-connector="createConnector"/>
            </div>
        </core-panel>

        <core-panel>
            <layout-row>
                <h2 slot="left">Provider</h2>
                <icon-button slot="right" icon="question"
                             v-show="hasProviderDocumentation(jobConfiguration.provider.type.key)"
                             v-on:clicked="$emit('open-documentation', jobConfiguration.provider.type.key)"/>
            </layout-row>
            <div class="table margin-bottom">
                <div class="tr" v-if="providerCategories.length > 1">
                    <div class="td"><label for="category-selection">Category</label></div>
                    <div class="td">
                        <select id="category-selection" v-model="jobConfiguration.provider.category"
                                v-on:change="loadProviderTypesOfCategory(jobConfiguration.provider.category.key, true).then(() => {
                                        loadProviderParametersOfType(jobConfiguration.provider.type.key)})">
                            <option v-for="providerCategory in providerCategories" v-bind:value="providerCategory"
                                    v-bind:key="providerCategory.key">
                                {{providerCategory.value}}
                            </option>
                        </select>
                    </div>
                </div>
                <div class="tr">
                    <div class="td"><label for="type-selection">Type</label></div>
                    <div class="td">
                        <select id="type-selection" v-model="jobConfiguration.provider.type"
                                v-on:change="loadProviderParametersOfType(jobConfiguration.provider.type.key)">
                            <option v-for="providerType in providerTypes" v-bind:value="providerType"
                                    v-bind:key="providerType.key">
                                {{providerType.value}}
                            </option>
                        </select>
                    </div>
                </div>
            </div>

            <div v-if="jobConfiguration.provider.parameters.length">
                <parameter-editor
                        :parent-id="jobConfiguration.provider.id"
                        :validation-errors="validationErrors"
                        :parameters="jobConfiguration.provider.parameters"
                        v-on:create-connector="createConnector"/>
            </div>
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
                triggerTypes: [],
                providerCategories: [],
                providerTypes: []
            }
        },
        methods: {
            loadTriggerCategories: async function () {
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
            loadTriggerTypesOfCategory: async function (categoryKey, selectFirst) {
                await IgorBackend.getData('/api/type/' + categoryKey).then((types) => {
                    for (let i = this.triggerTypes.length; i > 0; i--) {
                        this.triggerTypes.pop()
                    }
                    let component = this;
                    Array.from(types).forEach(function (item) {
                        component.triggerTypes.push(item)
                    });
                    if (selectFirst) {
                        this.jobConfiguration.trigger.type = this.triggerTypes[0]
                    }
                })
            },
            loadTriggerParametersOfType: async function (typeKey) {
                if (this.hasTriggerDocumentation(typeKey)) {
                    this.$emit('switch-documentation', typeKey);
                } else {
                    this.$emit('close-documentation');
                }
                await IgorBackend.getData('/api/parameters/trigger/' + typeKey).then((parameters) => {
                    this.jobConfiguration.trigger.parameters = parameters
                })
            },
            loadProviderCategories: async function () {
                await IgorBackend.getData('/api/category/provider').then((categories) => {
                    for (let i = this.providerCategories.length; i > 0; i--) {
                        this.providerCategories.pop()
                    }
                    let component = this;
                    Array.from(categories).forEach(function (item) {
                        component.providerCategories.push(item)
                    })
                    if (this.jobConfiguration.provider.category == null) {
                        this.jobConfiguration.provider.category = this.providerCategories[0]
                    }
                })
            },
            loadProviderTypesOfCategory: async function (categoryKey, selectFirst) {
                await IgorBackend.getData('/api/type/' + categoryKey).then((types) => {
                    for (let i = this.providerTypes.length; i > 0; i--) {
                        this.providerTypes.pop()
                    }
                    let component = this;
                    Array.from(types).forEach(function (item) {
                        component.providerTypes.push(item)
                    });
                    if (selectFirst) {
                        this.jobConfiguration.provider.type = this.providerTypes[0]
                    }
                })
            },
            loadProviderParametersOfType: function (typeKey) {
                if (this.hasProviderDocumentation(typeKey)) {
                    this.$emit('switch-documentation', typeKey);
                } else {
                    this.$emit('close-documentation');
                }
                IgorBackend.getData('/api/parameters/provider/' + typeKey).then((parameters) => {
                    this.jobConfiguration.provider.parameters = parameters
                })
            },
            createConnector: function (componentId, parameterIndex, connectorCategoryCandidates) {
                this.$emit('create-connector', componentId, parameterIndex, connectorCategoryCandidates)
            },
            hasTriggerDocumentation: function (typeKey) {
                if (!typeKey) {
                    return false;
                }
                for (let i = 0; i < this.triggerTypes.length; i++) {
                    if (this.triggerTypes[i].key === typeKey) {
                        return this.triggerTypes[i].documentationAvailable;
                    }
                }
                return false;
            },
            hasProviderDocumentation: function (typeId) {
                for (let i = 0; i < this.providerTypes.length; i++) {
                    if (this.providerTypes[i].key === typeId) {
                        return this.providerTypes[i].documentationAvailable;
                    }
                }
                return false;
            }
        },
        async mounted() {
            this.loadTriggerCategories().then(() => {
                this.loadTriggerTypesOfCategory(this.jobConfiguration.trigger.category.key, false).then(() => {
                    this.loadProviderCategories().then(() => {
                        this.loadProviderTypesOfCategory(this.jobConfiguration.provider.category.key, false)
                    })
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
