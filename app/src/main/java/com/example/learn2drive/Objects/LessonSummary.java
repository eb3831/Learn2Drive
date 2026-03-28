package com.example.learn2drive.Objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing the AI-generated summary of a completed driving lesson.
 * Designed to perfectly match the JSON schema returned by Gemini.
 */
public class LessonSummary implements Serializable
{
    private String lessonSummary;
    private List<String> topicsCovered;
    private List<String> strengths;
    private List<String> areasForImprovement;
    private List<String> recommendations;

    /**
     * Default constructor required for Firebase Realtime Database.
     */
    public LessonSummary()
    {
        this.topicsCovered = new ArrayList<>();
        this.strengths = new ArrayList<>();
        this.areasForImprovement = new ArrayList<>();
        this.recommendations = new ArrayList<>();
    }

    /**
     * Parameterized constructor to initialize a complete lesson summary.
     *
     * @param lessonSummary       A brief overview of the lesson.
     * @param topicsCovered       List of topics practiced.
     * @param strengths           List of the student's strong points.
     * @param areasForImprovement List of areas needing more practice.
     * @param recommendations     List of goals for the next lesson.
     */
    public LessonSummary(String lessonSummary, List<String> topicsCovered,
                         List<String> strengths, List<String> areasForImprovement,
                         List<String> recommendations)
    {
        this.lessonSummary = lessonSummary;
        this.topicsCovered = topicsCovered;
        this.strengths = strengths;
        this.areasForImprovement = areasForImprovement;
        this.recommendations = recommendations;
    }

    public String getLessonSummary()
    {
        return lessonSummary;
    }

    public void setLessonSummary(String lessonSummary)
    {
        this.lessonSummary = lessonSummary;
    }

    public List<String> getTopicsCovered()
    {
        return topicsCovered;
    }

    public void setTopicsCovered(List<String> topicsCovered)
    {
        this.topicsCovered = topicsCovered;
    }

    public List<String> getStrengths()
    {
        return strengths;
    }

    public void setStrengths(List<String> strengths)
    {
        this.strengths = strengths;
    }

    public List<String> getAreasForImprovement()
    {
        return areasForImprovement;
    }

    public void setAreasForImprovement(List<String> areasForImprovement)
    {
        this.areasForImprovement = areasForImprovement;
    }

    public List<String> getRecommendations()
    {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations)
    {
        this.recommendations = recommendations;
    }
}