package dev.raiven.survey.repository;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Repository;

import dev.raiven.survey.model.Criteria;
import dev.raiven.survey.model.MetricsCriteria;
import dev.raiven.survey.model.MetricsData;
import dev.raiven.survey.model.ProjectionCriteria;
import dev.raiven.survey.model.SortCriteria;
import dev.raiven.survey.model.Survey;
import dev.raiven.survey.model.SurveyResponse;

@Repository
public class SurveyRepository {
    List<Survey> data = new ArrayList<>();
    
    public SurveyRepository() {
        data.add(new Survey(
            "first_name", "last_name", 99, 0
        ));
        data.add(new Survey(
            "first_name_2", "last_name", 100, 0
        ));
    }

    public Boolean compare(String s1, String s2) {
        return Objects.equals(s1, s2);
    }

    List<Predicate<Survey>> buildPredicates(List<Criteria> params) {
        if (params == null) {
            return null;
        }

        List<Predicate<Survey>> allPredicates = new ArrayList<Predicate<Survey>>();
        for (Criteria criteria: params) {
            String key = criteria.key();
            try {  
                Field field = Survey.class.getDeclaredField(key.toString());  
                field.setAccessible(true); 
                allPredicates.add(s -> {
                    try {
                        ExpressionParser expressionParser = new SpelExpressionParser();
                        EvaluationContext context=new StandardEvaluationContext();
                        context.setVariable("criteria_1", field.get(s));
                        context.setVariable("criteria_2", criteria.value());
                        Expression expression;
                        if (field.getType() == String.class) {
                            expression = expressionParser.parseExpression("#criteria_1.equals(#criteria_2)");
                        } else {
                            String operator = criteria.operator();
                            if (criteria.operator().equalsIgnoreCase(":")) {
                                operator = "==";
                            } else if (criteria.operator().contains(":")) {
                                operator = operator.replace(":", "=");
                            }
                            expression = expressionParser.parseExpression(String.format("%d %s %d", field.get(s), operator, Integer.parseInt(criteria.value())));
                        }
                        
                        return Objects.requireNonNullElse((Boolean) expression.getValue(context), false);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                        return true;
                    }
                });
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return allPredicates;
    }

    Comparator<Survey> buildComparator(SortCriteria criteria) throws NoSuchFieldException, SecurityException {
        if (criteria == null) {
            return null;
        }

        Field field = Survey.class.getDeclaredField(criteria.key());
        field.setAccessible(true);
        Comparator comparator_order = criteria.direction().equalsIgnoreCase("asc") ? Comparator.naturalOrder() : Comparator.reverseOrder();
        Comparator<Survey> comparator = Comparator.comparing(o1 -> {
            try {
                Comparable v1 = (Comparable) field.get(o1);
                return v1;
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }, comparator_order);

        return comparator;
    }

    MetricsData buildMetricsData(List<Survey> result, MetricsCriteria metricsCriteria) {
        if (result == null || metricsCriteria == null) {
            return null;
        }
        Integer sum = result.stream().mapToInt(x -> {
            Field field;
            try {
                field = Survey.class.getDeclaredField(metricsCriteria.key());
                field.setAccessible(true);
                return Integer.parseInt(field.get(x).toString());
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                // e.printStackTrace();
            }
            return 0;
        }).sum();
        Long count = result.stream().count();
        Double avg = sum*1.0/count;
        Map<String, BigDecimal> aggregationData = new HashMap<>();
        aggregationData.put("count", new BigDecimal(count.toString()));
        aggregationData.put("sum", new BigDecimal(sum.toString()));
        aggregationData.put("avg", new BigDecimal(avg.toString()));

        MetricsData MetricsData = new MetricsData(
            metricsCriteria.metricsName(),
            aggregationData.get(metricsCriteria.metricsName()).toString()
        );

        return MetricsData;
    }

    public static Map<String, Object> buildProjections(Object obj, ProjectionCriteria criteria) {
        Map<String, Object> map = new HashMap<>();
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (criteria.projectionKeys().contains(field.getName())) {
                field.setAccessible(true);
                try { map.put(field.getName(), field.get(obj)); } catch (Exception e) { }
            }
        }
        return map;
    }

    public SurveyResponse findAll(List<Criteria> params, SortCriteria sortCriteria, MetricsCriteria metricsCriteria, ProjectionCriteria projectionCriteria) throws NoSuchFieldException, SecurityException {
        List<Survey> result = new ArrayList<>();
        // Search & Filter
        List<Predicate<Survey>> allPredicates = buildPredicates(params);
        if (allPredicates != null) {
            result.addAll(data.stream().filter(allPredicates.stream().reduce(x->true, Predicate::and)).toList()); 
        } else {
            result.addAll(data);
        }
        
        // Sort
        Comparator<Survey> comparator = buildComparator(sortCriteria);
        if (comparator != null) {
            Collections.sort(result, comparator);
        }
        
        // Aggregate
        MetricsData metricsData = buildMetricsData(result, metricsCriteria);

        List<Object> projectionResult = new ArrayList<>();
        // Returning result
        if (projectionCriteria != null) {
            for (Survey survey: result) {
                projectionResult.add(buildProjections(survey, projectionCriteria));
            }
        } else {
            projectionResult.addAll(result);
        }
        

        return new SurveyResponse(projectionResult, metricsData);
    }
}



