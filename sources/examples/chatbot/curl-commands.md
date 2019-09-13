# Curl commands


## Insert subscription

Esto devuelve código 200 y un id si todo va bien.

Ejemmplo de datos para subscribirse:

```json
{
        "Subscription": {
            "stationName": "Helsinki",
            "email": "cfsanchez@minsait.com",
            "quality": "moderate"
        }
}
```

Invocación del servicio usando curl. Importante la opción --insecure para evitar la verificación SSL ya que el certificado del servidor es autogenerado. Esto lo tendréis que tener en cuenta también en el Chatbot.
También hay que fijarse en la cabecera "X-SOFIA2-APIKey: 1a17b5502a3b49228b4ce97a16a23abf". Este es el token para autorizar la operación.

```bash
curl -X POST --insecure "https://s4citiespro.westeurope.cloudapp.azure.com/api-manager/server/api/v1/ChatBotSubscriptionAPI/" -H "accept: text/plain" -H "X-SOFIA2-APIKey: 1a17b5502a3b49228b4ce97a16a23abf" -H "Content-Type: application/json" -d "{ \"Subscription\": { \"stationName\": \"Helsinki\", \"email\": \"cfsanchez@minsait.com\", \"no2Threshold\": 0, \"o3Threshold\": 0, \"so2Threshold\": 0 }}"
```

## Obtain data of a district in a date.

La url sin codificar para http:

```url
https://s4citiespro.westeurope.cloudapp.azure.com/api-manager/server/api/v1/ChatBotAPI/AirQuality?$date[0]={date}&$date[1]={date}&$stationName={stationName}&queryType=SQLLIKE&targetdb=BDTR&date=2018-05-10&stationName=Helsinki&query=select avg(c.GeoAirQuality.aqi) as aqi from GeoAirQuality as c where c.GeoAirQuality.timestamp >= DATE({$date}) AND c.GeoAirQuality.timestamp <= DATE({$date}) AND c.GeoAirQuality.stationName = {$stationName} group by GeoAirQuality.stationName
```
Sólo hay que modificar los parámetros date y stationName.
Invocación del servicio usando curl. Como antes es importante fijarse en el --insecure y en el token para autenticar.

```bash
curl --insecure -X GET "https://s4citiespro.westeurope.cloudapp.azure.com/api-manager/server/api/v1/ChatBotAPI/AirQuality?%24date%5B0%5D=%7Bdate%7D&%24date%5B1%5D=%7Bdate%7D&%24stationName=%7BstationName%7D&queryType=SQLLIKE&targetdb=BDTR&date=2018-05-10&stationName=Helsinki&query=select%20avg(c.GeoAirQuality.aqi)%20as%20aqi%20from%20GeoAirQuality%20as%20c%20where%20c.GeoAirQuality.timestamp%20%3E%3D%20DATE(%7B%24date%7D)%20AND%20c.GeoAirQuality.timestamp%20%3C%3D%20DATE(%7B%24date%7D)%20AND%20c.GeoAirQuality.stationName%20%3D%20%7B%24stationName%7D%20group%20by%20GeoAirQuality.stationName" -H "accept: application/json" -H "X-SOFIA2-APIKey: 1a17b5502a3b49228b4ce97a16a23abf" -H "Cacheable: false"
```

## Obtain the worst district based on date and type of measure.

La url sin codificar para http:

```url
https://s4citiespro.westeurope.cloudapp.azure.com/api-manager/server/api/v1/ChatBotAPI/WorstDistrict?$date[0]={date}&$date[1]={date}&targetdb=BDTR&date=2018-05-10&queryType=SQLLIKE&query=select A.GeoAirQuality.stationName as stationName, avg(A.GeoAirQuality.aqi) as aqi  from GeoAirQuality as A where A.GeoAirQuality.timestamp >= DATE({$date}) AND A.GeoAirQuality.timestamp <= DATE({$date}) group by A.GeoAirQuality.stationName order by aqi desc limit 1
```

La url es algo larga, pero sólo hay que cambiar los dos últimos parámetros measure y date.
El comando curl para probarla es:

```bash
curl --insecure -X GET "https://s4citiespro.westeurope.cloudapp.azure.com/api-manager/server/api/v1/ChatBotAPI/WorstDistrict?%24date%5B0%5D=%7Bdate%7D&%24date%5B1%5D=%7Bdate%7D&targetdb=BDTR&date=2018-05-10&queryType=SQLLIKE&query=select%20A.GeoAirQuality.stationName%20as%20stationName%2C%20avg(A.GeoAirQuality.aqi)%20as%20aqi%20%20from%20GeoAirQuality%20as%20A%20where%20A.GeoAirQuality.timestamp%20%3E%3D%20DATE(%7B%24date%7D)%20AND%20A.GeoAirQuality.timestamp%20%3C%3D%20DATE(%7B%24date%7D)%20group%20by%20A.GeoAirQuality.stationName%20order%20by%20aqi%20desc%20limit%201" -H "accept: application/json" -H "X-SOFIA2-APIKey: 1a17b5502a3b49228b4ce97a16a23abf" -H "Cacheable: false"
```

## Obtain the best district based on date and type of measure 

La url sin condificar para http es la siguiente:

```url
https://s4citiespro.westeurope.cloudapp.azure.com/api-manager/server/api/v1/ChatBotAPI/\BestDistrict?$date[0]={date}&$date[1]={date}&date=2018-05-10&targetdb=BDTR&query=select A.GeoAirQuality.stationName as stationName, avg(A.GeoAirQuality.aqi) as aqi from GeoAirQuality as A where A.GeoAirQuality.timestamp >= DATE({$date}) AND A.GeoAirQuality.timestamp <= DATE({$date}) group by A.GeoAirQuality.stationName order by aqi limit 1&queryType=SQLLIKE
```

Aunque es una url algo aparatosa sólo hay que modificar los dos últimos parámetros: measure y date.
El comando curl para probarlo:

```bash
curl --insecure -X  GET "https://s4citiespro.westeurope.cloudapp.azure.com/api-manager/server/api/v1/ChatBotAPI/BestDistrict?%24date%5B0%5D=%7Bdate%7D&%24date%5B1%5D=%7Bdate%7D&date=2018-05-10&targetdb=BDTR&query=select%20A.GeoAirQuality.stationName%20as%20stationName%2C%20avg(A.GeoAirQuality.aqi)%20as%20aqi%20from%20GeoAirQuality%20as%20A%20where%20A.GeoAirQuality.timestamp%20%3E%3D%20DATE(%7B%24date%7D)%20AND%20A.GeoAirQuality.timestamp%20%3C%3D%20DATE(%7B%24date%7D)%20group%20by%20A.GeoAirQuality.stationName%20order%20by%20aqi%20limit%201&queryType=SQLLIKE" -H "accept: application/json" -H "X-SOFIA2-APIKey: 1a17b5502a3b49228b4ce97a16a23abf" -H "Cacheable: false"
```