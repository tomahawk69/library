# library
Backend: Java 8, Spring Boot
Front-end: bower, nodeJS, gulp, AngularJS

How start backend:
1) clean + install library-core
2) clean + spring-boot:run or library-web\mvnw.cmd "spring-boot:run" -Dmaven.repo.local=<path-to-local repository>
3) app should start on port 8080

How to start front-end:
1) install npm
2) npm install -g yo gulp
3) bower install
4) gulp serve
5) front-end should start on port 3000  

Active functions:
1) Frontend with Main and Dashboard views
2) Dashboard has "Refresh" button to get/update information from the specified folder. Folder is now hardcoded in application
3) Dashboard has "Stop refresh" button to stop refresh
4) Footer has information about backend and data state

Logging properties (debug by default) are in library-web\src\main\resources\log4j2.xml 