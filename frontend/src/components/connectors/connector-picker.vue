<template>
    <modal-dialog>
        <template v-slot:header>
            <layout-row>
                <template v-slot:left>
                    <h1>Select Connector</h1>
                </template>
                <template v-slot:right>
                    <input-button icon="times" v-on:clicked="$emit('cancel')" />
                </template>
            </layout-row>
        </template>

        <template v-slot:body>
            <div class="max-width">
                <label v-if="connectors == null || connectors.length === 0">
                    No suitable connector found. Please create a new connector with the button on the right below.
                </label>

                <div class="connectors-container max-width">
                    <feedback-box
                        v-for="connector in connectors"
                        :key="connector.id"
                        :clickable="true"
                        v-on:feedback-clicked="$emit('selected', connector)"
                        :data-e2e="dataE2EName('picker-', connector.name)"
                    >
                        <template v-slot:left>
                            <div class="truncate">{{ connector.name }}</div>
                        </template>
                    </feedback-box>
                    <layout-row>
                        <template v-slot:right>
                            <input-button icon="plus" v-on:clicked="$emit('create')" />
                        </template>
                    </layout-row>
                </div>
            </div>
        </template>

        <template v-slot:footer>
            <list-pager
                :page="page"
                v-on:first="$emit('first-page')"
                v-on:previous="$emit('previous-page')"
                v-on:next="$emit('next-page')"
                v-on:last="$emit('last-page')"
            />
        </template>
    </modal-dialog>
</template>

<script>
import ModalDialog from "../common/modal-dialog.vue";
import LayoutRow from "../common/layout-row.vue";
import InputButton from "../common/input-button.vue";
import ListPager from "../common/list-pager.vue";
import FeedbackBox from "../common/feedback-box.vue";
import Utils from "@/utils/utils.js";

export default {
    name: "connector-picker",
    components: { FeedbackBox, ListPager, InputButton, LayoutRow, ModalDialog },
    props: ["connectors", "page"],
    data: function () {
        return {
            feedback: "",
            feedbackOk: true,
        };
    },
    methods: {
        dataE2EName: function (prefix, suffix) {
            return prefix + Utils.toKebabCase(suffix);
        },
    },
};
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
