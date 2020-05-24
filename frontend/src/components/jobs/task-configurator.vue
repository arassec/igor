<template>
    <div class="sticky max-width">
        <core-panel>
            <layout-row>
                <h1 slot="left" class="truncate">
                    <font-awesome-icon icon="tasks" class="margin-right"/>
                    {{ task.name.length > 0 ? task.name : 'Task' }}
                </h1>
                <icon-button slot="right" icon="question" v-on:clicked="$emit('open-documentation', 'task')"/>
            </layout-row>
            <div class="table">
                <div class="tr">
                    <div class="td"><label>Active</label></div>
                    <div class="td align-left">
                        <font-awesome-icon :icon="task.active ? 'check-square' : 'square'"
                                           v-on:click="task.active = !task.active"/>
                    </div>
                </div>
                <div class="tr">
                    <div class="td"><label for="name">Name*</label></div>
                    <div class="td">
                        <input-validated id="name" :type="'text'" v-model="task.name"
                                         :parent-id="task.id" :property-id="'name'"
                                         :validation-errors="validationErrors"/>
                    </div>
                </div>
                <div class="tr" v-bind:style="!showAdvancedParameters ? 'visibility: collapse' : ''">
                    <div class="td"><label for="description">Description</label></div>
                    <div class="td">
                        <input id="description" type="text" autocomplete="off" v-model="task.description"/>
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
                <h2 slot="left">Provider</h2>
                <icon-button slot="right" icon="question" v-show="hasDocumentation(task.provider.type.key)"
                             v-on:clicked="$emit('open-documentation', task.provider.type.key)"/>
            </layout-row>
            <div class="table">
                <div class="tr" v-if="providerCategories.length > 1">
                    <div class="td"><label for="category-selection">Category</label></div>
                    <div class="td">
                        <select id="category-selection" v-model="task.provider.category"
                                v-on:change="loadTypesOfCategory(task.provider.category.key, true).then(() => {
                                        loadParametersOfType(task.provider.type.key)})">
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
                        <select id="type-selection" v-model="task.provider.type"
                                v-on:change="loadParametersOfType(task.provider.type.key)">
                            <option v-for="providerType in providerTypes" v-bind:value="providerType"
                                    v-bind:key="providerType.key">
                                {{providerType.value}}
                            </option>
                        </select>
                    </div>
                </div>
            </div>
        </core-panel>

        <core-panel v-if="task.provider.parameters.length">
            <h2>Provider Configuration</h2>
            <parameter-editor
                    :parent-id="task.provider.id"
                    :validation-errors="validationErrors"
                    :parameters="task.provider.parameters"
                    v-on:create-connector="createConnector"/>
        </core-panel>

    </div>
</template>

<script>
    import ParameterEditor from '../common/parameter-editor'
    import CorePanel from '../common/core-panel'
    import IgorBackend from '../../utils/igor-backend.js'
    import LayoutRow from "../common/layout-row";
    import IconButton from "../common/icon-button";
    import InputValidated from "../common/input-validated";

    export default {
        name: 'task-configurator',
        components: {InputValidated, IconButton, LayoutRow, CorePanel, ParameterEditor},
        props: ['task', 'validationErrors'],
        data: function () {
            return {
                nameValidationError: '',
                showAdvancedParameters: false,
                providerCategories: [],
                providerTypes: []
            }
        },
        methods: {
            loadCategories: async function () {
                await IgorBackend.getData('/api/category/provider').then((categories) => {
                    for (let i = this.providerCategories.length; i > 0; i--) {
                        this.providerCategories.pop()
                    }
                    let component = this;
                    Array.from(categories).forEach(function (item) {
                        component.providerCategories.push(item)
                    })
                })
            },
            loadTypesOfCategory: async function (categoryKey, selectFirst) {
                await IgorBackend.getData('/api/type/' + categoryKey).then((types) => {
                    for (let i = this.providerTypes.length; i > 0; i--) {
                        this.providerTypes.pop()
                    }
                    let component = this;
                    Array.from(types).forEach(function (item) {
                        component.providerTypes.push(item)
                    });
                    if (selectFirst) {
                        this.task.provider.type = this.providerTypes[0]
                    }
                })
            },
            loadParametersOfType: function (typeKey) {
                if (this.hasDocumentation(typeKey)) {
                    this.$emit('switch-documentation', typeKey);
                } else {
                    this.$emit('close-documentation');
                }
                IgorBackend.getData('/api/parameters/provider/' + typeKey).then((parameters) => {
                    this.task.provider.parameters = parameters
                })
            },
            createConnector: function (parameterIndex, connectorCategoryCandidates) {
                this.$emit('create-connector', this.task.id, parameterIndex, connectorCategoryCandidates)
            },
            hasDocumentation: function (typeId) {
                for (let i = 0; i < this.providerTypes.length; i++) {
                    if (this.providerTypes[i].key === typeId) {
                        return this.providerTypes[i].documentationAvailable;
                    }
                }
                return false;
            }
        },
        watch: {
            task: function () {
                // When an action is moved in the tree-navigation, the vue-model changes for the component!
                this.loadCategories().then(() => {
                    this.loadTypesOfCategory(this.task.provider.category.key, false)
                })
            }
        },
        mounted() {
            this.loadCategories().then(() => {
                this.loadTypesOfCategory(this.task.provider.category.key, false).then(() => {
                    // Don't load type parameters if they are provided within the component's model:
                    if (!(Array.isArray(this.task.provider.parameters) && this.task.provider.parameters.length)) {
                        this.loadParametersOfType(this.task.provider.type.key)
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

    ::placeholder { /* Chrome, Firefox, Opera, Safari 10.1+ */
        color: var(--color-font);
        opacity: 1; /* Firefox */
    }

</style>
