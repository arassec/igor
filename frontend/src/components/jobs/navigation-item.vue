<template>
    <div class="row" v-on:click="$emit('clicked')" :class="style">
        <div class="left">
            <slot name="left"/>
        </div>
        <div class="center">
            <slot name="center"/>
        </div>
        <div class="right">
            <slot name="right"/>
            <slot name="menu"/>
        </div>
    </div>
</template>

<script>
    export default {
        name: 'navigation-item',
        props: ['selected', 'hasValidationErrors'],
        computed: {
            style: function () {
                if (this.selected) {
                    return "info";
                } else if (this.hasValidationErrors) {
                    return "alert";
                }
                return "unselected";
            }
        }
    }
</script>

<style scoped>

    .row {
        display: flex;
        flex-direction: row;
        justify-content: center;
        align-items: center;
        line-height: 100%;
        color: var(--color-font);
        padding: .5em;
    }

    .row:hover {
        cursor: pointer;
        background-color: var(--color-foreground);
    }

    .row label:hover {
        cursor: pointer;
    }

    .left {
        flex: 0 0 auto;
        min-width: 0;
        overflow: hidden;
    }

    .center {
        flex: 1 1 auto;
        min-width: 0;
        text-align: left;
        margin: 0 1em 0 0;
        overflow: hidden;
    }

    .right {
        flex: 0 0 auto;
        position: relative;
    }

    .move-icon {
        opacity: 50%;
    }

    .move-icon:hover {
        cursor: move;
        opacity: 100%;
    }

    .unselected {
        background-color: var(--color-background);
    }

    .unselected:hover {
        background-color: var(--color-foreground);
    }

    .menu {
        position: absolute;
        border: 1px solid var(--color-font);
        background: var(--color-foreground);
        z-index: 100;
        top: -.25em;
        width: 8em;
    }

    .menu-item {
        padding: .25em .5em .25em .5em;
        position: static;
    }

    .menu-item:hover {
        background-color: var(--color-background);
    }

</style>
