<template>
  <core-panel>
    <h1>
      <slot name="title">default title</slot>
    </h1>
    <button-row>
      <div class="filter" slot="left">
        <input id="filter-input" type="text" v-model="filterText" placeholder="filter list"/>
        <font-awesome-icon icon="search"/>
        <input-button icon="times" v-if="filterText.length > 0" v-on:clicked="filterText = ''"/>
      </div>
      <p slot="right">
        <router-link :to="addButtonTarget">
          <input-button icon="plus"/>
        </router-link>
      </p>
    </button-row>
  </core-panel>
</template>

<script>
import InputButton from './input-button'
import ButtonRow from './button-row'
import CorePanel from './core-panel'

export default {
  name: 'list-header',
  components: {CorePanel, ButtonRow, InputButton},
  props: ['filter', 'addButtonText', 'addButtonTarget'],
  data: function () {
    return {
      filterText: ''
    }
  },
  watch: {
    filterText: function (val, oldVal) {
      this.filter(val)
    }
  },
  mounted: function () {
    document.addEventListener('keydown', (e) => {
      if (e.keyCode === 27) {
        this.filterText = ''
      }
    })
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

</style>
