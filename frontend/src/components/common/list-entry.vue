<template>
    <div v-on:click.stop="$emit('clicked')">
        <core-panel :small="true" class="list-entry">
            <layout-row class="list-row">
                <slot name="left" slot="left"/>
                <slot name="right" slot="right"/>
            </layout-row>
        </core-panel>
    </div>
</template>

<script>
  import CorePanel from "./core-panel";
  import LayoutRow from "./layout-row";

  export default {
    name: "list-entry",
    components: {LayoutRow, CorePanel}
  }
</script>

<style scoped>

    .list-entry {
        display: block;
    }

    .list-entry:hover {
        cursor: pointer;
        background-color: var(--nav-background-color);
    }

    .list-row {
        height: 25px;
    }

</style>
