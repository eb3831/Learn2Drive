package com.example.learn2drive.Helpers;

public class Prompts
{
    public static final String PHOTO_PROMPT = "Analyze the provided image of an Israeli Identity Card (Teudat Zehut)./n" +
            "Your task is to extract exactly two fields: the ID Number and the Date of Birth. /n" +
            "Instructions: /n" +
            "1. Locating ID: Find the 9-digit number (including checksum). /n" +
            "2. Locating DOB: Find the date of birth in DD.MM.YYYY format. /n" +
            "3. Accuracy: Ensure the numbers are extracted correctly without any extra characters or text. /n" +
            "4. Output: Provide the result strictly in JSON format according to the schema below. " +
            "If a field is not found, return null for that field. /n";

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
            "You are an expert driving instructor assistant. Your task is to analyze the audio recording of a driving lesson. \n" +
                    "\n" +
                    "Important Instructions:\n" +
                    "- The recording is up to 60 minutes long and may contain casual background conversations, small talk, or irrelevant remarks. \n" +
                    "- You MUST ignore all irrelevant chatter. Focus strictly on the driving lesson content, " +
                    "the instructor's feedback, and the student's driving performance.\n" +
                    "- CRITICAL LANGUAGE REQUIREMENT: All the text generated for the values in the JSON object MUST be written in fluent Hebrew. " +
                    "The JSON keys must remain in English as defined in the schema.\n" +
                    "\n" +
                    "Please extract the key learning points and return the analysis STRICTLY as a JSON object, adhering exactly to the schema provided.";

    public static final String LESSON_SUMMARY_SCHEMA = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
            "  \"title\": \"Driving Lesson Summary Extraction\",\n" +
            "  \"description\": \"Schema for extracting key points, strengths, and areas for improvement from a driving lesson audio recording. All output values must be in Hebrew.\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"lessonSummary\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"description\": \"A brief, 2-3 sentence overview of the lesson's main events, written in Hebrew.\",\n" +
            "      \"example\": \"השיעור התמקד בנהיגה עירונית וניווט בצמתים מורכבים.\"\n" +
            "    },\n" +
            "    \"topicsCovered\": {\n" +
            "      \"type\": \"array\",\n" +
            "      \"description\": \"A list of specific driving topics or maneuvers covered during the lesson, written in Hebrew.\",\n" +
            "      \"items\": {\n" +
            "        \"type\": \"string\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"strengths\": {\n" +
            "      \"type\": \"array\",\n" +
            "      \"description\": \"A list of specific actions or behaviors the student executed well, written in Hebrew.\",\n" +
            "      \"items\": {\n" +
            "        \"type\": \"string\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"areasForImprovement\": {\n" +
            "      \"type\": \"array\",\n" +
            "      \"description\": \"A list of mistakes, hesitations, or areas where the student needs more practice, written in Hebrew.\",\n" +
            "      \"items\": {\n" +
            "        \"type\": \"string\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"recommendations\": {\n" +
            "      \"type\": \"array\",\n" +
            "      \"description\": \"A list of specific goals or focus areas recommended for the next driving lesson, written in Hebrew.\",\n" +
            "      \"items\": {\n" +
            "        \"type\": \"string\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\"lessonSummary\", \"topicsCovered\", \"strengths\", \"areasForImprovement\", \"recommendations\"]\n" +
            "}";

}
