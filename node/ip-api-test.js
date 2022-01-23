// Get the IP-related information from ip-api.com
const http = require('http');

const options = 
{
   hostname: 'ip-api.com',
   port: 80,
   path: '/json/74.105.6.165',
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
      try
      {
         ipDataFromIpApi = JSON.parse(data);
         console.log(ipDataFromIpApi)
         console.log(ipDataFromIpApi.lat)
      }
      catch(error)
      {
         console.log("Encountered error. Message: " + error);
      }
   })
})

req.on('error', error => { console.error(error) });
req.end();