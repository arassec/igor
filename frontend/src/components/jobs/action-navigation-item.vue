<template>
    <navigation-item class="action-row" v-on:clicked="$emit('action-is-selected', action.id)" :class="style">
        <font-awesome-icon slot="left" icon="grip-vertical" class="margin-right fa-fw move-icon"/>
        <label slot="center" class="action-label">{{action.name.length > 0 ? action.name : action.type.value}}</label>
        <icon-button slot="right" icon="ellipsis-v" v-on:clicked="showMenu = !showMenu"/>
        <div slot="menu" class="menu" v-show="showMenu" v-on:mouseleave="showMenu = false">
            <ul>
                <li class="menu-item" v-on:click="$emit('delete-action', action.id)">
                    <font-awesome-icon icon="trash" class="margin-right fa-fw"/>
                    Delete
                </li>
            </ul>
        </div>
    </navigation-item>
</template>

<script>
    import NavigationItem from "./navigation-item";
    import IconButton from "../common/icon-button";

    export default {
        name: "action-navigation-item",
        components: {IconButton, NavigationItem},
        props: ['action', 'selectedActionId', 'validationErrors'],
        data: function () {
            return {
                showMenu: false
            }
        },
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
        border-top: 1px solid var(--color-font);
        margin: 0 0 0 .5em;
        font-size: 85%;
    }

</style>