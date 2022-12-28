<template>
    <core-content>
        <div class="documentation-container">
            <input-button icon="times" v-on:clicked="$emit('close')" class="close-button" />

            <div id="documentation" v-html="format(documentation)" />
        </div>
    </core-content>
</template>

<script>
import CoreContent from "./core-content.vue";
import InputButton from "./input-button.vue";
import MarkdownIt from "markdown-it";

export default {
    name: "documentation-container",
    components: { InputButton, CoreContent },
    props: ["documentation"],
    methods: {
        format: function () {
            if (!this.documentation) {
                return;
            }
            let md = new MarkdownIt();
            return md.render(this.documentation);
        },
    },
};
</script>

<style scoped>
.content {
    z-index: 3;
    flex-grow: 1;
    margin-right: 0;
    overflow: hidden;
}

.documentation-container {
    color: var(--color-font);
    background-color: var(--color-background);
    padding: 1em;
}

#documentation :deep(h2) {
    margin: 2em 0 0.5em 0;
}

#documentation :deep(table) {
    border-collapse: collapse;
    margin: 1em 0 1em 0;
}

#documentation :deep(p) {
    margin: 0 0 1em 0;
}

#documentation :deep(pre) {
    border: 1px solid var(--color-font);
    padding: 0.5em;
}

#documentation :deep(table, th, td) {
    border: 1px solid var(--color-font);
}

#documentation :deep(th) {
    border: 1px solid var(--color-font);
    padding: 0.25em;
}

#documentation :deep(td) {
    border: 1px solid var(--color-font);
    padding: 0.25em;
}

.close-button {
    float: right;
}
</style>
