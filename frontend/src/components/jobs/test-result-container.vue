<template>
    <core-content class="sticky">
        <div class="test-result-container">

            <layout-row>
                <div slot="left">
                    <h1>Dry run results</h1>
                </div>
                <input-button slot="right" icon="times" v-on:clicked="$emit('close')"/>
            </layout-row>

            <p v-if="!selectedTestResults">
                Please select a <b>Task</b> or <b>Action</b> to see detailed dry-run results.
            </p>
            <p v-else-if="selectedTestResults.results.length == 0 && !selectedTestResults.errorCause">
                No test data available.
            </p>
            <pre v-else-if="selectedTestResults.results.length > 0 && !selectedTestResults.errorCause" class="normal-bg">
<code>{{ format(selectedTestResults.results) }}</code></pre>
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
        flex: 2;
        margin-right: 0;
        overflow: hidden;
        max-height: calc(100vh - 20px);
    }

    .test-result-container {
        color: var(--color-font);
        background-color: var(--color-background);
        padding: 15px;
    }

    pre {
        overflow: auto;
        height: auto;
        max-height: calc(100vh / 1.5);
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

    .slide-fade-enter-active {
        transition: all .5s ease;
    }

    .slide-fade-leave-active {
        transition: all .5s ease;
    }

    .slide-fade-enter, .slide-fade-leave-to {
        opacity: 0;
    }

</style>
