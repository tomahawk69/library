# library
Backend: Java 8, Spring Boot.  
Front-end: bower, nodeJS, gulp, AngularJS, Material Design.

How start backend:  
1. clean + install library-common
2. go to library-core/src/main/resources
3. copy application.properties.example to application.properties if last doesn't exist  
4. update params in application.properties  
5. clean + spring-boot:run on library-core or library-core\mvnw.cmd "spring-boot:run" -Dmaven.repo.local=<path-to-local repository>
6. web-app should start on port 8083 (default port may be changed in application.properties)  

How to prepare front-end:  
1. install npm  
2. npm install -g yo gulp  
3. go to library-web/web-app
4. bower install  
5. npm install  

How to start front-end:  
1. go to library-web/web-app
2. gulp serve  
3. front-end should start on port 3000  

Active functions:  
1. Frontend with Main and Dashboard views  
2. Dashboard has "Refresh" button to get/update information from the specified folder. Folder is now hardcoded in application  
3. Dashboard has "Stop refresh" button to stop refresh  
4. Dashboard has "Enable debug messages" switch  
5. Footer has information about backend and data state
6. Update stored files data in the .db file in the root of library
7. Get the list of libraries on dashboard
8. Select library on dashboard

Logging properties (info by default) are in library-core\src\main\resources\log4j2.xml

Roadmap:
1. Technical debt
2. Move DB structure management to procedure
3. Implement backups
4. Move to one of DB frameworks