const http = require('http');

const PORT = 3000;

var server = http.createServer((req, res) => {
	switch(req.method) {
		case 'GET': // Read
			res.writeHead(200, {'Content-Type': 'text/plain'});
  			var date = new Date();
  			res.end('okay ' + date.toDateString());
			break;

		case 'POST': // Create
			var body = '';
	        req.on('data', function (data) {
	            body += data;
	            console.log("Partial body: " + body);
	        });
	        req.on('end', function () {
	            console.log("Body: " + body);
	        });
	        res.writeHead(200, {'Content-Type': 'text/html'});
	        res.end('post received');
			break;

		case 'PUT': // Update
			break;
		case 'DELETE': // Delete
			break;
		default:
			break;
	}
});

server.listen(PORT, function(){
    console.log("Server listening on: http://localhost:%s", PORT);
});