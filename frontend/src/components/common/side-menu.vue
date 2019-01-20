<template>
    <core-content class="side-menu">
        <core-panel>
            <h1>
                <slot name="title"/>
            </h1>
            <div class="buttons">
                <slot name="buttons"/>
            </div>
            <div class="content" v-if="hasContentSlot">
                <slot name="content"/>
            </div>
            <div class="feedback">
                <slot name="feedback"/>
            </div>
        </core-panel>
    </core-content>
</template>

<script>
import CoreContent from './core-content'
import CorePanel from './core-panel'

export default {
  name: 'side-menu',
  components: {CorePanel, CoreContent},
  computed: {
    hasContentSlot: function () {
      return !!this.$slots['content']
    }
  }
}
</script>

<style scoped>

    .buttons {
        padding-bottom: 15px;
        border-bottom: 1px solid var(--font-color-light);
        margin-bottom: 15px;
    }

    .content {
        padding-bottom: 15px;
        border-bottom: 1px solid var(--font-color-light);
        margin-bottom: 15px;
    }

    .feedback {

    }

    .side-menu {
        min-width: 445px;
    }
</style>
