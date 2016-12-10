# ratelimit-api

https://ratelimit-api.herokuapp.com

Find Hotels by City ID with optional sorting of the result by price (Use 'asc' or 'desc')
e.g. https://ratelimit-api.herokuapp.com/api/hotels?key=apikey1&cityId=Bangkok&sort=desc

Two API Keys

1. "apikey1" with 10 seconds limit
2. "apikey2" with no rate limit set. Going to use default global rate limit.

Global rate limit is 5 seconds.
On exceeding the limit, api key will be suspended for next 20 seconds.

Both global rate limti and suspension time can be changed in application.conf.
