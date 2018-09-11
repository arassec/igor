<template>
    <transition name="modal">
        <div class="modal-mask" @click="close">
            <div class="modal-wrapper">
                <div class="modal-container center" @click.stop>

                    <div class="modal-header">
                        <h1>
                            <slot name="header">
                                default header
                            </slot>
                        </h1>
                    </div>

                    <div class="modal-body">
                        <slot name="body">
                            default body
                        </slot>
                    </div>

                    <div class="modal-footer">
                        <slot name="footer">

                        </slot>
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
        background-color: var(--panel-background-color);
        transition: all .3s ease;
        display: flex;
        flex-direction: column;
        align-items: flex-start
    }

    .modal-header {
        overflow:hidden;
    }

    .modal-body {
        margin-bottom: 20px;
        overflow:hidden;
    }

    .modal-footer {
        width: 100%;
    }

    .modal-enter {
        opacity: 0;
    }

    .modal-leave-active {
        opacity: 0;
    }

    .modal-enter .modal-container,
    .modal-leave-active .modal-container {
        -webkit-transform: scale(1.1);
        transform: scale(1.1);
    }

    .center {
        position: fixed;
        top: 50%;
        left: 50%;
        width: auto;
        height: auto;
        -webkit-transform: translate(-50%,-50%);
        -moz-transform: translate(-50%,-50%);
        -ms-transform: translate(-50%,-50%);
        -o-transform: translate(-50%,-50%);
        transform: translate(-50%,-50%);
    }

</style>
