module.exports = {
    title: "Igor Reference Guide",
    themeConfig: {
        nav: [
            {text: "Home", link: "/"},
            {text: "User Guide", link: "/user/"},
            {text: "GitHub", link: "https://www.github.com/arassec/igor"}
        ],
        logo: "/logo.png",
        sidebar: {
            "/user/": [
                {
                    title: "Introduction",
                    collapsable: false,
                    children: [
                        "",
                        "installation"
                    ]
                },
                {
                    title: "Core Principals",
                    collapsable: false,
                    children: [
                        "core/job",
                        "core/task",
                        "core/action",
                        "core/connector"
                    ]
                },
                {
                    title: "Misc Components",
                    collapsable: false,
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
                    collapsable: false,
                    children: [
                        "module-file/list-files-provider"
                    ]
                },
                {
                    title: "Message Components",
                    collapsable: false,
                    children: [
                        "module-message/send-message-action"
                    ]
                }
            ]
        }
    },
    dest: "./target/doc/",
    base: "/igor/" + process.env.REVISION + "/"
}