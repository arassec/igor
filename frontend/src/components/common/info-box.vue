<template>
    <div>
        <font-awesome-icon v-if="icon != null && iconClass != null" :icon="icon" :class="[iconClass, 'margin-left']"/>
        <font-awesome-icon v-if="icon != null && iconClass == null" :icon="icon" class="margin-left"/>
        <label class="margin-left">{{text}}</label>
    </div>
</template>

<script>
export default {
  name: 'info-box',
  props: ['text', 'icon', 'iconClass']
}
</script>

<style scoped>

    div {
        color: var(--font-color-light);
        background-color: var(--info-background-color);
        line-height: 25px;
        margin-bottom: 10px;
        max-width: calc(var(--content-width) - 30px);
    }

    .margin-left {
        margin-left: 10px;
    }

</style>
