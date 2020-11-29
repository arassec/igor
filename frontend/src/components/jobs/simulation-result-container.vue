<template>
    <core-content>
        <div data-e2e="job-editor-simulation-results">
            <div class="simulation-result-container">
                <layout-row>
                    <div slot="left">
                        <h1>Simulation results</h1>
                    </div>
                    <input-button slot="right" icon="times" v-on:clicked="$emit('close')"/>
                </layout-row>
                <div class="selector-container">
                    <label for="mustache-selector"></label>
                    <input id="mustache-selector" slot="left" type="text" v-model="mustacheSelector"
                           class="mustache-selector" ref="mustacheSelector"/>
                    <input-button slot="right" icon="copy" v-on:clicked="copyToClipboard"
                        data-e2e="copy-to-clipboard-button"/>
                </div>
            </div>
            <div v-if="selectedSimulationResults && selectedSimulationResults.stale" class="simulation-result-container spacer-top error-bg">
                Stale simulation results. Please simulate the job again to update the results.
            </div>
            <div v-if="!selectedSimulationResults" class="simulation-result-container spacer-top">
                No simulation results available.
            </div>
            <div v-else-if="selectedSimulationResults.results.length === 0 && !selectedSimulationResults.errorCause"
                 class="simulation-result-container spacer-top">
                No simulation results available.
            </div>
            <div v-else-if="selectedSimulationResults.results.length > 0 && !selectedSimulationResults.errorCause"
                 v-for="(result, index) in selectedSimulationResults.results" v-bind:key="index"
                 class="simulation-result-container spacer-top" :data-e2e="'simulation-results-container-' + index">
                <simulation-result :data="result" :index="index" v-on:node-selected="createMustacheSelector"></simulation-result>
            </div>
            <div v-else-if="selectedSimulationResults.errorCause" class="simulation-result-container spacer-top">
        <pre class="error-bg">
<code>{{ selectedSimulationResults.errorCause }}</code></pre>
            </div>
        </div>
    </core-content>
</template>

<script>
import LayoutRow from '../common/layout-row'
import InputButton from '../common/input-button'
import CoreContent from '../common/core-content'
import SimulationResult from "@/components/jobs/simulation-result";

export default {
    name: 'simulation-result-container',
    components: {SimulationResult, CoreContent, InputButton, LayoutRow},
    props: ['selectedSimulationResults'],
    data: function () {
        return {
            mustacheSelector: null
        }
    },
    methods: {
        format: function (code) {
            return JSON.stringify(code, null, 2)
        },
        createMustacheSelector: function (selectedPath) {
            this.mustacheSelector = selectedPath.replace('root.', '{{') + '}}';
        },
        copyToClipboard: function () {
            const element = this.$refs.mustacheSelector;
            element.select();
            element.setSelectionRange(0, 99999);
            document.execCommand('copy');
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

.simulation-result-container {
    color: var(--color-font);
    background-color: var(--color-background);
    padding: .5em;
}

pre {
    overflow: auto;
    height: auto;
    word-break: normal !important;
    word-wrap: normal !important;
    white-space: pre !important;
}

.selector-container {
    display: flex;
    padding: .25em;
}

.mustache-selector {
    flex-grow: 1;
    border: none;
    background-color: var(--color-foreground);
    height: 2em;
    min-width: 20em;
    color: var(--color-font);
    margin-right: .25em;
}

.normal-bg {
    background-color: var(--color-foreground)
}

.error-bg {
    background-color: var(--color-alert)
}

</style>
