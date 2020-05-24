<template>
    <div>

        <div class="table">
            <template v-for="(param, index) in parameters" v-bind:id="param.name" v-bind:index="index">
                <div class="tr" v-bind:key="param.name"
                     v-bind:style="(!showAdvancedParameters && param.advanced) ? 'visibility: collapse' : ''">
                    <div class="text-top td">
                        <label>{{formatParameterName(param)}}</label>
                    </div>
                    <div class="td" :class="isBoolean(param.type) ? 'align-left' : ''">

                        <input-validated v-if="isNumber(param.type)"
                                         v-model.number="param.value" :type="'text'"
                                         :parent-id="parentId"
                                         :property-id="param.name"
                                         :validation-errors="validationErrors"/>

                        <font-awesome-icon v-else-if="isBoolean(param.type)"
                                           :icon="param.value ? 'check-square' : 'square'"
                                           v-on:click="param.value = !param.value"/>

                        <input-validated v-else-if="isConnector(param)" :disabled="true"
                                         v-model="param.connectorName"
                                         :parent-id="parentId"
                                         :property-id="param.name"
                                         :validation-errors="validationErrors"
                                         :has-button="true"/>

                        <textarea-validated v-else-if="param.subtype === 'MULTI_LINE'" v-model="param.value"
                                            :parent-id="parentId"
                                            :property-id="param.name"
                                            :validation-errors="validationErrors"/>

                        <input-validated v-else
                                         :type="parameterInputTypes[index]" v-model.trim="param.value"
                                         :parent-id="parentId"
                                         :property-id="param.name"
                                         :validation-errors="validationErrors"
                                         :has-button="(!isNumber(param.type) && !isBoolean(param.type) && param.secured)|| (param.subtype === 'CRON')"/>

                        <input-button v-if="!isNumber(param.type) && !isBoolean(param.type) && param.secured"
                                      icon="eye" v-on:clicked="toggleCleartext(index)" class="margin-left"/>

                        <input-button v-else-if="isConnector(param)" icon="link"
                                      v-on:clicked="openConnectorPicker(index, param.categoryCandidates)"
                                      class="margin-left"/>

                        <input-button v-else-if="param.subtype === 'CRON'" v-on:clicked="openCronPicker(index)"
                                      icon="clock"
                                      class="margin-left"/>

                    </div>
                </div>
            </template>
            <div v-if="advancedParametersExist()" class="arrow-container">
                <font-awesome-icon class="arrow"
                                   v-bind:icon="showAdvancedParameters ? 'chevron-up' : 'chevron-down'"
                                   v-on:click="showAdvancedParameters = !showAdvancedParameters"/>
            </div>
        </div>

        <connector-picker v-show="showConnectorPicker" :connectors="connectorPage.items" :page="connectorPage"
                          v-on:cancel="showConnectorPicker = false"
                          v-on:create="createConnector"
                          v-on:selected="setSelectedConnector"
                          v-on:first-page="loadConnectorPage(connectorParameterCategoryCandidates, 0)"
                          v-on:previous-page="loadConnectorPage(connectorParameterCategoryCandidates, (connectorPage.number - 1))"
                          v-on:next-page="loadConnectorPage(connectorParameterCategoryCandidates, (connectorPage.number + 1))"
                          v-on:last-page="loadConnectorPage(connectorParameterCategoryCandidates, (connectorPage.totalPages - 1))"
        />

        <cron-picker v-show="showCronPicker" v-on:selected="setCronExpression" v-on:cancel="showCronPicker = false"/>

    </div>
</template>

<script>
    import InputButton from './input-button'
    import ConnectorPicker from '../connectors/connector-picker'
    import CronPicker from "./cron-picker";
    import IgorBackend from '../../utils/igor-backend.js'
    import InputValidated from "./input-validated";
    import TextareaValidated from "./textarea-validated";

    export default {
        name: 'parameter-editor',
        components: {TextareaValidated, InputValidated, ConnectorPicker, CronPicker, InputButton},
        props: ['parentId', 'validationErrors', 'parameters'],
        data: function () {
            return {
                validationOk: true,
                showConnectorPicker: false,
                showCronPicker: false,
                showAdvancedParameters: false,
                connectorParameterIndex: 0,
                connectorParameterCategoryCandidates: null,
                cronParameterIndex: 0,
                parameterValidationErrors: [],
                parameterInputTypes: [],
                connectorPage: {
                    page: -1,
                    size: 10,
                    totalPages: 0,
                    items: []
                }
            }
        },
        methods: {
            isNumber: function (parameter) {
                return (parameter === 'int' ||
                    parameter === 'java.lang.Integer' ||
                    parameter === 'long' ||
                    parameter === 'java.lang.Long')
            },
            isBoolean: function (parameter) {
                return (parameter === 'boolean' ||
                    parameter === 'java.lang.Boolean')
            },
            isConnector: function (parameter) {
                return !!parameter.connector;
            },
            formatParameterName: function (parameter) {
                let string = parameter.name.replace(/\.?([A-Z])/g, function (x, y) {
                    return ' ' + y.toLowerCase()
                }).replace(/^_/, '')
                let star = "";
                if (parameter.required) {
                    star = "*";
                }
                return string.charAt(0).toUpperCase() + string.slice(1) + star
            },
            toggleCleartext: function (index) {
                if (this.parameterInputTypes[index] === 'password') {
                    this.$set(this.parameterInputTypes, index, 'text')
                } else {
                    this.$set(this.parameterInputTypes, index, 'password')
                }
            },
            openConnectorPicker: async function (index, connectorCategoryCandidates) {
                this.connectorParameterIndex = index
                this.connectorParameterCategoryCandidates = connectorCategoryCandidates
                await this.loadConnectorPage(connectorCategoryCandidates, 0)
                this.showConnectorPicker = true
            },
            loadConnectorPage: async function (connectorCategoryCandidates, pageNumber) {
                let typeIds = '';
                connectorCategoryCandidates.forEach(categoryCandidate => {
                    categoryCandidate.typeCandidates.forEach(typeCandidate => {
                        typeIds += btoa(typeCandidate.key) + ","
                    })
                });
                this.connectorPage = await IgorBackend.getData('/api/connector/candidate/' + typeIds + '?pageNumber=' + pageNumber +
                    '&pageSize=' + this.connectorPage.size)
            },
            setSelectedConnector: function (connector) {
                this.parameters[this.connectorParameterIndex].connectorName = connector.name
                this.parameters[this.connectorParameterIndex].value = connector.id
                this.showConnectorPicker = false
            },
            createConnector: function () {
                this.$emit('create-connector', this.connectorParameterIndex, this.connectorParameterCategoryCandidates)
            },
            openCronPicker: function (index) {
                this.cronParameterIndex = index
                this.showCronPicker = true
            },
            setCronExpression: function (value) {
                this.parameters[this.cronParameterIndex].value = value
                this.showCronPicker = false
            },
            advancedParametersExist: function () {
                for (let index = 0; index < this.parameters.length; index++) {
                    if (this.parameters[index].advanced) {
                        return true
                    }
                }
                return false
            }
        },
        created: function () {
            let component = this
            for (let i in this.parameters) {
                if (this.parameters.hasOwnProperty(i)) {
                    let param = this.parameters[i]
                    if (!component.isNumber(param.type) && !component.isBoolean(param.type)) {
                        if (param.secured) {
                            component.$set(component.parameterInputTypes, i, 'password')
                        } else {
                            component.$set(component.parameterInputTypes, i, 'text')
                        }
                    }
                }
            }
        }
    }
</script>

<style scoped>

    .arrow-container {
        margin: 0 0 0 .25em;
    }

    .arrow:hover {
        cursor: pointer;
    }

    .text-top {
        vertical-align: top;
    }

    .panel .validation-error {
        background-color: var(--color-alert);
    }

    ::placeholder { /* Chrome, Firefox, Opera, Safari 10.1+ */
        color: var(--color-font);
        opacity: 1; /* Firefox */
    }

</style>
