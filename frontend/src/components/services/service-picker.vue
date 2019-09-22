<template>
    <modal-dialog>

        <layout-row slot="header">
            <h1 slot="left">Select Service</h1>
            <input-button slot="right" icon="times" v-on:clicked="$emit('cancel')"/>
        </layout-row>

        <div slot="body">
            <label v-if="services == null || services.length === 0">
                No service of the required category is available. Please create a new service with the button on the right below.
            </label>

            <div class="services-container max-width">
                <feedback-box v-for="service in services" :key="service.id" :clickable="true"
                              v-on:feedback-clicked="$emit('selected', service)">
                    <div slot="left" class="truncate">{{service.name}}</div>
                </feedback-box>
                <layout-row slot="footer">
                    <input-button slot="right" icon="plus" v-on:clicked="$emit('create')"/>
                </layout-row>
            </div>
        </div>

        <list-pager slot="footer" :page="page"
                    v-on:first="$emit('first-page')"
                    v-on:previous="$emit('previous-page')"
                    v-on:next="$emit('next-page')"
                    v-on:last="$emit('last-page')"/>

    </modal-dialog>
</template>

<script>
    import ModalDialog from '../common/modal-dialog'
    import LayoutRow from '../common/layout-row'
    import InputButton from '../common/input-button'
    import ListPager from "../common/list-pager";
    import FeedbackBox from "../common/feedback-box";

    export default {
    name: 'service-picker',
    components: {FeedbackBox, ListPager, InputButton, LayoutRow, ModalDialog},
    props: ['services', 'page'],
    data: function () {
      return {
        feedback: '',
        feedbackOk: true
      }
    }
  }
</script>

<style scoped>

    table {
        width: 100%;
    }

    .first {
        width: 100%;
    }

    .last {
        padding-right: 0px !important;
    }

    .services-container {
        min-height: 425px;
    }

</style>
