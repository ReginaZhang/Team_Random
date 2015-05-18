Our project uses a microservices approach, with different services responsible for different parts of the site's functionality. The modules folder contains these services. modules/getFoodSuggestion/ contains the Java code responsible for handling user registration and login, and the management of users' diets. modules/food_api contains the Python code responsible for getting food nutrition information from the US gov nutrition api, storing it in the local database and caching it. modules/forum contains the Clojure and Clojurescript code for the forum backend and frontend respectively, as well as the backend code for giving food suggestions... Our final report examines this in more detail.

All our static files (CSS, JS, HTML) are in static/, and this folder is served by Nginx. A copy of our Nginx config is stored in nginx.conf. SQL fiels for rebuilding our database are stored in sql/; anyone altering tables in the DB should update the corresponding files in this folder to match.

Our site uses an "interesting" approach to session handling. Rather than using a cookie, we track user sessions based on the IP and user-agent header. This means two people sharing the same IP and browser would have the same session. While this is unsafe, so is using unencrypted cookies to handle sessions, and so since we didn't have time to implement proper session encryption, we figured our approach was good enough, as security is was not a focus of our project.

The Java project can be build with Maven, by running `mvn compile` in modules/getFoodSuggesion/. The .pom can also be opened as a project in IntelliJ or Eclipse, if one desires to use an IDE.

The Cherrypy Python services can be run simply by `python2 whatever.py`.

The forum backend can be run by `lein ring server` in modules/forum. The generated JS for the forum frontend can be created by running `lein cljsbuild once` in modules/forum, than copying cljs.js from modules/forum/static/js to /static/js. modules/forum/src/clj/forum contains the Clojure sources (Clojure running on the JVM, used on the forum backend), while modules/forum/src/cljs/forum/ contains the Clojurescript sources (Clojure transpiled to Javascript, running in the browser), from which the code for the forum frontend is generated.

# Due Due Due
- Part E ---> Monday 5pm Week 7 (I think it is a typo in spec ?)
    - Submit all code + Report and then Presentation

# Have Fun and Jiayou !

![Image of YAO](http://img.qqday.com/allimg/120627/0921062E3-0.jpg)

# Team_Random

Project work for INFO30005 Web Infomation Technolegies

Project Name: Health Overflows