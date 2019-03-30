<template>
    <modal-dialog @close="$emit('cancel')">
        <h1 slot="header">Delete Service?</h1>
        <div slot="body">
            <div class="paragraph">
                Do you really want to delete service '{{serviceName.replace(' (unused)', '')}}'?
            </div>
            <div class="paragraph" v-if="affectedJobs.length > 0">
                The following jobs are using this service and will not be working any more:
                <ul class="a">
                    <li v-for="affectedJob in affectedJobs" :key="affectedJob.key">
                        {{affectedJob.value}}
                    </li>
                </ul>
            </div>
            <div class="paragraph" v-if="affectedJobs.length > 0">
                Delete affected jobs, too:
                <font-awesome-icon :icon="deleteJobs ? 'check-square' : 'square'" v-on:click="deleteJobs = !deleteJobs"/>
            </div>
        </div>
        <div slot="footer">
            <button-row>
                <input-button slot="left" v-on:clicked="$emit('cancel')" icon="times"/>
                <input-button slot="right" v-on:clicked="deleteJobs ? $emit('delete-plus') : $emit('delete')" icon="check"/>
            </button-row>
        </div>
    </modal-dialog>
</template>

<script>
    import ModalDialog from "../common/modal-dialog";
    import ButtonRow from "../common/button-row";
    import InputButton from "../common/input-button";

    export default {
        name: "delete-service-dialog",
        components: {InputButton, ButtonRow, ModalDialog},
        props: ['serviceId', 'serviceName'],
        data: function () {
            return {
                affectedJobs: [],
                deleteJobs: false
            }
        },
        mounted: function () {
            let component = this
            this.$http.get('/api/service/' + component.serviceId + "/job-references").then(function (response) {
                component.affectedJobs = Array.from(response.data)
            }).catch(function (error) {
                component.$root.$data.store.setFeedback('Could not load affected jobs (' + error + ')', true)
            })
        }
    }
</script>

<style scoped>

    .paragraph {
        margin-bottom: 20px;
    }

    .paragraph ul {
        margin-top: 10px;
        list-style-type: square;
    }

    .paragraph ul li {
        margin-left: 45px;
    }

</style>
