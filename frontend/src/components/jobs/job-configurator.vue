<template>
    <core-panel>
        <h1>
            <font-awesome-icon icon="toolbox"/>
            {{ jobConfiguration.name.length > 0 ? jobConfiguration.name : 'New Job' }}
        </h1>

        <table>
            <tr>
                <td><label>Name</label></td>
                <td>
                    <input type="text" autocomplete="off" v-model="jobConfiguration.name"/>
                </td>
                <td>
                    <validation-error v-if="jobValidation.name.length > 0">
                        {{jobValidation.name}}
                    </validation-error>
                </td>
            </tr>
            <tr>
                <td><label for="trigger-input">Trigger</label></td>
                <td>
                    <input id="trigger-input" type="text" autocomplete="off"
                           v-model="jobConfiguration.trigger"/>
                    <input-button v-on:clicked="showCronTrigger = true" icon="clock" class="button-margin-left"/>
                </td>
                <td>
                    <validation-error v-if="jobValidation.trigger.length > 0">
                        {{jobValidation.trigger}}
                    </validation-error>
                </td>
            </tr>
            <tr>
                <td><label for="description-input">Description</label></td>
                <td>
                    <input id="description-input" type="text" autocomplete="off"
                           v-model="jobConfiguration.description"/>
                </td>
                <td/>
            </tr>
            <tr>
                <td><label>Active</label></td>
                <td>
                    <font-awesome-icon :icon="jobConfiguration.active ? 'check-square' : 'square'"
                                       v-on:click="jobConfiguration.active = !jobConfiguration.active"/>
                </td>
                <td/>
            </tr>
        </table>

        <cron-picker v-show="showCronTrigger" v-on:selected="setCronTrigger" v-on:cancel="showCronTrigger = false"/>

    </core-panel>
</template>

<script>
import CronPicker from '../common/cron-picker'
import CorePanel from '../common/core-panel'
import ValidationError from '../common/validation-error'
import InputButton from '../common/input-button'

export default {
  name: 'job-configurator',
  components: {InputButton, ValidationError, CorePanel, CronPicker},
  props: ['jobConfiguration', 'jobValidation'],
  data: function () {
    return {
      showCronTrigger: false,
    }
  },
  methods: {
    setCronTrigger: function (value) {
      this.jobConfiguration.trigger = value
      this.showCronTrigger = false
    }
  }
}
</script>

<style scoped>

</style>
