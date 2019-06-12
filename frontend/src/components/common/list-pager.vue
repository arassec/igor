<template>
    <div class="list-pager" v-if="page.totalPages > 0" :class="dark ? 'dark': ''">
        <input-button icon="angle-double-left" v-on:clicked="$emit('first')" class="pager-button margin"
                      :disabled="page.number == 0"/>
        <input-button icon="angle-left" v-on:clicked="$emit('previous')" class="pager-button margin"
                      :disabled="page.number == 0"/>
        {{(page.number + 1)}}/{{page.totalPages}}
        <input-button icon="angle-right" v-on:clicked="$emit('next')" class="pager-button margin"
                      :disabled="page.number == (page.totalPages -1)"/>
        <input-button icon="angle-double-right" v-on:clicked="$emit('last')" class="pager-button"
                      :disabled="page.number == (page.totalPages -1)"/>
    </div>
</template>

<script>
  import InputButton from "./input-button";

  export default {
    name: "list-pager",
    components: {InputButton},
    props: ['page', 'dark']
  }
</script>

<style scoped>

    .list-pager {
        margin: 10px;
        text-align: center;
    }

    .margin {
        margin-right: 5px;
    }

    .dark .pager-button {
        border: 1px solid var(--panel-background-color);
        background-color: var(--panel-background-color);
        color: var(--main-background-color);
    }

    .dark .pager-button:hover {
        border: 1px solid var(--info-background-color);
        background-color: var(--info-background-color);
    }

    .dark .disabled:hover {
        border: 1px solid var(--panel-background-color);
        background-color: var(--panel-background-color);
    }

</style>
