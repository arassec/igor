<template>
    <div class="filter">
        <label for="input-filter">{{ label }} </label>
        <input id="input-filter" type="text" v-model="filterText" />
    </div>
</template>

<script>
import { useTupleStore } from "@/stores/tuple";

export default {
    name: "input-filter",
    components: {},
    props: ["filter", "filterKey", "label"],
    data: function () {
        return {
            filterText: "",
        };
    },
    watch: {
        filterText: function (val) {
            useTupleStore().setValue(this.filterKey, val);
            this.filter(val);
        },
    },
    mounted: function () {
        document.addEventListener("keydown", (e) => {
            if (e.key === "Escape") {
                this.filterText = "";
            }
        });
        if (useTupleStore().getValue(this.filterKey)) {
            this.filterText = useTupleStore().getValue(this.filterKey);
        }
    },
};
</script>

<style scoped>
.filter {
    position: relative;
    color: var(--color-font);
    margin: 0.15em 1em 0 0;
}

.filter :deep(input) {
    padding: 0.25em 0.25em 0.25em 0.25em;
    line-height: 1em;
    color: var(--color-font);
    background-color: var(--color-foreground);
    border: none;
}
</style>
