<template>
    <navigation-item class="action-row" v-on:clicked="$emit('action-is-selected', action.id)" :class="style">
        <font-awesome-icon slot="left" icon="grip-vertical" class="margin-right fa-fw move-icon"/>
        <label slot="center" class="action-label">{{action.name.length > 0 ? action.name : action.type.value}}</label>
        <icon-button slot="right" icon="trash" v-on:clicked="$emit('delete-action', action.id)"/>
    </navigation-item>
</template>

<script>
    import NavigationItem from "./navigation-item";
    import IconButton from "../common/icon-button";

    export default {
        name: "action-navigation-item",
        components: {IconButton, NavigationItem},
        props: ['action', 'selectedActionId', 'validationErrors'],
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
                return "unselected";
            }
        }
    }
</script>

<style scoped>

    .action-label {
        display: inline-block;
        padding: .2em 0 .2em 0;
    }

    .action-row {
        border-top: .1em solid var(--color-font);
    }

</style>