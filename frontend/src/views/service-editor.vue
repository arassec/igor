<template>
    <core-container>

        <side-menu>
            <p slot="title">Service Configuration</p>
            <button-row slot="buttons">
                <p slot="left">
                    <input-button v-on:clicked="cancelConfiguration()" icon="arrow-left" class="button-margin-right"/>
                    <input-button v-on:clicked="testConfiguration()" icon="plug" class="button-margin-right"/>
                    <input-button v-on:clicked="saveConfiguration()" icon="save"/>
                </p>
            </button-row>
            <feedback-panel slot="feedback" :feedback="feedback" :alert="!feedbackOk"
                            :requestInProgress="requestInProgress"/>
        </side-menu>

        <core-content>
            <core-panel>
                <h1>
                    <font-awesome-icon icon="cogs"/>
                    {{ serviceConfiguration.name.length > 0 ? serviceConfiguration.name : 'Unnamed Service' }}
                </h1>
                <table>
                    <tr>
                        <td><label>Name</label></td>
                        <td><input type="text" autocomplete="off"
                                   v-model="serviceConfiguration.name"/></td>
                        <td>
                            <validation-error v-if="nameValidationError.length > 0">
                                {{nameValidationError}}
                            </validation-error>
                        </td>
                    </tr>
                    <tr>
                        <td><label for="category-input">Category</label></td>
                        <td>
                            <select id="category-input" v-model="selectedCategory"
                                    v-on:change="loadServiceTypes(selectedCategory)" :disabled="!newService">
                                <option v-for="category in serviceCategories" v-bind:value="category.type"
                                        v-bind:key="category.type">
                                    {{category.label}}
                                </option>
                            </select>
                        </td>
                        <td></td>
                    </tr>
                    <tr>
                        <td><label for="type-input">Type</label></td>
                        <td>
                            <select id="type-input" v-model="serviceConfiguration.serviceType"
                                    v-on:change="loadTypeParameters(serviceConfiguration.serviceType.type)"
                                    :disabled="!newService">
                                <option v-for="type in serviceTypes" v-bind:value="type" v-bind:key="type.type">
                                    {{type.label}}
                                </option>
                            </select>
                        </td>
                        <td></td>
                    </tr>
                </table>
            </core-panel>

            <core-panel v-if="Object.keys(serviceConfiguration.parameters).length > 0">
                <h2>Service Parameters</h2>
                <parameter-editor :parameters="serviceConfiguration.parameters" ref="parameterEditor"/>
            </core-panel>

        </core-content>

    </core-container>
</template>

<script>
import ParameterEditor from '../components/common/parameter-editor'
import InputButton from '../components/common/input-button'
import CorePanel from '../components/common/core-panel'
import CoreContainer from '../components/common/core-container'
import CoreContent from '../components/common/core-content'
import ButtonRow from '../components/common/button-row'
import ValidationError from '../components/common/validation-error'
import FeedbackPanel from '../components/common/feedback-panel'
import SideMenu from '../components/common/side-menu'

export default {
  name: 'service-editor',
  components: {
    SideMenu,
    FeedbackPanel,
    ValidationError,
    ButtonRow,
    CoreContent,
    CoreContainer,
    CorePanel,
    InputButton,
    ParameterEditor
  },
  props: ['serviceId'],
  data: function () {
    return {
      newService: true,
      serviceCategories: [],
      selectedCategory: '',
      serviceTypes: [],
      nameValidationError: '',
      feedback: '',
      feedbackOk: true,
      requestInProgress: false,
      serviceConfiguration: {
        name: '',
        serviceCategory: {},
        serviceType: {},
        parameters: {}
      }
    }
  },
  methods: {
    loadService: function (id) {
      this.feedback = ''
      this.feedbackOk = true
      let component = this
      this.$http.get('/api/service/' + id).then(function (response) {
        component.serviceConfiguration = response.data
        component.serviceCategories.push(component.serviceConfiguration.serviceCategory)
        component.selectedCategory = component.serviceConfiguration.serviceCategory.type
        component.serviceTypes.push(component.serviceConfiguration.serviceType)
      }).catch(function (error) {
        component.feedback = error
        component.feedbackOk = false
      })
    },
    loadServiceCategories: function () {
      let component = this
      this.$http.get('/api/servicecategory').then(function (response) {
        for (let i = component.serviceCategories.length; i > 0; i--) {
          component.serviceCategories.pop()
        }
        Array.from(response.data).forEach(function (item) {
          component.serviceCategories.push(item)
        })
        component.selectedCategory = component.serviceCategories[0].type
        component.loadServiceTypes(component.selectedCategory)
      }).catch(function (error) {
        component.feedback = error
        component.feedbackOk = false
      })
    },
    loadServiceTypes: function (category) {
      let component = this
      this.$http.get('/api/servicetype/' + category).then(function (response) {
        for (let i = component.serviceTypes.length; i > 0; i--) {
          component.serviceTypes.pop()
        }
        Array.from(response.data).forEach(function (item) {
          component.serviceTypes.push(item)
        })
        component.serviceConfiguration.serviceType = component.serviceTypes[0]
        component.loadTypeParameters(component.serviceConfiguration.serviceType.type)
      }).catch(function (error) {
        component.feedback = error
        component.feedbackOk = false
      })
    },
    loadTypeParameters: function (type) {
      let component = this
      this.$http.get('/api/serviceparams/' + type).then(function (response) {
        component.serviceConfiguration.parameters = response.data
      }).catch(function (error) {
        component.feedback = error
        component.feedbackOk = false
      })
    },
    testConfiguration: function () {
      if (!this.validateInput()) {
        return
      }

      this.feedback = 'Testing...'
      this.requestInProgress = true

      let component = this
      this.$http.post('/api/service/test', this.serviceConfiguration).then(function (response) {
        component.feedback = response.data
        component.feedbackOk = true
        component.requestInProgress = false
      }).catch(function (error) {
        component.feedback = error.response.data
        component.feedbackOk = false
        component.requestInProgress = false
      })
    },
    saveConfiguration: function () {
      if (!this.validateInput()) {
        return
      }

      this.feedback = 'Saving...'
      this.requestInProgress = true

      let component = this

      if (this.newService) {
        this.$http.post('/api/service', this.serviceConfiguration).then(function () {
          component.feedback = ''
          component.feedbackOk = true
          component.requestInProgress = false
          component.$root.$data.store.setFeedback('Service \'' + component.serviceConfiguration.name + '\' saved.', false)
        }).catch(function (error) {
          component.feedback = 'Saving failed! (' + error.response.data.error + ')'
          component.feedbackOk = false
          component.requestInProgress = false
        })
      } else {
        this.$http.put('/api/service', this.serviceConfiguration).then(function () {
          component.$root.$data.store.setFeedback('Service \'' + component.serviceConfiguration.name + '\' updated.', false)
          component.feedback = ''
          component.feedbackOk = true
          component.requestInProgress = false
        }).catch(function (error) {
          component.feedback = 'Saving failed! (' + error.response.data.error + ')'
          component.feedbackOk = false
          component.requestInProgress = false
        })
      }
    },
    cancelConfiguration: function () {
      this.$router.push({name: 'services'})
    },
    validateInput: function () {
      this.feedback = ''
      this.feedbackOk = true
      this.nameValidationError = ''

      let nameValidationResult = true

      if (this.serviceConfiguration.name == null || this.serviceConfiguration.name === '') {
        this.nameValidationError = 'Name must be set'
        nameValidationResult = false
      }

      let parameterValidationResult = true
      if (typeof this.$refs.parameterEditor !== 'undefined') {
        parameterValidationResult = this.$refs.parameterEditor.validateInput()
      }

      return (nameValidationResult && parameterValidationResult)
    }
  },
  mounted () {
    if (this.serviceId != null) {
      this.newService = false
      this.loadService(this.serviceId)
    } else {
      this.loadServiceCategories()
    }
  }
}
</script>

<style scoped>

    .service-parameters {
        margin: 25px 0px 0px 0px;
    }

</style>
