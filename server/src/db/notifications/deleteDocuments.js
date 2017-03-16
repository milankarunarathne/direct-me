exports.deleteDocuments = (db, query, opts, callback) => {
    console.log('deleteDocuments', query);
	// Get the documents collection
    var collection = db.collection(opts.collectionName);
    // Delete some documents
    collection.remove(query, function(err, result) {
        if(!err) {
            console.log("Delete ", result, "documents from the collection");
            callback(err, result);
        } else {
            callback(err);
        }
    });
}