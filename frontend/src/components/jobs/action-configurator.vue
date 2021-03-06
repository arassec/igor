<template>
    <div class="sticky max-width" :data-e2e="dataE2eName()">
        <core-panel class="min-height">
            <layout-row>
                <h1 slot="left" class="truncate" data-e2e="title">
                    <font-awesome-icon icon="wrench" class="margin-right"/>
                    {{ action.name.length > 0 ? action.name : action.type.value }}
                </h1>
                <icon-button slot="right" icon="question" v-on:clicked="$emit('open-documentation', 'action')"/>
            </layout-row>
            <div class="table full-width">
                <div class="tr">
                    <div class="td"><label>Active</label></div>
                    <div class="td align-left">
                        <font-awesome-icon :icon="action.active ? 'check-square' : 'square'"
                                           v-on:click="action.active = !action.active"/>
                    </div>
                </div>
                <div class="tr">
                    <div class="td"><label>Name</label></div>
                    <div class="td">
                        <input type="text" autocomplete="off" v-model="action.name" class="full-width"
                               data-e2e="name-input"/>
                    </div>
                </div>
                <div class="tr" v-bind:style="!showAdvancedParameters ? 'visibility: collapse' : ''">
                    <div class="td"><label for="description-input">Description</label></div>
                    <div class="text-top td">
                        <textarea rows="8" cols="35" id="description-input" autocomplete="off"
                                  v-model="action.description" class="textarea"/>
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
                    <icon-button slot="right" icon="question" v-show="hasDocumentation(action.type.key)"
                                 v-on:clicked="$emit('open-documentation', action.type.key)"/>
                </layout-row>
                <div class="table margin-bottom">
                    <div class="tr">
                        <div class="td"><label>Category</label></div>
                        <div class="td">
                            <select v-model="action.category" v-on:change="loadTypesOfCategory(action.category.key, true).then(() => {
                                        loadParametersOfType(action.type.key)})"
                                    data-e2e="action-category-selector">
                                <option v-for="category in actionCategories" v-bind:value="category"
                                        v-bind:key="category.key">
                                    {{ category.value }}
                                </option>
                            </select>
                        </div>
                    </div>
                    <div class="tr">
                        <div class="td">Type</div>
                        <div class="td">
                            <select v-model="action.type" v-on:change="loadParametersOfType(action.type.key)"
                                    data-e2e="action-type-selector">
                                <option v-for="type in actionTypes" v-bind:value="type"
                                        v-bind:key="type.key">
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
                        v-on:create-connector="createConnector"/>
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
                    this.action.type = this.actionTypes[0]
                }
            })
        },
        loadParametersOfType: function (typeKey) {
            if (this.hasDocumentation(typeKey)) {
                this.$emit('switch-documentation', typeKey);
            } else {
                this.$emit('close-documentation');
            }
            IgorBackend.getData('/api/parameters/action/' + typeKey).then((parameters) => {
                this.action.parameters = parameters
            })
        },
        createConnector: function (componentId, parameterIndex, connectorCategoryCandidates) {
            this.$emit('create-connector', componentId, parameterIndex, connectorCategoryCandidates)
        },
        hasDocumentation: function (typeId) {
            for (let i = 0; i < this.actionTypes.length; i++) {
                if (this.actionTypes[i].key === typeId) {
                    return this.actionTypes[i].documentationAvailable;
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
