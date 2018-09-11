<template>
    <div class="header-container">
        <h1>
            <slot name="title">default title</slot>
        </h1>
        <div class="button-row">
            <input type="text" v-model="filterText"/>
            <font-awesome-icon v-if="filterText.length <= 0" class="search-icon" icon="search"/>
            <font-awesome-icon v-if="filterText.length > 0" class="clear-icon" icon="times"
                v-on:click="filterText = ''"/>
            <router-link :to="addButtonTarget">
                <input-button>
                    <font-awesome-icon icon="plus"/>
                    {{addButtonText}}
                </input-button>
            </router-link>
        </div>
    </div>
</template>

<script>
import InputButton from './input-button'

export default {
  name: 'list-header',
  components: {InputButton},
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

    .header-container {
        display: flex;
        flex-direction: column;
        background-color: var(--panel-background-color);
        color: var(--font-color-light);
        padding: 15px;
        margin-bottom: 10px;
        -webkit-box-shadow: 2px 2px 5px 0px rgba(163, 163, 163, 0.75);
        -moz-box-shadow: 2px 2px 5px 0px rgba(163, 163, 163, 0.75);
        box-shadow: 2px 2px 5px 0px rgba(163, 163, 163, 0.75);
    }

    .header-container h1 {
        font-size: 150%;
        color: var(--font-color-light);
    }

    .button-row {
        display: flex;
    }

    .button-row input {
        border: none;
        background-color: var(--element-background-color);
        height: 25px;
        min-width: 300px;
        color: var(--font-color-light);
        padding-left: 5px;
    }

    .button-row input:focus {
        background-color: var(--element-background-color-focus);
        color: var(--panel-background-color);
        outline: none;
    }

    .button-row input:focus + .search-icon {
        color: var(--element-background-color);
    }

    .button-row a {
        margin-left: auto;
    }

    .search-icon {
        margin: 5px 0px 0px -20px;
        color: var(--element-background-color-focus);
    }

    .clear-icon {
        margin: 5px 0px 0px -20px;
        color: var(--element-background-color);
    }

    .clear-icon:hover {
        color: var(--font-color-dark);
        cursor: pointer;
    }

</style>
