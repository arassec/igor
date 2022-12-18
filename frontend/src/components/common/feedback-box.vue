<template>
    <layout-row :class="{info: !alert,  alert: alert}" class="margin-bottom"
                v-on:clicked="clickable ? $emit('feedback-clicked') : false"
                :clickable="clickable" :align-left="alignLeft">
        <template v-slot:left>
            <slot name="left"/>
        </template>
        <template v-slot:right>
            <slot name="right"/>
        </template>
    </layout-row>
</template>

<script>
import LayoutRow from './layout-row'

export default {
    name: 'feedback-box',
    components: {LayoutRow},
    props: ['alert', 'clickable', 'alignLeft']
}
</script>

<style scoped>

div {
    color: var(--color-font);
    line-height: 25px;
    padding: 5px;
}

.margin-bottom {
    margin-bottom: 5px;
}

.info {
    background-color: var(--color-foreground);
}

.alert {
    background-color: var(--color-alert);
}

</style>
