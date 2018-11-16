<template>
  <transition name="fade">
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
  </transition>
</template>

<script>
import ButtonRow from '../common/button-row'
import InputButton from '../common/input-button'

export default {
  name: 'test-result-container',
  components: {InputButton, ButtonRow},
  props: ['heading', 'selectedTestResults'],
  methods: {
    format: function (code) {
      return JSON.stringify(code, null, 2)
    }
  }
}
</script>

<style scoped>

  .test-result-container {
    margin-left: 45px;
    color: var(--font-color-light);
    background-color: var(--nav-background-color);
    padding: 15px;
  }

  .fade-enter-active, .fade-leave-active {
    transition: max-width .2s linear;
    max-width: var(--content-width);
  }

  .fade-enter, .fade-leave-to {
    max-width: 0px;
  }

</style>
