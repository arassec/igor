<template>
    <div class="filter">
        <label for="input-filter">{{label}} </label>
        <input id="input-filter" type="text" v-model="filterText"/>
    </div>
</template>

<script>
    export default {
        name: "input-filter",
        components: {},
        props: ['filter', 'filterKey', 'label'],
        data: function () {
            return {
                filterText: ''
            }
        },
        watch: {
            filterText: function (val) {
                this.$root.$data.store.setValue(this.filterKey, val);
                this.filter(val)
            }
        },
        mounted: function () {
            document.addEventListener('keydown', (e) => {
                if (e.keyCode === 27) {
                    this.filterText = ''
                }
            });
            if (this.$root.$data.store.getValue(this.filterKey)) {
                this.filterText = this.$root.$data.store.getValue(this.filterKey)
            }
        }
    }
</script>

<style scoped>

    .filter {
        position: relative;
        color: var(--color-font);
        margin: .15em 1em 0 0;
    }

    .filter >>> input {
        padding: .25em .25em .25em .25em;
        line-height: 1em;
        color: var(--color-font);
        background-color: var(--color-foreground);
        border: none;
    }

</style>