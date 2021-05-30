module.exports = {
    title: "Igor Reference Documentation (v" + process.env.REVISION + ")",
    head: [
        ['meta', {name: "Cache-Control", content: "no-cache, no-store, must-revalidate"}],
        ['meta', {name: "Pragma", content: "no-cache"}],
        ['meta', {name: "Expires", content: "0"}]
    ],
    themeConfig: {
        searchMaxSuggestions: 25,
        nav: [
            {text: "Home", link: "/"},
            {text: "User Guide", link: "/user/"},
            {text: "Developer Guide", link: "/developer/"},
            {text: "Examples", link: "/examples/"},
            {text: "GitHub", link: "https://www.github.com/arassec/igor"}
        ],
        sidebar: {
            "/user/": [
                {
                    title: "Getting Started",
                    children: [
                        "",
                        "configuration",
                        "changelog"
                    ]
                },
                {
                    title: "Core Elements",
                    children: [
                        "core/job",
                        "core/action",
                        "core/connector"
                    ]
                },
                {
                    title: "User Interface",
                    children: [
                        "job-overview",
                        "job-editor",
                        "connector-overview",
                        "connector-editor"
                    ]
                },
                {
                    title: "Components",
                    sidebarDepth: 1,
                    children: [
                        {
                            title: "Triggers",
                            sidebarDepth: 0,
                            children: [
                                "igor-plugin-core/cron-trigger",
                                "igor-plugin-core/manual-trigger",
                                "igor-plugin-core/web-hook-trigger",
                                "igor-plugin-message/rabbitmq-message-trigger"
                            ]
                        },
                        {
                            title: "Actions",
                            sidebarDepth: 0,
                            children: [
                                "igor-plugin-core/filter-persisted-value-action",
                                "igor-plugin-core/persist-value-action",
                                "igor-plugin-core/filter-by-regexp-action",
                                "igor-plugin-core/filter-by-timestamp-action",
                                "igor-plugin-core/limit-action",
                                "igor-plugin-core/log-action",
                                "igor-plugin-core/pause-action",
                                "igor-plugin-core/skip-action",
                                "igor-plugin-core/sort-by-timestamp-pattern-action",
                                "igor-plugin-core/split-array-action",
                                "igor-plugin-core/copy-file-action",
                                "igor-plugin-core/delete-file-action",
                                "igor-plugin-core/list-files-action",
                                "igor-plugin-core/move-file-action",
                                "igor-plugin-core/read-file-action",
                                "igor-plugin-core/http-request-action",
                                "igor-plugin-core/add-data-action",
                                "igor-plugin-message/send-rabbitmq-message-action",
                                "igor-plugin-message/send-email-message-action",
                                "igor-plugin-message/receive-email-message-action"
                            ]
                        },
                        {
                            title: "Connectors",
                            sidebarDepth: 0,
                            children: [
                                "igor-plugin-core/http-web-connector",
                                "igor-plugin-core/localfs-file-connector",
                                "igor-plugin-file/ftp-file-connector",
                                "igor-plugin-file/ftps-file-connector",
                                "igor-plugin-file/scp-file-connector",
                                "igor-plugin-file/sftp-file-connector",
                                "igor-plugin-message/rabbitmq-message-connector",
                                "igor-plugin-message/email-imap-message-connector",
                                "igor-plugin-message/email-smtp-message-connector"

                            ]
                        }
                    ]
                }
            ],
            "/developer/": [
                {
                    title: "Getting Started",
                    children: [
                        "",
                        "developing"
                    ]
                },
                {
                    title: "Adding Components",
                    children: [
                        "component",
                        "trigger",
                        "action",
                        "connector"
                    ]
                }
            ],
            "/examples/": [
                {
                    title: "Overview",
                    children: [
                        ""
                    ]
                },
                {
                    title: "File Handling",
                    children: [
                        "copynewfiles"
                    ]
                }
            ]
        },
        igorVersion: process.env.REVISION,
        igorJavaVersion: "Java 11",
        igorNodeVersion: "Node.js 14.17.0 LTS"
    },
    dest: "./target/doc/",
    base: "/igor/"
};
