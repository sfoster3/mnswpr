const HtmlWebpackPlugin = require('html-webpack-plugin');
const path = require('path');


module.exports = {
    entry: "./src/main/resources/static/js/index.js",
    output: {
        filename: 'main.js',
        path: path.resolve(__dirname, 'src/main/resources/dist'),
    },
    devtool: "source-map",
    module: {
        rules: [
            {
                test: /\.(js|jsx)$/,
                exclude: /node_modules/,
                use: {
                    loader: "babel-loader"
                }
            },
            {
                test: /\.html$/,
                use: [
                    {
                        loader: "html-loader"
                    }
                ]
            }
        ]
    },
    plugins: [new HtmlWebpackPlugin({
        title: "Mnswpr",
        template: "./src/main/resources/templates/base.html"
    })]
};