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
                                "igor-plugin-common/cron-trigger",
                                "igor-plugin-common/manual-trigger",
                                "igor-plugin-common/web-hook-trigger"
                            ]
                        },
                        {
                            title: "Actions",
                            sidebarDepth: 0,
                            children: [
                                "igor-plugin-common/filter-persisted-value-action",
                                "igor-plugin-common/persist-value-action",
                                "igor-plugin-common/filter-by-regexp-action",
                                "igor-plugin-common/filter-by-timestamp-action",
                                "igor-plugin-common/limit-action",
                                "igor-plugin-common/log-action",
                                "igor-plugin-common/pause-action",
                                "igor-plugin-common/skip-action",
                                "igor-plugin-common/sort-by-timestamp-pattern-action",
                                "igor-plugin-common/split-array-action",
                                "igor-plugin-common/copy-file-action",
                                "igor-plugin-common/delete-file-action",
                                "igor-plugin-common/list-files-action",
                                "igor-plugin-common/move-file-action",
                                "igor-plugin-common/read-file-action",
                                "igor-plugin-common/send-message-action",
                                "igor-plugin-common/http-request-action"
                            ]
                        },
                        {
                            title: "Connectors",
                            sidebarDepth: 0,
                            children: [
                                "igor-plugin-common/localfs-file-connector",
                                "igor-plugin-file/ftp-file-connector",
                                "igor-plugin-file/ftps-file-connector",
                                "igor-plugin-file/http-file-connector",
                                "igor-plugin-file/https-file-connector",
                                "igor-plugin-file/scp-file-connector",
                                "igor-plugin-file/sftp-file-connector",
                                "igor-plugin-message/rabbitmq-message-connector",
                                "igor-plugin-common/http-web-connector"
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
        igorNodeVersion: "Node.js 12.19.0 LTS"
    },
    dest: "./target/doc/",
    base: "/igor/"
};