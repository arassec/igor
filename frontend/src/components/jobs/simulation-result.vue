<template>
    <div class="simulation-results">
        <div v-if="showJson">
            <layout-row>
                <vue-json-pretty
                    slot="left"
                    :data="data"
                    :deep="2"
                    :showLength="true"
                    :showLine="false"
                    v-on:click="(path) => selectOrClose(path)">
                </vue-json-pretty>
                <icon-button slot="right" icon="chevron-up" v-on:clicked="showJson = false" :data-e2e="'hide-sim-results-json-' + index"/>
            </layout-row>
        </div>
        <div v-else class="placeholder">
            <div>{{ placeholderJson }}</div>
            <icon-button icon="chevron-down" v-on:clicked="showJson = true" :data-e2e="'show-sim-results-json-' + index"/>
        </div>
    </div>
</template>

<script>
import VueJsonPretty from 'vue-json-pretty'
import '../../assets/vue-json-pretty.styles.css'
import IconButton from "@/components/common/icon-button";
import LayoutRow from "@/components/common/layout-row";

export default {
    name: "simulation-result",
    components: {LayoutRow, IconButton, VueJsonPretty},
    props: ['data', 'index'],
    data: function () {
        return {
            showJson: false
        };
    },
    methods: {
        selectOrClose: function (path) {
            if (path !== 'root') {
                this.$emit('node-selected', path);
            }
        }
    },
    computed: {
        placeholderJson: function () {
            return JSON.stringify(this.data).substr(0, 200);
        }
    }
}
</script>

<style scoped>

.simulation-results {
    background-color: var(--color-foreground);
}

.placeholder {
    display: flex;
    padding: .25em;
}

.placeholder div {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.placeholder button {
    margin-left: 1em;
}

</style>