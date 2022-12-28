import { createRouter, createWebHistory } from "vue-router";

import ConnectorEditor from "../views/connector-editor.vue";
import ConnectorOverview from "../views/connector-overview.vue";
import JobEditor from "../views/job-editor.vue";

import JobOverview from "../views/job-overview.vue";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: "/",
            name: "job-overview",
            component: JobOverview,
        },
        {
            path: "/connector-overview",
            name: "connector-overview",
            component: ConnectorOverview,
        },
        {
            path: "/connector-editor/:connectorId?",
            name: "connector-editor",
            component: ConnectorEditor,
            props: true,
        },
        {
            path: "/job-editor/:jobId?",
            name: "job-editor",
            component: JobEditor,
            props: true,
        },
    ],
});

export default router;
