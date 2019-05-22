<template>
    <core-content class="side-menu">
        <core-panel>
            <h1 class="no-margin">
                <slot name="title"/>
            </h1>
            <div class="header sticky">
                <slot name="header"/>
            </div>
            <div class="content" v-if="hasContentSlot">
                <slot name="content"/>
            </div>
            <div class="footer">
                <slot name="footer"/>
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

    .no-margin {
        margin-bottom: 0px;
    }

    .header {
        padding-top: 15px;
        padding-bottom: 15px;
        border-bottom: 1px solid var(--font-color-light);
        margin-bottom: 15px;
        z-index: 666;
        background-color: var(--panel-background-color);
    }

    .content {
        padding-bottom: 15px;
        border-bottom: 1px solid var(--font-color-light);
        margin-bottom: 15px;
    }

    .sticky {
        z-index: 5;
        position: sticky;
        top: 0px;
    }

</style>
