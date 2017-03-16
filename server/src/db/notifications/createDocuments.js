exports.createDocuments = (db, data, opts, callback) => {
    console.log('createDocuments');
	// Get the documents collection
    var collection = db.collection(opts.collectionName);
    // Insert some documents
    collection.insertOne(data, function(err, result) {
        if(!err) {
            console.log("Inserted ", result, " documents into the collection");
            callback(err, result.ops[0]);
        }
    });
}