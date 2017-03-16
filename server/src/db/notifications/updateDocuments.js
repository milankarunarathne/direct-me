exports.updateDocuments = (db, query, data, opts, callback) => {
    console.log('updateDocuments');
	// Get the documents collection
    var collection = db.collection(opts.collectionName);
    // Update some documents
    collection.update(query, {$set: data}, {w: 1, upset: false, multi: false}, function(err, result) {
        if(!err) {
            console.log("Updated ", result, " documents into the collection");
            callback(err, result.result);
        }
    });
}