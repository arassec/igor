<template>
    <div class="row">
        <div class="left">
            <slot name="left"/>
        </div>
        <div class="right">
            <slot name="right"/>
        </div>
    </div>
</template>

<script>
export default {
  name: 'button-row'
}
</script>

<style scoped>

    .row {
        display: flex;
        flex-direction: row;
    }

    .left {

    }

    .right {
        margin-left: auto;
    }

    .margin-right {
        margin-right: 5px;
    }

</style>
