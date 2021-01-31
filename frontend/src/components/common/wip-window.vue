<template>
    <modal-dialog v-if="wip.message.length > 0" data-e2e="wip-modal">
        <h1 slot="footer">
            <font-awesome-icon icon="circle-notch" class="fa-spin spinner"/>
            {{ wip.message }}
            <input-button class="cancel-button" icon="times" v-if="wip.cancelCallback" v-on:clicked="cancel"/>
        </h1>
    </modal-dialog>
</template>

<script>
import ModalDialog from './modal-dialog'
import InputButton from "@/components/common/input-button";

export default {
    name: 'wip-window',
    components: {InputButton, ModalDialog},
    methods: {
        cancel: function() {
            this.$root.$data.store.getWip().cancelCallback();
        }
    },
    computed: {
        wip: function () {
            return this.$root.$data.store.getWip()
        }
    }
}
</script>

<style scoped>

h1 {
    margin-bottom: 0px;
}

.spinner {
    margin: 0 .25em 0 0;
}

.cancel-button {
    margin: .25em 0 0 5em;
    float: right;
}

</style>
