<template>
    <core-content>
        <div class="test-result-container">
            <button-row>
                <h1 slot="left">{{heading}}</h1>
                <input-button slot="right" icon="times" v-on:clicked="$emit('close')"/>
            </button-row>
            <pre>
      <code>
{{ format(selectedTestResults) }}
      </code>
    </pre>
        </div>
    </core-content>
</template>

<script>
import ButtonRow from '../common/button-row'
import InputButton from '../common/input-button'
import CoreContent from '../common/core-content'

export default {
  name: 'test-result-container',
  components: {CoreContent, InputButton, ButtonRow},
  props: ['heading', 'selectedTestResults'],
  methods: {
    format: function (code) {
      return JSON.stringify(code, null, 2)
    }
  }
}
</script>

<style scoped>

    .content {
        flex-shrink: 1;
    }

    .test-result-container {
        color: var(--font-color-light);
        background-color: var(--nav-background-color);
        padding: 15px;
        -webkit-box-shadow: 2px 2px 5px 0px rgba(163, 163, 163, 0.75);
        -moz-box-shadow: 2px 2px 5px 0px rgba(163, 163, 163, 0.75);
        box-shadow: 2px 2px 5px 0px rgba(163, 163, 163, 0.75);
    }

    pre {
        white-space: pre-wrap;
    }

</style>
