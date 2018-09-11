<template>
    <modal-dialog>
        <p slot="header">Select Service</p>
        <p slot="body">
        <template>
            <div class="service-entry" v-for="service in services" :key="service.id">
                <div class="service-entry-name">
                    {{ service.name }}
                </div>
            </div>
        </template>
        </p>
    </modal-dialog>
</template>

<script>
import ModalDialog from '../common/modal-dialog'
import CorePanel from '../common/core-panel'
import ServiceEditor from './service-editor'

export default {
  name: 'service-picker',
  components: {ServiceEditor, CorePanel, ModalDialog},
  props: ['serviceCategory'],
  data: function () {
    return {
      services: [],
      feedback: '',
      feedbackOk: true
    }
  },
  methods: {
    loadServices: function () {
      this.feedback = ''
      this.feedbackOk = true
      let component = this
      this.$http.get('/api/service/category/' + this.serviceCategory).then(function (response) {
        for (let i = component.services.length; i > 0; i--) {
          component.services.pop()
        }
        Array.from(response.data).forEach(function (item) {
          component.services.push(item)
        })
        component.services.sort((a, b) => a.localeCompare(b))
      }).catch(function (error) {
        component.feedback = error
        component.feedbackOk = false
      })
    }
  },
  mounted () {
    this.loadServices()
  }
}
</script>

<style scoped>

    .service-entry {
    }

    .service-entry-name {
        margin-top: 2px;
        font-weight: bold;
    }

</style>
