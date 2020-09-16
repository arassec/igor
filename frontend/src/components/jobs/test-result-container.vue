<template>
    <core-content>
        <div class="test-result-container">
            <layout-row>
                <div slot="left">
                    <h1>Simulation results</h1>
                </div>
                <input-button slot="right" icon="times" v-on:clicked="$emit('close')"/>
            </layout-row>

            <p v-if="!selectedTestResults">
                No simulation results available.
            </p>
            <p v-else-if="selectedTestResults.results.length === 0 && !selectedTestResults.errorCause">
                No simulation results available.
            </p>
            <pre v-else-if="selectedTestResults.results.length > 0 && !selectedTestResults.errorCause"
                 v-for="(result, index) in selectedTestResults.results" v-bind:key="index"
                 class="normal-bg">
<code>{{ format(result) }}</code></pre>
            <pre v-else-if="selectedTestResults.errorCause" class="error-bg">
<code>{{ selectedTestResults.errorCause }}</code></pre>
        </div>
    </core-content>
</template>

<script>
import LayoutRow from '../common/layout-row'
import InputButton from '../common/input-button'
import CoreContent from '../common/core-content'

export default {
    name: 'test-result-container',
    components: {CoreContent, InputButton, LayoutRow},
    props: ['selectedTestResults'],
    methods: {
      format: function (code) {
        return JSON.stringify(code, null, 2)
      }
    }
  }
</script>

<style scoped>

    .content {
        z-index: 2;
        flex-grow: 2;
        margin-right: 0;
        overflow: hidden;
    }

    .test-result-container {
        color: var(--color-font);
        background-color: var(--color-background);
        padding: 1em;
    }

    pre {
        overflow: auto;
        height: auto;
        word-break: normal !important;
        word-wrap: normal !important;
        white-space: pre !important;
    }

    .normal-bg {
        background-color: var(--color-foreground)
    }

    .error-bg {
        background-color: var(--color-alert)
    }

</style>
