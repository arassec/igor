<template>
    <div class="simulation-results">
        <div v-if="showJson">
            <layout-row>
                <template v-slot:left>
                    <vue-json-pretty
                        :data="data"
                        :deep="2"
                        :showLength="true"
                        :showLine="false"
                        v-on:nodeClick="(nodeData) => selectOrClose(nodeData.path)"
                    >
                    </vue-json-pretty>
                </template>
                <template v-slot:right>
                    <icon-button
                        icon="chevron-up"
                        v-on:clicked="showJson = false"
                        :data-e2e="'hide-sim-results-json-' + index"
                    />
                </template>
            </layout-row>
        </div>
        <div v-else class="placeholder">
            <div>{{ placeholderJson }}</div>
            <icon-button
                icon="chevron-down"
                v-on:clicked="showJson = true"
                :data-e2e="'show-sim-results-json-' + index"
            />
        </div>
    </div>
</template>

<script>
import "@/assets/vue-json-pretty.styles.css";
import VueJsonPretty from "vue-json-pretty";
import IconButton from "@/components/common/icon-button.vue";
import LayoutRow from "@/components/common/layout-row.vue";

export default {
    name: "simulation-result",
    components: { LayoutRow, IconButton, VueJsonPretty },
    props: ["data", "index"],
    data: function () {
        return {
            showJson: false,
        };
    },
    methods: {
        selectOrClose: function (path) {
            if (path !== "root") {
                this.$emit("node-selected", path);
            }
        },
    },
    computed: {
        placeholderJson: function () {
            return JSON.stringify(this.data).substr(0, 200);
        },
    },
};
</script>

<style scoped>
.simulation-results {
    background-color: var(--color-foreground);
}

.placeholder {
    display: flex;
    padding: 0.25em;
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
