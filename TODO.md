# TODO
## Data:
    + Countries*
    + Crawler:
	    - Jobs (also create Models, Tables, Formats, Routes, Repos)*
		    + Add extra fields to job to support ajax
	    - Some urls might be done with Ajax so click event to call:
		    + Modify algo to use Extractor action with in Ajax mode
    + Payment Methods*
    + Stadiums
    + Subscriptions*
    + Roles*
    
## Training
+ Seed database:
	- Players
		+ Futhead
	- Teams:
		+ Futhead
	- Games:
		- SofaScore
+ Winda Iterators*
    - Finish Converting DB data to INDArray features

## API
+ Fix head2head in GameItemResponse
+ Android Home route and response*
+ Shopping Cart create routes
+ Complete league routes calculating values from query and models*
+ Complete games routes: List and Detail*
+ Favourite Teams
+ Authorisation
	- Android
		+ valid for 14 days then after pay per device
		+ subscription validation
+ Subscriptions:
	- Weekly: KSH 99*
	- BiWeekly: KSH 149*
	- Monthly: KSH 299*
+ Mailing:
	- Twril compiler
	- Subscription Email *
	- Change password email *
	- Marketing Emails
+ SMS:
	- Premium Betting Tips: 10/- per SMS
	- 
+ Notifications:
	- Betting Tips
	- Game Kickoffs

### Crawler
+ Akka Crawler *
+ PhantomJS Data Scrapper*
+ Add a managing actor:
    - Starts at server startup and loads job definitions*
    - Manages crawler actors
+ Add Redis URL Caching to Scrapper
+ Logic for storing (games, events, statistics) and (players, statistics) into database
+ Pick Websites: *
	- SofaScore: Live Games and Summary
	- Futhead: Players & Teams
	- Goal.com: News

### Scheduled Workers
+ Authentication Token Invalidation*
+ Shopping Cart Invalidation
+ Subscription Invalidation *