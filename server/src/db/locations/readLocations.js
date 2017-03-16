exports.readLocations = (db, query, opts, callback) => {
    console.log('readLocations', query);
	// Get the documents collection
    var collection = db.collection(opts.collectionName);
    // Insert some documents
    collection.find(query).toArray(function(err, result) {
        if(!err) {
            console.log("Read ", result, "documents from the collection");
            callback(err, result);
        } else {
            callback(err);
        }
    });
}