package com.example.learn2drive.Helpers;

public class Prompts
{
    public static final String PHOTO_PROMPT = "Analyze the provided image of an Israeli Identity Card (Teudat Zehut)./n" +
            "Your task is to extract exactly two fields: the ID Number and the Date of Birth. /n" +
            "Instructions: /n" +
            "1. Locating ID: Find the 9-digit number (including checksum). /n" +
            "2. Locating DOB: Find the date of birth in DD.MM.YYYY format. /n" +
            "3. Accuracy: Ensure the numbers are extracted correctly without any extra characters or text. /n" +
            "4. Output: Provide the result strictly in JSON format according to the schema below. If a field is not found, return null for that field. /n";

    public static final String ID_CARD_SCHEMA = "{\n" +
        "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
        "  \"title\": \"Israeli ID Card Extraction\",\n" +
        "  \"description\": \"Schema for extracting specific fields from an Israeli Identity Card (Teudat Zehut).\",\n" +
        "  \"type\": \"object\",\n" +
        "  \"properties\": {\n" +
        "    \"id_number\": {\n" +
        "      \"type\": \"string\",\n" +
        "      \"description\": \"The 9-digit Israeli Identity Number.\",\n" +
        "      \"pattern\": \"^[0-9]{9}$\",\n" +
        "      \"example\": \"123456789\"\n" +
        "    },\n" +
        "    \"date_of_birth\": {\n" +
        "      \"type\": \"string\",\n" +
        "      \"description\": \"The person's date of birth as it appears on the ID card.\",\n" +
        "      \"example\": \"01.01.1990\"\n" +
        "    }\n" +
        "  },\n" +
        "  \"required\": [\"id_number\", \"date_of_birth\"]\n" +
        "}";

    public static final String LESSON_SUMMARY_PROMPT =
            "You are a driving instructor's assistant. " +
                    "Listen to the following audio recording of a driving lesson. " +
                    "Analyze the instructor's feedback and the events of the lesson. " +
                    "Your task is to provide a structured summary emphasizing the student's progress. " +
                    "Output the content strictly in Hebrew, but keep the JSON keys exactly as defined in the schema. " +
                    "Provide the result strictly in JSON format according to the schema below:\n";

    public static final String LESSON_SUMMARY_SCHEMA = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
            "  \"title\": \"Driving Lesson Summary\",\n" +
            "  \"description\": \"Schema for summarizing a driving lesson from an audio recording.\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"overall_summary\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"description\": \"A brief, 1-2 sentence overall summary of how the lesson went, written in Hebrew.\"\n" +
            "    },\n" +
            "    \"strengths\": {\n" +
            "      \"type\": \"array\",\n" +
            "      \"description\": \"A list of things the student did well during the lesson, written in Hebrew.\",\n" +
            "      \"items\": {\n" +
            "        \"type\": \"string\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"areas_for_improvement\": {\n" +
            "      \"type\": \"array\",\n" +
            "      \"description\": \"A list of areas where the student needs to improve, mistakes made, or things to focus on next time, written in Hebrew.\",\n" +
            "      \"items\": {\n" +
            "        \"type\": \"string\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\"overall_summary\", \"strengths\", \"areas_for_improvement\"]\n" +
            "}";

}
