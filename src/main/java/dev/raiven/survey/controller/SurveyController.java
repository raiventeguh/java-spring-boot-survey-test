package dev.raiven.survey.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.raiven.survey.model.Criteria;
import dev.raiven.survey.model.MetricsCriteria;
import dev.raiven.survey.model.ProjectionCriteria;
import dev.raiven.survey.model.SortCriteria;
import dev.raiven.survey.model.Survey;
import dev.raiven.survey.model.SurveyResponse;
import dev.raiven.survey.repository.SurveyRepository;

@RestController
@RequestMapping("/surveys")
public class SurveyController {
    
    private final SurveyRepository repository;
    
    public SurveyController(SurveyRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public SurveyResponse findAll(@RequestParam(value = "filter") Optional<String> filter, // field, operator, value
                                  @RequestParam(value = "sort") Optional<String> sort, // key : direction
                                  @RequestParam(value = "metrics") Optional<String> metrics, // metricsName : key
                                  @RequestParam(value = "projection") Optional<String> projection) throws NoSuchFieldException, SecurityException {
        
        Pattern pattern = Pattern.compile("(\\w+?)(:|<|>|<:|>:)(\\w+?),");
        Matcher matcher = pattern.matcher(filter.orElse("") + ",");
        List<Criteria> params = new ArrayList<>();
        while (matcher.find()) {
            params.add(new Criteria(matcher.group(1), matcher.group(2), matcher.group(3)));
        }

               
        Pattern sortPattern = Pattern.compile("(\\w+?)(:)(\\w+?),");
        Matcher sortMatcher = sortPattern.matcher(sort.orElse("") + ",");
        SortCriteria sortCriteria = null;
        while (sortMatcher.find()) {
            sortCriteria = new SortCriteria(sortMatcher.group(1), sortMatcher.group(3));
        }   

        Pattern metricsPattern = Pattern.compile("(\\w+?)(:)(\\w+?),");
        Matcher metricsMatcher = metricsPattern.matcher(metrics.orElse("") + ",");
        MetricsCriteria metricsCriteria = null;
        while (metricsMatcher.find()) {
            metricsCriteria = new MetricsCriteria(metricsMatcher.group(1), metricsMatcher.group(3));
        }   

        Pattern projectionPattern = Pattern.compile("(\\w+?),");
        Matcher projectionMatcher = projectionPattern.matcher(projection.orElse("") + ",");
        ProjectionCriteria projectionCriteria = null;
        List<String> keys = new ArrayList<>();
        while (projectionMatcher.find()) {
           keys.add(projectionMatcher.group(1));
        }  
        if (!keys.isEmpty()) {
            projectionCriteria = new ProjectionCriteria(keys);
        }

        return repository.findAll(params, sortCriteria, metricsCriteria, projectionCriteria);
    } 
}
