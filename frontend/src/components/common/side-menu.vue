<template>
    <core-content class="side-menu">
        <div data-e2e="side-menu" class="sticky">
            <core-panel v-if="hasHeaderSlot">
                <h1 class="no-margin">
                    <slot name="title" />
                </h1>
                <div class="header sticky">
                    <slot name="header" />
                </div>
            </core-panel>
            <slot name="content" />
            <slot name="footer" />
        </div>
    </core-content>
</template>

<script>
import CoreContent from "./core-content.vue";
import CorePanel from "./core-panel.vue";

export default {
    name: "side-menu",
    components: { CorePanel, CoreContent },
    computed: {
        hasHeaderSlot: function () {
            return !!this.$slots["header"];
        },
    },
};
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
</style>
