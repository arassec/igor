<template>
  <core-container class="min-width">

    <spacer-item/>

    <core-content>

      <list-header :filter="filter" :filter-key="'job-schedule-list-filter'">
        <p slot="title">Scheduled Jobs</p>
      </list-header>

      <list-entry v-for="scheduleEntry in filteredSchedule"
                  v-bind:key="scheduleEntry.id"
                  v-bind:id="scheduleEntry.id">
        <list-name slot="left">{{scheduleEntry.name}}</list-name>
        <list-name slot="right">{{scheduleEntry.date}}</list-name>
      </list-entry>

    </core-content>

    <spacer-item/>

    <background-icon left="true" icon-one="clipboard-list"/>

  </core-container>
</template>

<script>
    import SpacerItem from "../components/common/spacer-item";
    import CoreContainer from "../components/common/core-container";
    import CoreContent from "../components/common/core-content";
    import ListEntry from "../components/common/list-entry";
    import ListName from "../components/common/list-name";
    import ListHeader from "../components/common/list-header";
    import BackgroundIcon from '../components/common/background-icon'

    export default {
        name: 'app-status',
        components: {BackgroundIcon, ListHeader, ListName, ListEntry, CoreContent, CoreContainer, SpacerItem},
        data: function () {
            return {
                schedule: [],
                filterText: ''
            }
        },
        methods: {
            loadSchedule: function () {
                let component = this
                this.$http.get('/api/job/schedule').then(function (response) {
                    for (let i = component.schedule.length; i > 0; i--) {
                        component.schedule.pop()
                    }
                    Array.from(response.data).forEach(function (item) {
                        component.schedule.push(item)
                    })
                }).catch(function (error) {
                    component.$root.$data.store.setFeedback('Schedule could not be loaded (' + error + ')', true)
                })
            },
            filter: function (filterTextFromListHeader) {
                this.filterText = filterTextFromListHeader

            }
        },
        computed: {
            filteredSchedule: function () {
                let component = this
                return this.schedule.filter(function (scheduleEntry) {
                    return scheduleEntry.name.includes(component.filterText)
                })
            }
        },
        mounted() {
            this.loadSchedule()
        }

    }
</script>

<style scoped>

  .min-width {
    --content-width: 800px;
  }

</style>
