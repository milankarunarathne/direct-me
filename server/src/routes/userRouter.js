var express = require('express');
var router = express.Router();

router.get('/user', function (req, res) {
  res.send('Birds home page')
})

router.post('/user', function (req, res) {
  res.send('Birds home page')
})

module.exports = router;