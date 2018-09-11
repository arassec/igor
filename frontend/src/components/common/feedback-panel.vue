<template>
    <div v-if="feedback.length > 0" class="feedback-panel">
        <alert-box v-if="feedback.length > 0 && !feedbackOk" :text="feedback"/>
        <info-box v-if="feedback.length > 0 && feedbackOk && requestInProgress" :text="feedback"
                  :icon="'spinner'" :iconClass="'fa-spin'"/>
        <info-box v-if="feedback.length > 0 && feedbackOk && !requestInProgress" :text="feedback"/>
    </div>
</template>

<script>
import AlertBox from './alert-box'
import InfoBox from './info-box'
export default {
  name: 'feedback-panel',
  components: {InfoBox, AlertBox},
  props: ['feedback', 'feedbackOk', 'requestInProgress']
}
</script>

<style scoped>

    .feeback-panel {
        margin-bottom: 10px;
    }

</style>
