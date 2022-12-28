<template>
    <div>
        <div class="table">
            <div
                v-for="(param, index) in parameters"
                v-bind:key="param.name"
                class="tr"
                v-bind:style="!showAdvancedParameters && param.advanced ? 'visibility: collapse' : ''"
            >
                <div class="text-top td">
                    <label>{{ param.displayName }}</label>
                </div>
                <div class="td" :class="isBoolean(param.type) ? 'align-left' : ''">
                    <input-validated
                        v-if="isNumber(param.type)"
                        v-model.number="param.value"
                        :parent-id="parentId"
                        :property-id="param.name"
                        :validation-errors="validationErrors"
                        v-on:keypress="checkNumber($event)"
                        :data-e2e="'parameter-' + param.name"
                        :is-number="true"
                        @input="param.value = $event"
                    />

                    <font-awesome-icon
                        v-else-if="isBoolean(param.type)"
                        :icon="param.value ? 'check-square' : 'square'"
                        v-on:click="param.value = !param.value"
                        :data-e2e="'parameter-' + param.name"
                    />

                    <input-validated-with-button
                        v-else-if="isConnector(param)"
                        :disabled="true"
                        v-model="param.connectorName"
                        :parent-id="parentId"
                        :property-id="param.name"
                        :validation-errors="validationErrors"
                        :icon="'link'"
                        v-on:icon-clicked="openConnectorPicker(index, param.categoryCandidates)"
                        :data-e2e="'parameter-' + param.name"
                        :button-data-e2e="'picker'"
                    />

                    <input-validated-with-button
                        v-else-if="param.subtype === 'CRON'"
                        :disabled="false"
                        :type="parameterInputTypes[index]"
                        v-model.trim="param.value"
                        :parent-id="parentId"
                        :property-id="param.name"
                        :validation-errors="validationErrors"
                        :icon="'clock'"
                        v-on:icon-clicked="openCronPicker(index)"
                        :data-e2e="'parameter-' + param.name"
                        :button-data-e2e="'cron-picker'"
                    />

                    <input-validated-with-button
                        v-else-if="!isNumber(param.type) && !isBoolean(param.type) && param.secured"
                        :disabled="false"
                        :type="parameterInputTypes[index]"
                        v-model.trim="param.value"
                        :parent-id="parentId"
                        :property-id="param.name"
                        :validation-errors="validationErrors"
                        :icon="'eye'"
                        v-on:icon-clicked="toggleCleartext(index)"
                        :data-e2e="'parameter-' + param.name"
                        :button-data-e2e="'show-password-button'"
                    />

                    <textarea-validated
                        v-else-if="param.subtype === 'MULTI_LINE'"
                        v-model="param.value"
                        :parent-id="parentId"
                        :property-id="param.name"
                        :validation-errors="validationErrors"
                        :data-e2e="'parameter-' + param.name"
                        @input="param.value = $event"
                    />

                    <input-validated
                        v-else
                        :type="parameterInputTypes[index]"
                        v-model.trim="param.value"
                        :parent-id="parentId"
                        :property-id="param.name"
                        :validation-errors="validationErrors"
                        :data-e2e="'parameter-' + param.name"
                        @input="param.value = $event"
                    />
                </div>
            </div>
            <div v-if="advancedParametersExist()" class="arrow-container">
                <font-awesome-icon
                    class="arrow"
                    :class="arrowColor()"
                    data-e2e="toggle-advanced-parameters"
                    v-bind:icon="showAdvancedParameters ? 'chevron-up' : 'chevron-down'"
                    v-on:click="showAdvancedParameters = !showAdvancedParameters"
                />
            </div>
        </div>

        <connector-picker
            v-show="showConnectorPicker"
            :connectors="connectorPage.items"
            :page="connectorPage"
            v-on:cancel="showConnectorPicker = false"
            v-on:create="createConnector"
            v-on:selected="setSelectedConnector"
            v-on:first-page="loadConnectorPage(connectorParameterCategoryCandidates, 0)"
            v-on:previous-page="loadConnectorPage(connectorParameterCategoryCandidates, connectorPage.number - 1)"
            v-on:next-page="loadConnectorPage(connectorParameterCategoryCandidates, connectorPage.number + 1)"
            v-on:last-page="loadConnectorPage(connectorParameterCategoryCandidates, connectorPage.totalPages - 1)"
        />

        <cron-picker v-show="showCronPicker" v-on:selected="setCronExpression" v-on:cancel="showCronPicker = false" />
    </div>
</template>

<script>
import ConnectorPicker from "../connectors/connector-picker.vue";
import CronPicker from "./cron-picker.vue";
import IgorBackend from "../../utils/igor-backend.js";
import InputValidated from "./input-validated.vue";
import TextareaValidated from "./textarea-validated.vue";
import InputValidatedWithButton from "./input-validated-with-button.vue";

export default {
    name: "parameter-editor",
    components: {
        InputValidatedWithButton,
        TextareaValidated,
        InputValidated,
        ConnectorPicker,
        CronPicker,
    },
    props: ["parentId", "validationErrors", "parameters"],
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
                items: [],
            },
        };
    },
    methods: {
        isNumber: function (parameter) {
            return (
                parameter === "byte" ||
                parameter === "java.lang.Byte" ||
                parameter === "short" ||
                parameter === "java.lang.Short" ||
                parameter === "int" ||
                parameter === "java.lang.Integer" ||
                parameter === "long" ||
                parameter === "java.lang.Long" ||
                parameter === "float" ||
                parameter === "java.lang.Float" ||
                parameter === "double" ||
                parameter === "java.lang.Double"
            );
        },
        isBoolean: function (parameter) {
            return parameter === "boolean" || parameter === "java.lang.Boolean";
        },
        isConnector: function (parameter) {
            return !!parameter.connector;
        },
        checkNumber: function (evt) {
            let charCode = evt.which ? evt.which : evt.keyCode;
            if ((charCode > 31 && (charCode < 48 || charCode > 57)) || charCode === 46) {
                evt.preventDefault();
            } else {
                return true;
            }
        },
        toggleCleartext: function (index) {
            if (this.parameterInputTypes[index] === "password") {
                this.parameterInputTypes[index] = "text";
            } else {
                this.parameterInputTypes[index] = "password";
            }
        },
        openConnectorPicker: async function (index, connectorCategoryCandidates) {
            this.connectorParameterIndex = index;
            this.connectorParameterCategoryCandidates = connectorCategoryCandidates;
            await this.loadConnectorPage(connectorCategoryCandidates, 0);
            this.showConnectorPicker = true;
        },
        loadConnectorPage: async function (connectorCategoryCandidates, pageNumber) {
            let typeIds = "";
            connectorCategoryCandidates.forEach((categoryCandidate) => {
                categoryCandidate.typeCandidates.forEach((typeCandidate) => {
                    typeIds += btoa(typeCandidate.key) + ",";
                });
            });
            this.connectorPage = await IgorBackend.getData(
                "/api/connector/candidate/" +
                    typeIds +
                    "?pageNumber=" +
                    pageNumber +
                    "&pageSize=" +
                    this.connectorPage.size
            ).catch((error) => {
                console.error("Error during backend request: " + error);
            });
        },
        setSelectedConnector: function (connector) {
            this.$emit("connector-selected", connector, this.connectorParameterIndex);
            this.showConnectorPicker = false;
        },
        createConnector: function () {
            this.$emit(
                "create-connector",
                this.parentId,
                this.connectorParameterIndex,
                this.connectorParameterCategoryCandidates
            );
        },
        openCronPicker: function (index) {
            this.cronParameterIndex = index;
            this.showCronPicker = true;
        },
        setCronExpression: function (value) {
            this.$emit("set-cron-expression", value, this.cronParameterIndex);
            this.showCronPicker = false;
        },
        advancedParametersExist: function () {
            for (const element of this.parameters) {
                if (element.advanced) {
                    return true;
                }
            }
            return false;
        },
        updateParameterInputTypes: function () {
            let component = this;
            for (let i in this.parameters) {
                if (Object.prototype.hasOwnProperty.call(this.parameters, i)) {
                    let param = this.parameters[i];
                    if (!component.isNumber(param.type) && !component.isBoolean(param.type)) {
                        if (param.secured) {
                            component.parameterInputTypes[i] = "password";
                        } else {
                            component.parameterInputTypes[i] = "text";
                        }
                    } else {
                        component.parameterInputTypes[i] = null;
                    }
                }
            }
        },
        arrowColor: function () {
            if (this.validationErrors && this.parentId in this.validationErrors) {
                for (const element of this.parameters) {
                    if (element.advanced && element.name in this.validationErrors[this.parentId]) {
                        return "arrow-alert";
                    }
                }
            }
            return "";
        },
    },
    created: function () {
        this.updateParameterInputTypes();
    },
    watch: {
        parameters: function () {
            this.updateParameterInputTypes();
        },
    },
};
</script>

<style scoped>
.arrow-container {
    margin: 0 0 0 0.25em;
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

::placeholder {
    /* Chrome, Firefox, Opera, Safari 10.1+ */
    color: var(--color-font);
    opacity: 1; /* Firefox */
}

.arrow-alert {
    color: var(--color-alert);
}
</style>
