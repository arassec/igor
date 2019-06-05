<template>
    <core-content class="sticky">
        <div class="test-result-container">
            <layout-row>
                <div slot="left">
                    <h1 v-if="!errorCause && !selectedTestResults">Dry run successful</h1>
                    <h1 v-if="!errorCause && selectedTestResults">Dry run details</h1>
                    <h1 v-if="selectedTestResults && errorCause">Dry run failed</h1>
                </div>
                <input-button slot="right" icon="times" v-on:clicked="$emit('close')"/>
            </layout-row>
            <p v-if="!errorCause && !selectedTestResults">
                Please select a <b>Task</b> or <b>Action</b> to see detailed dry-run results.
            </p>
            <p v-if="!errorCause && selectedTestResults.length == 0">
                No test data available.
            </p>
            <pre v-if="!errorCause && selectedTestResults.length > 0" class="normal-bg">
<code>{{ format(selectedTestResults) }}</code></pre>
            <pre v-if="errorCause" class="error-bg">
<code>{{ errorCause }}</code></pre>
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
    props: ['heading', 'selectedTestResults', 'errorCause'],
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
        margin-right: 0px;
        overflow: hidden;
        max-height: calc(100vh - 20px);
    }

    .test-result-container {
        color: var(--font-color-light);
        background-color: var(--panel-background-color);
        padding: 15px;
        -webkit-box-shadow: 2px 2px 5px 0px rgba(163, 163, 163, 0.75);
        -moz-box-shadow: 2px 2px 5px 0px rgba(163, 163, 163, 0.75);
        box-shadow: 2px 2px 5px 0px rgba(163, 163, 163, 0.75);
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
        background-color: var(--info-background-color)
    }

    .error-bg {
        background-color: var(--alert-background-color)
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
