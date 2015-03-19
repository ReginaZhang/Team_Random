**Technical Overview**

*Architecture*

Our approach uses a microservice-driven, cloud-based approach to maximise scalability, modularity and robustness.

The backend consists of separate processes communicating via JSON over HTTP. This facilitates modularity, enhancing the separation of concerns by decoupling different functional units. 

Such decoupling makes the code simpler and more flexible, preventing the creation of complex interdependencies among different pieces of code. This simplicity reduces time spent on debugging and maintenance, enhancing developer productivity and hence increasing development speed.

Our architecture also brings increased scalability, allowing different backend functionalities to be easily distributed across multiple machines. This is due to the use of JSON over HTTP for communication, which does not require the communicating processes to exist on the same machine or even the same cluster. Robustness is also increased: processes can be monitored and if one fails another can quickly be spun up to replace it, without bringing the whole system down.

This microservice-based approach also increases the flexibility of backend technology choices, allowing different languages to be used for different functionality depending on which is more suitable. Java could for instance be used for services under heavy load, while a more lightweight, dynamic language like python could be used for rapid development of less performance-critical services. This also allows team members to make optimal use of their various different languages proficiencies and backgrounds.

*Data*

Our site gathers health and nutrition data from multiple sites, such as the Health.Gov nutrition API, and caches it in a local MySql database, the workhorse of the web. We also harness the power of MySql for storing user-generated diet and exercise data, and the comments, questions and answers from our forum section. MySql supports transparent sharding and master-master replication, allowing it to easily scale on the cloud as the application’s user base grows

*Frontend*

The frontend to our site will be powered by modern HTML5 and CSS3 technology, unified by Javascript. The Microsoft Typescript compiler, which allows the use of optional type annotations, will increase the correctness of our Javascript code, reducing bugs and increasing maintainability. In MVC terms our frontend will be the View, with the Model and Controller constituted by our various backend services.

