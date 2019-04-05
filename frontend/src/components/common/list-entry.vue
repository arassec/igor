<template>
    <core-panel :class="'small-panel'">
        <button-row>
            <slot name="left" slot="left"/>
            <slot name="right" slot="right"/>
        </button-row>
    </core-panel>
</template>

<script>
    import CorePanel from "./core-panel";
    import ButtonRow from "./button-row";

    export default {
        name: "list-entry",
        components: {ButtonRow, CorePanel}
    }
</script>

<style scoped>

    .small-panel {
        padding: 5px 15px 5px 15px;
    }

</style>