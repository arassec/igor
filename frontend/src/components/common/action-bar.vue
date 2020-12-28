<template>
    <layout-row class="action-bar">
        <slot name="left" slot="left"/>
        <slot name="right" slot="right"/>
    </layout-row>
</template>

<script>
import LayoutRow from "./layout-row";

export default {
        name: "action-bar",
        components: {LayoutRow}
    }
</script>

<style scoped>

    .action-bar {
        width: 100%;
        background-color: var(--color-background);
        flex-shrink: 0;
        padding: .5em;
    }

</style>