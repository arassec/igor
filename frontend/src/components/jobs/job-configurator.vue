<template>
    <div class="sticky max-width" v-if="jobConfiguration" data-e2e="job-configurator">

        <core-panel>
            <layout-row>
                <h1 slot="left" class="truncate" data-e2e="job-title">
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
                                           v-on:click="jobConfiguration.active = !jobConfiguration.active"
                                           data-e2e="job-active-button"/>
                    </div>
                </div>
                <div class="tr">
                    <div class="td"><label for="name">Name*</label></div>
                    <div class="td">
                        <input-validated id="name" :type="'text'" v-model="jobConfiguration.name"
                                         :parent-id="jobConfiguration.id" :property-id="'name'"
                                         :validation-errors="validationErrors"
                                         data-e2e="job-name-input"/>
                    </div>
                </div>
                <div class="tr" v-bind:style="!showAdvancedParameters ? 'visibility: collapse' : ''">
                    <div class="text-top td"><label for="description-input">Description</label></div>
                    <div class="td">
                        <textarea rows="8" cols="35" id="description-input" autocomplete="off"
                                  v-model="jobConfiguration.description" class="textarea"/>
                    </div>
                </div>
                <div class="tr" v-bind:style="!showAdvancedParameters ? 'visibility: collapse' : ''">
                    <div class="td"><label for="numexechistory-input">History Limit</label></div>
                    <div class="td">
                        <input-validated id="numexechistory-input" :type="'text'"
                                         :parent-id="jobConfiguration.id" :property-id="'historyLimit'"
                                         :validation-errors="validationErrors"
                                         v-model.number="jobConfiguration.historyLimit"/>
                    </div>
                </div>
                <div class="tr" v-bind:style="!showAdvancedParameters ? 'visibility: collapse' : ''">
                    <div class="td"><label for="numsimlimit-input">Simulation Limit</label></div>
                    <div class="td">
                        <input-validated id="numsimlimit-input" :type="text"
                                         :parent-id="jobConfiguration.id" :property-id="'simulationLimit'"
                                         :validation-errors="validationErrors"
                                         v-model.number="jobConfiguration.simulationLimit"/>
                    </div>
                </div>
                <div class="tr" v-bind:style="!showAdvancedParameters ? 'visibility: collapse' : ''">
                    <div class="td"><label for="faulttolerant-input">Fault tolerant</label></div>
                    <div class="td align-left">
                        <font-awesome-icon id="faulttolerant-input"
                                           :icon="jobConfiguration.faultTolerant ? 'check-square' : 'square'"
                                           v-on:click="jobConfiguration.faultTolerant = !jobConfiguration.faultTolerant"
                                           data-e2e="job-faulttolerant-button"/>
                    </div>
                </div>
                <div class="tr">
                    <div class="td align-left">
                        <font-awesome-icon class="arrow" :class="arrowColor()"
                                           v-bind:icon="showAdvancedParameters ? 'chevron-up' : 'chevron-down'"
                                           v-on:click="showAdvancedParameters = !showAdvancedParameters"
                                           data-e2e="toggle-advanced-job-parameters"/>
                    </div>
                </div>
            </div>
        </core-panel>

        <div>
            <core-panel class="spacer-top">
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
                                    data-e2e="trigger-category-selector"
                                    v-on:change="loadTriggerTypesOfCategory(jobConfiguration.trigger.category.key, true).then(() => {
                                        loadTriggerParametersOfType(jobConfiguration.trigger.type.key)})">
                                <option v-for="triggerCategory in triggerCategories" v-bind:value="triggerCategory"
                                        v-bind:key="triggerCategory.key">
                                    {{ triggerCategory.value }}
                                </option>
                            </select>
                        </div>
                    </div>
                    <div class="tr">
                        <div class="td"><label for="type">Type</label></div>
                        <div class="td">
                            <select id="type" v-model="jobConfiguration.trigger.type"
                                    data-e2e="trigger-type-selector"
                                    v-on:change="loadTriggerParametersOfType(jobConfiguration.trigger.type.key)">
                                <option v-for="triggerType in triggerTypes" v-bind:value="triggerType"
                                        v-bind:key="triggerType.key">
                                    {{ triggerType.value }}
                                </option>
                            </select>
                        </div>
                    </div>
                </div>
            </core-panel>

            <core-panel class="spacer-top">
                <h2>Trigger Parameters</h2>
                <div v-if="jobConfiguration.trigger.parameters.length">
                    <parameter-editor
                        :parent-id="jobConfiguration.trigger.id"
                        :validation-errors="validationErrors"
                        :parameters="jobConfiguration.trigger.parameters"
                        v-on:create-connector="createConnector"/>
                </div>
                <p v-else>
                    This trigger has no parameters to configure.
                </p>
            </core-panel>
        </div>

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
        arrowColor: function () {
            if (this.validationErrors && this.jobConfiguration.id in this.validationErrors
                && (('historyLimit' in this.validationErrors[this.jobConfiguration.id]) || ('simulationLimit' in this.validationErrors[this.jobConfiguration.id]))) {
                return 'arrow-alert';
            }
            return '';
        }
    },
    async mounted() {
        this.loadTriggerCategories().then(() => {
            this.loadTriggerTypesOfCategory(this.jobConfiguration.trigger.category.key, false);
        })
    }
}
</script>

<style scoped>

.arrow:hover {
    cursor: pointer;
}

.arrow-alert {
    color: var(--color-alert);
}

.textarea {
    width: 100%;
    height: 100%;
    resize: vertical;
}

.text-top {
    vertical-align: top;
}

</style>
