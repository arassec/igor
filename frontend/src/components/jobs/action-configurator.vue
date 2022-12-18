<template>
    <div class="sticky max-width" :data-e2e="dataE2eName()">
        <core-panel class="min-height">
            <layout-row>
                <h1 slot="left" class="truncate" data-e2e="title">
                    <font-awesome-icon icon="wrench" class="margin-right"/>
                    {{ action.name.length > 0 ? action.name : action.type.value }}
                </h1>
                <template v-slot:right>
                    <icon-button icon="question" v-on:clicked="$emit('open-documentation', 'action')"/>
                </template>
            </layout-row>
            <div class="table full-width">
                <div class="tr">
                    <div class="td"><label>Active</label></div>
                    <div class="td align-left">
                        <font-awesome-icon :icon="action.active ? 'check-square' : 'square'"
                                           v-on:click="$emit('toggle-action-active')"/>
                    </div>
                </div>
                <div class="tr">
                    <div class="td"><label>Name</label></div>
                    <div class="td">
                        <input type="text" autocomplete="off" :value="action.name" class="full-width"
                               data-e2e="name-input" @input="changeActionName"/>
                    </div>
                </div>
                <div class="tr" v-bind:style="!showAdvancedParameters ? 'visibility: collapse' : ''">
                    <div class="td"><label for="description-input">Description</label></div>
                    <div class="text-top td">
                        <textarea rows="8" cols="35" id="description-input" autocomplete="off"
                                  :value="action.description" @change="changeActionDescription"
                                  class="textarea"/>
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

        <div>
            <core-panel class="spacer-top">
                <layout-row>
                    <h2 slot="left">Action</h2>
                    <template v-slot:right>
                        <icon-button icon="question" v-show="hasDocumentation(action.type.key)"
                                     v-on:clicked="$emit('open-documentation', action.type.key)"/>
                    </template>
                </layout-row>
                <div class="table margin-bottom">
                    <div class="tr" v-if="actionCategories.length > 0">
                        <div class="td"><label>Category</label></div>
                        <div class="td">
                            <select :value="action.category.key" @change="changeActionCategory($event)"
                                    data-e2e="action-category-selector">
                                <option v-for="category in actionCategories" :value="category.key" :key="category.key">
                                    {{ category.value }}
                                </option>
                            </select>
                        </div>
                    </div>
                    <div class="tr" v-if="actionTypes.length > 0">
                        <div class="td">Type</div>
                        <div class="td">
                            <select :value="action.type.key" @change="changeActionParameters"
                                    data-e2e="action-type-selector">
                                <option v-for="type in actionTypes" :value="type.key" :key="type.key">
                                    {{ type.value }}
                                </option>
                            </select>
                        </div>
                    </div>
                </div>
            </core-panel>

            <core-panel class="spacer-top">
                <h2>Action Parameters</h2>
                <div v-if="action.parameters.length">
                    <parameter-editor
                        :parent-id="action.id"
                        :validation-errors="validationErrors"
                        :parameters="action.parameters"
                        v-on:create-connector="createConnector"
                        v-on:connector-selected="connectorSelected"
                        v-on:set-cron-expression="setCronExpression"/>
                </div>
                <p v-else>
                    This Action has no parameters to configure.
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
import Utils from "@/utils/utils";

export default {
    name: 'action-configurator',
    components: {LayoutRow, IconButton, CorePanel, ParameterEditor},
    props: ['action', 'validationErrors', 'eventTrigger'],
    data: function () {
        return {
            showAdvancedParameters: false,
            actionCategories: [],
            actionTypes: []
        }
    },
    methods: {
        loadCategories: async function () {
            await IgorBackend.getData('/api/category/action').then((categories) => {
                for (let i = this.actionCategories.length; i > 0; i--) {
                    this.actionCategories.pop()
                }
                let component = this
                Array.from(categories).forEach(function (item) {
                    component.actionCategories.push(item)
                })
            })
        },
        loadTypesOfCategory: async function (categoryKey, selectFirst) {
            await IgorBackend.getData('/api/type/action/' + categoryKey).then((types) => {
                for (let i = this.actionTypes.length; i > 0; i--) {
                    this.actionTypes.pop()
                }
                let component = this
                Array.from(types).forEach(function (item) {
                    if (!component.eventTrigger || item.supportsEvents) {
                        component.actionTypes.push(item)
                    }
                })
                if (selectFirst) {
                    this.$emit('change-action-type', this.actionTypes[0]);
                }
            })
        },
        loadParametersOfType: async function (typeKey) {
            if (this.hasDocumentation(typeKey)) {
                this.$emit('switch-documentation', typeKey);
            } else {
                this.$emit('close-documentation');
            }
            IgorBackend.getData('/api/parameters/action/' + typeKey).then((parameters) => {
                this.$emit('change-action-parameters', parameters);
            })
        },
        createConnector: function (componentId, parameterIndex, connectorCategoryCandidates) {
            this.$emit('create-connector', componentId, parameterIndex, connectorCategoryCandidates)
        },
        connectorSelected: function (connector, connectorParameterIndex) {
            this.$emit('connector-selected', connector, connectorParameterIndex)
        },
        setCronExpression: function (cronExpression, cronParameterIndex) {
            this.$emit('set-cron-expression', cronExpression, cronParameterIndex)
        },
        hasDocumentation: function (typeId) {
            for (const element of this.actionTypes) {
                if (element.key === typeId) {
                    return element.documentationAvailable;
                }
            }
            return typeId === 'missing-component-action';
        },
        dataE2eName: function () {
            if (this.action.name) {
                return 'action-configurator-' + Utils.toKebabCase(this.action.name);
            } else {
                return 'action-configurator-' + Utils.toKebabCase(this.action.type.value);
            }
        },
        changeActionName: function (event) {
            this.$emit('change-action-name', event.target.value);
        },
        changeActionDescription: function (event) {
            this.$emit('change-action-description', event.target.value);
        },
        changeActionCategory: function (event) {
            const chosenCategoryKey = event.target.value;
            this.loadTypesOfCategory(chosenCategoryKey, true).then(() => {
                for (const element of this.actionCategories) {
                    if (element.key === chosenCategoryKey) {
                        this.$emit('change-action-category', element);
                    }
                }
                this.loadParametersOfType(this.action.type.key)
            });
        },
        changeActionParameters: function (event) {
            const chosenTypeKey = event.target.value;
            this.loadParametersOfType(chosenTypeKey).then(() => {
                for (const element of this.actionTypes) {
                    if (element.key === chosenTypeKey) {
                        this.$emit('change-action-type', element);
                    }
                }
            })
        }
    },
    watch: {
        action: function () {
            // When an action is moved in the tree-navigation, the vue-model changes for the component!
            this.loadCategories().then(() => {
                this.loadTypesOfCategory(this.action.category.key, false)
            })
        }
    },
    mounted: function () {
        this.loadCategories().then(() => {
            this.loadTypesOfCategory(this.action.category.key, false).then(() => {
                // Don't load type parameters as they are already provided within the component's model:
                if (!(Array.isArray(this.action.parameters) && this.action.parameters.length)) {
                    this.loadParametersOfType(this.action.type.key)
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

.textarea {
    width: 100%;
    height: 100%;
    resize: vertical;
}

.text-top {
    vertical-align: top;
}

</style>
