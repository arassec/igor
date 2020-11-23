<template>
    <div class="simulation-results">
        <vue-json-pretty :data="data"
                         :deep="2"
                         :showLength="true"
                         :showLine="false"
                         v-on:click="(path) => $emit('node-selected', path)">
        </vue-json-pretty>
    </div>
</template>

<script>
import VueJsonPretty from 'vue-json-pretty'
import '../../assets/vue-json-pretty.styles.css'

export default {
    name: "simulation-result",
    components: {VueJsonPretty},
    props: ['data']
}
</script>

<style scoped>

.simulation-results {
    padding: .25em;
    background-color: var(--color-foreground);
}

</style>