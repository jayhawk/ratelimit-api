# ratelimit-api

https://ratelimit-api.herokuapp.com

Find Hotels by City Id with optional sorting of the result by price
e.g. https://ratelimit-api.herokuapp.com/api/hotels?key=apikey1&cityId=Bangkok&sort=Desc

Two API Keys

1. "apikey1" with 10 seconds limit
2. "apikey2" with 10 seconds limit

Global rate limit is also 10s.
On exceeding the limit, api key will be suspended for next 20 seconds.
