module.exports = {
    runtimeCompiler: true,
    devServer: {
        proxy: {
            // proxy all webpack dev-server requests starting with /api to our Spring Boot backend (localhost:8080)
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true
            }
        }
    },
    outputDir: './target/dist/',
    lintOnSave: true
}
