<template>
    <core-content class="side-menu">
        <core-panel>
            <h1 class="no-margin">
                <slot name="title"/>
            </h1>
            <div class="header sticky">
                <slot name="header"/>
            </div>
        </core-panel>
        <core-panel v-if="hasContentSlot">
            <div class="content">
                <slot name="content"/>
            </div>
        </core-panel>
        <core-panel>
            <div class="footer">
                <slot name="footer"/>
            </div>
        </core-panel>
    </core-content>
</template>

<script>
    import CoreContent from './core-content'
    import CorePanel from './core-panel'

    export default {
        name: 'side-menu',
        components: {CorePanel, CoreContent},
        computed: {
            hasContentSlot: function () {
                return !!this.$slots['content']
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
        padding-top: 15px;
        z-index: 666;
        background-color: var(--color-background);
    }

    .sticky {
        z-index: 5;
        position: sticky;
        top: 0px;
    }

</style>
