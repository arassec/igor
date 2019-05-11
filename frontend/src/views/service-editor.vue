<template>
    <core-container>

        <side-menu class="side-menu-small">
            <p slot="title">Service Configuration</p>
            <layout-row slot="header">
                <p slot="left">
                    <input-button v-on:clicked="cancelConfiguration()" icon="arrow-left" class="button-margin-right"/>
                    <input-button v-on:clicked="testConfiguration()" icon="plug" class="button-margin-right"/>
                    <input-button v-on:clicked="saveConfiguration()" icon="save"/>
                </p>
            </layout-row>
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
                                    v-on:change="loadTypesOfCategory(serviceConfiguration.category.key, true).then(() => {
                                        loadParametersOfType(serviceConfiguration.type.key)})" :disabled="!newService">
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
                                    v-on:change="loadParametersOfType(serviceConfiguration.type.key)"
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

            <core-panel>
                <h2>Service Parameters</h2>
                <parameter-editor v-if="Object.keys(serviceConfiguration.parameters).length > 0"
                                  :parameters="serviceConfiguration.parameters" ref="parameterEditor"/>
                <p v-if="Object.keys(serviceConfiguration.parameters).length == 0">
                    This service has no parameters to configure.
                </p>
            </core-panel>

        </core-content>

        <background-icon right="true" icon-one="cogs"/>

    </core-container>
</template>

<script>
  import ParameterEditor from '../components/common/parameter-editor'
  import InputButton from '../components/common/input-button'
  import CorePanel from '../components/common/core-panel'
  import CoreContainer from '../components/common/core-container'
  import CoreContent from '../components/common/core-content'
  import LayoutRow from '../components/common/layout-row'
  import ValidationError from '../components/common/validation-error'
  import SideMenu from '../components/common/side-menu'
  import FormatUtils from '../utils/format-utils.js'
  import IgorBackend from '../utils/igor-backend.js'
  import BackgroundIcon from "../components/common/background-icon";

  export default {
    name: 'service-editor',
    components: {
      BackgroundIcon,
      SideMenu,
      ValidationError,
      LayoutRow,
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
        IgorBackend.getData('/api/service/' + id).then((serviceConfiguration) => {
          this.serviceConfiguration = serviceConfiguration
          this.serviceCategories.push(this.serviceConfiguration.category)
          this.serviceTypes.push(this.serviceConfiguration.type)
          this.newService = false
        })
      },
      loadCategories: async function (fixedCategoryKey) {
        await IgorBackend.getData('/api/category/service').then((categories) => {
          for (let i = this.serviceCategories.length; i > 0; i--) {
            this.serviceCategories.pop()
          }
          let component = this
          Array.from(categories).forEach(function (item) {
            if (fixedCategoryKey) {
              if (item.key === fixedCategoryKey) {
                component.serviceCategories.push(item)
              }
            } else {
              component.serviceCategories.push(item)
            }
          })
          if (!('key' in this.serviceConfiguration.category)) {
            this.serviceConfiguration.category = this.serviceCategories[0]
          }
        })
      },
      loadTypesOfCategory: async function (categoryKey, selectFirst) {
        await IgorBackend.getData('/api/type/service/' + categoryKey).then((types) => {
          for (let i = this.serviceTypes.length; i > 0; i--) {
            this.serviceTypes.pop()
          }
          let component = this
          Array.from(types).forEach(function (item) {
            component.serviceTypes.push(item)
          })
          if (selectFirst) {
            this.serviceConfiguration.type = this.serviceTypes[0]
          }
        })
      },
      loadParametersOfType: function (typeKey) {
        IgorBackend.getData('/api/parameters/service/' + typeKey).then((parameters) => {
          this.serviceConfiguration.parameters = parameters
        })
      },
      testConfiguration: async function () {
        if (!(await this.validateInput())) {
          return
        }
        IgorBackend.postData('/api/service/test', this.serviceConfiguration, 'Testing service', 'Test OK.', 'Testing Failed!')
      },
      saveConfiguration: async function () {
        if (!(await this.validateInput())) {
          return
        }
        if (this.newService) {
          IgorBackend.postData('/api/service', this.serviceConfiguration, 'Saving service',
              'Service \'' + FormatUtils.formatNameForSnackbar(this.serviceConfiguration.name) + '\' saved.',
              'Saving failed!').then((result) => {
            this.serviceConfiguration = result;
            this.newService = false
            this.$router.push({name: 'service-editor', params: {serviceId: this.serviceConfiguration.id}})
            let jobData = this.$root.$data.store.getJobData()
            if (jobData.jobConfiguration != null) {
              let serviceParameter = {
                name: this.serviceConfiguration.name,
                id: this.serviceConfiguration.id
              }
              jobData.serviceParameter = serviceParameter
            }
          })
        } else {
          IgorBackend.putData('/api/service', this.serviceConfiguration, 'Saving service',
              'Service \'' + FormatUtils.formatNameForSnackbar(this.serviceConfiguration.name) + '\' updated.',
              'Saving failed!')
        }
      },
      cancelConfiguration: function () {
        let jobData = this.$root.$data.store.getJobData()
        if (jobData.jobConfiguration != null) {
          this.$router.push({name: 'job-editor'})
        } else {
          this.$router.push({name: 'app-status'})
        }
      },
      validateInput: async function () {
        this.nameValidationError = ''

        if (this.serviceConfiguration.name == null || this.serviceConfiguration.name === '') {
          this.nameValidationError = 'Name must be set'
        } else {
          let nameAlreadyExists = await IgorBackend.getData('/api/service/check/'
              + this.serviceConfiguration.name + '/' + (this.serviceConfiguration.id === undefined ? -1 : this.serviceConfiguration.id))
          if (nameAlreadyExists === true) {
            this.nameValidationError = 'A service with this name already exists!'
          }
        }

        let parameterValidationResult = true
        if (typeof this.$refs.parameterEditor !== 'undefined') {
          parameterValidationResult = this.$refs.parameterEditor.validateInput()
        }

        return ((this.nameValidationError.length === 0) && parameterValidationResult)
      }
    },
    mounted() {
      let serviceData = this.$root.$data.store.getServiceData()
      // Service duplication: don't load type parameters because they are provided by the root service configuration
      if (serviceData.serviceConfiguration != null) {
        this.serviceConfiguration = serviceData.serviceConfiguration
        this.loadCategories(null).then(() => {
          this.loadTypesOfCategory(this.serviceConfiguration.category.key, false)
        })
      } else {
        // Load a service configuration from the backend
        if (this.serviceId != null) {
          this.loadService(this.serviceId)
        } else {
          // Create a new service from within a job configuration. The category is fixed since the job requires it.
          let jobData = this.$root.$data.store.getJobData()
          if (jobData.serviceCategory != null) {
            this.loadCategories(jobData.serviceCategory).then(() => {
              this.loadTypesOfCategory(this.serviceConfiguration.category.key, true).then(() => {
                this.loadParametersOfType(this.serviceConfiguration.type.key)
              })
            })
          } else {
            // Create a new service without restrictions:
            this.loadCategories(null).then(() => {
              this.loadTypesOfCategory(this.serviceConfiguration.category.key, true).then(() => {
                this.loadParametersOfType(this.serviceConfiguration.type.key)
              })
            })
          }
        }
      }
    }
  }
</script>

<style scoped>

</style>
