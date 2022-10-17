# java-spring-boot-survey-test


# Pre requisite
1. java 17
2. maven

# Framework
1. Spring Boot 
2. Gson


# Run Application 
```
mvn spring-boot:run
```

# Endpoint
```
localhost:8080/surveys
```

# Query param 
```
Filtering
format: [field_name][:|>|<|<:|>:][values]
example: salary>:90000


Orering
format: [field_name]:[direction asc|desc]
example: salary:desc

Aggregate
format: [metrics_name avg|sum|count]:[field_name]
example: sum:salary

Projection
format: [field_name],[field_name2]...
example: salary,location


Example: 
http://localhost:8080/surveys
http://localhost:8080/surveys?sort=salary:desc&metrics=sum:salary&projection=salary&filter=salary:90000
```

# Field Attribute 
```
String industry
String title
Integer salary
String currency
String location
```

# Response Schema
```
{
  "surveyData": [ // depends on projectsions, if not defined follow survey schema
    {
      "industry": "media",
      "title": "senior marketing manager",
      "salary": 90000,
      "currency": "USD",
      "location": "Washington, DC"
    }
  ],
  "MetricsData": {
    "metricsName": "avg",
    "value": "90000.0"
  }
}
```

# TODO not yet
1. Unit Test
2. Validation on Query param - Filter, Metrics, etc
3. Cleanup or expect dirty value in json dataset
4. Use all attribute, currently still missing few attribute (not consumed)