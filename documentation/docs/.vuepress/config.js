module.exports = {
    title: "Igor Reference Documentation (v" + process.env.REVISION + ")",
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
                                "module-misc/cron-trigger",
                                "module-misc/manual-trigger",
                                "module-web/web-hook-trigger"
                            ]
                        },
                        {
                            title: "Actions",
                            sidebarDepth: 0,
                            children: [
                                "module-misc/filter-persisted-value-action",
                                "module-misc/persist-value-action",
                                "module-misc/filter-by-regexp-action",
                                "module-misc/filter-by-timestamp-action",
                                "module-misc/limit-action",
                                "module-misc/log-action",
                                "module-misc/pause-action",
                                "module-misc/skip-action",
                                "module-misc/sort-by-timestamp-pattern-action",
                                "module-misc/split-array-action",
                                "module-file/copy-file-action",
                                "module-file/delete-file-action",
                                "module-file/list-files-action",
                                "module-file/move-file-action",
                                "module-file/read-file-action",
                                "module-message/send-message-action",
                                "module-web/http-request-action"
                            ]
                        },
                        {
                            title: "Connectors",
                            sidebarDepth: 0,
                            children: [
                                "module-file/ftp-file-connector",
                                "module-file/ftps-file-connector",
                                "module-file/localfs-file-connector",
                                "module-file/http-file-connector",
                                "module-file/https-file-connector",
                                "module-file/scp-file-connector",
                                "module-file/sftp-file-connector",
                                "module-message/rabbitmq-message-connector",
                                "module-web/http-web-connector"
                            ]
                        }
                    ]
                }
            ],
            "/developer/": [
                {
                    title: "Getting Started",
                    children: [
                        ""
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
                        "copyfiles"
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