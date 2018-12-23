<template>
  <div v-if="feedback.length > 0">
    <feedback-box v-if="feedback.length > 0 && alert" :text="feedback" :alert="alert"/>
    <feedback-box v-if="feedback.length > 0 && !alert && requestInProgress" :text="feedback"
                  :icon="'spinner'" :iconClass="'fa-spin'"/>
    <feedback-box v-if="feedback.length > 0 && !alert && !requestInProgress" :text="feedback"/>
  </div>
</template>

<script>
import FeedbackBox from './feedback-box'

export default {
  name: 'feedback-panel',
  components: {FeedbackBox},
  props: ['feedback', 'alert', 'requestInProgress']
}
</script>

<style scoped>

</style>
