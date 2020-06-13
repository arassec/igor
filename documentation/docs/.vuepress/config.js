module.exports = {
    title: "Igor Reference Guide",
    themeConfig: {
        searchMaxSuggestions: 25,
        nav: [
            {text: "Home", link: "/"},
            {text: "User Guide", link: "/user/"},
            {text: "Developer Guide", link: "/developer/"},
            {text: "JavaDoc", link: "/apidocs/"},
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
                        "core/task",
                        "core/action",
                        "core/connector"
                    ]
                },
                {
                    title: "Misc Components",
                    children: [
                        "module-misc/cron-trigger",
                        "module-misc/manual-trigger",
                        "module-misc/empty-input-provider",
                        "module-misc/fixed-input-provider",
                        "module-misc/filter-persisted-value-action",
                        "module-misc/persist-value-action",
                        "module-misc/filter-by-regexp-action",
                        "module-misc/filter-by-timestamp-action",
                        "module-misc/pause-action",
                        "module-misc/sort-by-timestamp-pattern-action"
                    ]
                },
                {
                    title: "File Components",
                    children: [
                        "module-file/list-files-provider",
                        "module-file/copy-file-action",
                        "module-file/delete-file-action",
                        "module-file/move-file-action",
                        "module-file/read-file-action",
                        "module-file/ftp-file-connector",
                        "module-file/ftps-file-connector",
                        "module-file/localfs-file-connector",
                        "module-file/http-file-connector",
                        "module-file/https-file-connector",
                        "module-file/scp-file-connector",
                        "module-file/sftp-file-connector"
                    ]
                },
                {
                    title: "Message Components",
                    children: [
                        "module-message/send-message-action",
                        "module-message/rabbitmq-message-connector",
                    ]
                }
            ]
        }
    },
    dest: "./target/doc/",
    base: "/igor/" + process.env.REVISION + "/"
}