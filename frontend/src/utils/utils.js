export default {
    formatNameForSnackbar: function (name) {
        let parts = name.split(" ");
        for (let i = 0; i < parts.length; i++) {
            if (parts[i].length > 16) {
                parts[i] = parts[i].substring(0, 16) + "..."
            }
        }
        return parts.join(" ");
    },
    formatInstant: function (instant) {
        let options = {year: "numeric", month: "2-digit", day: "2-digit"};
        let date = new Date(instant);
        return date.toLocaleTimeString(undefined, options)
    },
    capitalize: function (text) {
        return text.charAt(0).toUpperCase() + text.slice(1)
    },
    uuidv4: function () {
        return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, c =>
            (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
        )
    },
    findTask: function (jobConfiguration, taskId) {
        let result = null;
        jobConfiguration.tasks.forEach((task) => {
            if (taskId === task.id) {
                result = task;
            }
        });
        return result;
    },
    findTaskIndex: function (jobConfiguration, taskId) {
        let result = null;
        jobConfiguration.tasks.forEach(function (task, index) {
            if (task.id === taskId) {
                result = index;
            }
        });
        return result;
    },
    findTaskWithAction: function (jobConfiguration, actionId) {
        let result = null;
        jobConfiguration.tasks.forEach(function (task) {
            task.actions.forEach((action) => {
                if (actionId === action.id) {
                    result = task;
                }
            })
        });
        return result;
    },
    findAction: function (jobConfiguration, actionId) {
        let result = null;
        jobConfiguration.tasks.forEach((task) => {
            task.actions.forEach((action) => {
                if (actionId === action.id) {
                    result = action;
                }
            })
        });
        return result;
    },
    findActionIndex: function (task, actionId) {
        let result = -1;
        task.actions.forEach((action, index) => {
            if (action.id === actionId) {
                result = index;
            }
        });
        return result;
    },
}
