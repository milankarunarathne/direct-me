var express = require('express');
const ObjectID = require('mongodb').ObjectID;
var router = express.Router();

const readLocations = require('../db/locations/readLocations.js');

const collectionName = 'notifications';

router.get('/', function (req, res) {
    console.log('>>> GET: Locations Request');
    const mongodb = req.app.locals.mongodb;
    const query = req.query;
    const latitude = query.lat;
    const longitude = query.lon;
    const range = query.range;
    console.log('GET Query', query);
    const newQuery = {
        location: {
            latitude: {$lte: (latitude-range), $gte:(latitude+range)},
            longitude: {$lte:(longitude-range), $gte:(longitude+range)}
        }
    };

    readLocations.readDocuments(mongodb, newQuery, {collectionName: collectionName}, function(err, result) {
        if(!err) {
            res.json(result);
        } else {
            res.status(500).json({message: 'Unable to read'});
        }
    });
});

module.exports = router;