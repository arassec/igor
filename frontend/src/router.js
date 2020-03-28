import Vue from "vue";
import Router from "vue-router";
import ServiceEditor from "./views/service-editor";
import ServiceOverview from "./views/service-overview";
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
            path: "/service-overview",
            name: "service-overview",
            component: ServiceOverview
        },
        {
            path: "/service-editor/:serviceId?",
            name: "service-editor",
            component: ServiceEditor,
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
