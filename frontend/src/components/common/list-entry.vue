<template>
    <core-panel>
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

</style>