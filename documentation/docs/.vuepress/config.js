module.exports = {
    title: "Igor Reference Guide",
    themeConfig: {
        searchMaxSuggestions: 25,
        nav: [
            {text: "Home", link: "/"},
            {text: "User Guide", link: "/user/"},
            {text: "Developer Guide", link: "/developer/"},
            {text: "JavaDoc", link: "/apidocs/index.html"},
            {text: "GitHub", link: "https://www.github.com/arassec/igor"}
        ],
        sidebar: {
            "/user/": [
                {
                    title: "Introduction",
                    children: [
                        "",
                        "installation",
                        "configuration"
                    ]
                },
                {
                    title: "Using Igor",
                    children: [
                        "core/job",
                        "core/action",
                        "core/connector"
                    ]
                },
                {
                    title: "Available Triggers",
                    children: [
                        "module-misc/cron-trigger",
                        "module-misc/manual-trigger"
                    ]
                },
                {
                    title: "Available Providers",
                    children: [
                        "module-misc/empty-input-provider",
                        "module-misc/fixed-input-provider",
                        "module-file/list-files-provider"
                    ]
                },
                {
                    title: "Available Actions",
                    children: [
                        "module-misc/filter-persisted-value-action",
                        "module-misc/persist-value-action",
                        "module-misc/filter-by-regexp-action",
                        "module-misc/filter-by-timestamp-action",
                        "module-misc/pause-action",
                        "module-misc/sort-by-timestamp-pattern-action",
                        "module-file/copy-file-action",
                        "module-file/delete-file-action",
                        "module-file/move-file-action",
                        "module-file/read-file-action",
                        "module-message/send-message-action"
                    ]
                },
                {
                    title: "Available Connectors",
                    children: [
                        "module-file/ftp-file-connector",
                        "module-file/ftps-file-connector",
                        "module-file/localfs-file-connector",
                        "module-file/http-file-connector",
                        "module-file/https-file-connector",
                        "module-file/scp-file-connector",
                        "module-file/sftp-file-connector",
                        "module-message/rabbitmq-message-connector"
                    ]
                }
            ]
        }
    },
    dest: "./target/doc/",
    base: "/igor/" + process.env.REVISION + "/"
}