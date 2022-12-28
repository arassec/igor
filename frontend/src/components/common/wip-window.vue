<template>
    <modal-dialog v-if="wip.message.length > 0" data-e2e="wip-modal">
        <template v-slot:footer>
            <h1>
                <font-awesome-icon icon="circle-notch" class="fa-spin spinner" />
                {{ wip.message }}
                <input-button class="cancel-button" icon="times" v-if="wip.cancelCallback" v-on:clicked="cancel" />
            </h1>
        </template>
    </modal-dialog>
</template>

<script>
import ModalDialog from "./modal-dialog.vue";
import InputButton from "@/components/common/input-button.vue";
import { useWipStore } from "@/stores/wip";

export default {
    name: "wip-window",
    components: { InputButton, ModalDialog },
    methods: {
        cancel: function () {
            useWipStore().getWip().cancelCallback();
        },
    },
    computed: {
        wip: function () {
            return useWipStore().getWip();
        },
    },
};
</script>

<style scoped>
h1 {
    margin-bottom: 0px;
}

.spinner {
    margin: 0 0.25em 0 0;
}

.cancel-button {
    margin: 0.25em 0 0 5em;
    float: right;
}
</style>
