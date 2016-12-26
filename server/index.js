const http = require('http');
const url = require('url');
const MongoClient = require('mongodb').MongoClient;
const createDoc = require('./src/createDocuments.js');
const readDoc = require('./src/readDocuments.js');

const PORT = 3000;
const mongoURL = 'mongodb://localhost:27017/directme';
var mongodb = null;
const collectionName = 'locations';

var server = http.createServer((req, res) => {
    switch(req.method) {
        case 'GET': // Read
            var query = url.parse(req.url, true).query;
            // HACK: There is an issue with query s.t. 1 is interprete as '1' (a String)
            var newQuery = {};
            for(let key in query) {
                newQuery[key] = (!isNaN(query[key])) ? parseInt(query[key]) : query[key];
            }
            readDoc.readDocuments(mongodb, newQuery, { collectionName: collectionName}, function(err, result) {
                if(!err) {
                    res.writeHead(200, {'Content-Type': 'application/json'});
                    res.end(JSON.stringify(result));
                } else {
                    res.writeHead(400, {'Content-Type': 'application/json'});
                    res.end(JSON.stringify({message: 'Unable to read'}));
                }
            });
            break;

        case 'POST': // Create
            var body = '';
            req.on('data', function (data) {
                body += data;
                console.log("Partial body: " + body);
            });
            req.on('end', function () {
                console.log("Body: " + body);
                try {
                    body = JSON.parse(body);
                    createDoc.createDocuments(mongodb, body, { collectionName: collectionName}, function(err, result) {
                        if(!err) {
                            res.writeHead(200, {'Content-Type': 'application/json'});
                            res.end(JSON.stringify(result));
                        } else {
                            res.writeHead(400, {'Content-Type': 'application/json'});
                            res.end(JSON.stringify({message: 'Unable to insert'}));
                        }
                    });
                } catch(e) {
                    res.writeHead(400, {'Content-Type': 'application/json'});
                    res.end(JSON.stringify({message: 'Unable to parse JSON'}));
                }
            });
            break;

        case 'PUT': // Update
            break;
        case 'DELETE': // Delete
            break;
        default:
            break;
    }
});

MongoClient.connect(mongoURL, function(err, db) {
    if(!err) {
        mongodb = db;
        console.log("Connected to MongoDB");
        server.listen(PORT, function(){
            console.log("Server listening on: http://localhost:%s", PORT);
        });
    } else {
        db.close();
        server.close();
    }
});
