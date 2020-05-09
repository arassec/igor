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
            <table>
                <tr>
                    <td><label>Active</label></td>
                    <td>
                        <font-awesome-icon :icon="task.active ? 'check-square' : 'square'"
                                           v-on:click="task.active = !task.active"/>
                    </td>
                </tr>
                <tr>
                    <td><label for="name">Name*</label></td>
                    <td>
                        <input id="name" type="text" autocomplete="off" v-model="task.name"
                               :class="nameValidationError.length > 0 ? 'validation-error' : ''"
                               :placeholder="nameValidationError.length > 0 ? nameValidationError : ''"/>
                    </td>
                </tr>
                <tr v-bind:style="!showAdvancedParameters ? 'visibility: collapse' : ''">
                    <td><label for="description">Description</label></td>
                    <td>
                        <input id="description" type="text" autocomplete="off" v-model="task.description"/>
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
            <layout-row>
                <h2 slot="left">Provider</h2>
                <icon-button slot="right" icon="question" v-show="hasDocumentation(task.provider.type.key)"
                             v-on:clicked="$emit('open-documentation', task.provider.type.key)"/>
            </layout-row>
            <table>
                <tr v-if="providerCategories.length > 1">
                    <td><label for="category-selection">Category</label></td>
                    <td>
                        <select id="category-selection" v-model="task.provider.category"
                                v-on:change="loadTypesOfCategory(task.provider.category.key, true).then(() => {
                                        loadParametersOfType(task.provider.type.key)})">
                            <option v-for="providerCategory in providerCategories" v-bind:value="providerCategory"
                                    v-bind:key="providerCategory.key">
                                {{providerCategory.value}}
                            </option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td><label for="type-selection">Type</label></td>
                    <td>
                        <select id="type-selection" v-model="task.provider.type"
                                v-on:change="loadParametersOfType(task.provider.type.key)">
                            <option v-for="providerType in providerTypes" v-bind:value="providerType"
                                    v-bind:key="providerType.key">
                                {{providerType.value}}
                            </option>
                        </select>
                    </td>
                </tr>
            </table>
        </core-panel>

        <core-panel v-if="task.provider.parameters.length">
            <h2>Provider Configuration</h2>
            <parameter-editor v-bind:parameters="task.provider.parameters" ref="parameterEditor"
                              v-on:create-service="createService"/>
        </core-panel>

    </div>
</template>

<script>
    import ParameterEditor from '../common/parameter-editor'
    import CorePanel from '../common/core-panel'
    import IgorBackend from '../../utils/igor-backend.js'
    import LayoutRow from "../common/layout-row";
    import IconButton from "../common/icon-button";

    export default {
        name: 'task-configurator',
        components: {IconButton, LayoutRow, CorePanel, ParameterEditor},
        props: ['task'],
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
            validateInput: function () {
                this.nameValidationError = '';

                let nameValidationResult = true;
                if (this.task.name == null || this.task.name === '') {
                    this.nameValidationError = 'Name must be set';
                    nameValidationResult = false
                }

                let parameterValidationResult = true;
                if (typeof this.$refs.parameterEditor !== 'undefined') {
                    parameterValidationResult = this.$refs.parameterEditor.validateInput()
                }

                return (nameValidationResult && parameterValidationResult)
            },
            createService: function (parameterIndex, serviceCategoryCandidates) {
                this.$emit('create-service', this.task.id, parameterIndex, serviceCategoryCandidates)
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

    .panel .validation-error {
        background-color: var(--color-alert);
    }

    ::placeholder { /* Chrome, Firefox, Opera, Safari 10.1+ */
        color: var(--color-font);
        opacity: 1; /* Firefox */
    }

</style>
