let webpack = require("webpack");
let serviceHost = process.env.SERVICE_HOST || "http://localhost:8080";

module.exports = {
    lintOnSave: true,
    configureWebpack: {

        plugins: [
            new webpack.DefinePlugin({
                SERVICE_HOST: JSON.stringify(serviceHost)
            }),
        ]
    },
    devServer: {
        port: 8000,
    },
};
