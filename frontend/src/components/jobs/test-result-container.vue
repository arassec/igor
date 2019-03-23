<template>
    <transition name="slide-fade">
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
    </transition>
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
        flex: 2;
        margin-right: 0px;
        overflow: auto;
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
        word-break: normal !important;
        word-wrap: normal !important;
        white-space: pre !important;
        background-color: var(--info-background-color)
    }

    .slide-fade-enter-active {
        transition: all .5s ease;
    }

    .slide-fade-leave-active {
        transition: all .5s cubic-bezier(1.0, 0.5, 0.8, 1.0);
    }

    .slide-fade-enter, .slide-fade-leave-to {
        transform: translateX(10px);
        opacity: 0;
    }

</style>
