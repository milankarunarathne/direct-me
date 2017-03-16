var express = require('express');
const ObjectID = require('mongodb').ObjectID;
var router = express.Router();

const createNotification = require('../db/notifications/createDocuments.js');
const readNotification = require('../db/notifications/readDocuments.js');
const updateNotification = require('../db/notifications/updateDocuments.js');
const deleteNotification = require('../db/notifications/deleteDocuments.js');

const collectionName = 'notifications';

router.get('/', function (req, res) {
    console.log('>>> GET: Request');
    const mongodb = req.app.locals.mongodb;

    var query = req.query;
    // HACK: There is an issue with query s.t. 1 is interprete as '1' (a String)
    var newQuery = {};
    for(let key in query) {
        newQuery[key] = (!isNaN(query[key])) ? parseInt(query[key]) : query[key];
    }
    console.log('GET newQuery:', newQuery)

    readNotification.readDocuments(mongodb, newQuery, {collectionName: collectionName}, function(err, result) {
        if(!err) {
            res.json(result);
        } else {
            res.status(500).json({message: 'Unable to read'});
        }
    });
});

router.get('/:id', function (req, res) {
    console.log('GET Notification with id:', req.params.id);
    const mongodb = req.app.locals.mongodb;
    const id = new ObjectID(req.params.id);

    readNotification.readDocuments(mongodb, {'_id': id}, {collectionName: collectionName}, function(err, result) {
        if(!err) {
            res.json(result);
        } else {
            res.status(500).json({message: 'Unable to read'});
        }
    });
});

router.post('/', function (req, res) {
    console.log('>>> POST: Request')
    const mongodb = req.app.locals.mongodb;
    const body = req.body;
    console.log('POST Body:', body);

    createNotification.createDocuments(mongodb, body, { collectionName: collectionName}, function(err, result) {
        if(!err) {
            res.json(result);
        } else {
            res.status(500).json({message: 'Unable to create'});
        }
    });
});

router.put('/:id', function (req, res) {
    console.log('>>> PUT Notification with id:', req.params.id);
    const mongodb = req.app.locals.mongodb;
    const id = new ObjectID(req.params.id);
    const body = req.body;
    console.log('POST Body:', body);

    updateNotification.updateDocuments(mongodb, {'_id': id}, body, { collectionName: collectionName}, function(err, result) {
        if(!err) {
            res.json(result);
        } else {
            res.status(500).json({message: 'Unable to update ' + req.params.id });
        }
    });
});

router.delete('/:id', function (req, res) {
    console.log('>>> DELETE Notification with id:', req.params.id);
    const mongodb = req.app.locals.mongodb;
    const id = new ObjectID(req.params.id);

    deleteNotification.deleteDocuments(mongodb, {'_id': id}, {collectionName: collectionName}, function(err, result) {
        if(!err) {
            res.json(result);
        } else {
            res.status(500).json({message: 'Unable to Delete ' + req.params.id });
        }
    });
});

module.exports = router;