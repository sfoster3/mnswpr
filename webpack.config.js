const HtmlWebpackPlugin = require('html-webpack-plugin');
const HtmlWebpackExternalsPlugin = require('html-webpack-externals-plugin');
const path = require('path');


module.exports = {
    entry: "./src/main/resources/static/ts/index.tsx",
    output: {
        filename: 'main.js',
        path: path.resolve(__dirname, 'src/main/resources/dist'),
    },
    devtool: "source-map",
    module: {
        rules: [
            {
                test: /\.ts(x?)$/,
                exclude: /node_modules/,
                use: [
                    {
                        loader: "ts-loader"
                    }
                ]
            },
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
            },
            // All output '.js' files will have any sourcemaps re-processed by 'source-map-loader'.
            {
                enforce: "pre",
                test: /\.js$/,
                loader: "source-map-loader"
            }
        ]
    },
    resolve: {
        extensions: [".ts", ".tsx", ".js", ".jsx"]
    },
    plugins: [
        new HtmlWebpackPlugin({
            title: "Mnswpr",
            template: "./src/main/resources/templates/base.html"
        }),
        new HtmlWebpackExternalsPlugin({
            externals: [
                {
                    module: 'react',
                    entry: {
                        path: 'https://unpkg.com/react@16/umd/react.development.js',
                        attributes: {crossorigin: ''}
                    },
                    global: 'React',
                },
                {
                    module: 'react-dom',
                    entry: {
                        path: 'https://unpkg.com/react-dom@16/umd/react-dom.development.js',
                        attributes: {crossorigin: ''}
                    },
                    global: 'ReactDOM'
                }
            ],
        }),
    ]
};