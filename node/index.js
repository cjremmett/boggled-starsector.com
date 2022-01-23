var express = require('express');
var app = express();

const {MongoClient} = require('mongodb');

console.log('API started.');
var port = 8081;
app.listen(port);
console.log('Listening on Port ' + port + '.');

app.get('/api/', (req, res) => 
{
   var statusMessage = 
   {
      "message": "API is online.",
      "date/time": Date.now()
   };

   res.json(statusMessage)
});

app.get('/api/version/', (req, res) => 
{
   var currentVersion = 
   {
      "version": "8.0.1"
   };

   res.json(currentVersion);
});

app.get('/api/ip/', (req, res) => 
{
   // Use if not behind proxy - Vanilla Node
   //var ip = req.socket.remoteAddress;

   // Use if behind NGINX proxy - Vanilla Node
   // var ip = req.header('x-forwarded-for') || req.socket.remoteAddress;

   // Enable if behind NGINX proxy - Express
   app.set('trust proxy', true);

   // Express Only
   var ip = req.ip;

   var ipJson = 
   {
      "client-ip":ip
   };

   res.json(ipJson);
});

app.get('/api/numbers/countrymau', (req, res) => 
{
   let getMonthlyActiveUsers = new Promise(function(resolve, reject) 
   {
      var MongoClient = require('mongodb').MongoClient;
      var url = "mongodb://localhost:27017/";

      var dateNow = Date.now();
      var oneMonthAgo = dateNow - 2592000000;

      MongoClient.connect(url, function(err, db)
      {
         var query = { timestamp: { $gt: oneMonthAgo } };
         db.db("players").collection("players").find(query).toArray(function(err, result)
         {
            db.close();
            resolve(result);
         });
      });
   })
   .then ( (result) => 
   {
      let countries = [];

      result.forEach(element =>
      {
         var country = element["ip-info"].country;
         var index = countries.findIndex(c => c["country"] === country)
         if( index === -1)
         {
            countries.push({"country": country, "players": 1})
         }
         else
         {
            countries[index] = {"country": country, "players": countries[index].players + 1}
         }
      });

      return countries;
   })
   .then ( (countries) => 
   {
      countries = countries.sort(({"players":a}, {"players":b}) => b-a);
      return countries;
   })
   .then ( (countries) => 
   {
      var countriesString = "";
      countries.forEach(element =>
      {
         countriesString = countriesString + element.country + ": " + element.players + "<br>";
      });

      var countriesJson = 
      {
         "countrymau": countriesString
      };

      res.json(countriesJson);
   })
   .catch( () => 
   {
      console.log("Exception thrown getting the Monthly Active Users by country.")

      var messageJson = 
      {
         "countrymau": "An error occurred. Please notify me at cjremmett@gmail.com."
      };

      res.json(messageJson);
   });
});

app.get('/api/numbers/atau', (req, res) => 
{
   let getAllTimeActiveUsers = new Promise(function(resolve, reject) 
   {
      var MongoClient = require('mongodb').MongoClient;
      var url = "mongodb://localhost:27017/";

      MongoClient.connect(url, function(err, db)
      {
         var query = { };
         db.db("players").collection("players").find(query).toArray(function(err, result)
         {
            db.close();
            resolve(result.length);
         });
      });
   })
   .then ( (length) => 
   {
      console.log("Reported " + length + " as the All-Time Active Users value.")

      var messageJson = 
      {
         "atau": length
      };

      res.json(messageJson);
   })
   .catch( () => 
   {
      console.log("Exception thrown getting the All-Time Active Users.")

      var messageJson = 
      {
         "atau": "An error occurred. Please notify me at cjremmett@gmail.com."
      };

      res.json(messageJson);
   });
});

app.get('/api/numbers/mau', (req, res) => 
{
   let getMonthlyActiveUsers = new Promise(function(resolve, reject) 
   {
      var MongoClient = require('mongodb').MongoClient;
      var url = "mongodb://localhost:27017/";

      var dateNow = Date.now();
      var oneMonthAgo = dateNow - 2592000000;

      MongoClient.connect(url, function(err, db)
      {
         var query = { timestamp: { $gt: oneMonthAgo } };
         db.db("players").collection("players").find(query).toArray(function(err, result)
         {
            db.close();
            resolve(result.length);
         });
      });
   })
   .then ( (length) => 
   {
      console.log("Reported " + length + " as the Monthly Active Users value.")

      var messageJson = 
      {
         "mau": length
      };

      res.json(messageJson);
   })
   .catch( () => 
   {
      console.log("Exception thrown getting the Monthly Active Users.")

      var messageJson = 
      {
         "mau": "An error occurred. Please notify me at cjremmett@gmail.com."
      };

      res.json(messageJson);
   });
});

app.get('/api/numbers/dau', (req, res) => 
{
   let getDailyActiveUsers = new Promise(function(resolve, reject) 
   {
      var MongoClient = require('mongodb').MongoClient;
      var url = "mongodb://localhost:27017/";

      var dateNow = Date.now();
      var oneDayAgo = dateNow - 86400000;

      MongoClient.connect(url, function(err, db)
      {
         var query = { timestamp: { $gt: oneDayAgo } };
         db.db("players").collection("players").find(query).toArray(function(err, result)
         {
            db.close();
            resolve(result.length);
         });
      });
   })
   .then ( (length) => 
   {
      console.log("Reported " + length + " as the Daily Active Users value.")

      var messageJson = 
      {
         "dau": length
      };

      res.json(messageJson);
   })
   .catch( () => 
   {
      console.log("Exception thrown getting the Daily Active Users.")

      var messageJson = 
      {
         "dau": "An error occurred. Please notify me at cjremmett@gmail.com."
      };

      res.json(messageJson);
   });
});

app.put('/api/analytics/', (req, res) => 
{
   //Check if a valid UUID was provided
   if(!req.query.uuid.toString().length == 36) 
   {
      res.status(400);
      res.json({message: "Bad Request"});
      console.log("Client sent an improperly formatted UUID. Sent them Status Code 400.")
   }
   else
   {
      var timestamp = Date.now();
      var uuid = req.query.uuid;
      app.set('trust proxy', true);
      var ip = req.ip;
      var boggledHydroponicsEnabled = req.query.boggledHydroponicsEnabled;
      var boggledCloningEnabled = req.query.boggledCloningEnabled;
      var boggledDomedCitiesBuildableOnWaterWorlds = req.query.boggledDomedCitiesBuildableOnWaterWorlds;
      console.log("Client connected with UUID " + uuid + " and IP address " + ip + ".");

      let checkPlayerEntryAlreadyExistsPromise = new Promise(function(resolve, reject) 
      {
         var MongoClient = require('mongodb').MongoClient;
         var url = "mongodb://localhost:27017/";

         MongoClient.connect(url, function(err, db)
         {
            var query = { uuid: uuid };
            db.db("players").collection("players").find(query).toArray(function(err, result)
            {
               console.log("Query result from MongoDB:");
               console.log(result);
               db.close();
               if(result.length === 0)
               {
                  resolve(false);
               }
               else
               {
                  resolve(true);
               }
            });
         });
      })
      .catch( () => 
      {
         console.log("Exception thrown checking if player entry already exists.")
      });

      let getIpRelatedInformationPromise = new Promise(function(resolve, reject)
      {
         // Get the IP-related information from ip-api.com
         const http = require('http');
         var pathToUse = '/json/' + ip
         const options = 
         {
            hostname: 'ip-api.com',
            port: 80,
            path: pathToUse,
            method: 'GET',
            headers : {'Content-Type': 'application/json', 'Accept': 'application/json',},
         }

         const req = http.request(options, (response) => 
         {
            response.setEncoding('utf8');
            var data = '';
            response.on('data', (chunk) => { data = data + chunk });
   
            response.on('end', () => 
            {
               var ipDataFromIpApi = JSON.parse(data);
               console.log("Got response from IP-API.com:")
               console.log(ipDataFromIpApi);
               resolve(ipDataFromIpApi);
            })
         });

         req.on('error', error => { console.error(error) });
         req.end();
      })
      .catch( () => 
      {
         console.log("Exception thrown getting IP information.")
      });

      Promise.all([checkPlayerEntryAlreadyExistsPromise, getIpRelatedInformationPromise]).then((values) =>
      {
         console.log("All promises are resolved. Writing to database and responding to client.")

         if(values[0] === true)
         {
            var MongoClient = require('mongodb').MongoClient;
            var url = "mongodb://localhost:27017/";

            MongoClient.connect(url, function(err, db)
            {
               var ipData = values[1];
               var playerEntry = 
               {
                  "uuid": uuid,
                  "timestamp": timestamp,
                  "ip-info": ipData,
                  "settings-info":
                  {
                     "boggledHydroponicsEnabled": boggledHydroponicsEnabled,
                     "boggledCloningEnabled": boggledCloningEnabled,
                     "boggledDomedCitiesBuildableOnWaterWorlds": boggledDomedCitiesBuildableOnWaterWorlds
                  }
               };
   
               var query = { "uuid": uuid };
               db.db("players").collection("players").updateOne(query, { $set: playerEntry })
               .then( () => 
               {
                  db.close();
               })
               .catch( () => 
               {
                  console.log("Exception thrown trying to update player entry.")
               });

               res.status(200);
               res.json({message: "Player entry updated."});
               console.log("Status Code 200. Updated player entry:");
               console.log(playerEntry);
            });
         }
         else if(values[0] === false)
         {
            var MongoClient = require('mongodb').MongoClient;
            var url = "mongodb://localhost:27017/";

            MongoClient.connect(url, function(err, db)
            {
               var ipData = values[1];
               var playerEntry = 
               {
                  "uuid": uuid,
                  "timestamp": timestamp,
                  "ip-info": ipData,
                  "settings-info":
                  {
                     "boggledHydroponicsEnabled": boggledHydroponicsEnabled,
                     "boggledCloningEnabled": boggledCloningEnabled,
                     "boggledDomedCitiesBuildableOnWaterWorlds": boggledDomedCitiesBuildableOnWaterWorlds
                  }
               };
               
               db.db("players").collection("players").insertOne(playerEntry)
               .then( () => 
               {
                  db.close();
               })
               .catch( () => 
               {
                  console.log("Exception thrown trying to insert player entry.")
               });
               
               res.status(201);
               res.json({message: "Player entry created."});
               console.log("Status Code 201. Created player entry:");
               console.log(playerEntry);
            });
         }
      })
      .catch( () =>
      {
         console.log("Exception thrown in Promise all.")
      });
   }
});
