<template>
    <div>
        <feedback-box :text="'Test'" :alert="jobExecution.state === 'FAILED'"/>
        <input-button icon="info"/>
    </div>
</template>

<script>
import InputButton from '../common/input-button'
import FeedbackBox from '../common/feedback-box'
export default {
  name: 'job-execution-panel',
  components: {FeedbackBox, InputButton},
  props: ['jobExecution']
}
</script>

<style scoped>

</style>
