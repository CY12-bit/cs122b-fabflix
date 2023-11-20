demo video link: 

### Member contribution

Colin:
- Servlets
  - MovieList
  - Movie Class
  - Payment
  - Genre
- HTML/CSS/JS
  - Confirmation (HTML)
  - Index (HTML)
  - Payment (HTML/CSS/JS)
  - Shopping Cart (JS)
- Parser
  - MovieParser
  - CastParser
  - MovieObject
  - xmlParser
- Android
  - Movie model
  - MovieListActivity, MovieListViewAdaptor, activity_movielist.xml
  - SingleMovieListActivity, SingleMovieListViewAdaptor, activity_single_movie.xml, singlemovielist_row.xml
  - urlConstants
- 'Add to Cart' Functionality
- Implemented Batch Insertion Optimization for Parsers
- Implemented Multi-Threading Optimization in MovieParser
- 'Proceed to Payment' and 'Place Order' functionality
- Jump to MovieList Page from single pages
- Setting up Recaptcha on Google
- Adding HTTPS

Michelle:
- Servlets
  - SingleMovie, SingleStar
  - MovieList (Sorting, Pagination)
  - Jump
  - LoginFilter, Login
  - BrowseGenre, BrowseTitle
  - UpdateSecurePassword (User and Employee)
  - RecaptchaConstraints, RecaptchaVerifyUtils
  - DashboardServlet
  - Employee, EmployeeLoginFilter, EmployeeLoginServlet
- Parser
  - ActorParser
  - Actor
  - DBInfo
  - StarInMovie
  - CastParser2
- HTML/CSS/JS
  - Index (CSS)
  - Login (HTML, JS) (User & Employees)
  - Single Movie Page (JS, HTML)
  - Single Star Page (JS/HTML/CSS)
  - addMovie (JS/HTML)
  - addStar (JS/HTML)
  - dashboard (JS/HTML)
  - basic CSS
- Android
  - NukeSSLCerts, NetworkManager, LoginActivity, activity_login.xml
  - SearchActivity, activity_search.xml
  - AndroidManifest.xml

Both:
- User Servlet
- Shopping Cart (Servlet, JS)
- movielist.js
- create_table.sql
- setting up aws + maven project
- git
- movielist_row.xml

### Files that use PreparedStatements
- Almost all files in src (except User,Movie,MovieObject,Actor,RecaptchaConstraints,JumpServlet)
- most servlets already used prepared statements 
- changes: MovieListServlets (~line 135) changes to use PreparedStatements

### Parser Optimizations
1. Implemented Multi-Threading for MovieParser.
   - Use 3 threads to parse 3 batches of prepared statements at the same time. 
   - Reduced time of MovieParser to 1/3 its original time.
2. Implemented Batch Insertion in all Parsers.
   - Will create and execute prepared statements every 'x' objects. 
   - Reduced by time of all parsers to 1/(1.5) of their original times.
3. Implemented HashMap on CastParserV2.
   - Stores movies created in MovieParser in hashmap to be referenced by CastParser instead of calling db multiple times.
   - Reduces time of CastParserV2 by 1/2 its original time.
  
### Inconsistency Report
- located in movieLogs.txt, castLogs.txt, and actorLogs.txt
