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
                            <select id="category-input" v-model="serviceConfiguration.category"
                                    v-on:change="loadServiceTypes(serviceConfiguration.category.key)"
                                    :disabled="!newService">
                                <option v-for="category in serviceCategories" v-bind:value="category"
                                        v-bind:key="category.key">
                                    {{category.label}}
                                </option>
                            </select>
                        </td>
                        <td></td>
                    </tr>
                    <tr>
                        <td><label for="type-input">Type</label></td>
                        <td>
                            <select id="type-input" v-model="serviceConfiguration.type"
                                    v-on:change="loadTypeParameters(serviceConfiguration.type.key)"
                                    :disabled="!newService">
                                <option v-for="type in serviceTypes" v-bind:value="type" v-bind:key="type.key">
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
  import SideMenu from '../components/common/side-menu'
  import FormatUtils from '../utils/format-utils.js'

  export default {
    name: 'service-editor',
    components: {
      SideMenu,
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
        serviceTypes: [],
        nameValidationError: '',
        loadParameters: true,
        serviceConfiguration: {
          name: '',
          category: {},
          type: {},
          parameters: {}
        }
      }
    },
    methods: {
      loadService: function (id) {
        let component = this
        this.$http.get('/api/service/' + id).then(function (response) {
          component.serviceConfiguration = response.data
          component.serviceCategories.push(component.serviceConfiguration.category)
          component.selectedCategory = component.serviceConfiguration.category.key
          component.serviceTypes.push(component.serviceConfiguration.type)
        }).catch(function (error) {
          component.$root.$data.store.setFeedback('Loading service failed! (' + error + ')', true)
        })
      },
      loadServiceCategories: function (singleServiceCategory) {
        let component = this
        this.$http.get('/api/category/service').then(function (response) {
          for (let i = component.serviceCategories.length; i > 0; i--) {
            component.serviceCategories.pop()
          }
          Array.from(response.data).forEach(function (item) {
            if (singleServiceCategory) {
              if (item.key === singleServiceCategory) {
                component.serviceCategories.push(item)
              }
            } else {
              component.serviceCategories.push(item)
            }
          })
          if (!('key' in component.serviceConfiguration.category)) {
            component.serviceConfiguration.category = component.serviceCategories[0]
          }
          component.loadServiceTypes(component.serviceConfiguration.category.key)
        }).catch(function (error) {
          component.$root.$data.store.setFeedback('Loading categories failed! (' + error + ')', true)
        })
      },
      loadServiceTypes: function (category) {
        let component = this
        this.$http.get('/api/type/service/' + category).then(function (response) {
          for (let i = component.serviceTypes.length; i > 0; i--) {
            component.serviceTypes.pop()
          }
          Array.from(response.data).forEach(function (item) {
            component.serviceTypes.push(item)
          })
          if (!('key' in component.serviceConfiguration.type)) {
            component.serviceConfiguration.type = component.serviceTypes[0]
          }
          component.loadTypeParameters(component.serviceConfiguration.type.key)
        }).catch(function (error) {
          component.$root.$data.store.setFeedback('Loading types failed! (' + error + ')', true)
        })
      },
      loadTypeParameters: function (type) {
        let component = this
        if (component.loadParameters) {
          this.$http.get('/api/parameters/service/' + type).then(function (response) {
            component.serviceConfiguration.parameters = response.data
          }).catch(function (error) {
            component.$root.$data.store.setFeedback('Loading type parameters failed! (' + error + ')', true)
          })
        } else {
          // after we skipped for the first time, always update parameters
          component.loadParameters = true
        }
      },
      testConfiguration: function () {
        if (!this.validateInput()) {
          return
        }

        this.$root.$data.store.setWip('Testing service')

        let component = this
        this.$http.post('/api/service/test', this.serviceConfiguration).then(function () {
          component.$root.$data.store.setFeedback('Test OK.', false)
          component.$root.$data.store.clearWip()
        }).catch(function (error) {
          component.$root.$data.store.setFeedback('Testing failed! (' + error + ')', true)
          component.$root.$data.store.clearWip()
        })
      },
      saveConfiguration: function () {
        if (!this.validateInput()) {
          return
        }

        this.$root.$data.store.setWip('Saving service')

        let component = this
        if (this.newService) {
          this.$http.post('/api/service', this.serviceConfiguration).then(function (response) {
            component.serviceConfiguration = response.data;
            component.$root.$data.store.setFeedback('Service \'' + FormatUtils.formatNameForSnackbar(component.serviceConfiguration.name) + '\' saved.', false)
            component.$root.$data.store.clearWip()
            component.newService = false
            component.$router.push({name: 'service-editor', params: {serviceId: component.serviceConfiguration.id}})
            let jobData = component.$root.$data.store.getJobData()
            if (jobData.jobConfiguration != null) {
              let serviceParameter = {
                name: component.serviceConfiguration.name,
                id: component.serviceConfiguration.id
              }
              jobData.serviceParameter = serviceParameter
            }
          }).catch(function (error) {
            if (error.response.data === 'NAME_ALREADY_EXISTS_ERROR') {
              component.nameValidationError = 'A service with this name already exists!'
            }
            component.$root.$data.store.setFeedback('Saving failed! (' + error + ')', true)
            component.$root.$data.store.clearWip()
          })
        } else {
          this.$http.put('/api/service', this.serviceConfiguration).then(function () {
            component.$root.$data.store.setFeedback('Service \'' + FormatUtils.formatNameForSnackbar(component.serviceConfiguration.name) + '\' updated.', false)
            component.$root.$data.store.clearWip()
          }).catch(function (error) {
            if (error.response.data === 'NAME_ALREADY_EXISTS_ERROR') {
              component.nameValidationError = 'A service with this name already exists!'
            }
            component.$root.$data.store.setFeedback('Saving failed! (' + error + ')', true);
            component.$root.$data.store.clearWip()
          })
        }
      },
      cancelConfiguration: function () {
        let jobData = this.$root.$data.store.getJobData()
        if (jobData.jobConfiguration != null) {
          this.$router.push({name: 'job-editor'})
        } else {
          this.$router.push({name: 'services'})
        }
      },
      validateInput: function () {
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
    mounted() {
      let serviceData = this.$root.$data.store.getServiceData()
      if (serviceData.serviceConfiguration != null) {
        this.serviceConfiguration = serviceData.serviceConfiguration
        this.loadParameters = false
        this.loadServiceCategories(null)
      } else {
        if (this.serviceId != null) {
          this.newService = false
          this.loadService(this.serviceId)
        } else {
          let jobData = this.$root.$data.store.getJobData()
          if (jobData.serviceCategory != null) {
            this.loadServiceCategories(jobData.serviceCategory)
          } else {
            this.loadServiceCategories(null)
          }
        }
      }
    }
  }
</script>

<style scoped>

    .service-parameters {
        margin: 25px 0px 0px 0px;
    }

</style>
