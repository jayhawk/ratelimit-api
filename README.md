# ratelimit-api

https://ratelimit-api.herokuapp.com

Find Hotels by City ID with optional sorting of the result by price (Use 'asc' or 'desc')
e.g. https://ratelimit-api.herokuapp.com/api/hotels?key=apikey1&cityId=Bangkok&sort=desc

Two API Keys

1. "apikey1" with 10 seconds limit
2. "apikey2" with 10 seconds limit

Global rate limit is 10 seconds.
On exceeding the limit, api key will be suspended for next 20 seconds.
