<template>
    <navigation-item class="spacer-top" v-on:clicked="$emit('action-is-selected', action.id)" :class="style"
                     :data-e2e="dataE2eName">
        <font-awesome-icon slot="left" icon="grip-vertical" class="margin-right fa-fw move-icon"/>
        <label slot="center" class="action-label">{{ action.name.length > 0 ? action.name : action.type.value }}</label>
        <font-awesome-icon v-if="simulationResults && simulationResults.errorCause" slot="right" icon="plug" class="simulation-error"></font-awesome-icon>
        <icon-button slot="right" icon="trash" v-on:clicked="$emit('delete-action', action.id)"/>
    </navigation-item>
</template>

<script>
import NavigationItem from "./navigation-item";
import IconButton from "../common/icon-button";
import Utils from "../../utils/utils";

export default {
    name: "action-navigation-item",
    components: {IconButton, NavigationItem},
    props: ['action', 'selectedActionId', 'validationErrors', 'simulationResults'],
    computed: {
        actionSelected: function () {
            return this.selectedActionId === this.action.id;
        },
        style: function () {
            if (this.action.id in this.validationErrors) {
                return "alert";
            }
            if (this.selectedActionId === this.action.id) {
                return "info";
            }
            if (!this.action.active) {
                return "inactive";
            }
            return "unselected";
        },
        dataE2eName: function () {
            if (this.action.name) {
                return 'action-' + Utils.toKebabCase(this.action.name);
            } else {
                return 'action-' + Utils.toKebabCase(this.action.type.value);
            }
        }
    }
}
</script>

<style scoped>

.action-label {
    display: inline-block;
    padding: .2em 0 .2em 0;
}

.move-icon {
    opacity: 0.5;
}

.move-icon:hover {
    cursor: move;
    opacity: 1;
}

.inactive {
    background-color: var(--color-background);
    opacity: 0.75;
}

.simulation-error {
    color: var(--color-alert);
    margin-right: .25em;
}

</style>