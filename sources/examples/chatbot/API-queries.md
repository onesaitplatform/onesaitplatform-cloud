# Worst

This is the sql to create a query in the API REST to obtain the worst district in the city.

```sql
select A.GeoAirQuality.stationName as stationName, avg(A.GeoAirQuality.aqi) as aqi 
from GeoAirQuality as A
where A.GeoAirQuality.timestamp >= DATE({$date}) AND A.GeoAirQuality.timestamp <= DATE({$date})
group by A.GeoAirQuality.stationName
order by aqi desc
limit 1
```

# Best

This is the SQL to create the query of the API REST to obtain the best district in the city.

```sql
select A.GeoAirQuality.stationName as stationName, avg(A.GeoAirQuality.aqi) as aqi
from GeoAirQuality as A
where A.GeoAirQuality.timestamp >= DATE({$date}) AND A.GeoAirQuality.timestamp <= DATE({$date})
group by A.GeoAirQuality.stationName
order by aqi
limit 1
```

# Air quality of a day

This query allows to get information about an specific district in a specific date.
Due to there are several measures in a day, it returns the average of all the measures for that day in the district.

```sql
select avg(c.GeoAirQuality.aqi) as aqi from GeoAirQuality as c where c.GeoAirQuality.timestamp >= DATE({$date}) AND c.GeoAirQuality.timestamp <= DATE({$date}) AND c.GeoAirQuality.stationName = {$stationName} group by GeoAirQuality.stationName
```
