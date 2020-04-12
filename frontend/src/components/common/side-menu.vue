<template>
    <core-content class="side-menu">
        <core-panel v-if="hasHeaderSlot">
            <h1 class="no-margin">
                <slot name="title"/>
            </h1>
            <div class="header sticky">
                <slot name="header"/>
            </div>
        </core-panel>
        <slot name="content"/>
        <slot name="footer"/>
    </core-content>
</template>

<script>
    import CoreContent from './core-content'
    import CorePanel from './core-panel'

    export default {
        name: 'side-menu',
        components: {CorePanel, CoreContent},
        computed: {
            hasHeaderSlot: function () {
                return !!this.$slots['header']
            }
        }
    }
</script>

<style scoped>

    .side-menu {
        margin-top: 1em;
        min-width: 25em;
        flex-shrink: 0;
    }

    .no-margin {
        margin-bottom: 0;
    }

    .header {
        padding-top: 1.5em;
        z-index: 666;
        background-color: var(--color-background);
    }

    .sticky {
        z-index: 5;
        position: sticky;
        top: 0;
    }

</style>
