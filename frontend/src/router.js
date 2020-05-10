import Vue from "vue";
import Router from "vue-router";
import ConnectorEditor from "./views/connector-editor";
import ConnectorOverview from "./views/connector-overview";
import JobEditor from "./views/job-editor";
import JobOverview from "./views/job-overview";

Vue.use(Router);

export default new Router({
    routes: [
        {
            path: "/",
            name: "job-overview",
            component: JobOverview
        },
        {
            path: "/connector-overview",
            name: "connector-overview",
            component: ConnectorOverview
        },
        {
            path: "/connector-editor/:connectorId?",
            name: "connector-editor",
            component: ConnectorEditor,
            props: true
        },
        {
            path: "/job-editor/:jobId?",
            name: "job-editor",
            component: JobEditor,
            props: true
        }

    ]
});
