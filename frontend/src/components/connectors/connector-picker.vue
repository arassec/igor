<template>
    <modal-dialog>

        <layout-row slot="header">
            <h1 slot="left">Select Connector</h1>
            <input-button slot="right" icon="times" v-on:clicked="$emit('cancel')"/>
        </layout-row>

        <div slot="body" class="max-width">
            <label v-if="connectors == null || connectors.length === 0">
                No suitable connector found. Please create a new connector with the button on the right below.
            </label>

            <div class="connectors-container max-width">
                <feedback-box v-for="connector in connectors" :key="connector.id" :clickable="true"
                              v-on:feedback-clicked="$emit('selected', connector)">
                    <div slot="left" class="truncate">{{connector.name}}</div>
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
    name: 'connector-picker',
    components: {FeedbackBox, ListPager, InputButton, LayoutRow, ModalDialog},
    props: ['connectors', 'page'],
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

    .connectors-container {
        min-height: 425px;
    }

</style>
