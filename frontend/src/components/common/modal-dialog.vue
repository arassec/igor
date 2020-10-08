<template>
    <transition name="animate-css-modal"
                enter-active-class="animated fadeIn"
                leave-active-class="animated fadeOut">
        <div class="modal-mask" @click="close">
            <div class="modal-wrapper">
                <div class="modal-container center" @click.stop>

                    <div class="modal-header" v-if="hasHeader">
                        <slot name="header"/>
                    </div>

                    <div class="modal-body" v-if="hasBody">
                        <slot name="body"/>
                    </div>

                    <div class="modal-footer" v-if="hasFooter">
                        <slot name="footer"/>
                    </div>
                </div>
            </div>
        </div>
    </transition>
</template>

<script>
export default {
    name: 'modal-dialog',
    methods: {
        close: function () {
            this.$emit('close')
        }
    },
    computed: {
        hasHeader() {
            return this.$slots.header
        },
        hasBody() {
            return this.$slots.body
        },
        hasFooter() {
            return this.$slots.footer
        }
    }
}
</script>

<style scoped>
.modal-mask {
    position: fixed;
    z-index: 9998;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, .5);
    display: table;
    transition: opacity .3s ease;
}

.modal-wrapper {
    display: table-cell;
    vertical-align: middle;
}

.modal-container {
    min-width: 500px;
    max-width: 75%;
    padding: 15px;
    background-color: var(--color-background);
    transition: all .3s ease;
    display: flex;
    flex-direction: column;
    align-items: flex-start
}

.modal-header {
    overflow: hidden;
    width: 100%;
}

.modal-body {
    margin-bottom: 20px;
    overflow: hidden;
    width: 100%;
    color: var(--color-font);
}

.modal-footer {
    width: 100%;
    color: var(--color-font);
}

.center {
    position: fixed;
    top: 50%;
    left: 50%;
    width: auto;
    height: auto;
    -webkit-transform: translate(-50%, -50%);
    -moz-transform: translate(-50%, -50%);
    -ms-transform: translate(-50%, -50%);
    -o-transform: translate(-50%, -50%);
    transform: translate(-50%, -50%);
}

/* animate.css animation speed */
.animated {
    -webkit-animation-duration: var(--animate-css-duration);
    animation-duration: var(--animate-css-duration);
    -webkit-animation-fill-mode: both;
    animation-fill-mode: both;
}

</style>
