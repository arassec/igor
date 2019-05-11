<template>
  <core-panel>
    <h1>
      <slot name="title">default title</slot>
    </h1>
    <layout-row>
      <div class="filter" slot="left">
        <input id="filter-input" type="text" v-model="filterText" placeholder="filter list"/>
        <font-awesome-icon icon="search"/>
        <input-button icon="times" v-if="filterText.length > 0" v-on:clicked="filterText = ''" class="button-margin-left"/>
      </div>
      <p slot="right">
        <router-link :to="addButtonTarget" v-if="addButtonTarget != null">
          <input-button icon="plus"/>
        </router-link>
      </p>
    </layout-row>
  </core-panel>
</template>

<script>
import InputButton from './input-button'
import LayoutRow from './layout-row'
import CorePanel from './core-panel'

export default {
  name: 'list-header',
  components: {CorePanel, LayoutRow, InputButton},
  props: ['filter', 'filterKey', 'addButtonText', 'addButtonTarget'],
  data: function () {
    return {
      filterText: ''
    }
  },
  watch: {
    filterText: function (val) {
      this.$root.$data.store.setValue(this.filterKey, val)
      this.filter(val)
    }
  },
  mounted: function () {
    document.addEventListener('keydown', (e) => {
      if (e.keyCode === 27) {
        this.filterText = ''
      }
    })
    if (this.$root.$data.store.getValue(this.filterKey)) {
      this.filterText = this.$root.$data.store.getValue(this.filterKey)
    }
  }
}
</script>

<style scoped>

  .filter {
    position: relative;
  }

  .filter >>> input {
    padding-left: 25px;
  }

  .filter >>> input:focus + .fa-search {
    color: var(--panel-background-color);
  }

  .filter .fa-search {
    position: absolute;
    top: 5px;
    left: 7px;
    font-size: 15px;
  }

  ::placeholder { /* Chrome, Firefox, Opera, Safari 10.1+ */
    color: var(--font-color-light);
    opacity: 1; /* Firefox */
  }

  :-ms-input-placeholder { /* Internet Explorer 10-11 */
    color: var(--font-color-light);
  }

  ::-ms-input-placeholder { /* Microsoft Edge */
    color: var(--font-color-light);
  }

</style>
