<template>
    <core-content>
        <div class="test-result-container">
            <layout-row>
                <template v-slot:left>
                    <h1>Connection results</h1>
                </template>
                <template v-slot:right>
                    <input-button icon="times" v-on:clicked="$emit('close')" />
                </template>
            </layout-row>
            <label v-if="testError">Connection to the service failed!</label>
        </div>

        <div v-if="testError" class="test-result-container error-container spacer-top">
            <pre><code>{{ testError }}</code></pre>
        </div>

        <div v-if="!testError" class="test-result-container success-container spacer-top">
            <label>Connection successfully established.</label>
        </div>
    </core-content>
</template>

<script>
import CoreContent from "@/components/common/core-content.vue";
import LayoutRow from "@/components/common/layout-row.vue";
import InputButton from "@/components/common/input-button.vue";

export default {
    name: "connectiontest-result-container",
    components: { InputButton, LayoutRow, CoreContent },
    props: ["testError"],
};
</script>

<style scoped>
.content {
    z-index: 3;
    flex-grow: 1;
    margin-right: 0;
    overflow: hidden;
}

.test-result-container {
    color: var(--color-font);
    background-color: var(--color-background);
    padding: 0.5em;
}

.error-container {
    background-color: var(--color-alert);
    overflow: auto;
}

.success-container {
    background-color: var(--color-foreground);
    overflow: auto;
}
</style>
