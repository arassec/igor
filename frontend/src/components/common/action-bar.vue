<template>
    <layout-row class="action-bar">
        <template v-slot:left>
            <slot name="left" />
        </template>
        <template v-slot:right>
            <slot name="right" />
        </template>
    </layout-row>
</template>

<script>
import LayoutRow from "./layout-row.vue";

export default {
    name: "action-bar",
    components: { LayoutRow },
};
</script>

<style scoped>
.action-bar {
    width: 100%;
    background-color: var(--color-background);
    flex-shrink: 0;
    padding: 0.5em;
}
</style>
