// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import { createApp } from "vue";
import { createPinia } from "pinia";

import App from "./App.vue";
import router from "./router";

import { library } from "@fortawesome/fontawesome-svg-core";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { fas } from "@fortawesome/free-solid-svg-icons";

import "./assets/main.css";
import "animate.css/source/animate.css";
import { useFeedbackStore } from "@/stores/feedback";

library.add(fas);

const app = createApp(App);
app.use(createPinia());
app.use(router);
app.component("font-awesome-icon", FontAwesomeIcon);
app.mount("#app");

router.beforeEach((to, from, next) => {
    // First route change keeps the message save, so it can be displayed on the route's target site.
    if (!useFeedbackStore().isRouteChanged()) {
        useFeedbackStore().setRouteChanged(true);
    } else {
        useFeedbackStore().clearFeedback();
    }
    next();
});
