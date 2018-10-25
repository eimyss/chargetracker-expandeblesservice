# micro-expenses
 
This service used for expenes and bookings save  
Trello cards: https://trello.com/b/qmE6t9eQ/expenses-microservice  

It is intended to use as a docker container (later Kubernetes / Openshift)


## Communication

 * HTTP REST (json) 
 * Messaging (Rabbit) - async

## functionality

* bookings (for android app)
* actuator (monitoring / info)
* expenses (web app)

## Technology used
 
+ Spring (quite a lot)
+ eureka / ribbon / feign
+ messaging (rabbitmq)
+ jackson (for parsing json, mapping dtos, etc)
+ keycloack (for auth)
+ mvn docker plugin
+ mysql / h2 (for saving stuff)
+ prometheus and Zipkin clients (for tracing and monitoring)
+ swagger (for endpoint desc) 
+ logstash (for elk logging)


## TODO: 
*  Pending diagrams
 * multi tenancy 


# LICENCE 

* MIT
