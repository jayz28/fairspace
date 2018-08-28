const app = require('express')();
const webdav = require('webdav-server').v2;
const noopHttpAuthentication = require('./auth/noop-http-webdav-authentication');
const jwksVerifier = require('./auth/verify-jwt-with-jwks');

// Configuration parameters
const rootPath = process.env.FILES_FOLDER || '/data';
const jwksUrl = process.env.JWKS_URL;

const server = new webdav.WebDAVServer({
    httpAuthentication: noopHttpAuthentication,
    rootFileSystem: new webdav.PhysicalFileSystem(rootPath)
});

app.get('/', (req, res) => res.send('Hi, I\'m Titan!').end());

app.use(jwksVerifier.middleware({url: jwksUrl}))
app.use(webdav.extensions.express('/api/storage/webdav/', server));

module.exports = app;
